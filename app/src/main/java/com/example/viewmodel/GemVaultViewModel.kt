package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.GemstoneEntity
import com.example.data.model.SystemSettingsEntity
import com.example.data.model.UserEntity
import com.example.data.repository.GemVaultRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class GemVaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GemVaultRepository

    // Exposed States
    val users = MutableStateFlow<List<UserEntity>>(emptyList())
    val gemstones = MutableStateFlow<List<GemstoneEntity>>(emptyList())
    val auditLogs = MutableStateFlow<List<AuditLogEntity>>(emptyList())
    val systemSettings = MutableStateFlow(SystemSettingsEntity())

    // Active User / Auth State
    val currentUser = MutableStateFlow<UserEntity?>(null)
    val loginError = MutableStateFlow<String?>(null)
    val rememberMeUsername = MutableStateFlow("")

    // Inactivity Timeout (Default: 5 minutes = 300,000 ms)
    private var lastInteractionTime = System.currentTimeMillis()
    private val timeoutDurationMs = 5 * 60 * 1000L // 5 mins
    val isSessionTimeoutWarning = MutableStateFlow(false)

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val filterStatus = MutableStateFlow("All") // "All", "Available", "Reserved", "Sold"
    val filterColor = MutableStateFlow("All")
    val filterShape = MutableStateFlow("All")
    val filterSpecies = MutableStateFlow("All")
    val sortBy = MutableStateFlow("id") // "id", "name", "weightCarats", "estimatedValue", "status"
    val sortAscending = MutableStateFlow(false)
    val viewType = MutableStateFlow("Card") // "Card" or "Table"

    // Dialog & UI Control States
    val selectedGemstone = MutableStateFlow<GemstoneEntity?>(null)
    val showAddEditGemDialog = MutableStateFlow(false)
    val isEditingGem = MutableStateFlow(false) // true = edit, false = add
    val activeReportType = MutableStateFlow<String?>(null) // "Inventory", "Valuation", "Sales", "Storage", "Supplier"
    
    // Status notifications
    val uiNotification = MutableStateFlow<String?>(null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GemVaultRepository(
            database.userDao(),
            database.gemstoneDao(),
            database.auditLogDao(),
            database.systemSettingsDao()
        )

        // Seed default database on startup
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            
            // Collect database streams
            launch {
                repository.allUsers.collect { users.value = it }
            }
            launch {
                repository.allGemstones.collect { gemstones.value = it }
            }
            launch {
                repository.allAuditLogs.collect { auditLogs.value = it }
            }
            launch {
                repository.settingsFlow.filterNotNull().collect { systemSettings.value = it }
            }

            // Start inactivity check loop
            launch {
                while (true) {
                    delay(10000) // check every 10 seconds
                    checkSessionTimeout()
                }
            }
        }
    }

    // Record interaction to prevent timeout
    fun onUserInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        if (isSessionTimeoutWarning.value) {
            isSessionTimeoutWarning.value = false
        }
    }

    private fun checkSessionTimeout() {
        val user = currentUser.value
        if (user != null) {
            val elapsed = System.currentTimeMillis() - lastInteractionTime
            if (elapsed >= timeoutDurationMs) {
                logout(isTimeout = true)
            } else if (elapsed >= timeoutDurationMs - 30000) {
                // Warning in last 30 seconds
                isSessionTimeoutWarning.value = true
            }
        }
    }

    // --- Authentication ---
    fun login(usernameInput: String, passwordInput: String, rememberMe: Boolean) {
        viewModelScope.launch {
            onUserInteraction()
            val user = repository.getUserByUsername(usernameInput)
            if (user == null) {
                repository.insertAuditLog("system", "Failed Login", "User '$usernameInput' not found.")
                loginError.value = "Invalid username or password"
                return@launch
            }

            if (!user.isEnabled) {
                repository.insertAuditLog("system", "Failed Login", "User '${user.username}' is disabled.")
                loginError.value = "Account is disabled. Contact administrator."
                return@launch
            }

            val inputHash = repository.hashPassword(passwordInput)
            if (user.passwordHash == inputHash) {
                currentUser.value = user
                loginError.value = null
                if (rememberMe) {
                    rememberMeUsername.value = user.username
                } else {
                    rememberMeUsername.value = ""
                }
                repository.insertAuditLog(user.username, "Login", "Successful login. Role: ${user.role}")
                showNotification("Logged in as ${user.fullName}")
            } else {
                repository.insertAuditLog(user.username, "Failed Login", "Incorrect password provided.")
                loginError.value = "Invalid username or password"
            }
        }
    }

    fun logout(isTimeout: Boolean = false) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                if (isTimeout) {
                    repository.insertAuditLog(user.username, "Session Timeout", "Automatically logged out due to inactivity.")
                    showNotification("Logged out due to inactivity")
                } else {
                    repository.insertAuditLog(user.username, "Logout", "User initiated logout.")
                    showNotification("Logged out successfully")
                }
            }
            currentUser.value = null
        }
    }

    // --- My Account ---
    fun changePassword(currentPass: String, newPass: String, confirmPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            onUserInteraction()
            val user = currentUser.value
            if (user == null) {
                onResult(false, "No active user session")
                return@launch
            }

            if (newPass != confirmPass) {
                onResult(false, "New passwords do not match")
                return@launch
            }

            val fullUser = repository.getUserByUsername(user.username) ?: return@launch
            val currentHash = repository.hashPassword(currentPass)
            if (fullUser.passwordHash != currentHash) {
                repository.insertAuditLog(user.username, "Password Change Fail", "Incorrect current password entered.")
                onResult(false, "Incorrect current password")
                return@launch
            }

            val newHash = repository.hashPassword(newPass)
            val updatedUser = fullUser.copy(passwordHash = newHash)
            repository.updateUser(updatedUser)
            currentUser.value = updatedUser // update local cache

            repository.insertAuditLog(user.username, "Password Change", "Password changed successfully.")
            showNotification("Password changed successfully")
            onResult(true, "Password updated successfully!")
        }
    }

    // --- Admin User Management ---
    fun adminCreateUser(usernameInput: String, passwordInput: String, fullNameInput: String, roleInput: String) {
        viewModelScope.launch {
            onUserInteraction()
            val admin = currentUser.value ?: return@launch
            if (admin.role != "Administrator") return@launch

            if (usernameInput.isBlank() || passwordInput.isBlank() || fullNameInput.isBlank()) {
                showNotification("Please fill in all fields")
                return@launch
            }

            val existing = repository.getUserByUsername(usernameInput)
            if (existing != null) {
                showNotification("Username '$usernameInput' already exists")
                return@launch
            }

            val hash = repository.hashPassword(passwordInput)
            val newUser = UserEntity(
                username = usernameInput.trim().lowercase(),
                passwordHash = hash,
                fullName = fullNameInput.trim(),
                role = roleInput
            )
            repository.insertUser(newUser)
            repository.insertAuditLog(admin.username, "New User Creation", "Created staff user '${newUser.username}'.")
            showNotification("User '${newUser.username}' created successfully")
        }
    }

    fun adminToggleUserStatus(usernameInput: String) {
        viewModelScope.launch {
            onUserInteraction()
            val admin = currentUser.value ?: return@launch
            if (admin.role != "Administrator") return@launch
            if (usernameInput == admin.username) {
                showNotification("Cannot disable yourself")
                return@launch
            }

            val user = repository.getUserByUsername(usernameInput) ?: return@launch
            val updated = user.copy(isEnabled = !user.isEnabled)
            repository.updateUser(updated)

            val action = if (updated.isEnabled) "Enabled User" else "Disabled User"
            repository.insertAuditLog(admin.username, action, "$action '${user.username}'.")
            showNotification("User '${user.username}' status updated")
        }
    }

    fun adminChangeUserRole(usernameInput: String, newRole: String) {
        viewModelScope.launch {
            onUserInteraction()
            val admin = currentUser.value ?: return@launch
            if (admin.role != "Administrator") return@launch
            if (usernameInput == admin.username) {
                showNotification("Cannot change your own role")
                return@launch
            }

            val user = repository.getUserByUsername(usernameInput) ?: return@launch
            val updated = user.copy(role = newRole)
            repository.updateUser(updated)

            repository.insertAuditLog(admin.username, "Role Update", "Updated role of '${user.username}' to $newRole.")
            showNotification("User '${user.username}' role updated to $newRole")
        }
    }

    fun adminResetPassword(usernameInput: String, newPass: String) {
        viewModelScope.launch {
            onUserInteraction()
            val admin = currentUser.value ?: return@launch
            if (admin.role != "Administrator") return@launch

            val user = repository.getUserByUsername(usernameInput) ?: return@launch
            val hash = repository.hashPassword(newPass)
            val updated = user.copy(passwordHash = hash)
            repository.updateUser(updated)

            repository.insertAuditLog(admin.username, "Password Reset", "Reset password for '${user.username}'.")
            showNotification("Password for '${user.username}' reset successfully")
        }
    }

    fun adminDeleteUser(usernameInput: String) {
        viewModelScope.launch {
            onUserInteraction()
            val admin = currentUser.value ?: return@launch
            if (admin.role != "Administrator") return@launch
            if (usernameInput == admin.username) {
                showNotification("Cannot delete yourself")
                return@launch
            }

            repository.deleteUserByUsername(usernameInput)
            repository.insertAuditLog(admin.username, "Delete User", "Deleted user '$usernameInput'.")
            showNotification("User '$usernameInput' deleted")
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            onUserInteraction()
            val user = currentUser.value ?: return@launch
            if (user.role != "Administrator") return@launch

            repository.clearAuditLogs()
            repository.insertAuditLog(user.username, "Clear Audits", "Cleared all security audit log history.")
            showNotification("Audit logs history cleared")
        }
    }

    // --- Gemstone CRUD ---
    fun saveGemstone(gem: GemstoneEntity) {
        viewModelScope.launch {
            onUserInteraction()
            val user = currentUser.value ?: return@launch
            
            val isEdit = gem.id != 0
            if (isEdit) {
                repository.updateGemstone(gem)
                repository.insertAuditLog(user.username, "Gem Edit", "Updated Gem ${gem.gemId} (${gem.name})")
                showNotification("Gemstone ${gem.gemId} updated")
            } else {
                // Generate automated stock ID and custom ID if missing
                val prefix = "GEM-"
                val maxId = gemstones.value.maxOfOrNull { it.id } ?: 1000
                val nextGemId = "$prefix${maxId + 1}"
                
                val finalGem = gem.copy(
                    gemId = nextGemId,
                    storageLocation = calculateStorageLocation(gem)
                )
                repository.insertGemstone(finalGem)
                repository.insertAuditLog(user.username, "Gem Addition", "Added Gem ${finalGem.gemId} (${finalGem.name})")
                showNotification("Gemstone ${finalGem.gemId} added")
            }
            showAddEditGemDialog.value = false
            selectedGemstone.value = null
        }
    }

    fun deleteGemstone(gem: GemstoneEntity) {
        viewModelScope.launch {
            onUserInteraction()
            val user = currentUser.value ?: return@launch
            if (user.role != "Administrator") {
                showNotification("Only Administrators can delete gemstones")
                return@launch
            }

            repository.deleteGemstoneById(gem.id)
            repository.insertAuditLog(user.username, "Gem Deletion", "Deleted Gem ${gem.gemId} (${gem.name})")
            showNotification("Gemstone ${gem.gemId} deleted")
            if (selectedGemstone.value?.id == gem.id) {
                selectedGemstone.value = null
            }
        }
    }

    private fun calculateStorageLocation(gem: GemstoneEntity): String {
        val parts = mutableListOf<String>()
        if (gem.vault.isNotBlank()) parts.add(gem.vault)
        if (gem.cabinet.isNotBlank()) parts.add("Cab ${gem.cabinet}")
        if (gem.drawer.isNotBlank()) parts.add("Drw ${gem.drawer}")
        if (gem.tray.isNotBlank()) parts.add("Try ${gem.tray}")
        if (gem.box.isNotBlank()) parts.add("Box ${gem.box}")
        if (gem.pocket.isNotBlank()) parts.add("Pkt ${gem.pocket}")
        return if (parts.isEmpty()) "Unassigned" else parts.joinToString(" - ")
    }

    // Simulate Image/Doc uploading
    fun uploadFileToGemstone(gemId: String, fileName: String, fileType: String) {
        viewModelScope.launch {
            onUserInteraction()
            val user = currentUser.value ?: return@launch
            val gem = gemstones.value.firstOrNull { it.gemId == gemId } ?: return@launch
            
            val updated = if (fileType == "Image") {
                val currentImages = gem.images
                val newImages = if (currentImages.isBlank()) fileName else "$currentImages,$fileName"
                gem.copy(images = newImages)
            } else {
                val currentDocs = gem.documents
                val newDocs = if (currentDocs.isBlank()) fileName else "$currentDocs,$fileName"
                gem.copy(documents = newDocs)
            }
            
            repository.updateGemstone(updated)
            repository.insertAuditLog(user.username, "File Upload", "Uploaded $fileType '$fileName' for Gem $gemId")
            showNotification("$fileType uploaded successfully")
            // refresh selected gem
            if (selectedGemstone.value?.gemId == gemId) {
                selectedGemstone.value = updated
            }
        }
    }

    // --- Settings & Themes ---
    fun updateSystemSettings(
        companyName: String,
        address: String,
        contact: String,
        currency: String,
        logoUrl: String,
        isDarkMode: Boolean
    ) {
        viewModelScope.launch {
            onUserInteraction()
            val user = currentUser.value ?: return@launch
            if (user.role != "Administrator") return@launch

            val updated = SystemSettingsEntity(
                companyName = companyName,
                address = address,
                contactDetails = contact,
                currency = currency,
                companyLogoUrl = logoUrl,
                isDarkMode = isDarkMode
            )
            repository.updateSettings(updated)
            repository.insertAuditLog(user.username, "Settings Update", "Updated company system configuration parameters.")
            showNotification("System settings saved successfully")
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            onUserInteraction()
            val current = systemSettings.value
            val updated = current.copy(isDarkMode = !current.isDarkMode)
            repository.updateSettings(updated)
            showNotification("Theme switched")
        }
    }

    // --- Search & Filtering engine ---
    val filteredGemstones: StateFlow<List<GemstoneEntity>> = combine(
        gemstones, searchQuery, filterStatus, filterColor, filterShape, filterSpecies, sortBy, sortAscending
    ) { flowsArray ->
        @Suppress("UNCHECKED_CAST")
        val gemsList = flowsArray[0] as List<GemstoneEntity>
        val query = flowsArray[1] as String
        val status = flowsArray[2] as String
        val color = flowsArray[3] as String
        val shape = flowsArray[4] as String
        val species = flowsArray[5] as String
        val sortCol = flowsArray[6] as String
        val ascending = flowsArray[7] as Boolean

        var list = gemsList

        // Query Search
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.gemId.lowercase().contains(q) ||
                it.certificateNumber.lowercase().contains(q) ||
                it.color.lowercase().contains(q) ||
                it.originCountry.lowercase().contains(q) ||
                it.shape.lowercase().contains(q) ||
                it.species.lowercase().contains(q) ||
                it.supplier.lowercase().contains(q) ||
                it.buyer.lowercase().contains(q) ||
                it.storageLocation.lowercase().contains(q)
            }
        }

        // Status Filter
        if (status != "All") {
            list = list.filter { it.status.equals(status, ignoreCase = true) }
        }

        // Color Filter
        if (color != "All") {
            list = list.filter { it.color.contains(color, ignoreCase = true) }
        }

        // Shape Filter
        if (shape != "All") {
            list = list.filter { it.shape.contains(shape, ignoreCase = true) }
        }

        // Species Filter
        if (species != "All") {
            list = list.filter { it.species.contains(species, ignoreCase = true) }
        }

        // Sorting
        val sortedList = when (sortCol) {
            "name" -> list.sortedBy { it.name }
            "weightCarats" -> list.sortedBy { it.weightCarats }
            "estimatedValue" -> list.sortedBy { it.estimatedValue }
            "status" -> list.sortedBy { it.status }
            else -> list.sortedBy { it.id } // ID
        }

        if (ascending) sortedList else sortedList.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique Categories for filters
    val availableColors: StateFlow<List<String>> = gemstones.map { list ->
        listOf("All") + list.map { it.color }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val availableShapes: StateFlow<List<String>> = gemstones.map { list ->
        listOf("All") + list.map { it.shape }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val availableSpecies: StateFlow<List<String>> = gemstones.map { list ->
        listOf("All") + list.map { it.species }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // --- Dashboard Statistics ---
    val dashboardStats: StateFlow<DashboardStats> = gemstones.map { list ->
        val totalGems = list.size
        val totalCarats = list.sumOf { it.weightCarats }
        val availableGems = list.count { it.status.equals("Available", ignoreCase = true) }
        val reservedGems = list.count { it.status.equals("Reserved", ignoreCase = true) }
        val soldGems = list.count { it.status.equals("Sold", ignoreCase = true) }
        val estimatedValue = list.sumOf { it.estimatedValue }
        
        // Distribution for Charts
        val statusDistribution = list.groupBy { it.status }.mapValues { it.value.size }
        val valueDistributionByOrigin = list.groupBy { it.originCountry }.mapValues { it.value.sumOf { g -> g.estimatedValue } }

        DashboardStats(
            totalGems = totalGems,
            totalCarats = totalCarats,
            availableGems = availableGems,
            reservedGems = reservedGems,
            soldGems = soldGems,
            estimatedValue = estimatedValue,
            statusDistribution = statusDistribution,
            valueByOrigin = valueDistributionByOrigin
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // --- Export, Backup & Restore ---
    fun exportToCSV(): String {
        onUserInteraction()
        val sb = java.lang.StringBuilder()
        sb.append("Gem ID,Stock Number,Name,Species,Variety,Color,Shape,Weight(Carats),Estimated Value,Status,Location,Certificate\n")
        for (g in gemstones.value) {
            sb.append("\"${g.gemId}\",\"${g.stockNumber}\",\"${g.name}\",\"${g.species}\",\"${g.variety}\",\"${g.color}\",\"${g.shape}\",${g.weightCarats},${g.estimatedValue},\"${g.status}\",\"${g.storageLocation}\",\"${g.certificateNumber}\"\n")
        }
        viewModelScope.launch {
            val user = currentUser.value?.username ?: "system"
            repository.insertAuditLog(user, "CSV Export", "Exported complete gemstone inventory list to CSV.")
        }
        return sb.toString()
    }

    fun backupDatabase(): String {
        onUserInteraction()
        val root = JSONObject()
        try {
            // Backup Gemstones
            val gemsArray = JSONArray()
            for (g in gemstones.value) {
                val item = JSONObject()
                item.put("gemId", g.gemId)
                item.put("stockNumber", g.stockNumber)
                item.put("name", g.name)
                item.put("species", g.species)
                item.put("variety", g.variety)
                item.put("color", g.color)
                item.put("shape", g.shape)
                item.put("cut", g.cut)
                item.put("clarity", g.clarity)
                item.put("transparency", g.transparency)
                item.put("originCountry", g.originCountry)
                item.put("treatment", g.treatment)
                item.put("isNatural", g.isNatural)
                item.put("weightCarats", g.weightCarats)
                item.put("lengthMm", g.lengthMm)
                item.put("widthMm", g.widthMm)
                item.put("heightMm", g.heightMm)
                item.put("purchasePrice", g.purchasePrice)
                item.put("sellingPrice", g.sellingPrice)
                item.put("estimatedValue", g.estimatedValue)
                item.put("status", g.status)
                item.put("supplier", g.supplier)
                item.put("buyer", g.buyer)
                item.put("storageLocation", g.storageLocation)
                item.put("vault", g.vault)
                item.put("cabinet", g.cabinet)
                item.put("drawer", g.drawer)
                item.put("tray", g.tray)
                item.put("box", g.box)
                item.put("pocket", g.pocket)
                item.put("notes", g.notes)
                item.put("images", g.images)
                item.put("documents", g.documents)
                item.put("certificateNumber", g.certificateNumber)
                item.put("laboratoryName", g.laboratoryName)
                item.put("certIssueDate", g.certIssueDate)
                item.put("certExpiryDate", g.certExpiryDate)
                gemsArray.put(item)
            }
            root.put("gemstones", gemsArray)
            
            // Backup users (excluding passwords for safety or include hashed passwords)
            val usersArray = JSONArray()
            for (u in users.value) {
                val item = JSONObject()
                item.put("username", u.username)
                item.put("passwordHash", u.passwordHash)
                item.put("fullName", u.fullName)
                item.put("role", u.role)
                item.put("isEnabled", u.isEnabled)
                usersArray.put(item)
            }
            root.put("users", usersArray)
            root.put("timestamp", System.currentTimeMillis())

            viewModelScope.launch {
                val user = currentUser.value?.username ?: "system"
                repository.insertAuditLog(user, "Manual Backup", "Database successfully backed up as JSON string.")
            }
            showNotification("Backup generated successfully")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return root.toString()
    }

    fun restoreDatabase(jsonString: String): Boolean {
        onUserInteraction()
        if (jsonString.isBlank()) return false
        try {
            val root = JSONObject(jsonString)
            val gemsArray = root.optJSONArray("gemstones")
            val usersArray = root.optJSONArray("users")
            
            viewModelScope.launch {
                // Restore users
                if (usersArray != null) {
                    for (i in 0 until usersArray.length()) {
                        val obj = usersArray.getJSONObject(i)
                        val u = UserEntity(
                            username = obj.getString("username"),
                            passwordHash = obj.getString("passwordHash"),
                            fullName = obj.getString("fullName"),
                            role = obj.getString("role"),
                            isEnabled = obj.optBoolean("isEnabled", true)
                        )
                        repository.insertUser(u)
                    }
                }
                
                // Restore Gemstones
                if (gemsArray != null) {
                    for (i in 0 until gemsArray.length()) {
                        val obj = gemsArray.getJSONObject(i)
                        val gemId = obj.getString("gemId")
                        // see if exists
                        val existing = gemstones.value.firstOrNull { it.gemId == gemId }
                        val g = GemstoneEntity(
                            id = existing?.id ?: 0,
                            gemId = gemId,
                            stockNumber = obj.getString("stockNumber"),
                            name = obj.getString("name"),
                            species = obj.getString("species"),
                            variety = obj.getString("variety"),
                            color = obj.getString("color"),
                            shape = obj.getString("shape"),
                            cut = obj.getString("cut"),
                            clarity = obj.getString("clarity"),
                            transparency = obj.getString("transparency"),
                            originCountry = obj.getString("originCountry"),
                            treatment = obj.getString("treatment"),
                            isNatural = obj.optBoolean("isNatural", true),
                            weightCarats = obj.getDouble("weightCarats"),
                            lengthMm = obj.getDouble("lengthMm"),
                            widthMm = obj.getDouble("widthMm"),
                            heightMm = obj.getDouble("heightMm"),
                            purchasePrice = obj.getDouble("purchasePrice"),
                            sellingPrice = obj.getDouble("sellingPrice"),
                            estimatedValue = obj.getDouble("estimatedValue"),
                            status = obj.getString("status"),
                            supplier = obj.getString("supplier"),
                            buyer = obj.optString("buyer", ""),
                            storageLocation = obj.getString("storageLocation"),
                            vault = obj.optString("vault", ""),
                            cabinet = obj.optString("cabinet", ""),
                            drawer = obj.optString("drawer", ""),
                            tray = obj.optString("tray", ""),
                            box = obj.optString("box", ""),
                            pocket = obj.optString("pocket", ""),
                            notes = obj.optString("notes", ""),
                            images = obj.optString("images", ""),
                            documents = obj.optString("documents", ""),
                            certificateNumber = obj.optString("certificateNumber", ""),
                            laboratoryName = obj.optString("laboratoryName", ""),
                            certIssueDate = obj.optString("certIssueDate", ""),
                            certExpiryDate = obj.optString("certExpiryDate", "")
                        )
                        repository.insertGemstone(g)
                    }
                }
                val user = currentUser.value?.username ?: "system"
                repository.insertAuditLog(user, "Manual Restore", "Database restored successfully from backup.")
                showNotification("Database restored successfully")
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            showNotification("Failed to restore: invalid JSON backup format")
            return false
        }
    }

    private fun showNotification(msg: String) {
        viewModelScope.launch {
            uiNotification.value = msg
            delay(3000)
            if (uiNotification.value == msg) {
                uiNotification.value = null
            }
        }
    }

    fun dismissNotification() {
        uiNotification.value = null
    }
}

data class DashboardStats(
    val totalGems: Int = 0,
    val totalCarats: Double = 0.0,
    val availableGems: Int = 0,
    val reservedGems: Int = 0,
    val soldGems: Int = 0,
    val estimatedValue: Double = 0.0,
    val statusDistribution: Map<String, Int> = emptyMap(),
    val valueByOrigin: Map<String, Double> = emptyMap()
)
