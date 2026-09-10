package com.example.ui.screens

import android.os.Build
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServerHealthStatus
import com.example.data.model.UserRole
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

@Composable
fun SettingsScreen(
    userRole: UserRole,
    serverStatus: ServerHealthStatus,
    onOpenRoleConsole: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenLegal: (String) -> Unit,
    onSignOutSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. ACCOUNT CATEGORY
        SettingsCategoryTitle("ACCOUNT & IDENTITY")
        SettingsCard {
            SettingsRow(
                icon = Icons.Default.AdminPanelSettings,
                title = "Active Role Perspective",
                subtitle = userRole.displayName,
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Switch", color = FzPurpleLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = FzPurpleLight)
                    }
                },
                onClick = onOpenRoleConsole
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Default.Devices,
                title = "Account Identifier",
                subtitle = "usr_fz_7749 (Cryptographically Bound)",
                isMonospace = true
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Default.PowerSettingsNew,
                title = "Sign Out Device Session",
                subtitle = "Revoke this hardware terminal access",
                tint = FzStatusRed,
                onClick = onSignOutSession
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. SECURITY & INTEGRITY
        SettingsCategoryTitle("SECURITY & CRYPTOGRAPHY")
        SettingsCard {
            SettingsRow(
                icon = Icons.Default.Security,
                title = "Hardware Keystore Binding",
                subtitle = "Hardware-backed Android KeyStore AES-256",
                trailingContent = {
                    Text("ENCLAVE OK", color = FzStatusGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Default.LockReset,
                title = "Anti-Tamper Status",
                subtitle = "Clean environment • No unauthorized injectors",
                trailingContent = {
                    Text("VERIFIED", color = FzCyanSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Default.Security,
                title = "Fair Play Compliance",
                subtitle = "No memory hacking, no automation, no exploits"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. NETWORK & LICENSING INFRASTRUCTURE
        SettingsCategoryTitle("LICENSING CLOUD INFRASTRUCTURE")
        SettingsCard {
            SettingsRow(
                icon = Icons.Default.CloudDone,
                title = "Primary API Gateway",
                subtitle = "https://licensing.fzengine.app:443",
                trailingContent = {
                    ServerStatusBadge(status = serverStatus)
                },
                onClick = onOpenRoleConsole
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Default.Speed,
                title = "Gateway Round-Trip Ping",
                subtitle = "Latency: ${serverStatus.latencyMs}ms (TLS 1.3 Strict)"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. APPLICATION & TOOLS
        SettingsCategoryTitle("APPLICATION & SYSTEM")
        SettingsCard {
            SettingsRow(
                icon = Icons.Default.SystemUpdate,
                title = "Check for OTA Updates",
                subtitle = "Current Version: 1.0.0 (Build 1001)",
                onClick = onCheckForUpdates
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Default.Terminal,
                title = "Diagnostic Audit Logs",
                subtitle = "Inspect real-time telemetry and server transactions",
                onClick = onOpenDiagnostics
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                subtitle = "Data minimization & sandboxing details",
                onClick = { onOpenLegal("PRIVACY") }
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Default.Gavel,
                title = "Terms of Service",
                subtitle = "Authorized usage, licensing, and security policies",
                onClick = { onOpenLegal("TERMS") }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Footer Branding
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "FZ ENGINE v1.0.0",
                color = FzTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Legitimate Game Launcher & License Engine",
                color = FzTextSecondary.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SettingsCategoryTitle(text: String) {
    Text(
        text = text,
        color = FzCyanSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(FzSurfaceElevated)
            .border(1.dp, FzSurfaceBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = FzTextPrimary,
    isMonospace: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FzSurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (tint != FzTextPrimary) tint else FzPurpleLight,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = tint,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = FzTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                    lineHeight = 14.sp
                )
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = FzTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FzSurfaceBorder)
    )
}
