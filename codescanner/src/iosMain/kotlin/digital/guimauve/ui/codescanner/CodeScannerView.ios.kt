package digital.guimauve.ui.codescanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSArray
import platform.Foundation.arrayWithArray
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
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

    UIKitView(
        factory = {
            val captureDelegate = object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
                override fun captureOutput(
                    output: AVCaptureOutput,
                    didOutputMetadataObjects: List<*>,
                    fromConnection: AVCaptureConnection,
                ) {
                    // Convert didOutputMetadataObjects to NSArray of AVMetadataObject
                    val metadataObjects = didOutputMetadataObjects as? List<AVMetadataObject> ?: return

                    val metadataObject = metadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject ?: return

                    val stringValue = metadataObject.stringValue ?: return
                    val codeType = when (metadataObject.type) {
                        AVMetadataObjectTypeQRCode -> CodeType.QR_CODE
                        AVMetadataObjectTypeEAN13Code -> CodeType.EAN_13
                        AVMetadataObjectTypeCode128Code -> CodeType.CODE_128
                        else -> CodeType.QR_CODE // Handle other types or unknown
                    }

                    // Call onResult with ScanResult (you may need to adapt ScanResult constructor)
                    onResult(
                        ScanResult.Success(
                            string = stringValue,
                            type = codeType,
                            //image = null, // Image capture not implemented here
                            //corners = metadataObject.corners
                        )
                    )
                }
            }

            // Set up AVCaptureSession and inputs
            val session = AVCaptureSession().apply {
                val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)!!
                val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)!!
                addInput(input)
                // Add metadata output for code scanning
                val metadataOutput = AVCaptureMetadataOutput().also {
                    addOutput(it)
                    it.setMetadataObjectsDelegate(objectsDelegate = captureDelegate, queue = dispatch_get_main_queue())
                    it.metadataObjectTypes = NSArray.arrayWithArray(codeTypes.map {
                        when (it) {
                            CodeType.QR_CODE -> AVMetadataObjectTypeQRCode
                            CodeType.EAN_13 -> AVMetadataObjectTypeEAN13Code
                            CodeType.CODE_128 -> AVMetadataObjectTypeCode128Code
                            else -> AVMetadataObjectTypeQRCode
                        }
                    }.toList())
                }
            }
            // Preview layer
            val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
                videoGravity = AVLayerVideoGravityResizeAspectFill
            }
            session.startRunning()

            // Custom UIView to host preview layer
            object : UIView(frame = CGRectZero.readValue()) {
                override fun layoutSubviews() {
                    super.layoutSubviews()
                    previewLayer.setFrame(this.frame)
                }
            }.apply {
                layer.addSublayer(previewLayer)
            }
        },
        modifier = modifier,
    )

}
