package dev.mahasiswa.kelompokone.ui.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.mahasiswa.kelompokone.R
import kotlinx.coroutines.launch
import dev.mahasiswa.kelompokone.ui.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavController,
    fragmentActivity: FragmentActivity
) {
    val TAG = "LoginScreen"
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(factory = LoginViewModel.getFactory(context))
    val loginState by viewModel.loginState.collectAsState()
    val biometricState by viewModel.biometricState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var saveCredentials by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Use the passed FragmentActivity directly
    val activity = fragmentActivity

    // Check for biometric login on first appearance
    LaunchedEffect(Unit) {
        Log.d(TAG, "LaunchedEffect: biometricState=$biometricState, activity=${activity != null}")
        if (biometricState is BiometricState.Available) {
            // Show biometric prompt automatically
            viewModel.authenticateWithBiometric(activity)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background header
            Image(
                painter = painterResource(id = R.drawable.header_bg),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_uin),
                    contentDescription = "Logo",
                    modifier = Modifier.size(290.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Added image below logo and above email
                Image(
                    painter = painterResource(id = R.drawable.setoran_text),
                    contentDescription = "Setoran Text",
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter your email") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color(0xFF4CAF50),
                        cursorColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                // Password input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color(0xFF4CAF50),
                        cursorColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                // Save credentials checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = saveCredentials,
                        onCheckedChange = { saveCredentials = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50)
                        )
                    )
                    Text("Simpan kredensial untuk login otomatis")
                }
                
                Button(
                    onClick = { viewModel.login(email, password, saveCredentials) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008B8B))
                ) {
                    Text("Login", color = Color.White)
                }
                
                // Fingerprint button if available
                if (biometricState is BiometricState.Available) {
                    Log.d(TAG, "Showing fingerprint button")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.authenticateWithBiometric(activity) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF008B8B)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Login",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Login dengan Sidik Jari")
                    }
                } else {
                    Log.d(TAG, "Fingerprint button not shown. BiometricState: $biometricState, Activity: ${activity != null}")
                }
                
                when (loginState) {
                    is LoginState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                    }
                    is LoginState.Success -> {
                        LaunchedEffect(Unit) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                    is LoginState.Error -> {
                        LaunchedEffect(loginState) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    (loginState as LoginState.Error).message
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
