package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.LicenseEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.SellerEntity
import com.example.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FzEngineDao {
    // License
    @Query("SELECT * FROM licenses ORDER BY createdAt DESC LIMIT 1")
    fun getActiveLicenseFlow(): Flow<LicenseEntity?>

    @Query("SELECT * FROM licenses WHERE id = :id")
    suspend fun getLicenseById(id: String): LicenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLicense(license: LicenseEntity)

    @Query("UPDATE licenses SET status = :status WHERE id = :id")
    suspend fun updateLicenseStatus(id: String, status: String)

    @Query("DELETE FROM licenses")
    suspend fun clearLicenses()

    // Sellers
    @Query("SELECT * FROM sellers ORDER BY totalSales DESC")
    fun getAllSellersFlow(): Flow<List<SellerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSellers(sellers: List<SellerEntity>)

    @Query("SELECT COUNT(*) FROM sellers")
    suspend fun getSellersCount(): Int

    // Sessions
    @Query("SELECT * FROM sessions WHERE isRevoked = 0 ORDER BY isCurrent DESC, lastSeen DESC")
    fun getActiveSessionsFlow(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("UPDATE sessions SET isRevoked = 1 WHERE isCurrent = 0")
    suspend fun revokeOtherSessions()

    @Query("UPDATE sessions SET isRevoked = 1 WHERE isCurrent = 1")
    suspend fun revokeCurrentSession()

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAuditLogs()

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationsCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()
}
