package co.verifyspeed.example.android.views.data;

import android.util.Log;
import co.verifyspeed.android.VerifySpeed;
import co.verifyspeed.android.VerifySpeedError;
import co.verifyspeed.android.VerifySpeedListener;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VerifySpeedService {
    private static final String BASE_URL = "YOUR_BASE_URL";
    private static final String TAG = "VerifySpeedService";
    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Future<VerificationKeyModel> getVerificationKey(String methodName) {
        return executorService.submit(new Callable<VerificationKeyModel>() {
            @Override
            public VerificationKeyModel call() throws Exception {
                // Get verification key and deep link
                URL url = new URL(BASE_URL + "/YOUR_ENDPOINT");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject requestBody = new JSONObject();
                requestBody.put("methodName", methodName);
                connection.getOutputStream().write(requestBody.toString().getBytes());

                JSONObject response = readResponse(connection);
                return new VerificationKeyModel(
                        response.getString("verificationKey"),
                        response.optString("deepLink")
                );
            }
        });
    }

    public void verifyPhoneNumberWithOtp(String verificationKey, String phoneNumber) {
        executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                //* TIP: Verify phone number with OTP
                VerifySpeed.verifyPhoneNumberWithOtp(
                        phoneNumber,
                        verificationKey
                );
                return null;
            }
        });
    }

    public void validateOtp(
            String otpCode,
            String verificationKey,
            final OnValidationListener listener
    ) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                //* TIP: Validate OTP
                VerifySpeed.validateOTP(
                        otpCode,
                        verificationKey,
                        new VerifySpeedListener() {
                            @Override
                            public void onFail(VerifySpeedError error) {
                                Log.e(TAG, "Validation failed: " + error);
                                listener.onError(error);
                            }

                            @Override
                            public void onSuccess(String token) {
                                listener.onSuccess(token);
                            }
                        }
                );
            }
        });
    }

    public Future<String> getPhoneNumber(String token) {
        return executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                URL url = new URL(BASE_URL + "/YOUR_ENDPOINT");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject requestBody = new JSONObject();
                requestBody.put("token", token);
                connection.getOutputStream().write(requestBody.toString().getBytes());

                JSONObject response = readResponse(connection);
                return response.getString("phoneNumber");
            }
        });
    }

    public Future<Result<String>> getCountry() {
        return executorService.submit(new Callable<Result<String>>() {
            @Override
            public Result<String> call() {
                try {
                    URL url = new URL("https://api.country.is");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    JSONObject response = readResponse(connection);
                    String isoCountryCode = response.getString("country");
                    String callingCode = "+" + phoneUtil.getCountryCodeForRegion(isoCountryCode);
                    return Result.success(callingCode);
                } catch (Exception e) {
                    return Result.failure(e);
                }
            }
        });
    }

    private JSONObject readResponse(HttpURLConnection connection) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return new JSONObject(response.toString());
    }

    public interface OnValidationListener {
        void onSuccess(String token);
        void onError(VerifySpeedError error);
    }

    public static class Result<T> {
        private final T value;
        private final Exception error;

        private Result(T value, Exception error) {
            this.value = value;
            this.error = error;
        }

        public static <T> Result<T> success(T value) {
            return new Result<>(value, null);
        }

        public static <T> Result<T> failure(Exception error) {
            return new Result<>(null, error);
        }

        public T getValue() {
            return value;
        }

        public Exception getError() {
            return error;
        }

        public boolean isSuccess() {
            return error == null;
        }
    }
}
