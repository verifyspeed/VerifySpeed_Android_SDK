package co.verifyspeed.example.android.views.ui.otp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import co.verifyspeed.example.android.views.R;
import co.verifyspeed.example.android.views.ui.common.VerificationDialog;

public class OtpActivity extends AppCompatActivity {
    private OtpViewModel viewModel;
    private TextInputEditText otpInput;
    private Button verifyButton;
    private ProgressBar progressBar;
    private String verificationKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamicColors.applyToActivityIfAvailable(this);

        setContentView(R.layout.activity_otp);

        // Get verification key from intent
        verificationKey = getIntent().getStringExtra("verificationKey");
        if (verificationKey == null) {
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(OtpViewModel.class);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> new OnBackPressedDispatcher());

        // Initialize views
        otpInput = findViewById(R.id.otpInput);
        verifyButton = findViewById(R.id.verifyButton);
        progressBar = findViewById(R.id.progressBar);

        // Set up input validation
        setupOtpInputValidation();

        // Set up click listeners
        verifyButton.setOnClickListener(v -> verifyOtp());

        // Observe ViewModel states
        observeViewModelStates();
    }

    private void setupOtpInputValidation() {
        otpInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                verifyButton.setEnabled(s.length() == 6);
            }
        });
    }

    private void verifyOtp() {
        String otp = Objects.requireNonNull(otpInput.getText()).toString();
        viewModel.verifyOtp(otp, verificationKey, () -> {});
    }

    private void observeViewModelStates() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            verifyButton.setEnabled(!isLoading && otpInput.length() == 6);
            otpInput.setEnabled(!isLoading);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getShowSuccessDialog().observe(this, showDialog -> {
            if (showDialog) {
                showVerificationDialog();
            }
        });
    }

    private void showVerificationDialog() {
        VerificationDialog dialog = new VerificationDialog(
                this,
                "Verification Successful",
                Boolean.TRUE.equals(viewModel.getIsLoading().getValue()),
                viewModel.getPhoneNumber().getValue(),
                this::finish
        );
        dialog.show();
    }
} 