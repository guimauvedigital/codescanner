package digital.guimauve.ui.codescanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Vibrator
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@RequiresPermission(allOf = [Manifest.permission.VIBRATE, Manifest.permission.CAMERA])
@OptIn(ExperimentalGetImage::class)
@Composable
actual fun CodeScannerView(
    codeTypes: List<CodeType>,
    scanMode: ScanMode,
    scanInterval: Int,
    showViewfinder: Boolean,
    simulatedData: String,
    shouldVibrateOnSuccess: Boolean,
    onResult: (ScanResult) -> Unit,
    modifier: Modifier,
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Request camera permission if needed
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    if (!hasPermission) {
        // Launch permission request
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted -> hasPermission = granted }
        )
        LaunchedEffect(Unit) { launcher.launch(Manifest.permission.CAMERA) }
    }

    // Only start camera preview after permission granted
    if (!hasPermission) return

    AndroidView(
        factory = { ctx ->
            // Create PreviewView for camera preview
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            // Configure CameraX (ProcessCameraProvider) and ML Kit
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                // Build ML Kit barcode scanner options based on codeTypes
                val formats = when {
                    codeTypes.contains(CodeType.QR_CODE) -> Barcode.FORMAT_QR_CODE
                    else -> Barcode.FORMAT_ALL_FORMATS
                }
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(formats)
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                // Image analysis use-case
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage == null) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                val rawValue = barcode.rawValue ?: continue
                                val type = when (barcode.format) {
                                    Barcode.FORMAT_QR_CODE -> CodeType.QR_CODE
                                    Barcode.FORMAT_CODE_128 -> CodeType.CODE_128
                                    Barcode.FORMAT_EAN_13 -> CodeType.EAN_13
                                    else -> CodeType.QR_CODE
                                }
                                onResult(ScanResult.Success(rawValue, type))

                                if (shouldVibrateOnSuccess) {
                                    // Vibrate on success
                                    val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                    vibrator.vibrate(100)
                                }
                                if (scanMode == ScanMode.ONCE) {
                                    imageProxy.close()
                                    return@addOnSuccessListener
                                }
                            }
                            imageProxy.close()
                        }
                        .addOnFailureListener {
                            imageProxy.close()
                        }
                }

                // Bind to lifecycle
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )

}
