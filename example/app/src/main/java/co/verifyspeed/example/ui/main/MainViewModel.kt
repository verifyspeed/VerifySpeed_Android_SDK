package co.verifyspeed.example.ui.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.verifyspeed.example.data.VerifySpeedService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val verifySpeedService = VerifySpeedService()
    private val _countryCode = MutableStateFlow<String?>(null)
    val countryCode = _countryCode.asStateFlow()
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var phoneNumber by mutableStateOf<String?>(null)
        private set
    var showSuccessDialog by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            verifySpeedService.getCountry().onSuccess { code -> _countryCode.value = code }
        }
    }

    fun showSuccessDialog() {
        showSuccessDialog = true
    }

    fun hideSuccessDialog() {
        showSuccessDialog = false
    }

    suspend fun getPhoneNumberFromToken(token: String) {
        try {
            isLoading = true
            val number = verifySpeedService.getPhoneNumber(token)
            phoneNumber = number
        } catch (e: Exception) {
            // Handle error if needed
        } finally {
            isLoading = false
        }
    }
}
