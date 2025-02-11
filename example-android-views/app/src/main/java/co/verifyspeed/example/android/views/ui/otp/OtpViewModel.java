package co.verifyspeed.example.android.views.ui.otp;

import android.util.Log;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import co.verifyspeed.example.android.views.data.VerifySpeedService;
import co.verifyspeed.android.VerifySpeedError;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OtpViewModel extends ViewModel {
    private final VerifySpeedService verifySpeedService;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showSuccessDialog = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String TAG = "OtpViewModel";

    public OtpViewModel() {
        this.verifySpeedService = new VerifySpeedService();
    }

    public OtpViewModel(VerifySpeedService verifySpeedService) {
        this.verifySpeedService = verifySpeedService;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getPhoneNumber() {
        return phoneNumber;
    }

    public LiveData<Boolean> getShowSuccessDialog() {
        return showSuccessDialog;
    }

    public void verifyOtp(String otp, String verificationKey, Runnable onSuccess) {
        executorService.execute(() -> {
            try {
                verifySpeedService.validateOtp(
                    otp,
                    verificationKey,
                    new VerifySpeedService.OnValidationListener() {
                        @Override
                        public void onSuccess(String token) {
                            showPhoneNumberDialog(token, onSuccess);
                        }

                        @Override
                        public void onError(VerifySpeedError error) {
                            handleError(error.getMessage());
                        }
                    }
                );
            } catch (Exception e) {
                handleError(e.getMessage() != null ? e.getMessage() : "Verification failed");
            }
        });
    }

    private void showPhoneNumberDialog(String token, Runnable onSuccess) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                
                // First get the phone number
                String number = verifySpeedService.getPhoneNumber(token).get();
                
                // Make sure loading is false before showing dialog
                isLoading.postValue(false);
                
                // Format the phone number if needed
                if (number != null) {
                    number = number.trim();
                    if (!number.startsWith("+")) {
                        number = "+" + number;
                    }
                }
                
                // Update phone number and show dialog
                phoneNumber.postValue(number);
                showSuccessDialog.postValue(true);
                onSuccess.run();
            } catch (Exception e) {
                handleError("Failed to get phone number");
                isLoading.postValue(false);
            }
        });
    }

    private void handleError(String message) {
        error.postValue(message);
        Log.e(TAG, message);
    }
}
