package co.verifyspeed.example.android.views.ui.phone;

import android.util.Log;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import co.verifyspeed.example.android.views.data.VerificationKeyModel;
import co.verifyspeed.example.android.views.data.VerifySpeedService;
import co.verifyspeed.example.android.views.data.VerifySpeedService.Result;
import co.verifyspeed.example.android.views.ui.main.MainViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhoneNumberViewModel extends ViewModel {
    private final VerifySpeedService verifySpeedService;
    private final MainViewModel mainViewModel;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> selectedCountry = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String TAG = "PhoneNumberViewModel";

    public PhoneNumberViewModel() {
        this.verifySpeedService = new VerifySpeedService();
        this.mainViewModel = null;
    }

    public PhoneNumberViewModel(VerifySpeedService verifySpeedService, MainViewModel mainViewModel) {
        this.verifySpeedService = verifySpeedService;
        this.mainViewModel = mainViewModel;
        observeCountryCode();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSelectedCountry() {
        return selectedCountry;
    }

    public void fetchCountryCode() {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                Result<String> result = verifySpeedService.getCountry().get();
                if (result.isSuccess()) {
                    selectedCountry.postValue(result.getValue());
                } else {
                    error.postValue(result.getError().getMessage());
                }
            } catch (Exception e) {
                error.postValue("Error fetching country code: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    private void observeCountryCode() {
        if (mainViewModel != null) {
            String countryCode = mainViewModel.getCountryCode().getValue();
            selectedCountry.setValue(countryCode);
        }
    }

    public interface OnOtpSentListener {
        void onSuccess(String verificationKey);
    }

    public void sendOtp(String phoneNumber, String method, OnOtpSentListener listener) {
        executorService.execute(() -> {
            try {
                isLoading.postValue(true);
                error.postValue(null);

                // Get verification key
                VerificationKeyModel response = verifySpeedService.getVerificationKey(method).get();

                // Verify phone number with OTP
                verifySpeedService.verifyPhoneNumberWithOtp(
                        response.getVerificationKey(),
                        phoneNumber
                );

                // Navigate after successful OTP initiation
                listener.onSuccess(response.getVerificationKey());
            } catch (Exception e) {
                String errorMessage = "Network error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
                error.postValue(errorMessage);
                Log.e(TAG, "Error: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
}
