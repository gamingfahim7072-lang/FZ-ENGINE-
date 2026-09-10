package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.FzCyanSecondary
import com.example.ui.theme.FzPurplePrimary
import com.example.ui.theme.FzStatusGreen
import com.example.ui.theme.FzStatusRed
import com.example.ui.theme.FzSurfaceBorder
import com.example.ui.theme.FzSurfaceCard
import com.example.ui.theme.FzSurfaceElevated
import com.example.ui.theme.FzTextPrimary
import com.example.ui.theme.FzTextSecondary

@Composable
fun LicenseEntryDialog(
    isOpen: Boolean,
    keyInput: String,
    isKeyMasked: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onKeyChange: (String) -> Unit,
    onToggleMask: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val clipboardManager = LocalClipboardManager.current

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FzPurplePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = FzPurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ENTER LICENSE KEY",
                                color = FzTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "HTTPS Secure Cloud Verification",
                                color = FzTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = FzTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Input Field
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = onKeyChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("license_key_input"),
                    placeholder = {
                        Text(
                            text = "FZ-PRO30-XXXX-XXXX",
                            color = FzTextSecondary.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = FzTextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    visualTransformation = if (isKeyMasked) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onToggleMask) {
                                Icon(
                                    imageVector = if (isKeyMasked) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Key Mask",
                                    tint = FzTextSecondary
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.getText()?.text?.let { clipText ->
                                        onKeyChange(clipText.trim())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste from clipboard",
                                    tint = FzCyanSecondary
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FzPurplePrimary,
                        unfocusedBorderColor = FzSurfaceBorder,
                        focusedContainerColor = FzSurfaceCard,
                        unfocusedContainerColor = FzSurfaceCard,
                        cursorColor = FzPurplePrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                // Quick Presets Helper Pills for effortless server response testing
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Quick Test Keys (Simulate Server Responses):",
                    color = FzTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TestKeyPill(label = "Active 30D", key = "FZ-PRO30-7789-9942", onSelect = onKeyChange)
                    TestKeyPill(label = "Expired", key = "FZ-EXP-0000-1111", onSelect = onKeyChange)
                    TestKeyPill(label = "Revoked", key = "FZ-REV-2222-3333", onSelect = onKeyChange)
                    TestKeyPill(label = "Dev Limit", key = "FZ-DEV-4444-5555", onSelect = onKeyChange)
                }

                // Error Message
                AnimatedVisibility(visible = errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2B1418))
                            .border(1.dp, FzStatusRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = FzStatusRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = FzStatusRed,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Success Message
                AnimatedVisibility(visible = successMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F2B1B))
                            .border(1.dp, FzStatusGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FzStatusGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successMessage ?: "",
                            color = FzStatusGreen,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FzSurfaceCard,
                            contentColor = FzTextSecondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (successMessage != null) "Done" else "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = onSubmit,
                        modifier = Modifier
                            .testTag("submit_license_button")
                            .weight(1.5f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FzPurplePrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading && keyInput.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Validating...", fontSize = 13.sp)
                        } else {
                            Text(
                                text = "VALIDATE KEY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestKeyPill(
    label: String,
    key: String,
    onSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FzSurfaceCard)
            .border(1.dp, FzSurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onSelect(key) }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = FzCyanSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
