package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.NutriViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SlateDark = Color(0xFF0F172A)
private val CardSlate = Color(0xFF1E293B)
private val TextSecondaryDark = Color(0xFF94A3B8)
private val EmeraldMint = Color(0xFF10B981)
private val DeepSkyBlue = Color(0xFF0284C7)
private val CoralRed = Color(0xFFF43F5E)
private val AmberGold = Color(0xFFF59E0B)
private val PurpleIndigo = Color(0xFF6366F1)

sealed interface TestingState {
    object Idle : TestingState
    object Testing : TestingState
    data class Success(val msg: String) : TestingState
    data class Error(val msg: String) : TestingState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val goal by viewModel.userGoal.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var weightStr by remember { mutableStateOf("") }
    var heightStr by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var targetWeightStr by remember { mutableStateOf("") }

    var selectedGender by remember { mutableStateOf("Male") }
    var selectedActivityLevel by remember { mutableStateOf("Moderate") }
    var selectedGoalType by remember { mutableStateOf("Weight Loss") }

    var customApiKeyStr by remember { mutableStateOf("") }
    var showSavedToast by remember { mutableStateOf(false) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    var testingState by remember { mutableStateOf<TestingState>(TestingState.Idle) }

    // Sync input states when Room goal is loaded
    LaunchedEffect(goal) {
        weightStr = goal.weightKg.toString()
        heightStr = goal.heightCm.toString()
        ageStr = goal.age.toString()
        targetWeightStr = goal.targetWeightKg.toString()
        selectedGender = goal.gender
        selectedActivityLevel = goal.activityLevel
        selectedGoalType = goal.goalType
        customApiKeyStr = goal.customApiKey
    }

    // Auto-dismiss saved toast after 4 seconds
    LaunchedEffect(showSavedToast) {
        if (showSavedToast) {
            delay(4000)
            showSavedToast = false
        }
    }

    // Live Calculations for Real-time math preview
    val liveWeight = weightStr.toDoubleOrNull() ?: 70.0
    val liveHeight = heightStr.toDoubleOrNull() ?: 175.0
    val liveAge = ageStr.toIntOrNull() ?: 25
    val liveGender = selectedGender
    val liveGoalType = selectedGoalType
    val liveActivityLevel = selectedActivityLevel

    // Mifflin-St Jeor Formula
    val liveBmr = if (liveGender.lowercase() == "male") {
        (10.0 * liveWeight) + (6.25 * liveHeight) - (5.0 * liveAge) + 5.0
    } else {
        (10.0 * liveWeight) + (6.25 * liveHeight) - (5.0 * liveAge) - 161.0
    }

    val liveMultiplier = when (liveActivityLevel.lowercase()) {
        "sedentary" -> 1.2
        "moderate" -> 1.55
        "active" -> 1.725
        else -> 1.375
    }
    val liveTdee = liveBmr * liveMultiplier

    val liveCalories = when (liveGoalType.lowercase()) {
        "weight loss" -> (liveTdee - 500).coerceAtLeast(1200.0).toInt()
        "muscle gain" -> (liveTdee + 350).toInt()
        else -> liveTdee.toInt()
    }

    val liveProtein = when (liveGoalType.lowercase()) {
        "weight loss" -> (2.0 * liveWeight).toInt()
        "muscle gain" -> (2.2 * liveWeight).toInt()
        else -> (1.6 * liveWeight).toInt()
    }

    val liveFat = ((liveCalories * 0.25) / 9.0).toInt()
    val liveCarbs = ((liveCalories - (liveProtein * 4) - (liveFat * 9)) / 4).coerceAtLeast(50)

    val isDirty = weightStr.toDoubleOrNull() != goal.weightKg ||
            heightStr.toDoubleOrNull() != goal.heightCm ||
            ageStr.toIntOrNull() != goal.age ||
            selectedGender != goal.gender ||
            selectedGoalType != goal.goalType ||
            selectedActivityLevel != goal.activityLevel ||
            targetWeightStr.toDoubleOrNull() != goal.targetWeightKg ||
            customApiKeyStr.trim() != goal.customApiKey.trim()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Visual Intro
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                PurpleIndigo.copy(alpha = 0.2f),
                                DeepSkyBlue.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(colors = listOf(PurpleIndigo.copy(alpha = 0.3f), Color.Transparent)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar Initials Frame
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        DeepSkyBlue,
                                        PurpleIndigo
                                    )
                                )
                            )
                            .border(width = 2.dp, color = Color.White.copy(alpha = 0.4f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedGender.lowercase() == "female") "♀" else "♂",
                            fontSize = 32.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "NutriAI Elite Profile",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldMint)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Active Target calculations synchronized",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }

        // Live Macro Calculations Display
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isDirty) AmberGold.copy(alpha = 0.4f) else EmeraldMint.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isDirty) "Live Preview Targets" else "Current Saved Goals",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (isDirty) "Unsaved changes previewed below" else "Based on physical biometrics",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                            )
                        }

                        // Preview badge
                        if (isDirty) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "⚡ Real-time recalculation",
                                    fontSize = 11.sp,
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldMint.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "✓ Synchronized",
                                    fontSize = 11.sp,
                                    color = EmeraldMint,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4-Column Grid for Calories & Macros
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val calories = if (isDirty) liveCalories else goal.targetCalories
                        val protein = if (isDirty) liveProtein else goal.targetProtein
                        val carbs = if (isDirty) liveCarbs else goal.targetCarbs
                        val fat = if (isDirty) liveFat else goal.targetFat

                        // Calorie Block
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldMint.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = EmeraldMint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Calories",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                            Text(
                                text = "$calories",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Text(text = "kcal", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 10.sp))
                        }

                        // Protein Block
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(DeepSkyBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                    contentDescription = null,
                                    tint = DeepSkyBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Protein",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                            Text(
                                text = "${protein}g",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Text(
                                text = "${protein * 4} kcal",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 10.sp)
                            )
                        }

                        // Carbs Block
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AmberGold.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestaurantMenu,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Carbs",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                            Text(
                                text = "${carbs}g",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Text(
                                text = "${carbs * 4} kcal",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 10.sp)
                            )
                        }

                        // Fat Block
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(CoralRed.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = CoralRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Fat",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                            Text(
                                text = "${fat}g",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Text(
                                text = "${fat * 9} kcal",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 10.sp)
                            )
                        }
                    }

                    // TDEE Interactive Math Formula Explainer Dropdown
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SlateDark.copy(alpha = 0.6f))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PurpleIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-Calculation Breakdown (Mifflin-St Jeor Formula)",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "BMR (Basal Metabolic Rate): ${liveBmr.toInt()} kcal (Internal cellular organs consumption)\n" +
                                        "TDEE (Total Expenditure): ${liveBmr.toInt()} × ${liveMultiplier} (Activity multiplier) = ${liveTdee.toInt()} kcal\n" +
                                        "Recommended Calorie Target: ${liveCalories} kcal based on $liveGoalType",
                                fontSize = 10.sp,
                                color = TextSecondaryDark,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Physical Biometrics Inputs Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldMint.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = EmeraldMint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Physical Biometrics Profile",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Weight & Height
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text("Weight (kg)") },
                            leadingIcon = { Icon(imageVector = Icons.Default.MonitorWeight, contentDescription = null, tint = TextSecondaryDark) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_weight_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = EmeraldMint,
                                unfocusedBorderColor = TextSecondaryDark.copy(alpha = 0.3f),
                                focusedLabelColor = EmeraldMint,
                                unfocusedLabelColor = TextSecondaryDark
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = heightStr,
                            onValueChange = { heightStr = it },
                            label = { Text("Height (cm)") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Straighten, contentDescription = null, tint = TextSecondaryDark) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = EmeraldMint,
                                unfocusedBorderColor = TextSecondaryDark.copy(alpha = 0.3f),
                                focusedLabelColor = EmeraldMint,
                                unfocusedLabelColor = TextSecondaryDark
                            ),
                            singleLine = true
                        )
                    }

                    // Age & Target Weight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = { Text("Age (years)") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Cake, contentDescription = null, tint = TextSecondaryDark) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = EmeraldMint,
                                unfocusedBorderColor = TextSecondaryDark.copy(alpha = 0.3f),
                                focusedLabelColor = EmeraldMint,
                                unfocusedLabelColor = TextSecondaryDark
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = targetWeightStr,
                            onValueChange = { targetWeightStr = it },
                            label = { Text("Target Weight") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Flag, contentDescription = null, tint = TextSecondaryDark) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = EmeraldMint,
                                unfocusedBorderColor = TextSecondaryDark.copy(alpha = 0.3f),
                                focusedLabelColor = EmeraldMint,
                                unfocusedLabelColor = TextSecondaryDark
                            ),
                            singleLine = true
                        )
                    }

                    // Gender Interactive Selector Cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Biological Sex profile",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontWeight = FontWeight.Bold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("Male", "Female").forEach { sex ->
                                val isSelected = selectedGender.lowercase() == sex.lowercase()
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedGender = sex }
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) EmeraldMint else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) EmeraldMint.copy(alpha = 0.1f) else SlateDark
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (sex == "Male") Icons.Default.Male else Icons.Default.Female,
                                            contentDescription = null,
                                            tint = if (isSelected) EmeraldMint else TextSecondaryDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = sex,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (isSelected) Color.White else TextSecondaryDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Primary Goal Selectors
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Primary Nutrition Objective",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontWeight = FontWeight.Bold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("Weight Loss", "Deficit (-500)", Icons.AutoMirrored.Filled.TrendingDown),
                                Triple("Maintain", "Balance (TDEE)", Icons.Default.Refresh),
                                Triple("Muscle Gain", "Surplus (+350)", Icons.AutoMirrored.Filled.TrendingUp)
                            ).forEach { (goalType, subtitle, icon) ->
                                val isSelected = selectedGoalType.lowercase() == goalType.lowercase()
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedGoalType = goalType }
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) DeepSkyBlue else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) DeepSkyBlue.copy(alpha = 0.1f) else SlateDark
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) DeepSkyBlue else TextSecondaryDark,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = goalType,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.White else TextSecondaryDark,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextSecondaryDark.copy(alpha = 0.6f),
                                                fontSize = 9.sp
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Activity Level Selectors
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Daily Activity Multiplier",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontWeight = FontWeight.Bold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("Sedentary", "1.2x (Desk)", Icons.Default.Bed),
                                Triple("Moderate", "1.55x (Active)", Icons.AutoMirrored.Filled.DirectionsWalk),
                                Triple("Active", "1.725x (Athlete)", Icons.Default.FlashOn)
                            ).forEach { (act, subtitle, icon) ->
                                val isSelected = selectedActivityLevel.lowercase() == act.lowercase()
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedActivityLevel = act }
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) AmberGold else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) AmberGold.copy(alpha = 0.1f) else SlateDark
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) AmberGold else TextSecondaryDark,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = act,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.White else TextSecondaryDark,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextSecondaryDark.copy(alpha = 0.6f),
                                                fontSize = 9.sp
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Re-calculate & Save Button
                    Button(
                        onClick = {
                            val weight = weightStr.toDoubleOrNull() ?: goal.weightKg
                            val height = heightStr.toDoubleOrNull() ?: goal.heightCm
                            val age = ageStr.toIntOrNull() ?: goal.age
                            val targetWeight = targetWeightStr.toDoubleOrNull() ?: goal.targetWeightKg

                            viewModel.updateUserProfile(
                                weightKg = weight,
                                heightCm = height,
                                age = age,
                                gender = selectedGender,
                                activityLevel = selectedActivityLevel,
                                goalType = selectedGoalType,
                                targetWeightKg = targetWeight,
                                customApiKey = customApiKeyStr.trim()
                            )
                            focusManager.clearFocus()
                            showSavedToast = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldMint),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_profile_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Recalculate & Synchronize Targets",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }
                    }

                    // Save Confirm Feedback Banner
                    AnimatedVisibility(
                        visible = showSavedToast,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldMint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(width = 1.dp, color = EmeraldMint.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldMint)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Profile updated and local Room database synced!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EmeraldMint,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Gemini API Configuration Locked Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DeepSkyBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = DeepSkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gemini Secure API Configuration",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "Google AI Studio offers a completely FREE standard tier for Gemini 2.5 Flash with no credit cards required. Paste your custom key here to run local real-time food image analysis.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark, fontSize = 12.sp, lineHeight = 16.sp)
                    )

                    // Secure password field with eye icons
                    OutlinedTextField(
                        value = customApiKeyStr,
                        onValueChange = { customApiKeyStr = it },
                        label = { Text("Custom Gemini API Key", color = TextSecondaryDark) },
                        placeholder = { Text("Paste AI Studio API key (AIzaSy...)...", color = TextSecondaryDark.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = TextSecondaryDark) },
                        trailingIcon = {
                            val icon = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(imageVector = icon, contentDescription = null, tint = TextSecondaryDark)
                            }
                        },
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_api_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DeepSkyBlue,
                            unfocusedBorderColor = TextSecondaryDark.copy(alpha = 0.3f),
                            focusedContainerColor = SlateDark,
                            unfocusedContainerColor = SlateDark,
                            focusedLabelColor = DeepSkyBlue,
                            unfocusedLabelColor = TextSecondaryDark
                        ),
                        singleLine = true
                    )

                    // Verify Key Connection & Save Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Secure Ping Verify Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                testingState = TestingState.Testing
                                viewModel.testApiKey(customApiKeyStr.trim()) { success, responseMsg ->
                                    testingState = if (success) {
                                        TestingState.Success(responseMsg)
                                    } else {
                                        TestingState.Error(responseMsg)
                                    }
                                }
                            },
                            enabled = testingState !is TestingState.Testing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurpleIndigo,
                                disabledContainerColor = PurpleIndigo.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(46.dp)
                        ) {
                            if (testingState is TestingState.Testing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pinging...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test Key", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        // Save API key button
                        Button(
                            onClick = {
                                val weight = weightStr.toDoubleOrNull() ?: goal.weightKg
                                val height = heightStr.toDoubleOrNull() ?: goal.heightCm
                                val age = ageStr.toIntOrNull() ?: goal.age
                                val targetWeight = targetWeightStr.toDoubleOrNull() ?: goal.targetWeightKg

                                viewModel.updateUserProfile(
                                    weightKg = weight,
                                    heightCm = height,
                                    age = age,
                                    gender = selectedGender,
                                    activityLevel = selectedActivityLevel,
                                    goalType = selectedGoalType,
                                    targetWeightKg = targetWeight,
                                    customApiKey = customApiKeyStr.trim()
                                )
                                focusManager.clearFocus()
                                showSavedToast = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepSkyBlue),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("save_api_key_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Key", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Key connection check result
                    AnimatedVisibility(
                        visible = testingState != TestingState.Idle,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        when (val state = testingState) {
                            is TestingState.Success -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = EmeraldMint.copy(alpha = 0.12f)),
                                    border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldMint)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = state.msg,
                                            style = MaterialTheme.typography.bodySmall.copy(color = EmeraldMint, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                            is TestingState.Error -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CoralRed.copy(alpha = 0.12f)),
                                    border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CoralRed)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = state.msg,
                                            style = MaterialTheme.typography.bodySmall.copy(color = CoralRed, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        // Security & Privacy Card
        item {
            val user = currentUser
            if (user != null) {
                var pinCodeInput by remember(user.pinCode) { mutableStateOf(user.pinCode ?: "") }
                var showPinEdit by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CoralRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = CoralRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Security & Privacy Lock",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = "Protect your visual scan history and nutrition tracking logs on this device with secure lock features.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark, fontSize = 12.sp, lineHeight = 16.sp)
                        )

                         // Credentials Profile Details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateDark.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Profile Credentials",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = EmeraldMint)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Username: ${user.username}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Email: ${user.email}",
                                color = TextSecondaryDark,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Phone Number: ${user.phoneNumber}",
                                color = TextSecondaryDark,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondaryDark.copy(alpha = 0.2f)))

                        // Supabase Cloud Sync Section
                        var supUrlInput by remember { mutableStateOf(viewModel.supabaseUrl.value) }
                        var supKeyInput by remember { mutableStateOf(viewModel.supabaseKey.value) }
                        var keyVisible by remember { mutableStateOf(false) }
                        var isTestingConn by remember { mutableStateOf(false) }
                        var testResultMsg by remember { mutableStateOf<String?>(null) }
                        var testResultSuccess by remember { mutableStateOf(false) }
                        var showSqlHelp by remember { mutableStateOf(false) }

                        val isSyncing by viewModel.isSyncing.collectAsState()
                        val syncStatus by viewModel.syncStatus.collectAsState()
                        val context = androidx.compose.ui.platform.LocalContext.current

                        val supabaseUrlState by viewModel.supabaseUrl.collectAsState()
                        val supabaseKeyState by viewModel.supabaseKey.collectAsState()

                        LaunchedEffect(supabaseUrlState) {
                            supUrlInput = supabaseUrlState
                        }
                        LaunchedEffect(supabaseKeyState) {
                            supKeyInput = supabaseKeyState
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateDark.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = "Cloud",
                                        tint = EmeraldMint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Supabase Cloud Sync",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                IconButton(
                                    onClick = { showSqlHelp = !showSqlHelp },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showSqlHelp) Icons.AutoMirrored.Filled.Help else Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = "SQL Help",
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Synchronize your profile, water intakes, calories, weights, and chat coach logs across devices using your own private Supabase PostgreSQL database.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark, fontSize = 11.sp, lineHeight = 14.sp)
                            )

                            // URL Input
                            OutlinedTextField(
                                value = supUrlInput,
                                onValueChange = { supUrlInput = it },
                                label = { Text("Supabase URL", color = TextSecondaryDark) },
                                placeholder = { Text("https://xxx.supabase.co", color = TextSecondaryDark.copy(alpha = 0.4f)) },
                                modifier = Modifier.fillMaxWidth().testTag("supabase_url_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldMint,
                                    unfocusedBorderColor = TextSecondaryDark.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true
                            )

                            // Key Input
                            OutlinedTextField(
                                value = supKeyInput,
                                onValueChange = { supKeyInput = it },
                                label = { Text("Supabase Anon Key", color = TextSecondaryDark) },
                                placeholder = { Text("eyJhbGciOi...", color = TextSecondaryDark.copy(alpha = 0.4f)) },
                                modifier = Modifier.fillMaxWidth().testTag("supabase_key_input"),
                                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { keyVisible = !keyVisible }) {
                                        Icon(
                                            imageVector = if (keyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Key Visibility",
                                            tint = TextSecondaryDark
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldMint,
                                    unfocusedBorderColor = TextSecondaryDark.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true
                            )

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Save & Test button
                                Button(
                                    onClick = {
                                        viewModel.updateSupabaseCredentials(supUrlInput, supKeyInput)
                                        isTestingConn = true
                                        testResultMsg = "Testing..."
                                        viewModel.testSupabaseConnection(supUrlInput, supKeyInput) { success ->
                                            isTestingConn = false
                                            testResultSuccess = success
                                            testResultMsg = if (success) "Connection success!" else "Connection failed. Please verify URL and Key."
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("test_connection_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SlateDark,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(vertical = 10.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (isTestingConn) "Testing..." else "Test & Save",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                // Sync Now Button
                                Button(
                                    onClick = {
                                        viewModel.updateSupabaseCredentials(supUrlInput, supKeyInput)
                                        viewModel.syncWithSupabase()
                                    },
                                    modifier = Modifier.weight(1f).testTag("sync_supabase_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldMint,
                                        contentColor = SlateDark
                                    ),
                                    contentPadding = PaddingValues(vertical = 10.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !isSyncing
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSyncing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                color = SlateDark,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Sync,
                                                contentDescription = "Sync",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = if (isSyncing) "Syncing..." else "Sync Now",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            // Connection test feedback
                            testResultMsg?.let { msg ->
                                Text(
                                    text = msg,
                                    color = if (testResultSuccess) EmeraldMint else Color.Red,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                )
                            }

                            // Sync status feedback
                            syncStatus?.let { status ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Sync Output",
                                            color = EmeraldMint,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        )
                                        IconButton(
                                            onClick = { viewModel.clearSyncStatus() },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss",
                                                tint = TextSecondaryDark,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = status,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp)
                                    )
                                }
                            }

                            // SQL Setup Help Block
                            AnimatedVisibility(visible = showSqlHelp) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Setup Instructions:",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "1. Open your Supabase Dashboard -> SQL Editor.\n2. Paste the SQL setup script below.\n3. Click Run to create all sync tables instantly.",
                                        color = TextSecondaryDark,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp)
                                    )

                                    val sqlScript = """
                                        -- 1. Create Users Sync Table
                                        create table if not exists users (
                                          username text primary key,
                                          email text,
                                          phone_number text,
                                          password_hash text,
                                          salt text,
                                          pin_code text,
                                          biometric_enabled boolean,
                                          created_at bigint
                                        );

                                        -- 2. Create Logged Foods Table
                                        create table if not exists logged_foods (
                                          id bigint primary key,
                                          name text,
                                          meal_type text,
                                          calories integer,
                                          protein double precision,
                                          carbs double precision,
                                          fat double precision,
                                          quantity text,
                                          image_url text,
                                          date_string text,
                                          timestamp bigint,
                                          username text references users(username) on delete cascade
                                        );

                                        -- 3. Create Water Logs Table
                                        create table if not exists water_logs (
                                          id bigint primary key,
                                          amount_ml integer,
                                          date_string text,
                                          timestamp bigint,
                                          username text references users(username) on delete cascade
                                        );

                                        -- 4. Create Weight Logs Table
                                        create table if not exists weight_logs (
                                          id bigint primary key,
                                          weight_kg double precision,
                                          date_string text,
                                          timestamp bigint,
                                          username text references users(username) on delete cascade
                                        );

                                        -- 5. Create Chat Messages Table
                                        create table if not exists chat_messages (
                                          id bigint primary key,
                                          sender text,
                                          message text,
                                          timestamp bigint,
                                          username text references users(username) on delete cascade
                                        );

                                        -- 6. Create Scan Feedbacks Table
                                        create table if not exists scan_feedbacks (
                                          id bigint primary key,
                                          food_name text,
                                          original_calories integer,
                                          corrected_calories integer,
                                          original_ingredients text,
                                          corrected_ingredients text,
                                          feedback_text text,
                                          is_positive boolean,
                                          timestamp bigint,
                                          username text references users(username) on delete cascade
                                        );
                                    """.trimIndent()

                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("Supabase SQL", sqlScript)
                                            clipboard.setPrimaryClip(clip)
                                            android.widget.Toast.makeText(context, "SQL copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SlateDark,
                                            contentColor = EmeraldMint
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                        ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy SQL",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Copy SQL Setup Script",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondaryDark.copy(alpha = 0.2f)))

                        // PIN Lock Status/Edit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "4-Digit PIN Protection",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (user.pinCode.isNullOrEmpty()) "Disabled (Insecure)" else "Active (Secured)",
                                    color = if (user.pinCode.isNullOrEmpty()) CoralRed else EmeraldMint,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Button(
                                onClick = { showPinEdit = !showPinEdit },
                                colors = ButtonDefaults.buttonColors(containerColor = if (user.pinCode.isNullOrEmpty()) EmeraldMint else CoralRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (user.pinCode.isNullOrEmpty()) "Setup PIN" else "Change / Remove",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        AnimatedVisibility(visible = showPinEdit) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = pinCodeInput,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() } && input.length <= 4) {
                                            pinCodeInput = input
                                        }
                                    },
                                    label = { Text("Enter 4-Digit Passcode") },
                                    placeholder = { Text("e.g. 1234") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    visualTransformation = PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = EmeraldMint,
                                        unfocusedBorderColor = TextSecondaryDark.copy(alpha = 0.3f),
                                        focusedLabelColor = EmeraldMint,
                                        unfocusedLabelColor = TextSecondaryDark
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (pinCodeInput.length == 4) {
                                                viewModel.setPinCode(pinCodeInput)
                                                showPinEdit = false
                                            }
                                        },
                                        enabled = pinCodeInput.length == 4,
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldMint),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Save PIN", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }

                                    if (!user.pinCode.isNullOrEmpty()) {
                                        Button(
                                            onClick = {
                                                viewModel.setPinCode(null)
                                                pinCodeInput = ""
                                                showPinEdit = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Disable PIN", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondaryDark.copy(alpha = 0.2f)))

                        // Biometrics Switch Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Biometric Verification",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Unlock using your fingerprint scanner",
                                        color = TextSecondaryDark,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Switch(
                                checked = user.biometricEnabled,
                                onCheckedChange = { viewModel.toggleBiometrics(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldMint,
                                    checkedTrackColor = EmeraldMint.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextSecondaryDark,
                                    uncheckedTrackColor = SlateDark
                                )
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondaryDark.copy(alpha = 0.2f)))

                        // Logout row
                        Button(
                            onClick = { viewModel.logoutUser() },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log Out", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Log Out of Session (${user.username})",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Currently Guest Session",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Log in to secure your visual food scans and custom biometrics details.",
                            color = TextSecondaryDark,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
