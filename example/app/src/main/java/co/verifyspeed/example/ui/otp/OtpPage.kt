package co.verifyspeed.example.ui.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.verifyspeed.example.Screen

@Composable
fun OtpPage(
        modifier: Modifier = Modifier,
        verificationKey: String,
        onVerificationSuccess: () -> Unit,
        navController: NavHostController,
) {
    val viewModel = remember { OtpViewModel() }
    var otpValue by remember { mutableStateOf("") }
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
                        onDismissRequest = {
                            navController.navigate(Screen.Methods.route) {
                                popUpTo(Screen.Methods.route) { inclusive = true }
                            }
                        },
                        title = { Text("Verification Successful") },
                        text = {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Your verified phone number is: ${viewModel.phoneNumber}")
                            }
                        },
                        confirmButton = {
                            TextButton(
                                    onClick = {
                                        navController.navigate(Screen.Methods.route) {
                                            popUpTo(Screen.Methods.route) { inclusive = true }
                                        }
                                    }
                            ) { Text("OK") }
                        }
                )
            }

            OutlinedTextField(
                    value = otpValue,
                    onValueChange = { newValue: String ->
                        if (newValue.length <= 6 && newValue.all { char -> char.isDigit() }) {
                            otpValue = newValue
                        }
                    },
                    label = { Text("Enter 6-digit OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                    onClick = {
                        viewModel.verifyOtp(
                                otp = otpValue,
                                verificationKey = verificationKey,
                                onSuccess = onVerificationSuccess
                        )
                    },
                    enabled = otpValue.length == 6,
                    modifier = Modifier.fillMaxWidth()
            ) { Text("Verify OTP") }
        }
    }
}
