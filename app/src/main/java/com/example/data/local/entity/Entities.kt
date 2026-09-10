package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "licenses")
data class LicenseEntity(
    @PrimaryKey val id: String,
    val keyHash: String,
    val maskedKey: String,
    val plan: String,
    val durationDays: Int,
    val status: String,
    val createdAt: Long,
    val activatedAt: Long?,
    val expiresAt: Long?,
    val revokedAt: Long?,
    val deviceBinding: String?,
    val sellerId: String?,
    val sellerName: String?,
    val activationCount: Int,
    val maxActivations: Int
)

@Entity(tableName = "sellers")
data class SellerEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val country: String,
    val countryCode: String,
    val avatarTag: String,
    val verificationStatus: Boolean,
    val totalSales: Int,
    val amountSoldUsd: Double,
    val publishedContactMethod: String,
    val rating: Float,
    val createdAt: Long,
    val territory: String
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val deviceName: String,
    val deviceIdentifierHash: String,
    val platform: String,
    val appVersion: String,
    val createdAt: Long,
    val lastSeen: Long,
    val expiresAt: Long,
    val isCurrent: Boolean,
    val isRevoked: Boolean
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorId: String,
    val actorRole: String,
    val action: String,
    val timestamp: Long,
    val result: String,
    val metadata: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val category: String,
    val timestamp: Long,
    val isRead: Boolean
)
