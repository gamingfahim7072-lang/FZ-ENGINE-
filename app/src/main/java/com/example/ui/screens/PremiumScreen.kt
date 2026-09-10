package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LicenseEntity
import com.example.data.local.entity.SessionEntity
import com.example.data.model.LicenseStatus
import com.example.ui.theme.FzCyanSecondary
import com.example.ui.theme.FzPurpleLight
import com.example.ui.theme.FzPurplePrimary
import com.example.ui.theme.FzStatusAmber
import com.example.ui.theme.FzStatusGreen
import com.example.ui.theme.FzStatusOrange
import com.example.ui.theme.FzStatusRed
import com.example.ui.theme.FzSurfaceBorder
import com.example.ui.theme.FzSurfaceBorderHighlight
import com.example.ui.theme.FzSurfaceCard
import com.example.ui.theme.FzSurfaceElevated
import com.example.ui.theme.FzTextPrimary
import com.example.ui.theme.FzTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PremiumScreen(
    activeLicense: LicenseEntity?,
    activeSessions: List<SessionEntity>,
    onEnterKeyClick: () -> Unit,
    onDeactivateLicense: () -> Unit,
    onTerminateOtherSessions: () -> Unit,
    onSignOutCurrent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLicensed = activeLicense != null

    val currentStatus = when {
        activeLicense == null -> LicenseStatus.LOCKED
        activeLicense.status == LicenseStatus.REVOKED.name -> LicenseStatus.REVOKED
        activeLicense.status == LicenseStatus.EXPIRED.name -> LicenseStatus.EXPIRED
        activeLicense.expiresAt != null && (activeLicense.expiresAt - System.currentTimeMillis() < 86400000L * 3) -> LicenseStatus.EXPIRING_SOON
        else -> LicenseStatus.ACTIVE
    }

    val (statusColor, statusBg) = when (currentStatus) {
        LicenseStatus.ACTIVE -> Pair(FzStatusGreen, Color(0xFF0F2B1B))
        LicenseStatus.EXPIRING_SOON -> Pair(FzStatusOrange, Color(0xFF2C1910))
        LicenseStatus.EXPIRED -> Pair(FzStatusRed, Color(0xFF2C1113))
        LicenseStatus.REVOKED -> Pair(FzStatusRed, Color(0xFF2C1113))
        LicenseStatus.BANNED -> Pair(FzStatusRed, Color(0xFF2C1113))
        LicenseStatus.LOCKED -> Pair(FzStatusAmber, Color(0xFF2C2211))
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Premium Membership Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = FzPurplePrimary.copy(alpha = 0.35f))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF221A3A),
                            Color(0xFF141021)
                        )
                    )
                )
                .border(1.dp, FzSurfaceBorderHighlight, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Card Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FzPurplePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = FzPurpleLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FZ ENGINE PREMIUM",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isLicensed) activeLicense?.plan ?: "Pro Pass" else "Tier Status: Unlicensed",
                                color = FzCyanSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBg)
                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentStatus.displayName.uppercase(),
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Metadata Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(FzSurfaceElevated.copy(alpha = 0.7f))
                        .border(1.dp, FzSurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumDataRow(
                        label = "Plan",
                        value = activeLicense?.plan ?: "Standard (Locked)",
                        isHighlight = isLicensed
                    )
                    PremiumDataRow(
                        label = "Key ID",
                        value = activeLicense?.maskedKey ?: "Not Provisioned",
                        isMonospace = true
                    )
                    PremiumDataRow(
                        label = "Activated",
                        value = if (activeLicense?.activatedAt != null) dateFormat.format(Date(activeLicense.activatedAt)) else "None"
                    )
                    PremiumDataRow(
                        label = "Expiry",
                        value = if (activeLicense?.expiresAt != null) dateFormat.format(Date(activeLicense.expiresAt)) else "N/A",
                        isHighlight = isLicensed
                    )
                    PremiumDataRow(
                        label = "Seller",
                        value = activeLicense?.sellerName ?: "Authorized Distribution Network"
                    )
                    PremiumDataRow(
                        label = "Device Status",
                        value = if (isLicensed) "Bound to this Hardware (${activeLicense?.deviceBinding ?: "Current"})" else "Unbound"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Action Button
                Button(
                    onClick = onEnterKeyClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("enter_license_key_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FzPurplePrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLicensed) "CHANGE / EXTEND LICENSE" else "ENTER LICENSE KEY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                if (isLicensed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDeactivateLicense,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FzStatusRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FzStatusRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Deactivate License from this Device", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Device & Session Management Section
        Text(
            text = "DEVICE & SESSION MANAGEMENT",
            color = FzTextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Current Device Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(FzSurfaceElevated)
                .border(1.dp, FzSurfaceBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FzCyanSecondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = FzCyanSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                                color = FzTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Current Device • Android ${Build.VERSION.RELEASE}",
                                color = FzTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F2B1B))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(FzStatusGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ACTIVE", color = FzStatusGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FzSurfaceCard)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SessionDetailRow(label = "Platform", value = "Android JVM (API ${Build.VERSION.SDK_INT})")
                    SessionDetailRow(label = "App Version", value = "1.0.0 (Production Build)")
                    SessionDetailRow(label = "Session Security", value = "AES-256 Android Keystore Sandboxed")
                    SessionDetailRow(label = "Active Sessions", value = "${activeSessions.size} Authorized Device(s)")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Session Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTerminateOtherSessions,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FzSurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Sign Out Other Devices", color = FzCyanSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onSignOutCurrent,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FzSurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Sign Out Current Session", color = FzStatusRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumDataRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = FzTextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = if (isHighlight) FzPurpleLight else FzTextPrimary,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@Composable
private fun SessionDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = FzTextSecondary, fontSize = 11.sp)
        Text(text = value, color = FzTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
