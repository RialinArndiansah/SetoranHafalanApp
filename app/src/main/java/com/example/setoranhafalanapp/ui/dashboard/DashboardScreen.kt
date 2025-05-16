package com.example.setoranhafalan.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.setoranhafalanapp.R
import com.example.setoranhafalanapp.ui.login.LoginViewModel
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.geometry.Offset
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.util.Log
import kotlinx.coroutines.delay
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

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

    // Tab state untuk bottom navigation
    var selectedTab by remember { mutableStateOf(0) }
    
    // Navigation controller for internal navigation within the dashboard
    val dashboardNavController = rememberNavController()

    // Warna Teal untuk seluruh aplikasi
    val tealPrimary = Color(0xFF008B8B)    // Hijau kebiruan (teal)
    val tealDark = Color(0xFF006666)       // Teal gelap
    val tealLight = Color(0xFF00AEAE)      // Teal cerah
    val tealPastel = Color(0xFFE0F7FA)     // Teal sangat muda

    LaunchedEffect(Unit) {
        dashboardViewModel.fetchSetoranSaya()
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 12.dp,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(tealPrimary)
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (selectedTab) {
                                    0 -> "Dashboard Setoran"
                                    1 -> "Setoran Saya"
                                    2 -> "Profil"
                                    else -> "Dashboard Setoran"
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        actions = {
                            IconButton(
                                onClick = {
                                    loginViewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = tealPrimary
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = "Setoran",
                            tint = if (selectedTab == 0) tealPrimary else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Setoran",
                            color = if (selectedTab == 0) tealPrimary else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        // Reset to the main setoran saya screen when tab is selected
                        if (dashboardNavController.currentBackStackEntry?.destination?.route?.startsWith("detailSetoran") == true) {
                            dashboardNavController.popBackStack(route = "setoranSaya", inclusive = false)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.ViewList,
                            contentDescription = "Setoran Saya",
                            tint = if (selectedTab == 1) tealPrimary else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Setoran Saya",
                            color = if (selectedTab == 1) tealPrimary else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "Profil",
                            tint = if (selectedTab == 2) tealPrimary else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Profil",
                            color = if (selectedTab == 2) tealPrimary else Color.Gray
                        )
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> SetoranContent(
                    dashboardState = dashboardState,
                    snackbarHostState = snackbarHostState,
                    scope = scope
                )
                1 -> NavHost(
                    navController = dashboardNavController,
                    startDestination = "setoranSaya"
                ) {
                    composable("setoranSaya") {
                        SetoranSayaScreen(navController = dashboardNavController)
                    }
                    composable(
                        route = "detailSetoran/{kategori}",
                        arguments = listOf(
                            navArgument("kategori") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val kategori = backStackEntry.arguments?.getString("kategori") ?: "KP"
                        
                        // Add logging to help debug
                        Log.d("SetoranApp", "Opening detail for kategori: $kategori")
                        
                        SetoranDetailScreen(
                            kategori = kategori,
                            navController = dashboardNavController
                        )
                    }
                }
                2 -> ProfileContent(
                    dashboardState = dashboardState,
                    userName = userName
                )
            }
        }
    }
}

@Composable
fun SetoranContent(
    dashboardState: DashboardState,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    // Definisi warna teal
    val tealPrimary = Color(0xFF008B8B)    // Hijau kebiruan (teal)
    val tealLight = Color(0xFF00AEAE)      // Teal cerah
    val tealPastel = Color(0xFFE0F7FA)     // Teal sangat muda

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        when (val state = dashboardState) {
            is DashboardState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = tealPrimary)
                }
            }

            is DashboardState.Success -> {
                val data = state.data.data

                // Progress Circle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        drawCircle(
                            color = tealPastel,
                            radius = size.minDimension / 2 - 8.dp.toPx(),
                            style = Stroke(width = 16.dp.toPx())
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${data.setoran.info_dasar.persentase_progres_setor}%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = tealPrimary
                            )
                        )
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = tealPrimary
                            )
                        )
                        Text(
                            text = "Setoran",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = tealPrimary
                            )
                        )
                    }
                }

                // Daftar Setoran title
                Text(
                    text = "Daftar Setoran:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // List of Setoran
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(data.setoran.detail) { item ->
                        SetoranCard(
                            nama = item.nama,
                            label = item.label,
                            sudahSetor = item.sudah_setor,
                            tealColor = tealPrimary,
                            tealPastelColor = tealPastel
                        )
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

@Composable
fun SetoranCard(
    nama: String,
    label: String,
    sudahSetor: Boolean,
    tealColor: Color,
    tealPastelColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            // Background setengah lingkaran di sebelah kanan
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(140.dp)
                    .clip(RoundedCornerShape(topStart = 80.dp, bottomStart = 80.dp))
                    .background(tealPastelColor)
            )

            // Konten kartu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Nama surat
                    Text(
                        text = nama,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )

                    // Row untuk Label dan Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Label KP dengan background teal muda
                        Box(
                            modifier = Modifier
                                .background(
                                    color = tealPastelColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = tealColor
                                )
                            )
                        }

                        // Status dengan ikon lingkaran
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Ikon lingkaran (CheckCircle untuk sudah setor, RadioButtonUnchecked untuk belum setor)
                            Icon(
                                imageVector = if (sudahSetor) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = if (sudahSetor) "Sudah Setor" else "Belum Setor",
                                tint = if (sudahSetor) tealColor else Color.Red,
                                modifier = Modifier.size(16.dp)
                            )

                            // Teks status
                            Text(
                                text = if (sudahSetor) "Sudah Setor" else "Belum Setor",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (sudahSetor) tealColor else Color.Red,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Ikon More (titik tiga)
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    dashboardState: DashboardState,
    userName: String?
) {
    // Definisi warna teal
    val tealPrimary = Color(0xFF008B8B)    // Hijau kebiruan (teal)
    val tealDark = Color(0xFF006666)       // Teal gelap
    val tealLight = Color(0xFF00AEAE)      // Teal cerah
    val tealPastel = Color(0xFFE0F7FA)     // Teal sangat muda

    // State untuk foto profil
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    // Tambahkan scrollable state
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Content yang bisa di-scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header dengan shadow dan gradient
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                color = Color.Transparent,
                shadowElevation = 12.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    tealLight,
                                    tealPrimary,
                                    tealDark
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(0f, 500f)
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Foto profil
                        Box(
                            modifier = Modifier.padding(top = 8.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(BorderStroke(3.dp, Color.White), CircleShape)
                                    .clickable { launcher.launch("image/*") }
                            ) {
                                if (imageUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // Camera icon
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(6.dp)
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = "Change Photo",
                                    tint = tealPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Nama dan Jurusan
                        when (val state = dashboardState) {
                            is DashboardState.Success -> {
                                val data = state.data.data

                                Text(
                                    text = data.info.nama,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(top = 16.dp)
                                )

                                Text(
                                    text = "Teknik Informatika",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            is DashboardState.Loading -> {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Card untuk informasi profil
            when (val state = dashboardState) {
                is DashboardState.Success -> {
                    val data = state.data.data

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileInfoItem(
                                icon = Icons.Outlined.Person,
                                label = "Nama",
                                value = data.info.nama,
                                greenColor = tealPrimary
                            )

                            ProfileInfoItem(
                                icon = Icons.Outlined.Badge,
                                label = "NIM",
                                value = data.info.nim,
                                greenColor = tealPrimary
                            )

                            ProfileInfoItem(
                                icon = Icons.Outlined.Email,
                                label = "E-mail",
                                value = data.info.email,
                                greenColor = tealPrimary
                            )

                            ProfileInfoItem(
                                icon = Icons.Outlined.CalendarToday,
                                label = "Angkatan",
                                value = "Tahun ${data.info.angkatan}",
                                greenColor = tealPrimary
                            )

                            ProfileInfoItem(
                                icon = Icons.Outlined.MenuBook,
                                label = "Semester",
                                value = data.info.semester.toString(),
                                greenColor = tealPrimary
                            )

                            ProfileInfoItem(
                                icon = Icons.Outlined.SupervisorAccount,
                                label = "Dosen Pembimbing",
                                value = data.info.dosen_pa.nama,
                                greenColor = tealPrimary,
                                showDivider = false
                            )
                        }
                    }

                    // Tambahkan spacer di bawah untuk scroll area yang cukup
                    Spacer(modifier = Modifier.height(24.dp))
                }
                is DashboardState.Error -> {
                    Text(
                        text = "Terjadi kesalahan: ${state.message}",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    greenColor: Color,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(greenColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = greenColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                )
            }
        }

        if (showDivider) {
            Divider(
                color = Color.LightGray.copy(alpha = 0.5f),
                thickness = 0.5.dp,
                modifier = Modifier
                    .padding(start = 48.dp, top = 16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun SetoranSayaScreen(
    navController: NavController
) {
    // Get the ViewModel
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    
    // Definisi warna
    val tealPrimary = Color(0xFF2A9D8F) // Biru kehijauan sesuai permintaan

    // List of default category names - make sure they're normalized
    val categoryNames = listOf("KP", "SEMKP", "DAFTAR_TA", "SEMPRO", "SIDANG_TA")
        .map { it.trim().uppercase() }
    
    // Auto-scroll state
    val scrollState = rememberScrollState()
    val autoScrollEnabled = remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState { categoryNames.size }
    
    LaunchedEffect(Unit) {
        dashboardViewModel.fetchSetoranSaya()
    }
    
    // Auto-scrolling effect
    LaunchedEffect(autoScrollEnabled.value) {
        if (autoScrollEnabled.value) {
            while (true) {
                delay(3000) // Auto-scroll every 3 seconds
                if (currentPage < categoryNames.size - 1) {
                    currentPage++
                } else {
                    currentPage = 0
                }
                pagerState.animateScrollToPage(currentPage)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        Text(
            text = "Kategori Setoran",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = dashboardState) {
            is DashboardState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = tealPrimary)
                }
            }
            
            is DashboardState.Success -> {
                val data = state.data.data
                
                // Create a map to calculate progress for each category
                val categoryProgress = mutableMapOf<String, Int>()
                val categoryItemCounts = mutableMapOf<String, Int>()
                val categoryCompletedCounts = mutableMapOf<String, Int>()
                
                // Debug: Log the original labels
                data.setoran.detail.forEach { 
                    Log.d("SetoranApp", "Original label: '${it.label}'") 
                }
                
                // Normalize category names from the data - this creates a new list but doesn't modify the original data
                val normalizedCategories = data.setoran.detail.map { 
                    it.copy(label = it.label.trim().uppercase())
                }
                
                // Debug: Log the normalized labels
                normalizedCategories.forEach { 
                    Log.d("SetoranApp", "Normalized label: '${it.label}'") 
                }
                
                // Count items and completed items for each category
                normalizedCategories.forEach { setoranItem ->
                    val category = setoranItem.label
                    categoryItemCounts[category] = (categoryItemCounts[category] ?: 0) + 1
                    if (setoranItem.sudah_setor) {
                        categoryCompletedCounts[category] = (categoryCompletedCounts[category] ?: 0) + 1
                    }
                }
                
                // Calculate progress percentage for each category
                categoryItemCounts.forEach { (category, total) ->
                    val completed = categoryCompletedCounts[category] ?: 0
                    categoryProgress[category] = if (total > 0) (completed * 100) / total else 0
                }
                
                // Debug: Log the unique categories from data
                Log.d("SetoranApp", "Categories from data: ${categoryItemCounts.keys}")
                Log.d("SetoranApp", "Predefined categories: $categoryNames")
                
                // Get all unique categories (from both predefined list and data)
                val allCategories = mutableSetOf<String>()
                allCategories.addAll(categoryNames)
                allCategories.addAll(categoryItemCounts.keys)
                
                // Debug the final set
                Log.d("SetoranApp", "Final category set (${allCategories.size}): $allCategories")
                
                // Sort categories
                val sortedCategories = allCategories.toList().sorted()
                
                // Modern Card Pager View for horizontal scrolling with auto-animation
                Column(modifier = Modifier.fillMaxWidth()) {
                    // HorizontalPager for modern card view
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        modifier = Modifier
                            .height(320.dp)
                            .fillMaxWidth()
                    ) { page ->
                        if (page < sortedCategories.size) {
                            val categoryName = sortedCategories[page]
                            val progress = categoryProgress[categoryName] ?: 0
                            ModernKategoriCard(
                                kategori = KategoriSetoran(
                                    nama = categoryName,
                                    progress = progress
                                ),
                                total = categoryItemCounts[categoryName] ?: 0,
                                completed = categoryCompletedCounts[categoryName] ?: 0,
                                onCardClick = {
                                    autoScrollEnabled.value = false
                                    navController.navigate("detailSetoran/$categoryName")
                                }
                            )
                        }
                    }
                    
                    // Page indicator dots
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(sortedCategories.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (isSelected) 10.dp else 8.dp)
                                    .background(
                                        color = if (isSelected) tealPrimary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        currentPage = index
                                        autoScrollEnabled.value = false
                                    }
                            )
                        }
                    }
                }
                
                // Add setion title for recent progress
                Text(
                    text = "Kemajuan Terbaru",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
                
                // List of recently updated items
                val recentItems = normalizedCategories
                    .filter { it.sudah_setor }
                    .take(3) // Just take a few recent items
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentItems) { item ->
                        RecentProgressCard(
                            nama = item.nama,
                            kategori = item.label,
                            tealColor = tealPrimary
                        )
                    }
                }
            }
            
            is DashboardState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red
                    )
                }
            }
            
            else -> {}
        }
    }
}

@Composable
fun ModernKategoriCard(
    kategori: KategoriSetoran,
    total: Int,
    completed: Int,
    onCardClick: () -> Unit
) {
    val tealPrimary = Color(0xFF2A9D8F)
    val tealLight = Color(0xFFE0F7FA)
    val gradientColors = listOf(tealPrimary, Color(0xFF1A7F73)) // Darker gradient for depth

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(8.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.horizontalGradient(gradientColors)
                    )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title section
                Column {
                    Text(
                        text = kategori.nama.replace('_', ' '),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    
                    Text(
                        text = "$completed dari $total surat",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White
                        )
                    )
                }
                
                // Progress indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left column with progress text
                    Column {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        )
                        
                        Text(
                            text = "${kategori.progress}% Selesai",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        )
                    }
                    
                    // Circular progress indicator
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        // Background circle
                        Canvas(modifier = Modifier.size(80.dp)) {
                            drawCircle(
                                color = tealLight,
                                radius = size.minDimension / 2,
                                style = Stroke(width = 12.dp.toPx())
                            )
                        }
                        
                        // Progress circle
                        Canvas(modifier = Modifier.size(80.dp)) {
                            drawArc(
                                color = tealPrimary,
                                startAngle = -90f,
                                sweepAngle = 3.6f * kategori.progress,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx())
                            )
                        }
                        
                        // Progress text in center
                        Text(
                            text = "${kategori.progress}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = tealPrimary
                            )
                        )
                    }
                }
                
                // Spacer to push the button down
                Spacer(modifier = Modifier.weight(1f))
                
                // Action button
                Button(
                    onClick = onCardClick,
                    modifier = Modifier
                        .align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tealPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Lihat Detail")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.ArrowForward,
                        contentDescription = "Lihat Detail"
                    )
                }
            }
        }
    }
}

