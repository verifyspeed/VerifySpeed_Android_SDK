package co.verifyspeed.example.ui.message

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.verifyspeed.androidlibrary.VerifySpeed
import co.verifyspeed.example.ui.theme.ExampleTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//* TIP: Message Activity
//* This activity is launched from MainActivity when user selects a message-based verification method
//* When user leaves the app and returns via deep link, onResume is called
//* We need to notify VerifySpeed on resume to handle the deep link verification flow
//* Best practice: Create a separate activity for handling deep links that destroys itself
//* after successful verification to prevent unnecessary notifyOnResumed calls
//* when there is no active verification in progress

class MessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get the method from intent
        val method = intent.getStringExtra("method") ?: return finish()

        //* TIP: Set activity for VerifySpeed
        VerifySpeed.setActivity(this)

        setContent {
            ExampleTheme {
                MessagePage(
                        method = method,
                        onVerificationSuccess = {
                            // Finish the activity when user dismisses the dialog
                            finish()
                        },
                        onBackPressed = {
                            // Finish the activity when user click back button
                            finish()
                        }
                )
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            //* TIP Notify when activity is resumed for Deep Link Process
            VerifySpeed.notifyOnResumed()
        }
    }
}
