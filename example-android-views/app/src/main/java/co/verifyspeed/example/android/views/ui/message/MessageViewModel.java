package co.verifyspeed.example.android.views.ui.message;

import android.util.Log;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import co.verifyspeed.example.android.views.data.VerificationKeyModel;
import co.verifyspeed.example.android.views.data.VerifySpeedService;
import co.verifyspeed.android.VerifySpeed;
import co.verifyspeed.android.VerifySpeedError;
import co.verifyspeed.android.VerifySpeedErrorType;
import co.verifyspeed.android.VerifySpeedListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageViewModel extends ViewModel {
    private final VerifySpeedService verifySpeedService;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showSuccessDialog = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String TAG = "MessageViewModel";

    public MessageViewModel() {
        this.verifySpeedService = new VerifySpeedService();
    }

    public MessageViewModel(VerifySpeedService verifySpeedService) {
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

    public void verifyWithMessage(String method) {
        isLoading.setValue(true);
        error.setValue(null);

        executorService.execute(() -> {
            try {
                // Get verification key and deep link
                VerificationKeyModel response = verifySpeedService.getVerificationKey(method).get();

                // TIP: Verify phone number with Deep Link
                Log.d(TAG, "Attempting verification with deep link: " + response.getDeepLink());
                Log.d(TAG, "Using verification key: " + response.getVerificationKey());

                //* TIP: Verify phone number with Deep Link
                VerifySpeed.verifyPhoneNumberWithDeepLink(
                        response.getDeepLink(), //  deeplink: String,
                        response.getVerificationKey(), //  verificationKey: String,
                        true, //  redirectToStore: Boolean
                        new VerifySpeedListener() { //  callBackListener: VerifySpeedListener
                            @Override
                            public void onSuccess(String token) {
                                Log.d(TAG, "Verification successful with token: " + token);
                                showPhoneNumberDialog(token);
                            }

                            @Override
                            public void onFail(VerifySpeedError verifySpeedError) {
                                String errorMessage;
                                Log.e(TAG, "Verification failed with error type: " + verifySpeedError.getType());
                                Log.e(TAG, "Error message: " + verifySpeedError.getMessage());

                                if (verifySpeedError.getType() == VerifySpeedErrorType.ActivityNotSet) {
                                    errorMessage = "Activity is not initialized";
                                } else if (verifySpeedError.getType() == VerifySpeedErrorType.InvalidDeepLink) {
                                    errorMessage = "Invalid deep link: " + response.getDeepLink();
                                } else if (verifySpeedError.getMessage() != null && !verifySpeedError.getMessage().isEmpty()) {
                                    errorMessage = verifySpeedError.getMessage();
                                } else {
                                    errorMessage = "Failed to launch verification app";
                                }
                                handleError(errorMessage);
                            }
                        }
                );
            } catch (Exception e) {
                handleError(e.getMessage() != null ? e.getMessage() : "Verification failed");
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    private void showPhoneNumberDialog(String token) {
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

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