@Composable
fun RecentProgressCard(
    nama: String,
    kategori: String,
    tealColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Check icon in circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = tealColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = tealColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = nama,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Text(
                    text = kategori.replace('_', ' '),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Time/date info (mock data)
            Text(
                text = "Hari ini",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetoranDetailScreen(
    kategori: String,
    navController: NavController
) {
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    
    // Normalize the kategori parameter for comparison
    val normalizedKategori = kategori.trim().uppercase()
    
    val tealPrimary = Color(0xFF2A9D8F)
    val tealPastel = Color(0xFFE0F7FA)
    
    LaunchedEffect(Unit) {
        dashboardViewModel.fetchSetoranSaya()
    }
    
    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.Transparent
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            text = kategori,
                            color = Color.Black,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { /* Add search functionality */ }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Cari",
                                tint = Color.Black
                            )
                        }
                    }
                )
            }           
            
        }
    ) { padding ->
        when (val state = dashboardState) {
            is DashboardState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = tealPrimary)
                }
            }
            
            is DashboardState.Success -> {
                val data = state.data.data
                
                // Filter surah berdasarkan label/kategori yang sudah dinormalisasi
                val filteredSurah = data.setoran.detail.filter { 
                    it.label.trim().uppercase() == normalizedKategori
                }
                
                val completedCount = filteredSurah.count { it.sudah_setor }
                val totalCount = filteredSurah.size
                val progressPercentage = if (totalCount > 0) (completedCount * 100) / totalCount else 0
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Progress summary
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Text content
                            Column {
                                Text(
                                    text = "Progress Setoran $kategori",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                
                                Text(
                                    text = "$completedCount dari $totalCount surah",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            
                            // Circular progress indicator with larger size
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(80.dp)
                            ) {
                                // Background circle
                                Canvas(modifier = Modifier.size(80.dp)) {
                                    drawCircle(
                                        color = tealPastel,
                                        radius = size.minDimension / 2,
                                        style = Stroke(width = 10.dp.toPx())
                                    )
                                }
                                
                                // Progress circle
                                Canvas(modifier = Modifier.size(80.dp)) {
                                    drawArc(
                                        color = tealPrimary,
                                        startAngle = -90f,
                                        sweepAngle = 3.6f * progressPercentage,
                                        useCenter = false,
                                        style = Stroke(width = 10.dp.toPx())
                                    )
                                }
                                
                                // Progress text
                                Text(
                                    text = "$progressPercentage%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = tealPrimary
                                    )
                                )
                            }
                        }
                    }
                    
                    // Daftar Surah Heading
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daftar Surah",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "Urutkan",
                            style = MaterialTheme.typography.bodySmall,
                            color = tealPrimary,
                            modifier = Modifier.clickable { /* Add sorting logic */ }
                        )
                    }
                    
                    // List of Surah
                    if (filteredSurah.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada surah dengan kategori $kategori",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(filteredSurah) { item ->
                                SetoranCard(
                                    nama = item.nama,
                                    label = item.label,
                                    sudahSetor = item.sudah_setor,
                                    tealColor = tealPrimary,
                                    tealPastelColor = tealPastel
                                )
                            }
                        }
                    }
                }
            }
            
            is DashboardState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red
                    )
                }
            }
            
            else -> {}
        }
    }
}

// Data classes untuk SetoranSaya
data class KategoriSetoran(
    val nama: String,
    val progress: Int
)

data class SetoranItem(
    val nama: String,
    val sudahSetor: Boolean
)