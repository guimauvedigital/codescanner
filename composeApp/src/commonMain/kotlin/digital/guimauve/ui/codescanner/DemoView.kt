package digital.guimauve.ui.codescanner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun DemoView() {

    var results by remember { mutableStateOf<ScanResult?>(null) }

    Column {
        CodeScannerView(
            codeTypes = listOf(CodeType.QR_CODE),
            onResult = { code ->
                results = code
            },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
        results?.let {
            Text(
                text = results.toString(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

}
