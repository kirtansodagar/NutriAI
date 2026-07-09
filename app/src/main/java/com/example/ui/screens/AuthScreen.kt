package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.NutriViewModel
import kotlinx.coroutines.delay

private val HdBackground = Color(0xFF0F172A)
private val HdCardBg = Color(0xFF1E293B)
private val HdBorderColor = Color(0xFF334155)
private val HdPrimaryPurple = Color(0xFF10B981) // EmeraldMint theme accent
private val HdDarkText = Color(0xFFF8FAFC)
private val HdSecondaryText = Color(0xFF94A3B8)
private val HdErrorRed = Color(0xFFEF4444)

@Composable
fun AuthScreen(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authUiState by viewModel.authUiState.collectAsState()
    val activeOtpCode by viewModel.activeOtpCode.collectAsState()
    val otpPhoneNumber by viewModel.otpPhoneNumber.collectAsState()

    var isLoginMode by remember { mutableStateOf(true) }
    var usePhoneOtpLogin by remember { mutableStateOf(false) }

    // Password credentials inputs
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Phone OTP inputs
    var otpPhoneInput by remember { mutableStateOf("") }
    var otpCodeInput by remember { mutableStateOf("") }
    var resendCountdown by remember { mutableStateOf(0) }
    var isSendingOtp by remember { mutableStateOf(false) }

    // Registration inputs
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    // Floating Mock SMS notification banner
    var showMockSmsBanner by remember { mutableStateOf(false) }
    var receivedSmsCode by remember { mutableStateOf("") }

    // Reset fields on toggle
    LaunchedEffect(isLoginMode, usePhoneOtpLogin) {
        viewModel.clearAuthError()
        otpCodeInput = ""
        showMockSmsBanner = false
    }

    // Handle timer decrement for OTP resending limits
    LaunchedEffect(resendCountdown) {
        if (resendCountdown > 0) {
            delay(1000)
            resendCountdown -= 1
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HdBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Identity Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(HdPrimaryPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "NutriAI Logo",
                    tint = HdPrimaryPurple,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "NutriAI Secure Hub",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = HdDarkText,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = if (isLoginMode) "Access calorie metrics, custom scans, and coach safe rooms" else "Generate dynamic offline cryptographic account profiles",
                style = MaterialTheme.typography.bodyMedium.copy(color = HdSecondaryText),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic Action Tabs for switching Standard vs OTP inside Login Mode
            if (isLoginMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HdCardBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (!usePhoneOtpLogin) HdPrimaryPurple else Color.Transparent)
                            .clickable { usePhoneOtpLogin = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Email & Pass",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (!usePhoneOtpLogin) HdBackground else HdSecondaryText
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (usePhoneOtpLogin) HdPrimaryPurple else Color.Transparent)
                            .clickable { usePhoneOtpLogin = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Phone OTP",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (usePhoneOtpLogin) HdBackground else HdSecondaryText
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error & Status Alert Cards
            AnimatedVisibility(
                visible = authUiState is AuthUiState.Error,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                val errorMsg = (authUiState as? AuthUiState.Error)?.message ?: ""
                Card(
                    colors = CardDefaults.cardColors(containerColor = HdErrorRed.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, HdErrorRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMsg,
                        color = HdErrorRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Interactive Mock SMS Floating Notification Banner
            AnimatedVisibility(
                visible = showMockSmsBanner && isLoginMode && usePhoneOtpLogin,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7)),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            otpCodeInput = receivedSmsCode
                            showMockSmsBanner = false
                            Toast
                                .makeText(context, "OTP Autofilled!", Toast.LENGTH_SHORT)
                                .show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS OTP Notification",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Carrier SMS: [NutriAI Security]",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Your verification OTP is $receivedSmsCode. Tap to copy and autofill.",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Unified Inputs Container
            Card(
                colors = CardDefaults.cardColors(containerColor = HdCardBg),
                border = BorderStroke(1.dp, HdBorderColor),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    if (isLoginMode) {
                        if (!usePhoneOtpLogin) {
                            // EMAIL / USERNAME / PHONE LOGIN
                            Text(
                                text = "Multi-Identifier Secure Login",
                                style = MaterialTheme.typography.titleSmall.copy(color = HdDarkText, fontWeight = FontWeight.Bold)
                            )

                            OutlinedTextField(
                                value = loginIdentifier,
                                onValueChange = { loginIdentifier = it },
                                label = { Text("Username, Email or Phone", color = HdSecondaryText) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = HdSecondaryText
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = HdDarkText,
                                    unfocusedTextColor = HdDarkText,
                                    focusedBorderColor = HdPrimaryPurple,
                                    unfocusedBorderColor = HdBorderColor,
                                    focusedContainerColor = HdBackground,
                                    unfocusedContainerColor = HdBackground
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("username_input")
                            )

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = { Text("Enter Password", color = HdSecondaryText) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = HdSecondaryText
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Visibility",
                                            tint = HdSecondaryText
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = HdDarkText,
                                    unfocusedTextColor = HdDarkText,
                                    focusedBorderColor = HdPrimaryPurple,
                                    unfocusedBorderColor = HdBorderColor,
                                    focusedContainerColor = HdBackground,
                                    unfocusedContainerColor = HdBackground
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input")
                            )

                        } else {
                            // PHONE OTP LOGIN
                            Text(
                                text = "Direct SMS One-Time Passcode",
                                style = MaterialTheme.typography.titleSmall.copy(color = HdDarkText, fontWeight = FontWeight.Bold)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = otpPhoneInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' || it == '.' }) otpPhoneInput = input
                                    },
                                    label = { Text("Phone Number", color = HdSecondaryText) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = HdSecondaryText
                                        )
                                    },
                                    placeholder = { Text("e.g. +1234567890", color = HdSecondaryText.copy(alpha = 0.5f)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = HdDarkText,
                                        unfocusedTextColor = HdDarkText,
                                        focusedBorderColor = HdPrimaryPurple,
                                        unfocusedBorderColor = HdBorderColor,
                                        focusedContainerColor = HdBackground,
                                        unfocusedContainerColor = HdBackground
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1.3f)
                                )

                                Button(
                                    onClick = {
                                        isSendingOtp = true
                                        viewModel.requestOtp(otpPhoneInput) { success, result ->
                                            isSendingOtp = false
                                            if (success) {
                                                receivedSmsCode = result
                                                showMockSmsBanner = true
                                                resendCountdown = 60
                                                Toast.makeText(context, "OTP Code dispatched successfully via Carrier!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    enabled = otpPhoneInput.length >= 7 && resendCountdown == 0 && !isSendingOtp,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HdPrimaryPurple,
                                        disabledContainerColor = HdBorderColor
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .height(54.dp)
                                        .weight(1f)
                                ) {
                                    if (isSendingOtp) {
                                        CircularProgressIndicator(color = HdBackground, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text(
                                            text = if (resendCountdown > 0) "Resend (${resendCountdown}s)" else "Send OTP",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (resendCountdown > 0) HdSecondaryText else HdBackground
                                            )
                                        )
                                    }
                                }
                            }

                            // OTP Verification code input
                            AnimatedVisibility(visible = otpPhoneNumber != null) {
                                OutlinedTextField(
                                    value = otpCodeInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() } && input.length <= 6) {
                                            otpCodeInput = input
                                        }
                                    },
                                    label = { Text("6-Digit OTP Verification Passcode", color = HdSecondaryText) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.VpnKey,
                                            contentDescription = null,
                                            tint = HdSecondaryText
                                        )
                                    },
                                    placeholder = { Text("000000", color = HdSecondaryText.copy(alpha = 0.4f)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = HdDarkText,
                                        unfocusedTextColor = HdDarkText,
                                        focusedBorderColor = HdPrimaryPurple,
                                        unfocusedBorderColor = HdBorderColor,
                                        focusedContainerColor = HdBackground,
                                        unfocusedContainerColor = HdBackground
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Keep me signed in Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = HdPrimaryPurple,
                                    uncheckedColor = HdSecondaryText,
                                    checkmarkColor = HdBackground
                                )
                            )
                            Text(
                                text = "Keep me logged in on this device",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HdDarkText
                            )
                        }

                    } else {
                        // REGISTRATION FORM
                        Text(
                            text = "Register Secure Client Profile",
                            style = MaterialTheme.typography.titleSmall.copy(color = HdDarkText, fontWeight = FontWeight.Bold)
                        )

                        // Username Input
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Choose Username", color = HdSecondaryText) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = HdSecondaryText
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HdDarkText,
                                unfocusedTextColor = HdDarkText,
                                focusedBorderColor = HdPrimaryPurple,
                                unfocusedBorderColor = HdBorderColor,
                                focusedContainerColor = HdBackground,
                                unfocusedContainerColor = HdBackground
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Email Input
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email Address", color = HdSecondaryText) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = HdSecondaryText
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HdDarkText,
                                unfocusedTextColor = HdDarkText,
                                focusedBorderColor = HdPrimaryPurple,
                                unfocusedBorderColor = HdBorderColor,
                                focusedContainerColor = HdBackground,
                                unfocusedContainerColor = HdBackground
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Phone Number Input
                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' || it == '.' }) regPhone = input
                            },
                            label = { Text("Phone Number", color = HdSecondaryText) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = HdSecondaryText
                                )
                            },
                            placeholder = { Text("e.g. +1234567890", color = HdSecondaryText.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HdDarkText,
                                unfocusedTextColor = HdDarkText,
                                focusedBorderColor = HdPrimaryPurple,
                                unfocusedBorderColor = HdBorderColor,
                                focusedContainerColor = HdBackground,
                                unfocusedContainerColor = HdBackground
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Password Input
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password", color = HdSecondaryText) },
                            supportingText = {
                                Text(
                                    text = "Must be at least 6 characters and contain both letters and numbers.",
                                    color = HdSecondaryText,
                                    fontSize = 11.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = HdSecondaryText
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = HdSecondaryText
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HdDarkText,
                                unfocusedTextColor = HdDarkText,
                                focusedBorderColor = HdPrimaryPurple,
                                unfocusedBorderColor = HdBorderColor,
                                focusedContainerColor = HdBackground,
                                unfocusedContainerColor = HdBackground
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Confirm Password Input
                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            label = { Text("Confirm Password", color = HdSecondaryText) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = HdSecondaryText
                                )
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HdDarkText,
                                unfocusedTextColor = HdDarkText,
                                focusedBorderColor = HdPrimaryPurple,
                                unfocusedBorderColor = HdBorderColor,
                                focusedContainerColor = HdBackground,
                                unfocusedContainerColor = HdBackground
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Passcode Strength Indicator Meter
                        val lengthOk = regPassword.length >= 6
                        val contentOk = regPassword.any { it.isDigit() } && regPassword.any { it.isLetter() }
                        val isStrong = lengthOk && contentOk

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (regPassword.isEmpty()) HdBorderColor else if (isStrong) HdPrimaryPurple else HdErrorRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (regPassword.isEmpty()) "Empty" else if (isStrong) "Strong Passcode" else "Needs Letter & Number (Min 6)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (regPassword.isEmpty()) HdSecondaryText else if (isStrong) HdPrimaryPurple else HdErrorRed,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // PRIMARY SUBMIT ACTION BUTTON
                    Button(
                        onClick = {
                            viewModel.clearAuthError()
                            if (isLoginMode) {
                                if (!usePhoneOtpLogin) {
                                    // Standard Passcode Login
                                    viewModel.loginUser(loginIdentifier, loginPassword, rememberMe) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // SMS OTP Verification Login
                                    val currentExpected = activeOtpCode
                                    if (currentExpected == null) {
                                        Toast.makeText(context, "Please request an OTP first.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (otpCodeInput.trim().length != 6) {
                                        Toast.makeText(context, "Verification passcode must be exactly 6 digits.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.verifyOtp(otpCodeInput, rememberMe) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // Profile Creation / Registration
                                if (regPassword != regConfirmPassword) {
                                    Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.registerUser(
                                    username = regUsername,
                                    email = regEmail,
                                    phoneNumber = regPhone,
                                    password = regPassword
                                ) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        isLoginMode = true
                                        usePhoneOtpLogin = false
                                        loginIdentifier = regUsername
                                        loginPassword = ""
                                        regConfirmPassword = ""
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HdPrimaryPurple),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = if (isLoginMode) {
                                if (usePhoneOtpLogin) "Verify Code & Sign In" else "Sign In Safely"
                            } else {
                                "Create Profile Locally"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = HdBackground
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch Screen Modes (Login vs Registration) Toggle Button
            TextButton(
                onClick = {
                    isLoginMode = !isLoginMode
                    viewModel.clearAuthError()
                }
            ) {
                Text(
                    text = if (isLoginMode) "New to NutriAI? Create your Secure Profile" else "Already have a registered profile? Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HdPrimaryPurple
                    )
                )
            }
        }
    }
}

@Composable
fun PinLockScreen(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pinEntered by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val user by viewModel.currentUser.collectAsState()
    val username = user?.username ?: "User"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HdBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(HdPrimaryPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Device Locked",
                    tint = HdPrimaryPurple,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Device Passcode Required",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = HdDarkText
                )
            )

            Text(
                text = "Welcome back, $username. Unlock to access your records.",
                style = MaterialTheme.typography.bodyMedium.copy(color = HdSecondaryText),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Dot indicators for passcode
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val active = i < pinEntered.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (pinError) HdErrorRed
                                else if (active) HdPrimaryPurple
                                else HdBorderColor
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Beautiful Numpad Layout
            Column(
                modifier = Modifier.width(280.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val numRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("Bio", "0", "Del")
                )

                numRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { value ->
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(HdCardBg)
                                    .clickable {
                                        pinError = false
                                        when (value) {
                                            "Del" -> {
                                                if (pinEntered.isNotEmpty()) {
                                                    pinEntered = pinEntered.dropLast(1)
                                                }
                                            }
                                            "Bio" -> {
                                                if (user?.biometricEnabled == true) {
                                                    viewModel.unlockWithBiometrics()
                                                    Toast.makeText(context, "Fingerprint verification successful!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Biometric security not enabled in Settings/Profile.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            else -> {
                                                if (pinEntered.length < 4) {
                                                    pinEntered += value
                                                    if (pinEntered.length == 4) {
                                                        // Auto-verify PIN code
                                                        val correct = viewModel.verifyPin(pinEntered)
                                                        if (!correct) {
                                                            pinError = true
                                                            pinEntered = ""
                                                            Toast.makeText(context, "Incorrect PIN, please try again.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (value == "Bio") {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Biometrics unlock",
                                        tint = if (user?.biometricEnabled == true) HdPrimaryPurple else HdSecondaryText
                                    )
                                } else {
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (value == "Del") HdErrorRed else HdDarkText
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = { viewModel.logoutUser() }
            ) {
                Text(
                    text = "Sign Out & Switch Profile",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HdPrimaryPurple
                    )
                )
            }
        }
    }
}
