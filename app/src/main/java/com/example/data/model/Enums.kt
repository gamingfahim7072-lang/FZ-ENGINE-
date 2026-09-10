package com.example.data.model

enum class LicenseStatus(val displayName: String) {
    LOCKED("Locked"),
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
    REVOKED("Revoked"),
    BANNED("Banned")
}

enum class ServerHealthStatus(val displayName: String, val latencyMs: Int) {
    ONLINE("Online", 28),
    DEGRADED("Degraded", 340),
    MAINTENANCE("Maintenance", 0),
    OFFLINE("Offline", -1)
}

enum class UserRole(val displayName: String) {
    OWNER("Owner / Founder"),
    DEVELOPER("Core Developer"),
    SELLER("Authorized Seller"),
    USER("Standard User"),
    SUPPORT("Customer Support")
}

enum class NotificationCategory(val displayName: String) {
    LICENSE_ACTIVATED("License Activated"),
    LICENSE_EXPIRING("License Expiring Soon"),
    LICENSE_EXPIRED("License Expired"),
    SERVER_MAINTENANCE("Server Maintenance"),
    APP_UPDATE("App Update"),
    ANNOUNCEMENT("Announcement"),
    SUPPORT_RESPONSE("Support Response")
}

enum class RankingPeriod(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time");

    val label: String get() = displayName
}

enum class NavigationTab(val title: String) {
    HOME("Games"),
    PREMIUM("Premium"),
    SELLERS("Sellers"),
    SETTINGS("Settings")
}
