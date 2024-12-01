import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Shape

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.drawText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    screenTime: String,
    currentStake: String?,
    screenTimeLimit: ScreenTimeLimit?,
    onScreenTimeLimitSet: (ScreenTimeLimit) -> Unit,
    onAppUsageClick: () -> Unit,
    onWalletClick: () -> Unit,
    onStakeClick: () -> Unit
) {
    var showLimitSheet by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf(screenTimeLimit?.hours?.toString() ?: "") }
    var minutes by remember { mutableStateOf(screenTimeLimit?.minutes?.toString() ?: "") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DETOX",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White
                )
                IconButton(onClick = onWalletClick) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Screen Time Display with Modern Progress Arc
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                if (screenTimeLimit != null) {
                    val screenTimeMinutes = remember(screenTime) {
                        val parts = screenTime.split("h", "m")
                        when {
                            parts.size == 3 -> parts[0].trim().toInt() * 60 + parts[1].trim().toInt()
                            parts.size == 2 && screenTime.contains("h") -> parts[0].trim().toInt() * 60
                            parts.size == 2 -> parts[0].trim().toInt() // Just minutes
                            else -> 0
                        }
                    }
                    val progress = (screenTimeMinutes.toFloat() / screenTimeLimit.toMinutes()).coerceIn(0f, 1f)

                    // Modern Progress Arc
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    ) {
                        val strokeWidth = 12.dp.toPx()
                        val startAngle = 150f
                        val sweepAngle = 240f
                        val arcRadius = (size.minDimension - strokeWidth) / 2
                        
                        // Background Arc (remaining time)
                        drawArc(
                            color = Color.White.copy(alpha = 0.1f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )

                        // Progress Arc (elapsed time)
                        drawArc(
                            color = Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle * progress,
                            useCenter = false,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )

                        // Add time remaining text in the center
                        val timeRemaining = screenTimeLimit.toMinutes() - screenTimeMinutes
                        if (timeRemaining > 0) {
                            drawContext.canvas.nativeCanvas.apply {
                                val text = "${UsageStatsUtil.formatDuration(timeRemaining.toLong() * 60L * 1000L)} left"
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    alpha = (255 * 0.7f).toInt()
                                    textSize = 40f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                drawText(
                                    text,
                                    center.x,
                                    center.y + size.height / 4,
                                    paint
                                )
                            }
                        }
                    }
                }

                // Screen Time Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = screenTime,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "TODAY'S SCREEN TIME",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Column with weight to ensure it fits
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Set Limit Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF121212),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    onClick = { showLimitSheet = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (screenTimeLimit != null) "SCREEN TIME LIMIT" else "SET LIMIT",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                            if (screenTimeLimit != null) {
                                Text(
                                    text = "${screenTimeLimit.hours}h ${screenTimeLimit.minutes}m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Icon(
                            imageVector = if (screenTimeLimit != null) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                // App Usage Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF121212),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    onClick = onAppUsageClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "APP USAGE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "View detailed statistics",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stake Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (currentStake != null) Color(0xFF121212) else Color.White,
                shape = RoundedCornerShape(16.dp),
                border = if (currentStake != null) {
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                } else null,
                onClick = onStakeClick
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentStake != null) "CURRENT STAKE" else "STAKE NOW",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (currentStake != null) Color.White else Color.Black
                        )
                        if (currentStake != null) {
                            Text(
                                text = currentStake,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (currentStake != null) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (currentStake != null) Color.White else Color.Black
                    )
                }
            }
        }
    }

    // Bottom Sheet for Setting Time Limit
    if (showLimitSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLimitSheet = false },
            containerColor = Color(0xFF121212),
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp),
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp)
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "SET SCREEN TIME LIMIT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hours Input
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { 
                            if (it.isEmpty() || it.toIntOrNull() in 0..23) {
                                hours = it
                            }
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        suffix = { Text("h", color = Color.White.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Minutes Input
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { 
                            if (it.isEmpty() || it.toIntOrNull() in 0..59) {
                                minutes = it
                            }
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        suffix = { Text("m", color = Color.White.copy(alpha = 0.7f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        val h = hours.toIntOrNull() ?: 0
                        val m = minutes.toIntOrNull() ?: 0
                        if (h > 0 || m > 0) {
                            onScreenTimeLimitSet(ScreenTimeLimit(h, m))
                            showLimitSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    enabled = (hours.toIntOrNull() ?: 0) > 0 || (minutes.toIntOrNull() ?: 0) > 0,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CONFIRM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NavigationButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}