package co.verifyspeed.example.ui.otp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.verifyspeed.example.data.VerifySpeedService
import kotlinx.coroutines.launch

class OtpViewModel(private val verifySpeedService: VerifySpeedService = VerifySpeedService()) :
        ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var phoneNumber by mutableStateOf<String?>(null)
        private set
    var showSuccessDialog by mutableStateOf(false)
        private set

    fun verifyOtp(otp: String, verificationKey: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                verifySpeedService.validateOtp(
                        otpCode = otp,
                        verificationKey = verificationKey,
                        onSuccess = { token -> showPhoneNumberDialog(token, onSuccess) },
                        onError = { error -> handleError(error.message) }
                )
            } catch (e: Exception) {
                handleError(e.message ?: "Verification failed")
            }
        }
    }

    private fun showPhoneNumberDialog(token: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                showSuccessDialog = true

                phoneNumber = verifySpeedService.getPhoneNumber(token)
                onSuccess()
            } catch (e: Exception) {
                handleError("Failed to get phone number")
            } finally {
                isLoading = false
            }
        }
    }

    private fun handleError(message: String) {
        error = message
        Log.e(TAG, message)
    }

    companion object {
        private const val TAG = "OtpViewModel"
    }
}
