package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.GemVaultViewModel

@Composable
fun LoginScreen(
    viewModel: GemVaultViewModel,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()

    // Pre-fill remember me if saved
    val rememberedUser by viewModel.rememberMeUsername.collectAsState()
    LaunchedEffect(rememberedUser) {
        if (rememberedUser.isNotBlank()) {
            username = rememberedUser
            rememberMe = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Generate a luxury dark gemstone gradient reflection
                val goldGlow = Offset(size.width * 0.8f, size.height * 0.2f)
                val navyGlow = Offset(size.width * 0.2f, size.height * 0.8f)
                
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GoldPrimary.copy(alpha = 0.08f), Color.Transparent),
                        center = goldGlow,
                        radius = size.width * 0.8f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(NavyLight.copy(alpha = 0.15f), Color.Transparent),
                        center = navyGlow,
                        radius = size.width * 0.8f
                    )
                )
            }
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Company Header Logo & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(settings.companyLogoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Serendib Gemstones Logo",
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth(0.6f),
                    contentScale = ContentScale.Fit
                )
                
                Text(
                    text = "GEM VAULT",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.Serif
                )
                
                Text(
                    text = "SECURE INVENTORY MANAGEMENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 2.sp
                )
            }

            // Glassmorphic Login Form
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Secure Portal Access",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (loginError != null) {
                    Text(
                        text = loginError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Authorized Username", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldPrimary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                        .padding(bottom = 12.dp)
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Secure Password", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPassword) "Hide Password" else "Show Password",
                                tint = GoldPrimary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldPrimary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input")
                        .padding(bottom = 12.dp)
                )

                // Remember Me Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it ?: false },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GoldPrimary,
                            uncheckedColor = GoldPrimary.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = "Remember My Username",
                        color = TextLight,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign In Button
                Button(
                    onClick = { viewModel.login(username, password, rememberMe) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "AUTHORIZE ACCESS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Quick Login Helper panel
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderWidth = 1f
            ) {
                Text(
                    text = "DEVELOPMENT PORTAL ASSISTANT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Administrator Portal", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Button(
                            onClick = {
                                username = "admin"
                                password = "admin123"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Auto-Fill Admin", fontSize = 10.sp, color = GoldSecondary)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Staff Operations Portal", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Button(
                            onClick = {
                                username = "staff"
                                password = "staff123"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Auto-Fill Staff", fontSize = 10.sp, color = GoldSecondary)
                        }
                    }
                }
            }
        }
    }
}
