package digital.guimauve.ui.codescanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A multiplatform Composable view for scanning barcodes, QR codes, and more.
 * Set `codeTypes` to specify which codes to scan for, e.g. listOf(CodeType.QR).
 * Use `onResult` to handle the scan result or error.
 * For testing, set `simulatedData` to provide test data in the simulator.
 */
@Composable
expect fun CodeScannerView(
    codeTypes: List<CodeType>,
    scanMode: ScanMode = ScanMode.ONCE,
    scanInterval: Int = 2,
    showViewfinder: Boolean = false,
    simulatedData: String = "",
    shouldVibrateOnSuccess: Boolean = true,
    onResult: (ScanResult) -> Unit,
    modifier: Modifier = Modifier,
)
