package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String, // simple hashed password for verification
    val fullName: String,
    val role: String, // "Administrator" or "Staff"
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gemstones")
data class GemstoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gemId: String, // Auto-generated like "GEM-1001"
    val stockNumber: String,
    val name: String,
    val species: String,
    val variety: String,
    val color: String,
    val shape: String,
    val cut: String,
    val clarity: String,
    val transparency: String,
    val originCountry: String,
    val treatment: String,
    val isNatural: Boolean = true,
    val weightCarats: Double,
    val lengthMm: Double,
    val widthMm: Double,
    val heightMm: Double,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val estimatedValue: Double,
    val status: String, // "Available", "Reserved", "Sold"
    val supplier: String,
    val buyer: String = "",
    val storageLocation: String, // Calculated string like "Vault A - Cab 3"
    val vault: String = "",
    val cabinet: String = "",
    val drawer: String = "",
    val tray: String = "",
    val box: String = "",
    val pocket: String = "",
    val notes: String = "",
    val images: String = "", // Comma-separated or preset names
    val documents: String = "", // Comma-separated file names
    val certificateNumber: String = "",
    val laboratoryName: String = "",
    val certIssueDate: String = "",
    val certExpiryDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val username: String,
    val action: String, // "Login", "Logout", "Add Gem", "Edit Gem", "Delete Gem", "Password Change", etc.
    val details: String
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val companyName: String = "Serendib Gemstones (Pvt) Ltd.",
    val companyLogoUrl: String = "https://serendibgemstones.com/wp-content/uploads/2025/09/sg-logo-2-footer.webp",
    val address: String = "No. 45, Galle Road, Colombo 03, Sri Lanka",
    val contactDetails: String = "info@serendibgemstones.com | +94 11 234 5678",
    val currency: String = "USD",
    val isDarkMode: Boolean = true
)
