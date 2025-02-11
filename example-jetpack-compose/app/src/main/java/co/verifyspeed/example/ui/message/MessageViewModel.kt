package co.verifyspeed.example.ui.message

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.verifyspeed.android.VerifySpeed
import co.verifyspeed.android.VerifySpeedError
import co.verifyspeed.android.VerifySpeedListener
import co.verifyspeed.example.data.VerifySpeedService
import kotlinx.coroutines.launch

class MessageViewModel(private val verifySpeedService: VerifySpeedService = VerifySpeedService()) :
        ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var phoneNumber by mutableStateOf<String?>(null)
        private set
    var showSuccessDialog by mutableStateOf(false)
        private set

    fun verifyWithMessage(method: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null

                // Get verification key and deep link
                val response = verifySpeedService.getVerificationKey(method)

                //* TIP: Verify phone number with Deep Link
                VerifySpeed.verifyPhoneNumberWithDeepLink(
                    response.deepLink!!,
                    response.verificationKey,
                    true,
                    object : VerifySpeedListener {
                        override fun onFail(error: VerifySpeedError) {
                            handleError(error.message)
                        }

                        override fun onSuccess(token: String) {
                            showPhoneNumberDialog(token)
                        }
                    }
                )
            } catch (e: Exception) {
                handleError(e.message ?: "Verification failed")
            } finally {
                isLoading = false
            }
        }
    }

    private fun showPhoneNumberDialog(token: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                showSuccessDialog = true

                phoneNumber = verifySpeedService.getPhoneNumber(token)
            } catch (e: Exception) {
                handleError("Failed to get phone number")
            } finally {
                isLoading = false
            }
        }
    }

    private fun handleError(message: String?) {
        error = message
        Log.e(TAG, message ?: "Null")
    }

    companion object {
        private const val TAG = "MessageViewModel"
    }
}
