
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
import androidx.camera.view.TransformExperimental
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import java.util.concurrent.atomic.AtomicReference
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(
    ExperimentalGetImage::class,
    TransformExperimental::class
)
@Composable
fun MainScreen(
    scanType: ScanType,
    onCodeScanned: ((String) -> Unit)? = null

) {
    val scanArea = remember {
        AtomicReference<RectF?>(null)
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
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
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

                                if (!scanRequested.compareAndSet(true, false)) {
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

                                when (scanType) {
                                    ScanType.ScanQr -> {
                                        barcodeScanner.process(inputImage)
                                            .addOnSuccessListener { barcodes ->
                                                barcodes.firstOrNull { barcode ->
                                                    val box = barcode.boundingBox
                                                    box != null
                                                }?.rawValue?.let { qrCode ->
                                                    onCodeScanned?.let { it(qrCode) }
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

                    try {
                        cameraProvider.unbindAll()

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            previewUseCase,
                            analysisUseCase
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
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

        )

        Button(
            onClick = { scanRequested.set(true) },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text("Отсканировать")
        }
    }
}
