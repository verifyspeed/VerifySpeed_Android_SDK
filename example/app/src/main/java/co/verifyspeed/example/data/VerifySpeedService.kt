package co.verifyspeed.example.data

import android.util.Log
import co.verifyspeed.androidlibrary.VerifySpeed
import co.verifyspeed.androidlibrary.VerifySpeedError
import co.verifyspeed.androidlibrary.VerifySpeedListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class VerifySpeedService {
    companion object {
        private const val BASE_URL = "YOUR_BASE_URL"
        private const val TAG = "VerifySpeedService"
    }

    suspend fun getVerificationKey(methodName: String): VerificationKeyModel {
        return withContext(Dispatchers.IO) {
            // * TIP: Get verification key and deep link
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

    suspend fun verifyPhoneNumberWithOtp(verificationKey: String, phoneNumber: String) {
        withContext(Dispatchers.IO) {
            // * TIP: Verify phone number with OTP
            VerifySpeed.verifyPhoneNumberWithOtp(
                    verificationKey = verificationKey,
                    phoneNumber = phoneNumber
            )
        }
    }

    suspend fun validateOtp(
            otpCode: String,
            verificationKey: String,
            onSuccess: (String) -> Unit,
            onError: (VerifySpeedError) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            // * TIP: Validate OTP
            VerifySpeed.validateOTP(
                    otpCode = otpCode,
                    verificationKey = verificationKey,
                    callBackListener =
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
    }

    suspend fun getPhoneNumber(token: String): String {
        return withContext(Dispatchers.IO) {
            // * TIP: Get phone number
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
