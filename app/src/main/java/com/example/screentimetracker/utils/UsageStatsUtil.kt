import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.content.Intent
import android.os.Build

import java.util.Calendar
import java.util.concurrent.TimeUnit

object UsageStatsUtil {
    fun getUsageStats(context: Context): List<AppUsageInfo> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        // Get today's start and end time
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        // Get usage stats for today only
        val usageStats = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            ).associateBy { it.packageName }
            .also { stats ->
                // Debug log for usage stats
                Log.d("UsageStats", "Time range: ${formatDuration(startTime)} to ${formatDuration(endTime)}")
                stats.forEach { (pkg, usage) ->
                    Log.d("UsageStats", """
                        Raw usage data:
                        Package: $pkg
                        Total Time: ${formatDuration(usage.totalTimeInForeground)}
                        Last Time Used: ${formatTime(usage.lastTimeUsed)}
                        First Time Used: ${formatTime(usage.firstTimeStamp)}
                    """.trimIndent())
                }
            }
        } catch (e: SecurityException) {
            Log.e("UsageStats", "Permission denied", e)
            emptyMap()
        }

        // Get all installed apps
        val packageManager = context.packageManager
        return try {
            // Get all packages using different methods based on Android version
            val allApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android 11+
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .map { it.packageName }
            } else {
                // For older Android versions
                val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                packageManager.queryIntentActivities(intent, 0)
                    .map { it.activityInfo.packageName }
            }.toSet()

            // Debug log
            Log.d("UsageStats", "Found ${allApps.size} apps")
            
            // Create AppUsageInfo for all apps
            allApps.map { packageName ->
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    
                    Log.d("UsageStats", """
                        Processing app:
                        Package: $packageName
                        Name: ${packageManager.getApplicationLabel(appInfo)}
                        System App: $isSystemApp
                        Has Usage Data: ${usageStats.containsKey(packageName)}
                        Usage Time: ${usageStats[packageName]?.totalTimeInForeground ?: 0L}
                    """.trimIndent())

                    AppUsageInfo(
                        packageName = packageName,
                        appName = packageManager.getApplicationLabel(appInfo).toString(),
                        timeInForeground = usageStats[packageName]?.totalTimeInForeground ?: 0L,
                        isSystemApp = isSystemApp
                    )
                } catch (e: Exception) {
                    Log.e("UsageStats", "Error processing package $packageName", e)
                    null
                }
            }
            .filterNotNull()
            .sortedWith(
                compareByDescending<AppUsageInfo> { it.timeInForeground }
                    .thenBy { it.appName.lowercase() }
            )

        } catch (e: Exception) {
            Log.e("UsageStats", "Error getting app list", e)
            emptyList()
        }
    }

    // Update common apps list with more package names
    private val commonApps = setOf(
        "com.whatsapp",
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.google.android.youtube",
        "com.twitter.android",
        "com.snapchat.android",
        "com.linkedin.android",
        "com.pinterest",
        "com.spotify.music",
        "com.netflix.mediaclient",
        "com.amazon.mShop.android.shopping",
        "com.google.android.gm",
        "com.google.android.apps.photos",
        "com.google.android.apps.maps",
        "com.google.android.apps.messaging",
        "com.google.android.apps.docs",
        "com.google.android.apps.youtube.music",
        "com.zhiliaoapp.musically",
        "com.discord",
        "org.telegram.messenger",
        "com.reddit.frontpage",
        "com.instagram.lite",
        "com.facebook.lite",
        "com.whatsapp.w4b",
        "com.google.android.apps.tachyon", // Google Meet
        "com.microsoft.teams",
        "com.skype.raider",
        "com.viber.voip",
        "jp.naver.line.android",
        "com.tencent.mm", // WeChat
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.microsoft.emmx" // Edge
    )

    // Add debug logging
    private fun logAppInfo(context: Context, packageName: String) {
        try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val hasLaunchIntent = packageManager.getLaunchIntentForPackage(packageName) != null
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            
            Log.d("AppInfo", """
                Package: $packageName
                App Name: $appName
                System App: $isSystemApp
                Has Launch Intent: $hasLaunchIntent
            """.trimIndent())
        } catch (e: Exception) {
            Log.e("AppInfo", "Error getting info for $packageName", e)
        }
    }

    fun formatDuration(timeInMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }

    fun getAppName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    // Add helper function to format timestamp
    private fun formatTime(timestamp: Long): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val timeInForeground: Long,
    val isSystemApp: Boolean
) 