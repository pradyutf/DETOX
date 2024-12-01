package com.example.screentimetracker

import AppUsageScreen
import HomeScreen
import PermissionScreen
import ScreenTimeLimit
import SplashScreen
import StakeScreen
import UsageStatsUtil.formatDuration
import WalletScreen
import android.app.AlertDialog

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.BassBoost
import android.provider.Settings
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.screentime.ui.theme.ScreenTimeTheme
import com.example.screentimetracker.viewmodels.WalletViewModel



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Configure window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Set system bars color
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Configure system bars appearance
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Remove window background to prevent flash
        window.setBackgroundDrawableResource(android.R.color.black)

        // Check if we have usage stats permission
        if (!hasUsageStatsPermission()) {
            // Show a dialog explaining the permission
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Please grant usage access permission to see app usage statistics.")
                .setPositiveButton("Grant") { _, _ ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                .setCancelable(false)
                .show()
        }

        // Debug log for installed apps
        val apps = packageManager.getInstalledPackages(0)
        Log.d("ScreenTime", "Total installed apps: ${apps.size}")

        setContent {

            val walletViewModel: WalletViewModel = viewModel()
            val walletState by walletViewModel.walletState.collectAsState()
            var currentStake by remember { mutableStateOf<String?>(null) }
            ScreenTimeTheme {
                var showSplash by remember { mutableStateOf(true) }
                val navController = rememberNavController()
                // Add state for permission
                var hasPermission by remember { mutableStateOf(hasUsageStatsPermission()) }

                // Add this state in MainActivity
                var screenTimeLimit by remember { mutableStateOf<ScreenTimeLimit?>(null) }

                LaunchedEffect(Unit) {
                    if (!hasPermission) {
                        requestUsageStatsPermission()
                    }
                }

                if (showSplash) {
                    SplashScreen {
                        showSplash = false
                    }
                } else {
                    NavHost(navController = navController, startDestination = if (hasPermission) "home" else "permission") {
                        composable("permission") {
                            PermissionScreen(
                                onRequestPermission = {
                                    requestUsageStatsPermission()
                                },
                                onPermissionGranted = {
                                    hasPermission = true
                                    navController.navigate("home") {
                                        popUpTo("permission") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            val stats = remember { UsageStatsUtil.getUsageStats(this@MainActivity) }
                            val screenTime = remember(stats) {
                                val totalTime = stats.sumOf { it.timeInForeground }
                                UsageStatsUtil.formatDuration(totalTime)
                            }

                            // Debug log for usage stats
                            Log.d("ScreenTime", "Total apps with usage data: ${stats.size}")

                            HomeScreen(
                                screenTime = screenTime,
                                currentStake = currentStake,
                                screenTimeLimit = screenTimeLimit,
                                onScreenTimeLimitSet = { limit ->
                                    screenTimeLimit = limit
                                },
                                onAppUsageClick = { navController.navigate("appUsage") },
                                onWalletClick = { navController.navigate("wallet") },
                                onStakeClick = { navController.navigate("stake") }
                            )
                        }
                        composable("appUsage") {
                            val usageStats = remember {
                                UsageStatsUtil.getUsageStats(this@MainActivity).also { stats ->
                                    // Debug logging
                                    Log.d("ScreenTime", "Found ${stats.size} apps")
                                    stats.forEach { appInfo ->
                                        Log.d("ScreenTime", """
                                            App: ${appInfo.appName}
                                            Package: ${appInfo.packageName}
                                            Usage: ${formatDuration(appInfo.timeInForeground)}
                                        """.trimIndent())
                                    }
                                }
                            }
                            AppUsageScreen(
                                usageStats = usageStats,
                                onBackClick = { navController.navigateUp() }
                            )
                        }

                        composable("wallet") {
                            WalletScreen(
                                walletState = walletState,
                                onBackClick = { navController.navigateUp() },
                                onConnectWallet = { walletViewModel.connectWallet() },
                                onDisconnectWallet = { walletViewModel.disconnectWallet() },
                                onChangeWallet = { walletViewModel.changeWallet() }
                            )
                        }

                        composable("stake") {
                            StakeScreen(
                                currentStake = currentStake,
                                onBackClick = { navController.navigateUp() },
                                onStakeConfirm = { amount ->
                                    currentStake = "$amount ETH"
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        
        // For Android 11+ (API 30+), check if we can query packages
        val canQueryPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA).size > 0
            } catch (e: Exception) {
                false
            }
        } else {
            true
        }
        
        return mode == AppOpsManager.MODE_ALLOWED && canQueryPackages
    }

    private fun requestUsageStatsPermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
} 