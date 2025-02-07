package co.verifyspeed.example.ui.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import co.verifyspeed.example.ui.main.MainViewModel

@Composable
fun PhoneNumberPage(
    modifier: Modifier = Modifier,
    method: String,
    mainViewModel: MainViewModel,
    onNavigateToOtp: (String) -> Unit
) {
    val viewModel = remember { PhoneNumberViewModel(mainViewModel = mainViewModel) }
    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf(viewModel.selectedCountry.value ?: "+1") }
    val snackBarHostState = remember { SnackbarHostState() }

    // Show error in SnackBar when error state changes
    LaunchedEffect(viewModel.error) {
        viewModel.error?.let { error -> snackBarHostState.showSnackbar(message = error) }
    }

    // Update country code when selectedCountry changes
    LaunchedEffect(viewModel.selectedCountry) {
        viewModel.selectedCountry.value?.let { countryCode = it }
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
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Country code input
                OutlinedTextField(
                        value = countryCode,
                        onValueChange = {
                            if (it.length <= 4 && (it.isEmpty() || it.matches(Regex("^\\+?\\d*\$")))
                            ) {
                                countryCode = if (!it.startsWith("+")) "+$it" else it
                            }
                        },
                        label = { Text("Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(0.3f)
                )

                // Phone number input
                OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == ' ' || it == '-' }) {
                                phoneNumber = newValue.take(15)
                            }
                        },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                    onClick = {
                        val fullNumber = "$countryCode$phoneNumber".replace(Regex("[^+\\d]"), "")
                        viewModel.sendOtp(
                                phoneNumber = fullNumber,
                                method = method,
                                onSuccess = onNavigateToOtp
                        )
                    },
                    enabled = phoneNumber.isNotBlank() && !viewModel.isLoading,
                    modifier = Modifier.fillMaxWidth()
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send OTP")
                }
            }
        }
    }
}
