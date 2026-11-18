
package com.example.foodcheck

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.camera.core.ExperimentalGetImage
import android.view.ViewGroup
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.foodcheck.ui.theme.Gray
import android.graphics.RectF
import android.widget.Toast
import androidx.camera.core.UseCaseGroup
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.core.view.doOnLayout
import java.util.concurrent.atomic.AtomicReference
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalGetImage::class)
@androidx.annotation.OptIn(
    markerClass = [TransformExperimental::class]
)
@Composable
fun MainScreen(
    scanType: ScanType,
    onCodeScanned: ((String) -> Unit)? = null

) {
    val scanArea = remember {
        AtomicReference<RectF?>(null)
    }
    val transformFactory = remember {
        ImageProxyTransformFactory().apply {
            setUsingRotationDegrees(true)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current//Получаем информацию об Android
    val context = LocalContext.current

    val analysisExecutor = remember {
        CoroutineScope(Dispatchers.Default)
    }//Подготавливаем выполнение анализа кадров В корутине

    val scanRequested = remember {
        AtomicBoolean(false)
    }

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    } //Получаем CameraProvider

    DisposableEffect(Unit) {
        onDispose {
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }
    }//DisposableEffect используют, когда нужно выполнить очистку при удалении Composable из композиции.

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AndroidView(//View System
            factory = { ctx ->

                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val cameraproviderfuture = ProcessCameraProvider.getInstance(ctx)

                cameraproviderfuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val previewUseCase = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val permissionGranted =//просим разрещение
                        ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                    if (!permissionGranted) {
                        return@addListener
                    }

                    val selector = when {//выбираем камеру
                        cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                        cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                        else -> {
                            return@addListener
                        }
                    }

                    val barcodeScanner = BarcodeScanning.getClient(
                        BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(
                            Barcode.FORMAT_EAN_13,
                            Barcode.FORMAT_EAN_8,
                            Barcode.FORMAT_CODE_128
                        )
                            .build()
                    )

                    val analysisUseCase = ImageAnalysis.Builder()   // Обработка изображения
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(
                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build().also { analysis ->
                            analysis.setAnalyzer(
                                { runnable ->
                                    analysisExecutor.launch {
                                        runnable.run()
                                    }
                                }
                            ) { imageProxy ->

                                if (!scanRequested.get()) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val mediaImage = imageProxy.image

                                if (mediaImage == null) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val rotation =
                                    imageProxy.imageInfo.rotationDegrees

                                val inputImage = InputImage.fromMediaImage(
                                    mediaImage,
                                    rotation
                                )
                                val sourceTransform =
                                    transformFactory.getOutputTransform(imageProxy)

                                when (scanType) {
                                    ScanType.ScanQr -> {

                                        barcodeScanner.process(inputImage)
                                            .addOnSuccessListener { barcodes ->

                                                // 1. Получаем координаты нашей рамки
                                                val area = scanArea.get()

                                                // 2. Получаем систему координат PreviewView
                                                val targetTransform = previewView.outputTransform

                                                if (area != null && targetTransform != null) {

                                                    // 3. Создаём преобразование:
                                                    // кадр камеры -> экран камеры
                                                    val transform = CoordinateTransform(
                                                        sourceTransform,
                                                        targetTransform
                                                    )

                                                    // 4. Ищем первый код внутри рамки
                                                    val barcode = barcodes.firstOrNull { barcode ->

                                                        val box = barcode.boundingBox

                                                        if (box == null) {
                                                            false
                                                        } else {

                                                            // Копируем координаты найденного кода
                                                            val mappedBox = RectF(box)

                                                            // Переводим их в координаты PreviewView
                                                            transform.mapRect(mappedBox)

                                                            // Проверяем попадание в нашу рамку
                                                            area.contains(
                                                                mappedBox.centerX(),
                                                                mappedBox.centerY()
                                                            )
                                                        }
                                                    }

                                                    // 5. Возвращаем результат только подходящего кода
                                                    barcode?.rawValue?.let { code ->

                                                        scanRequested.set(false)

                                                        Toast.makeText(
                                                            ctx,
                                                            "Найден QR: $code",
                                                            Toast.LENGTH_LONG
                                                        ).show()

                                                        onCodeScanned?.invoke(code)
                                                    }
                                                }
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    }

                                    else -> {
                                        imageProxy.close()
                                    }
                                    // Остальные режимы
                                }

                            }
                        }

                    previewView.doOnLayout {

                        val viewPort = previewView.viewPort
                            ?: return@doOnLayout

                        val useCaseGroup = UseCaseGroup.Builder()
                            .setViewPort(viewPort)
                            .addUseCase(previewUseCase)
                            .addUseCase(analysisUseCase)
                            .build()

                        try {
                            cameraProvider.unbindAll()

                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                useCaseGroup
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .height(160.dp)
                .border(
                    width = 10.dp,
                    color = Gray,
                    shape = RoundedCornerShape(40.dp)
                )
                .onGloballyPositioned {coordinates ->
                    val bounds = coordinates.boundsInParent()

                    scanArea.set(
                        RectF(
                            bounds.left,
                            bounds.top,
                            bounds.right,
                            bounds.bottom
                        )
                    )
                }
        )

        Button(
            onClick = { scanRequested.set(true)
                 },
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        ) {
            Text("Отсканировать")
        }
    }
}
