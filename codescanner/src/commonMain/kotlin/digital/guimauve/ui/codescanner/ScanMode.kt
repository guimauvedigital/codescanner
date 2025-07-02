package digital.guimauve.ui.codescanner

/**
 * The operating mode for CodeScannerView.
 */
enum class ScanMode {

    /**
     * Scan exactly one code, then stop.
     */
    ONCE,

    /**
     * Scan each code no more than once.
     */
    ONCE_PER_CODE,

    /**
     * Keep scanning all codes until dismissed.
     */
    CONTINUOUS,

    /**
     * Scan only when capture button is tapped.
     */
    MANUAL,

}
