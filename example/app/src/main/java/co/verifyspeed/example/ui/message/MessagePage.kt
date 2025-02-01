package co.verifyspeed.example.ui.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessagePage(
        modifier: Modifier = Modifier,
        viewModel: MessageViewModel = remember { MessageViewModel() },
        method: String,
        onVerificationSuccess: () -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }

    // Show error in SnackBar when error state changes
    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { error -> snackBarHostState.showSnackbar(message = error) }
    }

    Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            if (viewModel.showSuccessDialog) {
                AlertDialog(
                        onDismissRequest = onVerificationSuccess,
                        title = { Text("Verification Successful") },
                        text = {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Your verified phone number is: ${viewModel.phoneNumber}")
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = onVerificationSuccess) { Text("OK") }
                        }
                )
            }

            Button(
                    onClick = { viewModel.verifyWithMessage(method = method) },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier.fillMaxWidth()
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Verify with Message")
                }
            }
        }
    }
}
