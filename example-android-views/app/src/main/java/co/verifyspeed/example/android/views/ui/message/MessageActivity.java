package co.verifyspeed.example.android.views.ui.message;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import co.verifyspeed.example.android.views.R;
import co.verifyspeed.android.VerifySpeed;
import co.verifyspeed.example.android.views.ui.common.VerificationDialog;

//* TIP: Message Activity
//* This activity is launched from MainActivity when user selects a message-based verification method
//* When user leaves the app and returns via deep link, onResume is called
//* We need to notify VerifySpeed on resume to handle the deep link verification flow
//* Best practice: Create a separate activity for handling deep links that destroys itself
//* after successful verification to prevent unnecessary notifyOnResumed calls
//* when there is no active verification in progress

public class MessageActivity extends AppCompatActivity {
    private MessageViewModel viewModel;
    private Button verifyButton;
    private ProgressBar progressBar;
    private String method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamicColors.applyToActivityIfAvailable(this);

        setContentView(R.layout.activity_message);

        //* TIP: Set your activity
        VerifySpeed.setActivity(this);

        // Get method from intent
        method = getIntent().getStringExtra("method");
        if (method == null) {
            finish();
            return;
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MessageViewModel.class);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> new OnBackPressedDispatcher());

        // Initialize views
        verifyButton = findViewById(R.id.verifyButton);
        progressBar = findViewById(R.id.progressBar);

        // Set up click listeners
        verifyButton.setOnClickListener(v -> verifyWithMessage());

        // Observe ViewModel states
        observeViewModelStates();
    }

    private void verifyWithMessage() {
        viewModel.verifyWithMessage(method);
    }

    private void observeViewModelStates() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            verifyButton.setEnabled(!isLoading);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });

        // Show dialog when verification is successful and phone number is available
        viewModel.getShowSuccessDialog().observe(this, showDialog -> {
            if (showDialog && viewModel.getPhoneNumber().getValue() != null) {
                showVerificationDialog();
            }
        });

        // Show dialog when phone number becomes available
        viewModel.getPhoneNumber().observe(this, phoneNumber -> {
            if (phoneNumber != null && Boolean.TRUE.equals(viewModel.getShowSuccessDialog().getValue())) {
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

    @Override
    protected void onResume() {
        super.onResume();
         //* TIP Notify when activity is resumed for Deep Link Process
        VerifySpeed.notifyOnResumed();
    }
}
