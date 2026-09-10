package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServerHealthStatus
import com.example.data.model.UserRole
import com.example.ui.theme.FzCyanSecondary
import com.example.ui.theme.FzPurpleLight
import com.example.ui.theme.FzPurplePrimary
import com.example.ui.theme.FzStatusAmber
import com.example.ui.theme.FzStatusGreen
import com.example.ui.theme.FzStatusOrange
import com.example.ui.theme.FzStatusRed
import com.example.ui.theme.FzSurfaceBorder
import com.example.ui.theme.FzSurfaceCard
import com.example.ui.theme.FzSurfaceElevated
import com.example.ui.theme.FzTextPrimary
import com.example.ui.theme.FzTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagementSheet(
    isOpen: Boolean,
    currentRole: UserRole,
    serverStatus: ServerHealthStatus,
    onRoleSelected: (UserRole) -> Unit,
    onServerStatusChanged: (ServerHealthStatus) -> Unit,
    onSimulateUpdate: (hasUpdate: Boolean, isRequired: Boolean) -> Unit,
    onOpenDiagnostics: () -> Unit,
    onRevokeLicense: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FzSurfaceElevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FzSurfaceBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
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
                            .background(FzPurplePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = FzPurplePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Admin & Role Console",
                            color = FzTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enforce backend permissions & state simulation",
                            color = FzTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = FzTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Role Perspective Switcher
                item {
                    Text(
                        text = "ACTIVE USER ROLE PERSPECTIVE",
                        color = FzCyanSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        UserRole.entries.forEach { role ->
                            val isSelected = currentRole == role
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) FzPurplePrimary.copy(alpha = 0.15f) else FzSurfaceCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) FzPurplePrimary else FzSurfaceBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onRoleSelected(role) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (role) {
                                            UserRole.OWNER -> Icons.Default.MilitaryTech
                                            UserRole.DEVELOPER -> Icons.Default.DeveloperMode
                                            UserRole.SELLER -> Icons.Default.Store
                                            UserRole.SUPPORT -> Icons.Default.HeadsetMic
                                            UserRole.USER -> Icons.Default.Person
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) FzPurplePrimary else FzTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = role.displayName,
                                            color = FzTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = when (role) {
                                                UserRole.OWNER -> "Full administrative control, plan generation, policy toggle"
                                                UserRole.DEVELOPER -> "Technical diagnostics, update simulation, telemetry"
                                                UserRole.SELLER -> "License inventory, sales commission, client binding"
                                                UserRole.SUPPORT -> "Ticket resolution queue, verified customer lookup"
                                                UserRole.USER -> "Standard launcher, personal license activation"
                                            },
                                            color = FzTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = FzPurplePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Server Status Control (Simulate all 4 server responses)
                item {
                    Text(
                        text = "SERVER INFRASTRUCTURE STATE",
                        color = FzCyanSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ServerHealthStatus.entries.forEach { status ->
                            val isSelected = serverStatus == status
                            val tint = when (status) {
                                ServerHealthStatus.ONLINE -> FzStatusGreen
                                ServerHealthStatus.DEGRADED -> FzStatusAmber
                                ServerHealthStatus.MAINTENANCE -> FzStatusOrange
                                ServerHealthStatus.OFFLINE -> FzStatusRed
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) tint.copy(alpha = 0.2f) else FzSurfaceCard)
                                    .border(1.dp, if (isSelected) tint else FzSurfaceBorder, RoundedCornerShape(10.dp))
                                    .clickable { onServerStatusChanged(status) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = status.displayName,
                                    color = if (isSelected) tint else FzTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Section: Update System Simulation
                item {
                    Text(
                        text = "OTA UPDATE POLICY SIMULATION",
                        color = FzCyanSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSimulateUpdate(false, false) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FzSurfaceCard),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Up to date", fontSize = 11.sp, color = FzTextPrimary)
                        }
                        Button(
                            onClick = { onSimulateUpdate(true, false) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FzSurfaceCard),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Available", fontSize = 11.sp, color = FzCyanSecondary)
                        }
                        Button(
                            onClick = { onSimulateUpdate(true, true) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FzSurfaceCard),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Required", fontSize = 11.sp, color = FzStatusOrange)
                        }
                    }
                }

                // Section: Role-Specific Action Tools
                item {
                    Text(
                        text = "ROLE ACTION TOOLBOX",
                        color = FzCyanSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Diagnostic Logs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FzSurfaceCard)
                                .border(1.dp, FzSurfaceBorder, RoundedCornerShape(12.dp))
                                .clickable { onOpenDiagnostics() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = FzCyanSecondary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Inspect Audit Telemetry Logs", color = FzTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Real-time cryptographic verification log entries", color = FzTextSecondary, fontSize = 10.sp)
                            }
                        }

                        // Deactivate current license for testing
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FzSurfaceCard)
                                .border(1.dp, FzSurfaceBorder, RoundedCornerShape(12.dp))
                                .clickable { onRevokeLicense() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = FzStatusRed)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Reset / Deactivate Active License", color = FzTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Clears active device binding to test locked state", color = FzTextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
