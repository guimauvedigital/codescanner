package digital.guimauve.ui.codescanner

sealed class ScanResult {

    data class Success(
        val string: String,
        val type: CodeType,
    ) : ScanResult()

    data class Error(
        val error: ScanError,
    ) : ScanResult()

}
