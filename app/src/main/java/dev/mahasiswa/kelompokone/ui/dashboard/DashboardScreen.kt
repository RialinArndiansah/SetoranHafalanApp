package dev.mahasiswa.kelompokone.ui.dashboard

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
import dev.mahasiswa.kelompokone.R
import dev.mahasiswa.kelompokone.ui.login.LoginViewModel
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
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.material.icons.filled.PictureAsPdf
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider

import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import dev.mahasiswa.kelompokone.data.model.InfoSetoran
import dev.mahasiswa.kelompokone.ui.navigation.Screen

// Helper function to format date in Indonesian format
private fun formatDateIndonesian(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "-"
    
    try {
        // Parse the input date (assuming it's in format like "2023-05-17")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString
        
        // Format to Indonesian date format
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return outputFormat.format(date)
    } catch (e: Exception) {
        return dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Remember previous tab for animation direction
    var previousTab by remember { mutableIntStateOf(0) }
    
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
    
    // Untuk dialog konfirmasi logout
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Function to handle logout and reset profile photo
    val handleLogout = {
        // Clear profile photo from SharedPreferences and ViewModel
        val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            remove("profile_photo")
            apply()
        }
        
        // Reset profile photo in ViewModel
        dashboardViewModel.updateProfilePhoto(null)
        
        // Perform logout
        loginViewModel.logout()
        
        // Navigate to login screen
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Dashboard.route) { inclusive = true }
        }
    }

    // Dialog konfirmasi logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        handleLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Ya, Keluar", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = tealPrimary
                    ),
                    border = BorderStroke(1.dp, tealPrimary)
                ) {
                    Text("Batal", color = tealPrimary)
                }
            }
        )
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
                            // Get profile photo URI from ViewModel
                            val context = LocalContext.current
                            val profilePhotoUri by dashboardViewModel.profilePhotoUri.collectAsState()
                            var showMenu by remember { mutableStateOf(false) }
                            
                            // Load profile photo on initial composition if not already loaded
                            LaunchedEffect(Unit) {
                                if (profilePhotoUri == null) {
                                    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                    val savedPhotoPath = sharedPrefs.getString("profile_photo", null)
                                    if (savedPhotoPath != null) {
                                        val file = File(savedPhotoPath)
                                        if (file.exists()) {
                                            dashboardViewModel.updateProfilePhoto(Uri.fromFile(file))
                                        }
                                    }
                                }
                            }
                            
                            Box {
                                // Profile Image Button
                                IconButton(
                                    onClick = { showMenu = true }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .border(1.dp, Color.White, CircleShape)
                                    ) {
                                        if (profilePhotoUri != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(profilePhotoUri)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Profile Picture",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.AccountCircle,
                                                contentDescription = "Profile",
                                                tint = Color.White,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                                
                                // Dropdown Menu
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier
                                        .background(Color.White)
                                        .width(200.dp)
                                ) {
                                    // Profile option
                                    DropdownMenuItem(
                                        text = { Text("Profile") },
                                        onClick = { 
                                            showMenu = false
                                            selectedTab = 2 // Switch to profile tab
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Person,
                                                contentDescription = "Profile"
                                            )
                                        }
                                    )
                                    
                                    // Divider
                                    Divider()
                                    
                                    // Logout option
                                    DropdownMenuItem(
                                        text = { Text("Logout") },
                                        onClick = { 
                                            showMenu = false
                                            showLogoutDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.ExitToApp,
                                                contentDescription = "Logout"
                                            )
                                        }
                                    )
                                }
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
                    onClick = { 
                        previousTab = selectedTab
                        selectedTab = 0
                    },
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
                        previousTab = selectedTab
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
                    onClick = { 
                        previousTab = selectedTab
                        selectedTab = 2 
                    },
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
            // Animated content transition based on selected tab
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    // Determine direction based on tab indices
                    val direction = if (targetState > initialState) 
                        AnimatedContentTransitionScope.SlideDirection.Left 
                    else 
                        AnimatedContentTransitionScope.SlideDirection.Right
                    
                    // Create slide animation with fade
                    slideInHorizontally(
                        animationSpec = tween(400, easing = EaseOut),
                        initialOffsetX = { fullWidth ->
                            // Slide in from right or left based on direction
                            if (direction == AnimatedContentTransitionScope.SlideDirection.Left) fullWidth else -fullWidth
                        }
                    ) + fadeIn(tween(300)) togetherWith
                    slideOutHorizontally(
                        animationSpec = tween(400, easing = EaseIn),
                        targetOffsetX = { fullWidth ->
                            // Slide out to right or left based on direction
                            if (direction == AnimatedContentTransitionScope.SlideDirection.Left) -fullWidth else fullWidth
                        }
                    ) + fadeOut(tween(300)) using SizeTransform(clip = false)
                }
            ) { targetTab ->
                when (targetTab) {
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
    val tealDark = Color(0xFF006666)       // Teal gelap

    // Get ViewModel for refreshing data
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(context))
    
    // Pull to refresh state
    var refreshing by remember { mutableStateOf(false) }
    
    // Filter state - default to SUDAH
    var selectedFilter by remember { mutableStateOf("SUDAH") }
    
    // Handle refresh action
    val onRefresh = {
        refreshing = true
        dashboardViewModel.fetchSetoranSaya()
    }
    
    // Reset refreshing state when data is loaded
    LaunchedEffect(dashboardState) {
        if (dashboardState !is DashboardState.Loading) {
            refreshing = false
        }
    }
    
    // Create pull refresh state
    @OptIn(ExperimentalMaterialApi::class)
    val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)

    // Main content with pull to refresh
    @OptIn(ExperimentalMaterialApi::class)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
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
                
                // Using a scrollable column that can overlay elements
                val scrollState = rememberScrollState()
                val scrollOffset by remember { derivedStateOf { scrollState.value } }
                val maxQuoteCardHeight = 180.dp  // Approximate height of the quote card
                val maxProgressHeight = 150.dp   // Approximate height of the progress circle
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // Inspirational quote section - will be hidden when scrolling down
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .graphicsLayer {
                                // Animate the card out of view when scrolling
                                alpha = (1 - (scrollOffset / 300f)).coerceIn(0f, 1f)
                                translationY = -scrollOffset.toFloat() / 2
                            },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = tealPastel
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ini dia riwayat hafalanmu!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = tealPrimary
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Dan sungguh, telah Kami mudahkan Al-Qur'an untuk diingat… (QS. Al-Qamar: 17)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = tealDark
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                                text = "Jangan berhenti! Setiap ayat yang kamu hafal adalah tangga menuju kemuliaan. 📖",
                                style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = Color.Black.copy(alpha = 0.8f)
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                    // Progress Circle - smaller size and will be hidden when scrolling down
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .padding(top = maxQuoteCardHeight)
                            .graphicsLayer {
                                // Animate the progress circle out of view when scrolling
                                alpha = (1 - (scrollOffset / 300f)).coerceIn(0f, 1f)
                                translationY = -scrollOffset.toFloat() / 2
                            },
                    contentAlignment = Alignment.Center
                ) {
                        Canvas(modifier = Modifier.size(150.dp)) {
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
                                style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = tealPrimary
                            )
                        )
                        Text(
                            text = "Progress",
                                style = MaterialTheme.typography.bodySmall.copy(
                                color = tealPrimary
                            )
                        )
                        Text(
                            text = "Setoran",
                                style = MaterialTheme.typography.bodySmall.copy(
                                color = tealPrimary
                            )
                        )
                    }
                }

                    // Scrollable content that will overlay the quote and progress sections when scrolled
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(top = maxQuoteCardHeight.plus(maxProgressHeight))
                    ) {
                        // Daftar Setoran header with filter
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                // Daftar Setoran title
                Text(
                    text = "Daftar Setoran:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                            // Filter chips in a row with better spacing
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                // Sudah filter chip
                                FilterChip(
                                    selected = selectedFilter == "SUDAH",
                                    onClick = { selectedFilter = "SUDAH" },
                                    label = { 
                                        Text(
                                            "Sudah Setor",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) 
                                    },
                                    enabled = true,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .height(36.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tealPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = tealPrimary.copy(alpha = 0.5f),
                                        enabled = true,
                                        selected = selectedFilter == "SUDAH"
                                    ),
                                    leadingIcon = if (selectedFilter == "SUDAH") {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.White
                                            )
                                        }
                                    } else null
                                )
                                
                                // Belum filter chip
                                FilterChip(
                                    selected = selectedFilter == "BELUM",
                                    onClick = { selectedFilter = "BELUM" },
                                    label = { 
                                        Text(
                                            "Belum Setor",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) 
                                    },
                                    enabled = true,
                                    modifier = Modifier.height(36.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tealPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = tealPrimary.copy(alpha = 0.5f),
                                        enabled = true,
                                        selected = selectedFilter == "BELUM"
                                    ),
                                    leadingIcon = if (selectedFilter == "BELUM") {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.RadioButtonUnchecked,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.White
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        // Apply filter to list
                        val filteredList = when (selectedFilter) {
                            "SUDAH" -> data.setoran.detail.filter { it.sudah_setor }
                            "BELUM" -> data.setoran.detail.filter { !it.sudah_setor }
                            else -> data.setoran.detail
                        }

                        // List of Setoran with filter applied
                        Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                            if (filteredList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (selectedFilter == "SUDAH") 
                                                Icons.Rounded.CheckCircle 
                                            else 
                                                Icons.Rounded.RadioButtonUnchecked,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = if (selectedFilter == "SUDAH") 
                                                tealPrimary.copy(alpha = 0.6f) 
                                            else 
                                                Color.Gray.copy(alpha = 0.6f)
                                        )
                                        
                                        Text(
                                            text = if (selectedFilter == "SUDAH")
                                                "Belum ada setoran yang divalidasi"
                                            else
                                                "Belum ada setoran nih, yuk semangat!",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Gray,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        
                                        if (selectedFilter == "BELUM") {
                                            Text(
                                                text = "Ayo segera selesaikan hafalan Anda 📖",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = tealPrimary,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            } else {
                                filteredList.forEach { item ->
                        SetoranCard(
                            nama = item.nama,
                            label = item.label,
                            sudahSetor = item.sudah_setor,
                                        infoSetoran = item.info_setoran,
                            tealColor = tealPrimary,
                            tealPastelColor = tealPastel
                        )
                                }
                            }
                            
                            // Add some padding at the bottom for better scrolling experience
                            Spacer(modifier = Modifier.height(16.dp))
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
        
        // Pull to refresh indicator at the top
        @OptIn(ExperimentalMaterialApi::class)
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = tealPrimary
        )
    }
}

@Composable
fun SetoranCard(
    nama: String,
    label: String,
    sudahSetor: Boolean,
    infoSetoran: InfoSetoran? = null,
    tealColor: Color,
    tealPastelColor: Color
) {
    // State for showing validation details dialog
    var showDetails by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    
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
                    
                    // Show tanggal setor if sudah setor and info_setoran exists
                    if (sudahSetor && infoSetoran != null) {
                        Text(
                            text = "Tanggal: ${formatDateIndonesian(infoSetoran.tgl_setoran)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.DarkGray
                            )
                        )
            }
        }
                
                // 3-dot menu
                Box {
                    IconButton(
                        onClick = { showDropdownMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Menu",
                            tint = if (sudahSetor) tealColor else Color.Gray
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Lihat Detail Validasi") },
                            onClick = { 
                                showDropdownMenu = false
                                showDetails = true
                            },
                            enabled = sudahSetor && infoSetoran != null
                        )
                    }
                }
            }
        }
    }
    
    // Dialog to show validation details
    if (showDetails && infoSetoran != null) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = { Text("Detail Validasi") },
            text = {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Surat: $nama",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Text(
                        text = "Tanggal Setoran: ${formatDateIndonesian(infoSetoran.tgl_setoran)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Tanggal Validasi: ${formatDateIndonesian(infoSetoran.tgl_validasi)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Divalidasi oleh: ${infoSetoran.dosen_yang_mengesahkan.nama}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "NIP: ${infoSetoran.dosen_yang_mengesahkan.nip}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                    
                    Text(
                        text = "Email: ${infoSetoran.dosen_yang_mengesahkan.email}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetails = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
fun ProfileContent(
    dashboardState: DashboardState,
    userName: String?
) {
    // Get ViewModel
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.getFactory(LocalContext.current))
    
    // Definisi warna teal
    val tealPrimary = Color(0xFF008B8B)    // Hijau kebiruan (teal)
    val tealDark = Color(0xFF006666)       // Teal gelap
    val tealLight = Color(0xFF00AEAE)      // Teal cerah
    val tealPastel = Color(0xFFE0F7FA)     // Teal sangat muda

    // Get the context for file operations
    val context = LocalContext.current
    
    // State for photo URI from ViewModel
    val profilePhotoUri by dashboardViewModel.profilePhotoUri.collectAsState()
    
    // Update the export function with all the requested improvements
    val exportToPdf = { studentData: DashboardState.Success ->
        try {
            Toast.makeText(context, "Membuat PDF Kartu Muroja'ah...", Toast.LENGTH_SHORT).show()
            
            // Get the data
            val data = studentData.data.data
            
            // Create a PDF document - with LARGER page size to ensure everything fits
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 920, 1).create() // Further increased height
            val page = pdfDocument.startPage(pageInfo)
            
            val canvas = page.canvas
            
            // Load the UIN logo from drawable resources
            val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_uin)
            
            // Create a smaller version of the logo for the header
            val scaledLogo = Bitmap.createScaledBitmap(logo, 70, 70, true)
            
            // Very small text paint for table contents to fit all entries
            val smallPaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 8f // Very small font size
            }
            
            // Regular text paint with smaller font size for better fit
            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 9f
            }
            
            // Title paint
            val titlePaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            
            // Subtitle paint
            val subtitlePaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 12f
                textAlign = Paint.Align.CENTER
            }
            
            // Bold paint for headers
            val boldPaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            
            // Header text paint (white color for contrast against background)
            val headerTextPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            
            // Centered text paint for prasyarat
            val centeredPaint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 8f
                textAlign = Paint.Align.CENTER
            }
            
            // Table header background color (UIN teal/green color)
            val headerBgPaint = Paint().apply {
                color = android.graphics.Color.rgb(0, 139, 139) // Teal color
                style = Paint.Style.FILL
            }
            
            // Thin line paint for table grid
            val thinLinePaint = Paint().apply {
                color = android.graphics.Color.argb(120, 128, 128, 128) // Semi-transparent gray
                style = Paint.Style.STROKE
                strokeWidth = 0.5f // Thinner lines
            }
            
            // Draw the logo on the left side of the header
            canvas.drawBitmap(scaledLogo, 60f, 30f, paint)
            
            // Draw header/title text (now centered but adjusted for logo)
            val centerX = pageInfo.pageWidth / 2f + 30f // Shifted slightly to account for logo
            canvas.drawText("KARTU MUROJA'AH JUZ 30", centerX, 50f, titlePaint)
            canvas.drawText("Program Studi Teknik Informatika", centerX, 70f, subtitlePaint)
            canvas.drawText("Fakultas Sains dan Teknologi", centerX, 90f, subtitlePaint)
            canvas.drawText("Universitas Islam Negeri Sultan Syarif Kasim Riau", centerX, 110f, subtitlePaint)
            
            // Draw divider line below header
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawLine(50f, 125f, 545f, 125f, paint)
            paint.style = Paint.Style.FILL
            
            // Draw student information with improved layout - aligned colons
            val studentInfoY = 150f
            val labelX = 50f
            val colonX = 170f  // Fixed position for all colons to align
            val valueX = colonX + 10f  // Value starts after colon with some padding
            
            // Nama
            canvas.drawText("Nama", labelX, studentInfoY, boldPaint)
            canvas.drawText(":", colonX, studentInfoY, boldPaint)
            canvas.drawText(data.info.nama, valueX, studentInfoY, boldPaint)
            
            // NIM
            canvas.drawText("NIM", labelX, studentInfoY + 20, boldPaint)
            canvas.drawText(":", colonX, studentInfoY + 20, boldPaint)
            canvas.drawText(data.info.nim, valueX, studentInfoY + 20, boldPaint)
            
            // Penasehat Akademik
            canvas.drawText("Penasehat Akademik", labelX, studentInfoY + 40, boldPaint)
            canvas.drawText(":", colonX, studentInfoY + 40, boldPaint)
            canvas.drawText(data.info.dosen_pa.nama, valueX, studentInfoY + 40, boldPaint)
            
            // Draw divider line between student info and table
            canvas.drawLine(50f, studentInfoY + 60, 545f, studentInfoY + 60, thinLinePaint)
            
            // Make table slightly smaller to ensure signatures fit
            val tableStartY = 220f
            val rowHeight = 14f // Further reduced row height
            val tableWidth = 495f
            
            // Column widths - Increased width for Dosen yang Mengesahkan
            val col1Width = 20f   // No - Smaller
            val col2Width = 160f  // Surah name - slightly reduced
            val col3Width = 95f   // Tanggal Muroja'ah - slightly reduced
            val col4Width = 70f   // Prasyarat - slightly reduced
            val col5Width = 150f  // Dosen yang Mengesahkan - Increased width
            
            // Map of category codes to full names
            val categoryFullNames = mapOf(
                "KP" to "Kerja Praktik",
                "SEMKP" to "Seminar KP",
                "DAFTAR_TA" to "Daftar TA",
                "SEMPRO" to "Seminar Proposal",
                "SIDANG_TA" to "Sidang TA"
            )
            
            // Define specific surah ranges for each category (these are the actual row numbers, not surah numbers)
            val categoryRanges = mapOf(
                "KP" to 0..6,             // Surah 1-7
                "SEMKP" to 9..15,         // Surah 10-16
                "DAFTAR_TA" to 17..21,    // Surah 18-22
                "SEMPRO" to 23..33,       // Surah 24-34
                "SIDANG_TA" to 35..35     // Surah 36
            )
            
            // Define specific rows where horizontal lines should be drawn in the prasyarat column
            // These correspond to the requested surahs: Al Buruj (8), Ad Duha (16), Al Zalzalah (22), Al Lahab (34)
            val specialDividerRows = setOf(7, 15, 21, 33) // Zero-indexed (row 8 = index 7, etc.)
            
            // Calculate midpoints for centered text in each category range
            val categoryMidpoints = categoryRanges.mapValues { (_, range) ->
                val midRow = (range.first + range.last) / 2
                tableStartY + (midRow + 1) * rowHeight + (rowHeight / 2)
            }
            
            // Draw colored header background
            canvas.drawRect(50f, tableStartY, 50f + tableWidth, tableStartY + rowHeight, headerBgPaint)
            
            // Draw table outline with thinner lines
            canvas.drawRect(50f, tableStartY, 50f + tableWidth, tableStartY + rowHeight, thinLinePaint)
            
            // Draw column dividers for header with thinner lines
            canvas.drawLine(50f + col1Width, tableStartY, 50f + col1Width, tableStartY + rowHeight, thinLinePaint)
            canvas.drawLine(50f + col1Width + col2Width, tableStartY, 50f + col1Width + col2Width, tableStartY + rowHeight, thinLinePaint)
            canvas.drawLine(50f + col1Width + col2Width + col3Width, tableStartY, 50f + col1Width + col2Width + col3Width, tableStartY + rowHeight, thinLinePaint)
            canvas.drawLine(50f + col1Width + col2Width + col3Width + col4Width, tableStartY, 50f + col1Width + col2Width + col3Width + col4Width, tableStartY + rowHeight, thinLinePaint)
            
            // Draw table header text with white text for contrast against colored background
            canvas.drawText("No", 55f, tableStartY + (rowHeight * 0.7f), headerTextPaint)
            canvas.drawText("Surah", 55f + col1Width + 5f, tableStartY + (rowHeight * 0.7f), headerTextPaint)
            canvas.drawText("Tanggal Muroja'ah", 55f + col1Width + col2Width + 5f, tableStartY + (rowHeight * 0.7f), headerTextPaint)
            canvas.drawText("Prasyarat", 55f + col1Width + col2Width + col3Width + 5f, tableStartY + (rowHeight * 0.7f), headerTextPaint)
            canvas.drawText("Dosen yang Mengesahkan", 55f + col1Width + col2Width + col3Width + col4Width + 5f, tableStartY + (rowHeight * 0.7f), headerTextPaint)
            
            // Function to wrap text if it exceeds maxWidth - compact version for small rows
            fun drawWrappedText(text: String, x: Float, y: Float, maxWidth: Float, linePaint: Paint) {
                if (linePaint.measureText(text) <= maxWidth) {
                    canvas.drawText(text, x, y, linePaint)
                    return
                }
                
                // Calculate how many characters can fit in maxWidth
                val chars = text.toCharArray()
                var index = 0
                while (index < chars.size) {
                    var end = index + 1
                    while (end <= chars.size && linePaint.measureText(text.substring(index, end)) <= maxWidth) {
                        end++
                    }
                    // Back up one character since we exceeded the width
                    end--
                    
                    // Draw the substring that fits
                    canvas.drawText(text.substring(index, end), x, y, linePaint)
                    
                    // No second line in this compact version - just use ellipsis
                    if (end < chars.size) {
                        val truncated = text.substring(index, end-3) + "..."
                        canvas.drawText(truncated, x, y, linePaint)
                    }
                    break
                }
            }
            
            // Extract setoran items
            val setoranItems = data.setoran.detail
            
            // Draw vertical dividers for the entire prasyarat column with thinner lines
            canvas.drawLine(50f + col1Width + col2Width + col3Width, tableStartY + rowHeight, 
                             50f + col1Width + col2Width + col3Width, tableStartY + 38 * rowHeight, thinLinePaint)
            canvas.drawLine(50f + col1Width + col2Width + col3Width + col4Width, tableStartY + rowHeight, 
                             50f + col1Width + col2Width + col3Width + col4Width, tableStartY + 38 * rowHeight, thinLinePaint)
            
            // Draw centered category names in prasyarat column at the midpoint of each range
            for ((category, midY) in categoryMidpoints) {
                val fullCategoryName = categoryFullNames[category] ?: category
                // Use centered paint to draw in the middle of the prasyarat column
                val centerX = 50f + col1Width + col2Width + col3Width + (col4Width / 2)
                canvas.drawText(fullCategoryName, centerX, midY, centeredPaint)
            }
            
            // Draw all 37 rows with compact layout - but skip horizontal lines in prasyarat column (except for special rows)
            for (i in 0 until 37) {
                val rowY = tableStartY + (i + 1) * rowHeight
                
                // Draw row outline - but skip specific areas in prasyarat column
                
                // Draw main horizontal line for this row with thinner lines
                canvas.drawLine(50f, rowY, 50f + col1Width + col2Width + col3Width, rowY, thinLinePaint) // Left side
                canvas.drawLine(50f + col1Width + col2Width + col3Width + col4Width, rowY, 
                                 50f + tableWidth, rowY, thinLinePaint) // Right side
                
                // Draw special horizontal dividers in the prasyarat column for the specified rows with thinner lines
                if (i in specialDividerRows) {
                    // Draw a horizontal line specifically in the prasyarat column
                    canvas.drawLine(
                        50f + col1Width + col2Width + col3Width, rowY,
                        50f + col1Width + col2Width + col3Width + col4Width, rowY,
                        thinLinePaint
                    )
                }
                
                // Vertical dividers - only for the non-prasyarat columns with thinner lines
                canvas.drawLine(50f, rowY, 50f, rowY + rowHeight, thinLinePaint) // Left edge
                canvas.drawLine(50f + col1Width, rowY, 50f + col1Width, rowY + rowHeight, thinLinePaint)
                canvas.drawLine(50f + col1Width + col2Width, rowY, 50f + col1Width + col2Width, rowY + rowHeight, thinLinePaint)
                canvas.drawLine(50f + tableWidth, rowY, 50f + tableWidth, rowY + rowHeight, thinLinePaint) // Right edge
                
                // Draw row content with smaller text
                canvas.drawText("${i + 1}", 53f, rowY + (rowHeight * 0.7f), smallPaint)
                
                if (i < setoranItems.size) {
                    val item = setoranItems[i]
                    
                    // Draw surah name with truncation
                    drawWrappedText(
                        item.nama, 
                        53f + col1Width + 2f, 
                        rowY + (rowHeight * 0.7f), 
                        col2Width - 5f,
                        smallPaint
                    )
                    
                    if (item.sudah_setor && item.info_setoran != null) {
                        // Format date in Indonesian
                        val formattedDate = formatDateIndonesian(item.info_setoran.tgl_setoran)
                        
                        // Draw date with truncation
                        drawWrappedText(
                            formattedDate,
                            53f + col1Width + col2Width + 2f,
                            rowY + (rowHeight * 0.7f),
                            col3Width - 5f,
                            smallPaint
                        )
                        
                        // Draw dosen name with truncation - now with more width
                        val dosenName = item.info_setoran.dosen_yang_mengesahkan.nama
                        drawWrappedText(
                            dosenName,
                            53f + col1Width + col2Width + col3Width + col4Width + 2f,
                            rowY + (rowHeight * 0.7f),
                            col5Width - 5f,
                            smallPaint
                        )
                    } else {
                        canvas.drawText("-", 53f + col1Width + col2Width + 2f, rowY + (rowHeight * 0.7f), smallPaint)
                        canvas.drawText("-", 53f + col1Width + col2Width + col3Width + col4Width + 2f, rowY + (rowHeight * 0.7f), smallPaint)
                    }
                    
                } else {
                    // Empty row
                    canvas.drawText("-", 53f + col1Width + 2f, rowY + (rowHeight * 0.7f), smallPaint)
                    canvas.drawText("-", 53f + col1Width + col2Width + 2f, rowY + (rowHeight * 0.7f), smallPaint)
                    canvas.drawText("-", 53f + col1Width + col2Width + col3Width + col4Width + 2f, rowY + (rowHeight * 0.7f), smallPaint)
                }
            }
            
            // Draw bottom line of the table with thinner lines
            canvas.drawLine(50f, tableStartY + 37 * rowHeight + rowHeight, 50f + tableWidth, tableStartY + 37 * rowHeight + rowHeight, thinLinePaint)
            
            // Signature section - positioned with more space to ensure visibility
            val signatureY = tableStartY + 37 * rowHeight + rowHeight + 60f
            
            // Format current date - full format for the signature section
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val currentDate = dateFormat.format(Date())
            
            // Draw improved signature section
            canvas.drawText("Pekanbaru, $currentDate", 350f, signatureY, paint)
            
            // Draw signature boxes with titles
            // Left signature (Academic advisor)
            canvas.drawText("Mengetahui,", 80f, signatureY, paint)
            canvas.drawText("Dosen Pembimbing Akademik", 80f, signatureY + 15, paint)
            
            // Draw signature box with thinner lines
            canvas.drawRect(80f, signatureY + 25, 220f, signatureY + 75, thinLinePaint)
            
            // Add name under signature box
            val dosenPaName = data.info.dosen_pa.nama
            val dosenPaNip = data.info.dosen_pa.nip ?: "-"
            canvas.drawText(dosenPaName, 80f, signatureY + 90, paint)
            canvas.drawText("NIP. $dosenPaNip", 80f, signatureY + 105, smallPaint)
            
            // Right signature (Student)
            canvas.drawText("Mahasiswa", 350f, signatureY + 15, paint)
            
            // Draw signature box with thinner lines
            canvas.drawRect(350f, signatureY + 25, 490f, signatureY + 75, thinLinePaint)
            
            // Add name under signature box
            canvas.drawText(data.info.nama, 350f, signatureY + 90, paint)
            canvas.drawText("NIM. " + data.info.nim, 350f, signatureY + 105, smallPaint)
            
            // Finish the page
            pdfDocument.finishPage(page)
            
            // Create a unique filename based on student info and timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val studentInitials = data.info.nama.split(" ")
                .mapNotNull { if (it.isNotEmpty()) it[0].toString() else null }
                .take(3)
                .joinToString("")
                .uppercase()
            
            val fileName = "KartuMurojaah_${studentInitials}_${data.info.nim}_$timestamp.pdf"
            
            // Save to Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val filePath = File(downloadsDir, fileName)
            
            try {
                // Write the document to the file
                pdfDocument.writeTo(FileOutputStream(filePath))
                pdfDocument.close()
                
                Toast.makeText(context, "PDF berhasil disimpan di Downloads: $fileName", Toast.LENGTH_LONG).show()
                
                // Try to open the PDF
                try {
                    val uri = FileProvider.getUriForFile(
                        context, 
                        "${context.packageName}.provider",
                        filePath
                    )
                    
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "PDF tersimpan, tetapi tidak ada aplikasi untuk membuka PDF", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    // Just show a success message if opening fails
                    Toast.makeText(context, "PDF berhasil disimpan di: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Fallback to app's private storage if Downloads access fails
                val directory = context.getExternalFilesDir(null)?.let { File(it, "PDFs") }
                if (directory?.exists() == false) {
                    directory.mkdirs()
                }
                
                val backupFilePath = File(directory, fileName)
                pdfDocument.writeTo(backupFilePath.outputStream())
                pdfDocument.close()
                
                Toast.makeText(
                    context, 
                    "Tidak dapat menyimpan ke Downloads. PDF disimpan di: ${backupFilePath.absolutePath}", 
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Function to save image to local storage
    fun saveImageToLocal(uri: Uri): Uri {
        // Create file in app's private storage directory
        val timeStamp = System.currentTimeMillis()
        val storageDir = context.filesDir
        val fileName = "profile_photo_$timeStamp.jpg"
        val imageFile = File(storageDir, fileName)
        
        try {
            // Copy the selected image to our app's private storage
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(imageFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            // Save the file path to SharedPreferences
            val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putString("profile_photo", imageFile.absolutePath)
                apply()
            }
            
            // Return the URI for the saved file
            return Uri.fromFile(imageFile)
        } catch (e: Exception) {
            Log.e("ProfileContent", "Failed to save image: ${e.message}", e)
            // Return original URI if saving failed
            return uri
        }
    }
    
    // Load saved profile photo on initial composition
    LaunchedEffect(Unit) {
        if (profilePhotoUri == null) {
            val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val savedPhotoPath = sharedPrefs.getString("profile_photo", null)
            if (savedPhotoPath != null) {
                val file = File(savedPhotoPath)
                if (file.exists()) {
                    dashboardViewModel.updateProfilePhoto(Uri.fromFile(file))
                }
            }
        }
    }
    
    // Image picker result handler
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Save selected image to local storage
            val savedUri = saveImageToLocal(it)
            // Update the ViewModel with the new URI
            dashboardViewModel.updateProfilePhoto(savedUri)
        }
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
                                if (profilePhotoUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(profilePhotoUri)
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

                    // Updated Export to PDF button with actual data
                    Button(
                        onClick = { 
                            if (state is DashboardState.Success) {
                                exportToPdf(state)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tealPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "Export to PDF",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Kartu Muroja'ah",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
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
    
    // Define coroutine scope for animations
    val scope = rememberCoroutineScope()
    
    // Pull to refresh state
    var refreshing by remember { mutableStateOf(false) }
    
    // Handle refresh action
    val onRefresh = {
        refreshing = true
        dashboardViewModel.fetchSetoranSaya()
    }
    
    // Reset refreshing state when data is loaded
    LaunchedEffect(dashboardState) {
        if (dashboardState !is DashboardState.Loading) {
            refreshing = false
        }
    }
    
    // Create pull refresh state
    @OptIn(ExperimentalMaterialApi::class)
    val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)
    
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
    
    // Animation specs
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    LaunchedEffect(Unit) {
        dashboardViewModel.fetchSetoranSaya()
    }
    
    // Auto-scrolling effect with smoother animation
    LaunchedEffect(autoScrollEnabled.value) {
        if (autoScrollEnabled.value) {
            while (true) {
                delay(4000) // A slightly longer delay for better UX
                if (currentPage < categoryNames.size - 1) {
                    currentPage++
                } else {
                    currentPage = 0
                }
                // Use smooth scrolling with custom animation
                pagerState.animateScrollToPage(
                    page = currentPage,
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    // Main content with pull to refresh
    @OptIn(ExperimentalMaterialApi::class)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
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
                
                // Modern Card Pager View for horizontal scrolling with enhanced animations
                Column(modifier = Modifier.fillMaxWidth()) {
                    // HorizontalPager with enhanced card animations
                    Box(
                        modifier = Modifier
                            .height(320.dp)
                            .fillMaxWidth()
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 48.dp), // Increased padding for better visibility of adjacent cards
                            pageSpacing = 16.dp, // Add space between pages
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            if (page < sortedCategories.size) {
                                val categoryName = sortedCategories[page]
                                val progress = categoryProgress[categoryName] ?: 0
                                
                                // Calculate card scale based on proximity to current page
                                val pageOffset = ((pagerState.currentPage - page) + pagerState
                                    .currentPageOffsetFraction).absoluteValue
                                
                                // Apply scaling effect - current page is full size, others are scaled down
                                val scaleFactor = 0.85f + (1f - 0.85f) * (1f - pageOffset.coerceIn(0f, 1f))
                                
                                // Apply rotation effect for a more dynamic look
                                val rotation = (pageOffset * 5f).coerceIn(-5f, 5f)
                                
                                // Apply alpha for fading effect
                                val alpha = lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                
                                // Enhanced card with animations
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            this.scaleX = scaleFactor
                                            this.scaleY = scaleFactor
                                            this.rotationZ = -rotation // Subtle rotation
                                            this.alpha = alpha
                                            
                                            // Add subtle 3D effect
                                            this.cameraDistance = 8f * density
                                        }
                                        .fillMaxWidth()
                                ) {
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
                        }
                    }
                    
                    // Animated page indicator dots with springy animations
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(sortedCategories.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            
                            // Animated size change for the selected dot
                            val size by animateFloatAsState(
                                targetValue = if (isSelected) 10f else 8f,
                                animationSpec = animationSpec,
                                label = "Dot size animation"
                            )
                            
                            // Animated color change for the selected dot
                            val color by animateColorAsState(
                                targetValue = if (isSelected) tealPrimary else Color.LightGray,
                                animationSpec = tween(300),
                                label = "Dot color animation"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(size.dp)
                                    .background(
                                        color = color,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        currentPage = index
                                        autoScrollEnabled.value = false
                                        // Animate to selected page with spring effect
                                        scope.launch {
                                            pagerState.animateScrollToPage(
                                                page = index,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                        }
                                    }
                            )
                        }
                    }
                }
                
                // Add section title for recent progress
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
        
        // Pull to refresh indicator at the top
        @OptIn(ExperimentalMaterialApi::class)
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = tealPrimary
        )
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
    
    // Filter state - default to SUDAH
    var selectedFilter by remember { mutableStateOf("SUDAH") }
    
    // Normalize the kategori parameter for comparison
    val normalizedKategori = kategori.trim().uppercase()
    
    // Define app colors
    val tealPrimary = Color(0xFF2A9D8F)
    val tealPastel = Color(0xFFE0F7FA)
    val tealDark = Color(0xFF006666)
    
    // Pull to refresh state
    var refreshing by remember { mutableStateOf(false) }
    
    // Handle refresh action
    val onRefresh = {
        refreshing = true
        dashboardViewModel.fetchSetoranSaya()
    }
    
    // Reset refreshing state when data is loaded
    LaunchedEffect(dashboardState) {
        if (dashboardState !is DashboardState.Loading) {
            refreshing = false
        }
    }
    
    // Create pull refresh state
    @OptIn(ExperimentalMaterialApi::class)
    val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)
    
    // Scroll state for main content
    val scrollState = rememberScrollState()
    
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
                            text = when (normalizedKategori) {
                                "KP" -> "Setoran Kerja Praktik"
                                "SEMKP" -> "Setoran Seminar KP"
                                "DAFTAR_TA" -> "Setoran Daftar TA"
                                "SEMPRO" -> "Setoran Seminar Proposal"
                                "SIDANG_TA" -> "Setoran Sidang TA"
                                else -> "Setoran $kategori"
                            },
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium.copy(
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
                    )
                )
            }
        }
    ) { padding ->
        @OptIn(ExperimentalMaterialApi::class)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
                .background(Color(0xFFF8F8F8))
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
                    
                    // Filter surah berdasarkan label/kategori yang sudah dinormalisasi
                    val filteredSurah = data.setoran.detail.filter { 
                        it.label.trim().uppercase() == normalizedKategori
                    }
                    
                    val completedCount = filteredSurah.count { it.sudah_setor }
                    val totalCount = filteredSurah.size
                    val progressPercentage = if (totalCount > 0) (completedCount * 100) / totalCount else 0

                    // Apply filter to surah list
                    val displaySurah = when (selectedFilter) {
                        "SUDAH" -> filteredSurah.filter { it.sudah_setor }
                        "BELUM" -> filteredSurah.filter { !it.sudah_setor }
                        else -> filteredSurah
                    }
                    
                    // Use a scrollable column for the main content
                    // This approach allows the content to scroll up completely
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        // Progress summary card
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
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Text content with improved spacing
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Progress Setoran",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = tealPrimary
                                        )
                                    )
                                    
                                    Text(
                                        text = "$completedCount dari $totalCount surah",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    
                                    // Additional text for more context
                                    if (completedCount == totalCount && totalCount > 0) {
                                        Text(
                                            text = "Setoran sudah lengkap!",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = tealPrimary
                                        )
                                    } else if (totalCount > 0) {
                                        Text(
                                            text = "Tersisa ${totalCount - completedCount} surah",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                // Circular progress indicator
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .padding(4.dp)
                                ) {
                                    // Background circle
                                    Canvas(modifier = Modifier.size(80.dp)) {
                                        drawCircle(
                                            color = tealPastel,
                                            radius = size.minDimension / 2,
                                            style = Stroke(width = 8.dp.toPx())
                                        )
                                    }
                                    
                                    // Progress circle
                                    Canvas(modifier = Modifier.size(80.dp)) {
                                        drawArc(
                                            color = tealPrimary,
                                            startAngle = -90f,
                                            sweepAngle = 3.6f * progressPercentage,
                                            useCenter = false,
                                            style = Stroke(width = 8.dp.toPx())
                                        )
                                    }
                                    
                                    // Progress text with percentage
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$progressPercentage%",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = tealPrimary
                                            )
                                        )
                                        Text(
                                            text = "Selesai",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = tealDark
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Filter section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 1.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Daftar Surah Heading
                                Text(
                                    text = "Daftar Surah",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                // Filter chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Filter:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    
                                    // Sudah filter chip
                                    FilterChip(
                                        selected = selectedFilter == "SUDAH",
                                        onClick = { selectedFilter = "SUDAH" },
                                        label = { 
                                            Text(
                                                "Sudah Setor",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) 
                                        },
                                        enabled = true,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .height(36.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = tealPrimary,
                                            selectedLabelColor = Color.White
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = tealPrimary.copy(alpha = 0.5f),
                                            enabled = true,
                                            selected = selectedFilter == "SUDAH"
                                        ),
                                        leadingIcon = if (selectedFilter == "SUDAH") {
                                            {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color.White
                                                )
                                            }
                                        } else null
                                    )
                                    
                                    // Belum filter chip
                                    FilterChip(
                                        selected = selectedFilter == "BELUM",
                                        onClick = { selectedFilter = "BELUM" },
                                        label = { 
                                            Text(
                                                "Belum Setor",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) 
                                        },
                                        enabled = true,
                                        modifier = Modifier.height(36.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = tealPrimary,
                                            selectedLabelColor = Color.White
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = tealPrimary.copy(alpha = 0.5f),
                                            enabled = true,
                                            selected = selectedFilter == "BELUM"
                                        ),
                                        leadingIcon = if (selectedFilter == "BELUM") {
                                            {
                                                Icon(
                                                    imageVector = Icons.Rounded.RadioButtonUnchecked,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color.White
                                                )
                                            }
                                        } else null
                                    )
                                }
                            }
                        }
                        
                        // Display surah list or empty state
                        if (displaySurah.isEmpty()) {
                            // Empty state
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                ) {
                                    // Empty state icon
                                    Icon(
                                        imageVector = if (selectedFilter == "SUDAH") 
                                            Icons.Rounded.CheckCircle 
                                        else 
                                            Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = if (selectedFilter == "SUDAH") 
                                            tealPrimary.copy(alpha = 0.6f) 
                                        else 
                                            Color.Gray.copy(alpha = 0.6f)
                                    )
                                    
                                    // Empty state text
                                    Text(
                                        text = if (selectedFilter == "SUDAH")
                                            "Belum ada setoran yang divalidasi pada kategori ini"
                                        else
                                            "Belum ada setoran pada kategori ini",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    
                                    if (selectedFilter == "BELUM") {
                                        Text(
                                            text = "Ayo segera selesaikan hafalan Anda untuk memenuhi prasyarat 📖",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = tealPrimary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            // Display list of surah items - not using LazyColumn because we're in a ScrollColumn
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                displaySurah.forEach { item ->
                                    SetoranCard(
                                        nama = item.nama,
                                        label = item.label,
                                        sudahSetor = item.sudah_setor,
                                        infoSetoran = item.info_setoran,
                                        tealColor = tealPrimary,
                                        tealPastelColor = tealPastel
                                    )
                                }
                                
                                // Add some bottom spacing
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                
                is DashboardState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Error,
                                contentDescription = null,
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Text(
                                text = "Error: ${state.message}",
                                color = Color.Red,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { dashboardViewModel.fetchSetoranSaya() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tealPrimary
                                )
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                
                else -> {}
            }
            
            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = tealPrimary
            )
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