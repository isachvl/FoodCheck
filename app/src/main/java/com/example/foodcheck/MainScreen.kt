package com.example.foodcheck


import android.Manifest
import android.R.attr.delay
import android.content.pm.PackageManager
import android.util.Size
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.foodcheck.ui.theme.Gray
import android.graphics.RectF
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.UseCaseGroup
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.view.doOnLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.foodcheck.internet.Product
import com.example.foodcheck.ui.theme.Whilteo
import java.util.concurrent.atomic.AtomicReference
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean


@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(
    markerClass = [TransformExperimental::class]
)
@Composable
fun MainScreen(showStorage: Boolean = false, onChooseStorage: (Product) -> Unit = {}, viewModel: ViewModelMainScreen = koinViewModel()) {
    val scanType = ScanType.ScanQr
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val showProductInfo = remember {
        mutableStateOf(false)
    }


    var cameraGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraGranted.value = granted
    }

    LaunchedEffect(Unit) {
        if (!cameraGranted.value) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    LaunchedEffect(state) {
        when (val currentState = state) {

            is ViewModelMainScreen.ProductState.Success -> {
                showProductInfo.value  = true
            }

            is ViewModelMainScreen.ProductState.Error -> {
                Toast.makeText(
                    context,
                    currentState.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> Unit
        }
    }


    val scanArea = remember {
        AtomicReference<RectF?>(null)
    }
    val transformFactory = remember {
        ImageProxyTransformFactory().apply {
            setUsingRotationDegrees(true)
        }
    }
    val showDonut = remember {
        mutableStateOf(false)
    }
    val previewHolder = remember {
        AtomicReference<PreviewView?>(null)
    }

    val success = state as?
            ViewModelMainScreen.ProductState.Success

    val blurFrame = remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    val blurRadius = remember {
        Animatable(0f)
    }

    val foundCode = remember {
        mutableStateOf<String?>(null)
    }
    val donutComposition = rememberLottieComposition(

        LottieCompositionSpec.RawRes(R.raw.loading)
    )
    val lifecycleOwner = LocalLifecycleOwner.current//Получаем информацию об Android

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




    LaunchedEffect(showDonut.value) {

        if (showDonut.value) {

            try {
                // Начинаем без размытия
                blurRadius.snapTo(0f)

                // Плавно размываем изображение
                blurRadius.animateTo(
                    targetValue = 20f,
                    animationSpec = tween(450)
                )

                // Ждём штрихкод от ML Kit максимум 10 секунд
                val code = withTimeoutOrNull(10_000L) {
                    snapshotFlow { foundCode.value }
                        .first { it != null }
                }

                // Останавливаем сканирование
                scanRequested.set(false)

                // Сразу запускаем запрос к API
                if (code != null) {
                    viewModel.onBarcodeScanned(code)
                }

                // Пока загружается продукт, убираем размытие
                blurRadius.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(200)
                )

            } finally {
                // Очищаем состояние сканирования
                scanRequested.set(false)
                blurFrame.value = null
                showDonut.value = false
            }
        }
    }
    if (cameraGranted.value) {
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
                    previewHolder.set(previewView)

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
                                                    val targetTransform =
                                                        previewView.outputTransform

                                                    if (area != null && targetTransform != null) {

                                                        // 3. Создаём преобразование:
                                                        // кадр камеры -> экран камеры
                                                        val transform = CoordinateTransform(
                                                            sourceTransform,
                                                            targetTransform
                                                        )

                                                        // 4. Ищем первый код внутри рамки
                                                        val barcode =
                                                            barcodes.firstOrNull { barcode ->

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

                                                            if (scanRequested.compareAndSet(
                                                                    true,
                                                                    false
                                                                )
                                                            ) {
                                                                foundCode.value = code
                                                            }
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
            blurFrame.value?.let { frame ->

                // Первый слой: размытая фотография целиком
                Image(
                    bitmap = frame,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(blurRadius.value.dp)
                )

                // Второй слой: чёткая фотография внутри рамки
                Image(
                    bitmap = frame,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {

                            val area = scanArea.get()

                            if (area != null) {
                                clipRect(
                                    left = area.left,
                                    top = area.top,
                                    right = area.right,
                                    bottom = area.bottom
                                ) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                        }
                )
            }
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
                    .onGloballyPositioned { coordinates ->
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

            val glassShape = RoundedCornerShape(40.dp)
            if (showProductInfo.value == false && !showStorage) {
                Button(
                    onClick = {

                        if (!showDonut.value) {

                            val bitmap = previewHolder.get()?.bitmap

                            if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                                foundCode.value = null
                                // Сохраняем снимок камеры
                                blurFrame.value = bitmap.asImageBitmap()

                                // Показываем пончик и запускаем размытие
                                showDonut.value = true

                                // Запускаем ML Kit
                                scanRequested.set(true)

                            } else {
                                Toast.makeText(
                                    context,
                                    "Камера ещё не готова",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .size(width = 200.dp, height = 60.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color(0xFF80CFFF).copy(alpha = 0.20f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            ),
                            shape = glassShape
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            ),
                            shape = glassShape
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),


                    ) {
                    Text(stringResource(R.string.Scan), fontSize = 20.sp)
                }
            }
            if (showDonut.value) {
                LottieAnimation(
                    composition = donutComposition.value,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(110.dp)
                )
            }


            if (showProductInfo.value  && success != null) {

                Box(

                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .align(Alignment.Center)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center

                ) {
                    ScannerInfo(
                        product = success.product,
                        onbackclick = {
                            showProductInfo.value = false
                        },
                        onAddToStorage = onChooseStorage
                    )
                }
            }
        }
    } else { Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Для сканирования нужен доступ к камере")

        Button(
            onClick = {
                cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        ) {
            Text("Разрешить доступ")
        }
    }}
}
