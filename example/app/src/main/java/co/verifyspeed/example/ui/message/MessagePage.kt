package co.verifyspeed.example.ui.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.verifyspeed.example.ui.common.VerificationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagePage(
        modifier: Modifier = Modifier,
        viewModel: MessageViewModel = remember { MessageViewModel() },
        method: String,
        onVerificationSuccess: () -> Unit,
        onBackPressed: () -> Unit = {},
) {
    val snackBarHostState = remember { SnackbarHostState() }

    // Show error in SnackBar when error state changes
    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { error -> snackBarHostState.showSnackbar(message = error) }
    }

    VerificationDialog(
            showDialog = viewModel.showSuccessDialog,
            isLoading = viewModel.isLoading,
            phoneNumber = viewModel.phoneNumber,
            title = "Verification Successful",
            onDismiss = onVerificationSuccess
    )

    Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                        title = { Text("Verify with Message") },
                        navigationIcon = {
                            IconButton(onClick = onBackPressed) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
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
