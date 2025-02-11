package co.verifyspeed.example.android.views.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import co.verifyspeed.example.android.views.R;

public class VerificationDialog extends Dialog {
    private final String title;
    private final boolean isLoading;
    private final String phoneNumber;
    private final Runnable onDismiss;

    public VerificationDialog(
            @NonNull Context context,
            String title,
            boolean isLoading,
            String phoneNumber,
            Runnable onDismiss
    ) {
        super(context);
        this.title = title;
        this.isLoading = isLoading;
        this.phoneNumber = phoneNumber;
        this.onDismiss = onDismiss;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_verification);

        // Prevent dialog from being dismissed when touching outside
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        // Initialize views
        TextView dialogTitle = findViewById(R.id.dialogTitle);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView phoneNumberText = findViewById(R.id.phoneNumberText);
        Button okButton = findViewById(R.id.okButton);

        // Set up the dialog
        dialogTitle.setText(title);
        
        // Handle visibility states
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            phoneNumberText.setVisibility(View.GONE);
        } else if (phoneNumber != null) {
            progressBar.setVisibility(View.GONE);
            phoneNumberText.setVisibility(View.VISIBLE);
            
            // Format and display the phone number
            String formattedNumber = phoneNumber.trim();
            if (!formattedNumber.startsWith("+")) {
                formattedNumber = "+" + formattedNumber;
            }
            phoneNumberText.setText(String.format("Your verified phone number is:\n%s", formattedNumber));
        }

        okButton.setOnClickListener(v -> {
            dismiss();
            if (onDismiss != null) {
                onDismiss.run();
            }
        });
    }
} 