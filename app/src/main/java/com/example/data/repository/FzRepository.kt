package com.example.data.repository

import android.content.Context
import android.os.Build
import com.example.data.local.dao.FzEngineDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.LicenseEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.SellerEntity
import com.example.data.local.entity.SessionEntity
import com.example.data.model.LicenseStatus
import com.example.data.model.NotificationCategory
import com.example.data.model.ServerHealthStatus
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class LicenseActivationResult {
    data class Success(val license: LicenseEntity, val message: String) : LicenseActivationResult()
    data class Failure(val code: ServerErrorCode, val message: String) : LicenseActivationResult()
}

enum class ServerErrorCode {
    INVALID,
    EXPIRED,
    REVOKED,
    BANNED,
    DEVICE_LIMIT,
    SERVER_ERROR,
    MAINTENANCE,
    NETWORK_OFFLINE
}

data class AppUpdateInfo(
    val latestVersion: String = "1.0.0",
    val minimumVersion: String = "1.0.0",
    val releaseNotes: String = "FZ ENGINE v1.0.0: Initial production release. Legitimate game launcher architecture, hardened license encryption, and seller hub.",
    val downloadUrl: String = "https://fzengine.app/download",
    val hasUpdate: Boolean = false,
    val isRequired: Boolean = false
)

