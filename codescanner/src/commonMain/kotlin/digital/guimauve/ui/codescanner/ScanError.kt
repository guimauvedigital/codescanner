package digital.guimauve.ui.codescanner

/**
 * An enum describing the ways CodeScannerView can hit scanning problems.
 */
enum class ScanError {

    /**
     * The camera could not be accessed.
     */
    BAD_INPUT,

    /**
     * The camera was not capable of scanning the requested codes.
     */
    BAD_OUTPUT,

    /**
     * Initialization failed.
     */
    INIT_ERROR,

    /**
     * The camera permission is denied
     */
    PERMISSION_DENIED,

}
