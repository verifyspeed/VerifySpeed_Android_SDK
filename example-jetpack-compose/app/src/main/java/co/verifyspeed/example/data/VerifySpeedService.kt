package co.verifyspeed.example.data

import android.util.Log
import co.verifyspeed.android.VerifySpeed
import co.verifyspeed.android.VerifySpeedError
import co.verifyspeed.android.VerifySpeedListener
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class VerifySpeedService {
    companion object {
        private const val BASE_URL = "YOUR_BASE_URL"
        private const val TAG = "VerifySpeedService"
        private val phoneUtil = PhoneNumberUtil.getInstance()
    }

    suspend fun getVerificationKey(methodName: String): VerificationKeyModel {
        return withContext(Dispatchers.IO) {
            //* TIP: Get verification key and deep link
            val url = URL("$BASE_URL/YOUR_ENDPOINT")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestBody = JSONObject().apply { put("methodName", methodName) }
            connection.outputStream.write(requestBody.toString().toByteArray())

            val response = readResponse(connection)
            VerificationKeyModel(
                    verificationKey = response.getString("verificationKey"),
                    deepLink = response.optString("deepLink")
            )
        }
    }

    fun verifyPhoneNumberWithOtp(verificationKey: String, phoneNumber: String) {
        //* TIP: Verify phone number with OTP
        VerifySpeed.verifyPhoneNumberWithOtp(
            phoneNumber,
            verificationKey,
        )
    }

    fun validateOtp(
            otpCode: String,
            verificationKey: String,
            onSuccess: (String) -> Unit,
            onError: (VerifySpeedError) -> Unit
    ) {
        //* TIP: Validate OTP
        VerifySpeed.validateOTP(
            otpCode,
            verificationKey,
            object : VerifySpeedListener {
                override fun onFail(error: VerifySpeedError) {
                    Log.e(TAG, "Validation failed: $error")
                    onError(error)
                }

                override fun onSuccess(token: String) {
                    onSuccess(token)
                }
            }
        )
    }

    suspend fun getPhoneNumber(token: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL("$BASE_URL/YOUR_ENDPOINT")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestBody = JSONObject().apply { put("token", token) }
            connection.outputStream.write(requestBody.toString().toByteArray())

            val response = readResponse(connection)
            response.getString("phoneNumber")
        }
    }

    suspend fun getCountry(): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                val url = URL("https://api.country.is")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val response = readResponse(connection)
                val isoCountryCode = response.getString("country")
                val callingCode = "+${phoneUtil.getCountryCodeForRegion(isoCountryCode)}"
                Result.success(callingCode)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readResponse(connection: HttpURLConnection): JSONObject {
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()
        return JSONObject(response.toString())
    }
}
