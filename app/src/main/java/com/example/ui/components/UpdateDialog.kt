package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.AppUpdateInfo
import com.example.ui.theme.FzCyanSecondary
import com.example.ui.theme.FzPurplePrimary
import com.example.ui.theme.FzStatusGreen
import com.example.ui.theme.FzStatusOrange
import com.example.ui.theme.FzSurfaceBorder
import com.example.ui.theme.FzSurfaceCard
import com.example.ui.theme.FzSurfaceElevated
import com.example.ui.theme.FzTextPrimary
import com.example.ui.theme.FzTextSecondary

@Composable
fun UpdateDialog(
    isOpen: Boolean,
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val isRequired = updateInfo.isRequired
    val hasUpdate = updateInfo.hasUpdate

    Dialog(
        onDismissRequest = { if (!isRequired) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isRequired,
            dismissOnClickOutside = !isRequired,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(FzSurfaceElevated)
                .border(1.dp, FzSurfaceBorder, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isRequired -> FzStatusOrange.copy(alpha = 0.2f)
                                        hasUpdate -> FzCyanSecondary.copy(alpha = 0.2f)
                                        else -> FzStatusGreen.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isRequired -> Icons.Default.Warning
                                    hasUpdate -> Icons.Default.SystemUpdate
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = when {
                                    isRequired -> FzStatusOrange
                                    hasUpdate -> FzCyanSecondary
                                    else -> FzStatusGreen
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when {
                                    isRequired -> "UPDATE REQUIRED"
                                    hasUpdate -> "UPDATE AVAILABLE"
                                    else -> "UP TO DATE"
                                },
                                color = FzTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "FZ ENGINE Distribution Network",
                                color = FzTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (!isRequired) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = FzTextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Version details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FzSurfaceCard)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Installed Version", color = FzTextSecondary, fontSize = 12.sp)
                        Text("v1.0.0 (Build 1001)", color = FzTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Latest Channel Version", color = FzTextSecondary, fontSize = 12.sp)
                        Text("v${updateInfo.latestVersion}", color = FzCyanSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Minimum Mandatory Version", color = FzTextSecondary, fontSize = 12.sp)
                        Text("v${updateInfo.minimumVersion}", color = FzTextSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Release notes
                Text(
                    text = "Release Notes",
                    color = FzTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FzSurfaceCard)
                        .padding(10.dp)
                ) {
                    Text(
                        text = updateInfo.releaseNotes,
                        color = FzTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                if (hasUpdate) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRequired) FzStatusOrange else FzPurplePrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRequired) "INSTALL MANDATORY UPDATE" else "DOWNLOAD UPDATE NOW",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FzSurfaceCard),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("OK", color = FzTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
