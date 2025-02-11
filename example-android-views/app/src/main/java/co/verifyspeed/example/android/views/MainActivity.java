package co.verifyspeed.example.android.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.color.DynamicColors;
import java.util.List;
import java.util.concurrent.ExecutionException;
import co.verifyspeed.android.VerifySpeed;
import co.verifyspeed.android.MethodModel;
import co.verifyspeed.example.android.views.data.VerifySpeedService;
import co.verifyspeed.example.android.views.ui.message.MessageActivity;
import co.verifyspeed.example.android.views.ui.main.MainViewModel;
import co.verifyspeed.example.android.views.ui.phone.PhoneNumberActivity;
import co.verifyspeed.example.android.views.ui.common.VerificationDialog;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private ProgressBar progressBar;
    private TextView messageText;
    private LinearLayout methodsContainer;
    VerifySpeedService verifySpeedService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dynamic colors
        DynamicColors.applyToActivityIfAvailable(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //* TIP: Set your activity
        VerifySpeed.setActivity(this);

        //* TIP: Set your client key
        VerifySpeed.setClientKey("YOUR_CLIENT_KEY");

        // Initialize services
        verifySpeedService = new VerifySpeedService();

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        messageText = findViewById(R.id.messageText);
        methodsContainer = findViewById(R.id.methodsContainer);

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Observe ViewModel states
        observeViewModelStates();

        // Initialize VerifySpeed and load methods
        initializeVerifySpeed();
        checkInterruptedSession();
    }

    private void observeViewModelStates() {
        mainViewModel.getShowSuccessDialog().observe(this, showDialog -> {
            if (showDialog) {
                showVerificationDialog();
            }
        });
    }

    @SuppressLint({"SetTextI18x", "SetTextI18n"})
    private void initializeVerifySpeed() {
        progressBar.setVisibility(View.VISIBLE);
        methodsContainer.setVisibility(View.GONE);
        messageText.setVisibility(View.GONE);

        //* TIP: Initialize to get available methods
        VerifySpeed.initialize().thenAccept(methods -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (methods.isEmpty()) {
                    messageText.setVisibility(View.VISIBLE); 
                    messageText.setText("Please check your client key set and correct to display available methods");
                } else {
                    methodsContainer.setVisibility(View.VISIBLE);
                    createMethodButtons(methods);
                }
            });
        }).exceptionally(error -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                messageText.setVisibility(View.VISIBLE);
                messageText.setText("Error: " + error.getMessage());
            });
            return null;
        });
    }

    private void checkInterruptedSession() {
        new Thread(() -> {
            //* TIP: Check for interrupted session
            VerifySpeed.checkInterruptedSession(token -> {
                if (token != null) {
                    try {
                        // Then show the dialog
                        runOnUiThread(() -> {
                            // First get the phone number
                            String phoneNumber = null;
                            try {
                                phoneNumber = verifySpeedService.getPhoneNumber(token).get();
                            } catch (ExecutionException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            // Update the ViewModel with the phone number
                            mainViewModel.setPhoneNumber(phoneNumber);
                            mainViewModel.showSuccessDialog();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            messageText.setVisibility(View.VISIBLE);
                            messageText.setText("Error retrieving phone number: " + e.getMessage());
                        });
                    }
                }
            });
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void createMethodButtons(List<MethodModel> methods) {
        methodsContainer.removeAllViews();
        
        for (MethodModel method : methods) {
            Button button = new Button(this);
            button.setText("Sign in with " + method.getDisplayName());
            button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            button.setOnClickListener(v -> handleMethodClick(method));
            methodsContainer.addView(button);
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleMethodClick(MethodModel method) {
        String methodName = method.getMethodName();
        String displayName = method.getDisplayName().toLowerCase();

        Intent intent;
        if (displayName.contains("otp")) {
            intent = new Intent(this, PhoneNumberActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else if (displayName.contains("message")) {
            intent = new Intent(this, MessageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            return;
        }
        
        intent.putExtra("method", methodName);
        try {
            startActivity(intent);
        } catch (Exception e) {
            messageText.setVisibility(View.VISIBLE);
            messageText.setText("Error launching activity: " + e.getMessage());
        }
    }

    private void showVerificationDialog() {
        VerificationDialog dialog = new VerificationDialog(
            this,
            "Interrupted Session Found",
            false,
            mainViewModel.getPhoneNumber().getValue(),
            null
        );
        dialog.show();
    }
}