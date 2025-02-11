package co.verifyspeed.example.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VerificationDialog(
    showDialog: Boolean,
    isLoading: Boolean,
    phoneNumber: String?,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit = onDismiss
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Your verified phone number is: $phoneNumber")
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm) { Text("OK") }
            }
        )
    }
} 