package co.verifyspeed.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.verifyspeed.android.MethodModel
import co.verifyspeed.android.VerifySpeed
import co.verifyspeed.example.ui.common.VerificationDialog
import co.verifyspeed.example.ui.main.MainViewModel
import co.verifyspeed.example.ui.message.MessageActivity
import co.verifyspeed.example.ui.otp.OtpPage
import co.verifyspeed.example.ui.phone.PhoneNumberPage
import co.verifyspeed.example.ui.theme.ExampleTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //* TIP: Set your activity
        VerifySpeed.setActivity(this)
        //* TIP: Set your client key
        VerifySpeed.setClientKey("YOUR_CLIENT_KEY")

        setContent { ExampleTheme { MainContent() } }
    }
}

sealed class Screen(val route: String, val title: String) {
    data object Methods : Screen("methods", "Methods")
    data object PhoneNumber : Screen("phoneNumber/{method}", "Phone Number")
    data object Otp : Screen("otp/{verificationKey}", "OTP Verification")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    val mainViewModel: MainViewModel = viewModel()
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentScreen =
            when (currentBackStackEntry.value?.destination?.route) {
                Screen.Methods.route -> Screen.Methods
                Screen.PhoneNumber.route -> Screen.PhoneNumber
                Screen.Otp.route -> Screen.Otp
                else -> Screen.Methods
            }

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (currentScreen != Screen.Methods) {
                    TopAppBar(
                            title = { Text(currentScreen.title) },
                            navigationIcon = {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                    )
                }
            }
    ) { paddingValues ->
        NavHost(
                navController = navController,
                startDestination = Screen.Methods.route,
                modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Methods.route) { MethodsList(navController, mainViewModel) }

            composable(Screen.PhoneNumber.route) { backStackEntry ->
                val method = backStackEntry.arguments?.getString("method") ?: return@composable

                PhoneNumberPage(
                        method = method,
                        mainViewModel = mainViewModel,
                        onNavigateToOtp = { verificationKey ->
                            navController.navigate(
                                    Screen.Otp.route.replace("{verificationKey}", verificationKey)
                            )
                        }
                )
            }

            composable(Screen.Otp.route) { backStackEntry ->
                val verificationKey =
                        backStackEntry.arguments?.getString("verificationKey") ?: return@composable

                OtpPage(
                        verificationKey = verificationKey,
                        navController = navController,
                        onVerificationSuccess = {}
                )
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun MethodsList(navController: NavHostController, mainViewModel: MainViewModel) {
    val methods = remember { mutableStateOf<List<MethodModel>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        //* TIP: Initialize to get available methods
        methods.value = VerifySpeed.initialize().get()
        isLoading.value = false
    }

    LaunchedEffect(Unit) {
        //* TIP: Check for interrupted session
        VerifySpeed.checkInterruptedSession { token ->
            if (token != null) {
                mainViewModel.showSuccessDialog()
                GlobalScope.launch { mainViewModel.getPhoneNumberFromToken(token) }
            }
        }
    }

    Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
    ) {
        VerificationDialog(
                showDialog = mainViewModel.showSuccessDialog,
                isLoading = mainViewModel.isLoading,
                phoneNumber = mainViewModel.phoneNumber,
                title = "Interrupted Session Found",
                onDismiss = { mainViewModel.hideSuccessDialog() }
        )

        if (isLoading.value) {
            CircularProgressIndicator()
        } else if (methods.value.isEmpty()) {
            Text(
                    text =
                            "Please check your client key set and correct to display available methods",
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center
            )
        } else {
            methods.value.forEach { method ->
                Button(
                        onClick = {
                            val isOtp = method.displayName?.lowercase()?.contains("otp") == true
                            val isMessage =
                                    method.displayName?.lowercase()?.contains("message") == true

                            when {
                                isOtp -> {
                                    navController.navigate(
                                            Screen.PhoneNumber.route.replace(
                                                    "{method}",
                                                    method.methodName!!
                                            )
                                    )
                                }
                                isMessage -> {
                                    //* TIP Launch MessageActivity instead of navigating
                                    val intent =
                                            Intent(context, MessageActivity::class.java).apply {
                                                putExtra("method", method.methodName)
                                            }
                                    context.startActivity(intent)
                                }
                            }
                        },
                        shape = RoundedCornerShape(5.dp),
                        modifier =
                                Modifier.padding(vertical = 5.dp)
                                        .height(60.dp)
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                        colors =
                                buttonColors(
                                        containerColor = Color.Black,
                                        contentColor = Color.White,
                                ),
                        interactionSource = MutableInteractionSource()
                ) { Text("Sign in with ${method.displayName}") }
            }
        }
    }
}
