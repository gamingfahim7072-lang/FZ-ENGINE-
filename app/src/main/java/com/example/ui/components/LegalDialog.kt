package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.FzCyanSecondary
import com.example.ui.theme.FzPurplePrimary
import com.example.ui.theme.FzSurfaceBorder
import com.example.ui.theme.FzSurfaceCard
import com.example.ui.theme.FzSurfaceElevated
import com.example.ui.theme.FzTextPrimary
import com.example.ui.theme.FzTextSecondary

@Composable
fun LegalDialog(
    isOpen: Boolean,
    mode: String, // "PRIVACY" or "TERMS"
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val isPrivacy = mode == "PRIVACY"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.8f)
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FzCyanSecondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPrivacy) Icons.Default.PrivacyTip else Icons.Default.Gavel,
                                contentDescription = null,
                                tint = FzCyanSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPrivacy) "PRIVACY POLICY" else "TERMS OF SERVICE",
                                color = FzTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "FZ ENGINE Legal & Data Standards",
                                color = FzTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = FzTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FzSurfaceCard)
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        if (isPrivacy) {
                            LegalSectionTitle("1. Data Minimization Principle")
                            LegalBody("FZ ENGINE collects only cryptographic device hashes and session tokens strictly required for digital license verification and anti-fraud safeguards. No unnecessary telemetry, personal files, contacts, or location data are harvested.")

                            LegalSectionTitle("2. Local Storage Safeguards")
                            LegalBody("Sensitive session metadata is stored using secure local Android sandboxed databases. Plaintext license keys are never stored permanently; only cryptographic SHA-256 validation fingerprints are retained.")

                            LegalSectionTitle("3. Third-Party Games")
                            LegalBody("FZ ENGINE acts as a legitimate application launcher. It does not tamper with, read memory from, or modify files belonging to Carrom Disc Pool or other third-party titles.")

                            LegalSectionTitle("4. Account & Data Deletion")
                            LegalBody("Users may revoke active sessions or delete local diagnostic audit trails at any time via the Settings menu.")
                        } else {
                            LegalSectionTitle("1. Authorized Platform Usage")
                            LegalBody("FZ ENGINE is a legitimate software utility for game launching, digital license authentication, and authorized seller distribution.")

                            LegalSectionTitle("2. Anti-Cheating & Fair Play")
                            LegalBody("Users agree NOT to use FZ ENGINE in conjunction with unauthorized automation, aim-assist tooling, anti-cheat bypass scripts, or third-party game tampering. FZ ENGINE strictly enforces fair play and security standards.")

                            LegalSectionTitle("3. Digital License Terms")
                            LegalBody("Licenses are non-transferable, single-device bound by default unless multi-device tiers are provisioned. Any fraudulent key sharing or unauthorized resale triggers server-side key revocation.")

                            LegalSectionTitle("4. Server Availability")
                            LegalBody("Digital licenses are verified via secure HTTPS endpoints. Scheduled maintenance will be announced through the Notification Center.")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FzPurplePrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("I Understand & Agree", color = FzTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun LegalSectionTitle(title: String) {
    Text(
        text = title,
        color = FzCyanSecondary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun LegalBody(text: String) {
    Text(
        text = text,
        color = FzTextSecondary,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
