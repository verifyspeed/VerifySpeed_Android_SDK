package co.verifyspeed.example.android.views.ui.main;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import co.verifyspeed.example.android.views.data.VerifySpeedService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private final VerifySpeedService verifySpeedService;
    private final MutableLiveData<String> countryCode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showSuccessDialog = new MutableLiveData<>(false);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MainViewModel(Application application) {
        super(application);
        verifySpeedService = new VerifySpeedService();
        
        // Initialize country code
        executorService.execute(() -> {
            try {
                VerifySpeedService.Result<String> result = verifySpeedService.getCountry().get();
                if (result.isSuccess()) {
                    countryCode.postValue(result.getValue());
                } else {
                    error.postValue(result.getError().getMessage());
                }
            } catch (Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public LiveData<String> getCountryCode() {
        return countryCode;
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

    public void showSuccessDialog() {
        showSuccessDialog.setValue(true);
    }

    public void hideSuccessDialog() {
        showSuccessDialog.setValue(false);
    }

    public void getPhoneNumberFromToken(String token) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                String number = verifySpeedService.getPhoneNumber(token).get();
                phoneNumber.postValue(number);
            } catch (Exception e) {
                error.postValue(e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void setPhoneNumber(String number) {
        phoneNumber.setValue(number);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