class FzRepository(
    private val dao: FzEngineDao,
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _serverStatus = MutableStateFlow(ServerHealthStatus.ONLINE)
    val serverStatus = _serverStatus.asStateFlow()

    private val _currentUserRole = MutableStateFlow(UserRole.USER)
    val currentUserRole = _currentUserRole.asStateFlow()

    private val _updateInfo = MutableStateFlow(AppUpdateInfo())
    val updateInfo = _updateInfo.asStateFlow()

    val activeLicense: Flow<LicenseEntity?> = dao.getActiveLicenseFlow()
    val sellers: Flow<List<SellerEntity>> = dao.getAllSellersFlow()
    val activeSessions: Flow<List<SessionEntity>> = dao.getActiveSessionsFlow()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAuditLogsFlow()
    val notifications: Flow<List<NotificationEntity>> = dao.getNotificationsFlow()
    val unreadCount: Flow<Int> = dao.getUnreadNotificationsCountFlow()

    suspend fun initializeSeedData() = withContext(ioDispatcher) {
        // Register current device session if none
        val deviceIdHash = hashString("${Build.MANUFACTURER}-${Build.MODEL}-${Build.ID}")
        val currentSession = SessionEntity(
            id = "sess_${UUID.randomUUID().toString().take(8)}",
            userId = "usr_fz_7749",
            deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            deviceIdentifierHash = deviceIdHash.take(16),
            platform = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appVersion = "1.0.0",
            createdAt = System.currentTimeMillis() - 86400000L * 2,
            lastSeen = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 86400000L * 30,
            isCurrent = true,
            isRevoked = false
        )
        dao.insertSession(currentSession)

        // Seed additional remote session for demonstration
        val remoteSession = SessionEntity(
            id = "sess_rem_9921",
            userId = "usr_fz_7749",
            deviceName = "Samsung Galaxy Tab S9",
            deviceIdentifierHash = hashString("Samsung-SM-X710").take(16),
            platform = "Android 14 (API 34)",
            appVersion = "1.0.0",
            createdAt = System.currentTimeMillis() - 86400000L * 5,
            lastSeen = System.currentTimeMillis() - 3600000L * 4,
            expiresAt = System.currentTimeMillis() + 86400000L * 25,
            isCurrent = false,
            isRevoked = false
        )
        dao.insertSession(remoteSession)

        // Seed verified sellers
        if (dao.getSellersCount() == 0) {
            val sampleSellers = listOf(
                SellerEntity(
                    id = "sel_apex_01",
                    displayName = "Apex Gaming Hub",
                    country = "United States",
                    countryCode = "US",
                    avatarTag = "AP",
                    verificationStatus = true,
                    totalSales = 4820,
                    amountSoldUsd = 43380.0,
                    publishedContactMethod = "@ApexEngineHub (Telegram)",
                    rating = 4.95f,
                    createdAt = System.currentTimeMillis() - 86400000L * 180,
                    territory = "North America"
                ),
                SellerEntity(
                    id = "sel_pulse_02",
                    displayName = "Nova Velocity Core",
                    country = "India",
                    countryCode = "IN",
                    avatarTag = "NV",
                    verificationStatus = true,
                    totalSales = 3915,
                    amountSoldUsd = 31320.0,
                    publishedContactMethod = "+91 98210 44321 (WhatsApp)",
                    rating = 4.91f,
                    createdAt = System.currentTimeMillis() - 86400000L * 120,
                    territory = "South Asia"
                ),
                SellerEntity(
                    id = "sel_cyber_03",
                    displayName = "CyberGate License Desk",
                    country = "United Kingdom",
                    countryCode = "GB",
                    avatarTag = "CG",
                    verificationStatus = true,
                    totalSales = 2780,
                    amountSoldUsd = 25020.0,
                    publishedContactMethod = "support@cybergate-licenses.com",
                    rating = 4.88f,
                    createdAt = System.currentTimeMillis() - 86400000L * 90,
                    territory = "Europe"
                ),
                SellerEntity(
                    id = "sel_brazil_04",
                    displayName = "Titanium Keys LatAm",
                    country = "Brazil",
                    countryCode = "BR",
                    avatarTag = "TK",
                    verificationStatus = true,
                    totalSales = 2150,
                    amountSoldUsd = 17200.0,
                    publishedContactMethod = "@TitaniumLatam (Telegram)",
                    rating = 4.84f,
                    createdAt = System.currentTimeMillis() - 86400000L * 75,
                    territory = "Latin America"
                ),
                SellerEntity(
                    id = "sel_indo_05",
                    displayName = "Garuda Digital Pass",
                    country = "Indonesia",
                    countryCode = "ID",
                    avatarTag = "GD",
                    verificationStatus = true,
                    totalSales = 1890,
                    amountSoldUsd = 13230.0,
                    publishedContactMethod = "t.me/garudapass_official",
                    rating = 4.79f,
                    createdAt = System.currentTimeMillis() - 86400000L * 60,
                    territory = "Southeast Asia"
                ),
                SellerEntity(
                    id = "sel_berlin_06",
                    displayName = "Krypton Systems EU",
                    country = "Germany",
                    countryCode = "DE",
                    avatarTag = "KS",
                    verificationStatus = true,
                    totalSales = 1420,
                    amountSoldUsd = 12780.0,
                    publishedContactMethod = "desk@kryptonsystems.de",
                    rating = 4.82f,
                    createdAt = System.currentTimeMillis() - 86400000L * 45,
                    territory = "Europe"
                )
            )
            dao.insertSellers(sampleSellers)
        }

        // Seed notifications
        val initialNotifications = listOf(
            NotificationEntity(
                id = "notif_welcome",
                title = "Welcome to FZ ENGINE",
                message = "Experience next-generation legitimate game launcher and digital license management.",
                category = NotificationCategory.ANNOUNCEMENT.name,
                timestamp = System.currentTimeMillis() - 3600000L * 5,
                isRead = false
            ),
            NotificationEntity(
                id = "notif_security",
                title = "Security & Anti-Tamper Policy",
                message = "FZ ENGINE operates strictly compliant with fair-play standards and device security safeguards.",
                category = NotificationCategory.ANNOUNCEMENT.name,
                timestamp = System.currentTimeMillis() - 3600000L * 24,
                isRead = false
            )
        )
        dao.insertNotifications(initialNotifications)

        // Seed initial audit log
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = "SYSTEM",
                actorRole = "SYSTEM",
                action = "INITIALIZE_APPLICATION",
                timestamp = System.currentTimeMillis(),
                result = "SUCCESS",
                metadata = "Security engine initialized on ${Build.MODEL}"
            )
        )
    }

    suspend fun validateAndActivateLicense(rawKey: String): LicenseActivationResult = withContext(ioDispatcher) {
        val cleanKey = rawKey.trim().uppercase(Locale.US)

        // Local input sanity check
        if (cleanKey.length < 8) {
            return@withContext LicenseActivationResult.Failure(
                ServerErrorCode.INVALID,
                "License key format is too short. Expected format: FZ-XXXX-XXXX-XXXX"
            )
        }

        // Check server health
        when (_serverStatus.value) {
            ServerHealthStatus.OFFLINE -> {
                recordAudit("CLIENT", _currentUserRole.value.name, "ACTIVATE_KEY", "FAILED", "Server offline")
                return@withContext LicenseActivationResult.Failure(
                    ServerErrorCode.NETWORK_OFFLINE,
                    "No Internet Connection or License Verification Server is offline."
                )
            }
            ServerHealthStatus.MAINTENANCE -> {
                recordAudit("CLIENT", _currentUserRole.value.name, "ACTIVATE_KEY", "FAILED", "Server maintenance")
                return@withContext LicenseActivationResult.Failure(
                    ServerErrorCode.MAINTENANCE,
                    "License server is currently undergoing scheduled maintenance. Please try again shortly."
                )
            }
            ServerHealthStatus.DEGRADED -> {
                delay(800) // Simulate degraded latency
            }
            ServerHealthStatus.ONLINE -> {
                delay(550) // Realistic secure HTTPS round-trip & verification latency
            }
        }

        // Simulate server response dispatching based on test keys or patterns
        val keyHash = hashString(cleanKey)
        val deviceBinding = "${Build.MANUFACTURER}-${Build.MODEL}".take(24)

        if (cleanKey.contains("EXP")) {
            recordAudit("API_SERVER", "LICENSE_ENGINE", "VERIFY_KEY", "EXPIRED", "Key $cleanKey has expired")
            return@withContext LicenseActivationResult.Failure(
                ServerErrorCode.EXPIRED,
                "This license key expired on the server. Please contact an authorized seller for renewal."
            )
        }

        if (cleanKey.contains("REV")) {
            recordAudit("API_SERVER", "LICENSE_ENGINE", "VERIFY_KEY", "REVOKED", "Key $cleanKey was revoked")
            return@withContext LicenseActivationResult.Failure(
                ServerErrorCode.REVOKED,
                "This license has been revoked by the issuer due to a refund or security alert."
            )
        }

        if (cleanKey.contains("BAN")) {
            recordAudit("API_SERVER", "LICENSE_ENGINE", "VERIFY_KEY", "BANNED", "Account/Key banned")
            return@withContext LicenseActivationResult.Failure(
                ServerErrorCode.BANNED,
                "This license key has been blacklisted for policy violation."
            )
        }

        if (cleanKey.contains("DEV") || cleanKey.contains("LIMIT")) {
            recordAudit("API_SERVER", "LICENSE_ENGINE", "VERIFY_KEY", "DEVICE_LIMIT", "Maximum devices exceeded")
            return@withContext LicenseActivationResult.Failure(
                ServerErrorCode.DEVICE_LIMIT,
                "Device limit reached. This license is already bound to maximum allowed devices (1/1)."
            )
        }

        if (cleanKey.contains("ERR") || cleanKey.contains("500")) {
            recordAudit("API_SERVER", "LICENSE_ENGINE", "VERIFY_KEY", "SERVER_ERROR", "Internal server error")
            return@withContext LicenseActivationResult.Failure(
                ServerErrorCode.SERVER_ERROR,
                "Server is temporarily unavailable. Error Code: 503. Please try again."
            )
        }

        // If key doesn't start with FZ- or doesn't have at least two segments
        if (!cleanKey.startsWith("FZ-") && !cleanKey.startsWith("KEY-") && cleanKey.length < 10) {
            recordAudit("API_SERVER", "LICENSE_ENGINE", "VERIFY_KEY", "INVALID", "Invalid checksum")
            return@withContext LicenseActivationResult.Failure(
                ServerErrorCode.INVALID,
                "Invalid license key. Checksum verification failed on the server."
            )
        }

        // Determine plan duration
        val durationDays = when {
            cleanKey.contains("365") || cleanKey.contains("YEAR") -> 365
            cleanKey.contains("90") -> 90
            cleanKey.contains("7") -> 7
            else -> 30
        }

        val planName = when {
            durationDays == 365 -> "Pro Engine Pass (Annual)"
            durationDays == 90 -> "Pro Engine Pass (Quarterly)"
            durationDays == 7 -> "Trial Engine Pass (7 Days)"
            else -> "Pro Engine Pass (30 Days)"
        }

        val now = System.currentTimeMillis()
        val expiresAt = now + (durationDays * 86400000L)
        val masked = maskKey(cleanKey)

        val newLicense = LicenseEntity(
            id = "lic_${UUID.randomUUID().toString().take(8)}",
            keyHash = keyHash,
            maskedKey = masked,
            plan = planName,
            durationDays = durationDays,
            status = LicenseStatus.ACTIVE.name,
            createdAt = now,
            activatedAt = now,
            expiresAt = expiresAt,
            revokedAt = null,
            deviceBinding = deviceBinding,
            sellerId = "sel_apex_01",
            sellerName = "Apex Gaming Hub (Verified)",
            activationCount = 1,
            maxActivations = 1
        )

        dao.insertOrUpdateLicense(newLicense)

        // Record audit
        recordAudit(
            "CLIENT",
            _currentUserRole.value.name,
            "LICENSE_ACTIVATION",
            "SUCCESS",
            "Activated $planName on $deviceBinding"
        )

        // Push notification
        val expiryFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(expiresAt))
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                title = "License Activated Successfully",
                message = "$planName is now active on this device until $expiryFormatted.",
                category = NotificationCategory.LICENSE_ACTIVATED.name,
                timestamp = now,
                isRead = false
            )
        )

        LicenseActivationResult.Success(newLicense, "License activated successfully! Full features unlocked.")
    }

    suspend fun revokeCurrentLicense(reason: String = "User requested cancellation") = withContext(ioDispatcher) {
        val active = dao.getActiveLicenseFlow()
        dao.clearLicenses()
        recordAudit("CLIENT", _currentUserRole.value.name, "LICENSE_REVOKE", "SUCCESS", reason)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                title = "License Removed",
                message = "The active license on this device has been deactivated.",
                category = NotificationCategory.LICENSE_EXPIRED.name,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
    }

    suspend fun revokeOtherSessions() = withContext(ioDispatcher) {
        dao.revokeOtherSessions()
        recordAudit("CLIENT", _currentUserRole.value.name, "SESSIONS_REVOKE_OTHERS", "SUCCESS", "Terminated remote sessions")
    }

    suspend fun signOutCurrentSession() = withContext(ioDispatcher) {
        dao.revokeCurrentSession()
        recordAudit("CLIENT", _currentUserRole.value.name, "SESSION_LOGOUT", "SUCCESS", "Local session terminated")
    }

    fun setServerStatus(status: ServerHealthStatus) {
        _serverStatus.value = status
    }

    fun setUserRole(role: UserRole) {
        _currentUserRole.value = role
    }

    fun setUpdateStatus(hasUpdate: Boolean, isRequired: Boolean) {
        _updateInfo.value = _updateInfo.value.copy(
            hasUpdate = hasUpdate,
            isRequired = isRequired,
            latestVersion = if (hasUpdate) "1.1.0" else "1.0.0"
        )
    }

    suspend fun markNotificationRead(id: String) = withContext(ioDispatcher) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsRead() = withContext(ioDispatcher) {
        dao.markAllNotificationsAsRead()
    }

    suspend fun clearAllNotifications() = withContext(ioDispatcher) {
        dao.clearNotifications()
    }

    suspend fun clearDiagnosticLogs() = withContext(ioDispatcher) {
        dao.clearAuditLogs()
    }

    suspend fun recordAudit(actorId: String, actorRole: String, action: String, result: String, metadata: String) = withContext(ioDispatcher) {
        dao.insertAuditLog(
            AuditLogEntity(
                actorId = actorId,
                actorRole = actorRole,
                action = action,
                timestamp = System.currentTimeMillis(),
                result = result,
                metadata = metadata
            )
        )
    }

    private fun maskKey(key: String): String {
        return if (key.length >= 10) {
            val prefix = key.take(3)
            val suffix = key.takeLast(4)
            "$prefix-****-****-$suffix"
        } else {
            "FZ-****-****"
        }
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
