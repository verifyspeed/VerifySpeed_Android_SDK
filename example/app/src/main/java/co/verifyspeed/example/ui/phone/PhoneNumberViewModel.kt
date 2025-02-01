package co.verifyspeed.example.ui.phone

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.verifyspeed.example.MainViewModel
import co.verifyspeed.example.data.VerifySpeedService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhoneNumberViewModel(
        private val verifySpeedService: VerifySpeedService = VerifySpeedService(),
        private val mainViewModel: MainViewModel
) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private val _selectedCountry = MutableStateFlow<String?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()

    init {
        viewModelScope.launch {
            mainViewModel.countryCode.collect { code -> _selectedCountry.value = code }
        }
    }

    fun sendOtp(phoneNumber: String, method: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null

                // Get verification key
                val response = verifySpeedService.getVerificationKey(method)

                // Verify phone number with OTP
                verifySpeedService.verifyPhoneNumberWithOtp(
                        verificationKey = response.verificationKey,
                        phoneNumber = phoneNumber
                )

                // Navigate after successful OTP initiation
                onSuccess(response.verificationKey)
            } catch (e: Exception) {
                error = "Network error: ${e.message}"
                Log.e(TAG, "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    companion object {
        private const val TAG = "PhoneNumberViewModel"
    }
}
