package com.example.setoranhafalan.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.setoranhafalanapp.ui.login.LoginViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.getFactory(context))
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val userName by dashboardViewModel.userName.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        dashboardViewModel.fetchSetoranSaya()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard Setoran",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                actions = {
                    TextButton(onClick = {
                        loginViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }) {
                        Text("Logout", color = Color.White)
                    }
                }
            )
        },
        containerColor = Color.White,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                userName?.let {
                    Text(
                        text = "Selamat datang, $it!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                when (val state = dashboardState) {
                    is DashboardState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                        }
                    }

                    is DashboardState.Success -> {
                        val data = state.data.data
                        InfoText(label = "Nama", value = data.info.nama)
                        InfoText(label = "NIM", value = data.info.nim)
                        InfoText(label = "Email", value = data.info.email)
                        InfoText(label = "Angkatan", value = data.info.angkatan)
                        InfoText(label = "Semester", value = data.info.semester.toString())
                        InfoText(label = "Dosen Pembimbing", value = data.info.dosen_pa.nama)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Progres: ${data.setoran.info_dasar.persentase_progres_setor}%",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Daftar Setoran:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        LazyColumn {
                            items(data.setoran.detail) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF1F8E9)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = item.nama, fontWeight = FontWeight.Bold)
                                        Text(text = "Label: ${item.label}")
                                        Text(
                                            text = "Status: ${if (item.sudah_setor) "✅ Sudah Setor" else "❌ Belum Setor"}"
                                        )
                                        item.info_setoran?.let {
                                            Text(text = "Tanggal Setoran: ${it.tgl_setoran}")
                                            Text(text = "Dosen: ${it.dosen_yang_mengesahkan.nama}")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is DashboardState.Error -> {
                        LaunchedEffect(dashboardState) {
                            scope.launch {
                                snackbarHostState.showSnackbar(state.message)
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    )
}

@Composable
fun InfoText(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
