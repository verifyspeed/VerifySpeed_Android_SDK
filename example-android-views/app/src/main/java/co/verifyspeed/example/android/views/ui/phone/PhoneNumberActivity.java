package co.verifyspeed.example.android.views.ui.phone;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import co.verifyspeed.example.android.views.R;
import co.verifyspeed.example.android.views.ui.common.VerificationDialog;
import co.verifyspeed.example.android.views.ui.otp.OtpActivity;

public class PhoneNumberActivity extends AppCompatActivity {
    private PhoneNumberViewModel viewModel;
    private TextInputEditText countryCodeInput;
    private TextInputEditText phoneNumberInput;
    private Button sendOtpButton;
    private ProgressBar progressBar;
    private String method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamicColors.applyToActivityIfAvailable(this);

        setContentView(R.layout.activity_phone_number);

        // Get method from intent
        method = getIntent().getStringExtra("method");
        if (method == null) {
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(PhoneNumberViewModel.class);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        countryCodeInput = findViewById(R.id.countryCodeInput);
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        progressBar = findViewById(R.id.progressBar);

        // Fetch country code
        viewModel.fetchCountryCode();

        // Set up input validation
        setupPhoneNumberValidation();

        // Set up click listeners
        sendOtpButton.setOnClickListener(v -> sendOtp());

        // Observe ViewModel states
        observeViewModelStates();
    }

    private void setupPhoneNumberValidation() {
        TextWatcher phoneValidationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        };

        countryCodeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (!text.startsWith("+")) {
                    countryCodeInput.setText("+" + text);
                    countryCodeInput.setSelection(countryCodeInput.length());
                }
                validateInputs();
            }
        });

        phoneNumberInput.addTextChangedListener(phoneValidationWatcher);
    }

    private void validateInputs() {
        String countryCode = countryCodeInput.getText().toString();
        String phoneNumber = phoneNumberInput.getText().toString();
        boolean isValid = countryCode.length() > 1 && phoneNumber.length() > 0;
        sendOtpButton.setEnabled(isValid);
    }

    private void sendOtp() {
        String countryCode = countryCodeInput.getText().toString();
        String phoneNumber = phoneNumberInput.getText().toString();
        String fullNumber = countryCode + phoneNumber;
        fullNumber = fullNumber.replaceAll("[^+\\d]", "");
        
        viewModel.sendOtp(fullNumber, method, verificationKey -> {
            Intent intent = new Intent(this, OtpActivity.class);
            intent.putExtra("verificationKey", verificationKey);
            startActivity(intent);
            finish();
        });
    }

    private void observeViewModelStates() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            sendOtpButton.setEnabled(!isLoading && phoneNumberInput.length() > 0);
            countryCodeInput.setEnabled(!isLoading);
            phoneNumberInput.setEnabled(!isLoading);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });

        // Observe country code changes
        viewModel.getSelectedCountry().observe(this, countryCode -> {
            if (countryCode != null && !countryCode.isEmpty()) {
                countryCodeInput.setText(countryCode);
            }
        });
    }
} 