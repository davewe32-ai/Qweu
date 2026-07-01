package com.example.data.repository

import com.example.data.database.UserDao
import com.example.data.database.GemstoneDao
import com.example.data.database.AuditLogDao
import com.example.data.database.SystemSettingsDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.GemstoneEntity
import com.example.data.model.SystemSettingsEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.security.MessageDigest

class GemVaultRepository(
    private val userDao: UserDao,
    private val gemstoneDao: GemstoneDao,
    private val auditLogDao: AuditLogDao,
    private val settingsDao: SystemSettingsDao
) {
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allGemstones: Flow<List<GemstoneEntity>> = gemstoneDao.getAllGemstones()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()
    val settingsFlow: Flow<SystemSettingsEntity?> = settingsDao.getSettingsFlow()

    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password // fallback
        }
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUserByUsername(username: String) {
        userDao.deleteUserByUsername(username)
    }

    suspend fun getGemstoneById(id: Int): GemstoneEntity? {
        return gemstoneDao.getGemstoneById(id)
    }

    suspend fun insertGemstone(gemstone: GemstoneEntity): Long {
        return gemstoneDao.insertGemstone(gemstone)
    }

    suspend fun updateGemstone(gemstone: GemstoneEntity) {
        gemstoneDao.updateGemstone(gemstone)
    }

    suspend fun deleteGemstoneById(id: Int) {
        gemstoneDao.deleteGemstoneById(id)
    }

    suspend fun insertAuditLog(username: String, action: String, details: String) {
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                username = username,
                action = action,
                details = details
            )
        )
    }

    suspend fun clearAuditLogs() {
        auditLogDao.clearAuditLogs()
    }

    suspend fun getSettings(): SystemSettingsEntity {
        return settingsDao.getSettings() ?: SystemSettingsEntity().also {
            settingsDao.insertOrUpdateSettings(it)
        }
    }

    suspend fun updateSettings(settings: SystemSettingsEntity) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    // Seed default data if database is empty
    suspend fun seedDatabaseIfEmpty() {
        // 1. Seed users
        val adminUser = userDao.getUserByUsername("admin")
        if (adminUser == null) {
            userDao.insertUser(
                UserEntity(
                    username = "admin",
                    passwordHash = hashPassword("admin123"),
                    fullName = "Vault Administrator",
                    role = "Administrator"
                )
            )
        }
        val staffUser = userDao.getUserByUsername("staff")
        if (staffUser == null) {
            userDao.insertUser(
                UserEntity(
                    username = "staff",
                    passwordHash = hashPassword("staff123"),
                    fullName = "Senior Gemologist",
                    role = "Staff"
                )
            )
        }

        // 2. Seed system settings
        val currentSettings = settingsDao.getSettings()
        if (currentSettings == null) {
            settingsDao.insertOrUpdateSettings(SystemSettingsEntity())
        } else if (currentSettings.companyLogoUrl != "https://serendibgemstones.com/wp-content/uploads/2025/09/sg-logo-2-footer.webp") {
            settingsDao.insertOrUpdateSettings(currentSettings.copy(companyLogoUrl = "https://serendibgemstones.com/wp-content/uploads/2025/09/sg-logo-2-footer.webp"))
        }

        // 3. Seed some gemstones if empty
        val list = allGemstones.first()
        if (list.isEmpty()) {
            insertSeedGems()
        }
    }

    private suspend fun insertSeedGems() {
        val gems = listOf(
            GemstoneEntity(
                gemId = "GEM-1001",
                stockNumber = "SG-2026-001",
                name = "Ceylon Royal Blue Sapphire",
                species = "Corundum",
                variety = "Sapphire",
                color = "Royal Blue",
                shape = "Oval",
                cut = "Mixed Cut",
                clarity = "VVS1",
                transparency = "Transparent",
                originCountry = "Sri Lanka",
                treatment = "Heat Only",
                isNatural = true,
                weightCarats = 4.82,
                lengthMm = 10.2,
                widthMm = 8.4,
                heightMm = 5.9,
                purchasePrice = 12000.0,
                sellingPrice = 18500.0,
                estimatedValue = 20000.0,
                status = "Available",
                supplier = "Rathnapura Miners Guild",
                buyer = "",
                storageLocation = "Vault A, Cabinet 1, Drawer 2",
                vault = "Vault A",
                cabinet = "Cabinet 1",
                drawer = "Drawer 2",
                tray = "Tray B",
                box = "Box 12",
                pocket = "Pocket A",
                notes = "Exceptional saturation and clarity. Certified by SSEF.",
                certificateNumber = "SSEF-99812",
                laboratoryName = "SSEF Swiss Gemmological Institute",
                certIssueDate = "2026-01-15",
                certExpiryDate = ""
            ),
            GemstoneEntity(
                gemId = "GEM-1002",
                stockNumber = "SG-2026-002",
                name = "Mogok Pigeon Blood Ruby",
                species = "Corundum",
                variety = "Ruby",
                color = "Pigeon Blood Red",
                shape = "Cushion",
                cut = "Brilliant",
                clarity = "VS1",
                transparency = "Transparent",
                originCountry = "Myanmar (Burma)",
                treatment = "None (No Heat)",
                isNatural = true,
                weightCarats = 2.45,
                lengthMm = 7.8,
                widthMm = 7.2,
                heightMm = 4.8,
                purchasePrice = 25000.0,
                sellingPrice = 38000.0,
                estimatedValue = 42000.0,
                status = "Available",
                supplier = "Mogok Valley Gemstones",
                buyer = "",
                storageLocation = "Vault A, Cabinet 1, Drawer 1",
                vault = "Vault A",
                cabinet = "Cabinet 1",
                drawer = "Drawer 1",
                tray = "Tray A",
                box = "Box 1",
                pocket = "Pocket 1",
                notes = "Unheated specimen with superb fire and standard inclusions.",
                certificateNumber = "GUB-2026-104",
                laboratoryName = "Gübelin Gem Lab",
                certIssueDate = "2026-03-22",
                certExpiryDate = ""
            ),
            GemstoneEntity(
                gemId = "GEM-1003",
                stockNumber = "SG-2026-003",
                name = "Muzo Emerald Octagon",
                species = "Beryl",
                variety = "Emerald",
                color = "Deep Vivid Green",
                shape = "Emerald Cut",
                cut = "Step Cut",
                clarity = "SI1",
                transparency = "Semi-Transparent",
                originCountry = "Colombia",
                treatment = "Minor Oil",
                isNatural = true,
                weightCarats = 6.15,
                lengthMm = 12.1,
                widthMm = 10.3,
                heightMm = 7.1,
                purchasePrice = 35000.0,
                sellingPrice = 52000.0,
                estimatedValue = 55000.0,
                status = "Reserved",
                supplier = "Bogota Emerald Syndicate",
                buyer = "Lord Alistair Sterling",
                storageLocation = "Vault B, Cabinet 2, Drawer 1",
                vault = "Vault B",
                cabinet = "Cabinet 2",
                drawer = "Drawer 1",
                tray = "Tray C",
                box = "Box 4",
                pocket = "Pocket 2",
                notes = "Classic Muzo green. Minor cedar wood oil treatment noted.",
                certificateNumber = "GRS-2025-08122",
                laboratoryName = "GRS Gemresearch Swisslab",
                certIssueDate = "2025-11-10",
                certExpiryDate = ""
            ),
            GemstoneEntity(
                gemId = "GEM-1004",
                stockNumber = "SG-2026-004",
                name = "Padparadscha Sapphire Pear",
                species = "Corundum",
                variety = "Sapphire",
                color = "Sunset Pink-Orange",
                shape = "Pear",
                cut = "Mixed",
                clarity = "VVS2",
                transparency = "Transparent",
                originCountry = "Sri Lanka",
                treatment = "None",
                isNatural = true,
                weightCarats = 3.12,
                lengthMm = 9.5,
                widthMm = 6.8,
                heightMm = 4.2,
                purchasePrice = 14000.0,
                sellingPrice = 21000.0,
                estimatedValue = 24000.0,
                status = "Sold",
                supplier = "Elahera Mining Partners",
                buyer = "Amara Jewels Tokyo",
                storageLocation = "Vault C (Delivered)",
                vault = "Vault C",
                cabinet = "",
                drawer = "",
                tray = "",
                box = "",
                pocket = "",
                notes = "Stunning sunset hue with equal pink and orange distribution. Sold to Tokyo client.",
                certificateNumber = "GIA-6628192",
                laboratoryName = "GIA Gemological Institute of America",
                certIssueDate = "2026-02-04",
                certExpiryDate = ""
            )
        )
        for (g in gems) {
            gemstoneDao.insertGemstone(g)
        }
    }
}
