package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.LicenseEntity
import com.example.data.model.ServerHealthStatus
import com.example.ui.components.ServerStatusBadge
import com.example.ui.theme.FzCyanSecondary
import com.example.ui.theme.FzPurpleLight
import com.example.ui.theme.FzPurplePrimary
import com.example.ui.theme.FzStatusGreen
import com.example.ui.theme.FzStatusRed
import com.example.ui.theme.FzSurfaceBorder
import com.example.ui.theme.FzSurfaceCard
import com.example.ui.theme.FzSurfaceElevated
import com.example.ui.theme.FzTextPrimary
import com.example.ui.theme.FzTextSecondary
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    activeLicense: LicenseEntity?,
    serverStatus: ServerHealthStatus,
    onUnlockClick: () -> Unit,
    onOpenGameClick: () -> Unit,
    onViewGameDetails: () -> Unit,
    onViewLicenseInfo: () -> Unit,
    onSupportClick: () -> Unit,
    onServerStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLicensed = activeLicense != null
    var cardMenuExpanded by remember { mutableStateOf(false) }

    // Live countdown timer for expiry
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val remainingMillis = remember(currentTime, activeLicense) {
        (activeLicense?.expiresAt ?: 0L) - currentTime
    }

    val countdownText = remember(remainingMillis) {
        if (remainingMillis <= 0) "Expired"
        else {
            val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60
            if (days > 0) "${days}d ${hours}h ${minutes}m left"
            else "%02d:%02d:%02d left".format(hours, minutes, seconds)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp) // Room for floating bottom nav
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Promotional Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = FzPurplePrimary.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, FzSurfaceBorder, RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_fz_banner),
                contentDescription = "FZ ENGINE Promotional Artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark gradient overlay for pristine legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xEA0B0910),
                                Color(0xAA0B0910),
                                Color(0x330B0910)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x881E1533))
                            .border(1.dp, FzPurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(FzCyanSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LEGITIMATE LAUNCHER",
                            color = FzCyanSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Carousel dots (visual indicator)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(16.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(FzPurplePrimary))
                        Box(modifier = Modifier.size(6.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(FzTextSecondary.copy(alpha = 0.4f)))
                        Box(modifier = Modifier.size(6.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(FzTextSecondary.copy(alpha = 0.4f)))
                    }
                }

                Column {
                    Text(
                        text = "FZ ENGINE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "High-performance digital licensing & legitimate game launcher hub.",
                        color = FzTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Section Header: Supported Games
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SUPPORTED GAMES",
                color = FzTextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                letterSpacing = 0.8.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(FzStatusGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "1 Verified Title",
                    color = FzTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Supported Game Card: Carrom Disc Pool
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("game_card_carrom")
                .clip(RoundedCornerShape(20.dp))
                .background(FzSurfaceElevated)
                .border(
                    1.dp,
                    if (isLicensed) FzPurplePrimary.copy(alpha = 0.5f) else FzSurfaceBorder,
                    RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Game Thumbnail
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, FzSurfaceBorder, RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_carrom_cover),
                            contentDescription = "Carrom Disc Pool Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Title & Specs
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Carrom Disc Pool",
                                color = FzTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            // Card Overflow Menu
                            Box {
                                IconButton(
                                    onClick = { cardMenuExpanded = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = FzTextSecondary
                                    )
                                }

                                DropdownMenu(
                                    expanded = cardMenuExpanded,
                                    onDismissRequest = { cardMenuExpanded = false },
                                    modifier = Modifier
                                        .background(FzSurfaceCard)
                                        .border(1.dp, FzSurfaceBorder, RoundedCornerShape(8.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("View Details", color = FzTextPrimary, fontSize = 13.sp) },
                                        onClick = {
                                            cardMenuExpanded = false
                                            onViewGameDetails()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("License Information", color = FzTextPrimary, fontSize = 13.sp) },
                                        onClick = {
                                            cardMenuExpanded = false
                                            onViewLicenseInfo()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Support", color = FzCyanSecondary, fontSize = 13.sp) },
                                        onClick = {
                                            cardMenuExpanded = false
                                            onSupportClick()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Miniclip • Verified Integration",
                            color = FzTextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Supported Status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F2618))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(FzStatusGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Supported v8.2.0+",
                                    color = FzStatusGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // License Status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isLicensed) Color(0xFF1E1435) else Color(0xFF2B1418))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLicensed) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isLicensed) FzPurpleLight else FzStatusRed,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isLicensed) "UNLOCKED" else "LOCKED",
                                    color = if (isLicensed) FzPurpleLight else FzStatusRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fair Play Safeguard Guarantee Banner inside card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FzSurfaceCard)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = FzCyanSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Legitimate launcher. No memory modification, aim-assist, or anti-cheat tampering.",
                        color = FzTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Action Button
                if (isLicensed) {
                    Button(
                        onClick = onOpenGameClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_game_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FzPurplePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OPEN GAME",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onUnlockClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("unlock_game_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FzSurfaceCard,
                            contentColor = FzPurpleLight
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FzPurplePrimary.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = FzPurpleLight
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UNLOCK PRO PASS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Bottom License Status & Server Telemetry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FzSurfaceCard)
                .border(1.dp, FzSurfaceBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT LICENSE",
                            color = FzTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isLicensed) activeLicense?.plan ?: "Pro Active" else "No Active License",
                            color = if (isLicensed) FzTextPrimary else FzTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (isLicensed) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EXPIRY COUNTDOWN",
                                color = FzTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = countdownText,
                                color = FzCyanSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(FzSurfaceBorder))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = FzTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cloud Licensing Node",
                            color = FzTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    ServerStatusBadge(
                        status = serverStatus,
                        onClick = onServerStatusClick
                    )
                }
            }
        }
    }
}
