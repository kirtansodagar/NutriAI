package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LoggedFood
import com.example.ui.viewmodel.NutriViewModel
import com.example.ui.viewmodel.ScanUiState
import java.text.SimpleDateFormat
import java.util.Locale

// Unified Cal AI Cosmic Palette
private val SlateDark = Color(0xFF0F172A)      // Deep Space background
private val CardSlate = Color(0xFF1E293B)      // Card container background
private val SlateMuted = Color(0xFF334155)     // Muted borders and grids
private val TextPrimaryDark = Color.White
private val TextSecondaryDark = Color(0xFF94A3B8)

private val EmeraldMint = Color(0xFF10B981)    // High contrast neon active/protein
private val NeonIndigo = Color(0xFF6366F1)     // High contrast carbs/water
private val AmberGold = Color(0xFFF59E0B)      // High contrast lipids/fats/warnings
private val CoralRed = Color(0xFFEF4444)       // Calorie overages/deletes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: NutriViewModel,
    onNavigateToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val loggedFoods by viewModel.loggedFoods.collectAsState()
    val waterLogs by viewModel.waterLogs.collectAsState()
    val weightLogs by viewModel.weightLogs.collectAsState()
    val goal by viewModel.userGoal.collectAsState()
    val quickLogState by viewModel.quickLogState.collectAsState()

    val focusManager = LocalFocusManager.current

    var showAddManualDialog by remember { mutableStateOf(false) }
    var activeMealTypeForManualLog by remember { mutableStateOf("Breakfast") }

    // Quick Describe Local Input
    var quickDescriptionText by remember { mutableStateOf("") }
    var quickDescriptionMeal by remember { mutableStateOf("Breakfast") }
    var quickDescriptionMealExpanded by remember { mutableStateOf(false) }
    var quickLogFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var quickLogSuccess by remember { mutableStateOf(false) }

    // Weight Logging Dialog State
    var showWeightDialog by remember { mutableStateOf(false) }

    // Clear feedback when user navigates dates or starts typing
    LaunchedEffect(selectedDate) {
        viewModel.clearQuickLogState()
        quickLogFeedbackMessage = null
        quickLogSuccess = false
    }

    LaunchedEffect(quickLogState) {
        when (quickLogState) {
            is ScanUiState.Success -> {
                val parsed = (quickLogState as ScanUiState.Success).result
                quickLogFeedbackMessage = "Logged: ${parsed.name} (${parsed.calories} kcal) successfully!"
                quickLogSuccess = true
                quickDescriptionText = ""
            }
            is ScanUiState.Error -> {
                quickLogFeedbackMessage = (quickLogState as ScanUiState.Error).message
                quickLogSuccess = false
            }
            else -> {}
        }
    }

    // Date formatting for readable header
    val formattedDateHeader = remember(selectedDate) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (date != null) {
                SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(date)
            } else {
                selectedDate
            }
        } catch (e: Exception) {
            selectedDate
        }
    }

    // Totals calculations
    val totalCalories = loggedFoods.sumOf { it.calories }
    val totalProtein = loggedFoods.sumOf { it.protein }
    val totalCarbs = loggedFoods.sumOf { it.carbs }
    val totalFat = loggedFoods.sumOf { it.fat }
    val totalWaterMl = waterLogs.sumOf { it.amountMl }

    val remainingCalories = (goal.targetCalories - totalCalories).coerceAtLeast(0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Date Header & Selector
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.selectPreviousDay() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Previous Day",
                        tint = TextSecondaryDark
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedDateHeader,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimaryDark,
                            fontSize = 18.sp
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (selectedDate != viewModel.getTodayDateString()) {
                            Text(
                                text = "Go to Today",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldMint,
                                    fontWeight = FontWeight.Black
                                ),
                                modifier = Modifier
                                    .clickable { viewModel.selectToday() }
                                    .padding(vertical = 4.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(EmeraldMint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TODAY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = EmeraldMint,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.selectNextDay() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Next Day",
                        tint = TextSecondaryDark
                    )
                }
            }
        }

        // 2. Describe & Log Card (Instant Natural Language Logger)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SlateMuted)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonIndigo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI Quick Describe & Log",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Type what you ate in plain English. Our AI will compute nutrients and log it instantly.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = quickDescriptionText,
                        onValueChange = {
                            quickDescriptionText = it
                            if (quickLogFeedbackMessage != null) {
                                quickLogFeedbackMessage = null
                            }
                        },
                        placeholder = {
                            Text(
                                "e.g. 2 scrambled eggs with cheddar cheese and double slice wheat bread",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark.copy(alpha = 0.6f))
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_describe_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = SlateMuted,
                            focusedContainerColor = SlateDark.copy(alpha = 0.5f),
                            unfocusedContainerColor = SlateDark.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Meal Type Dropdown Indicator
                        Box {
                            Surface(
                                onClick = { quickDescriptionMealExpanded = true },
                                color = SlateMuted,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = quickDescriptionMeal,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = TextPrimaryDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = quickDescriptionMealExpanded,
                                onDismissRequest = { quickDescriptionMealExpanded = false },
                                modifier = Modifier.background(CardSlate)
                            ) {
                                listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { meal ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(meal, color = TextPrimaryDark) },
                                        onClick = {
                                            quickDescriptionMeal = meal
                                            quickDescriptionMealExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Analyze & Log Trigger Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.quickLogFood(quickDescriptionText, quickDescriptionMeal)
                            },
                            enabled = quickDescriptionText.isNotBlank() && quickLogState !is ScanUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonIndigo,
                                disabledContainerColor = SlateMuted
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("quick_describe_submit_btn")
                        ) {
                            if (quickLogState is ScanUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = TextPrimaryDark,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyze & Log", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Async Result Feedback
                    quickLogFeedbackMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (quickLogSuccess) EmeraldMint.copy(alpha = 0.1f) else CoralRed.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (quickLogSuccess) EmeraldMint else CoralRed,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // 3. High-fidelity Calorie circular Ring
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SlateMuted),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENERGY BALANCE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondaryDark,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .background(EmeraldMint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CAL AI LEVEL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldMint,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Custom arc gauge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(150.dp)
                        ) {
                            val animatedPercent by animateFloatAsState(
                                targetValue = if (goal.targetCalories > 0) totalCalories.toFloat() / goal.targetCalories else 0f,
                                animationSpec = tween(1000),
                                label = "PercentAnimation"
                            )

                            Canvas(modifier = Modifier.size(130.dp)) {
                                // Background track circle
                                drawCircle(
                                    color = SlateMuted,
                                    radius = size.minDimension / 2,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                                // Filled arc representing calories
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(EmeraldMint, NeonIndigo, EmeraldMint)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = (animatedPercent * 360f).coerceAtMost(360f),
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            // Text inside gauge
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$remainingCalories",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimaryDark,
                                        fontSize = 28.sp
                                    )
                                )
                                Text(
                                    text = "kcal left",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextSecondaryDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        // Numeric stats beside gauge
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = CoralRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Daily Budget",
                                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondaryDark)
                                    )
                                }
                                Text(
                                    text = "${goal.targetCalories} kcal",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Restaurant,
                                        contentDescription = null,
                                        tint = EmeraldMint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Consumed",
                                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondaryDark)
                                    )
                                }
                                Text(
                                    text = "$totalCalories kcal",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Macronutrient progress cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SlateMuted),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Macronutrients Today",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimaryDark
                        )
                    )

                    // Protein
                    MacroProgressBar(
                        label = "Protein",
                        current = totalProtein,
                        target = goal.targetProtein.toDouble(),
                        color = EmeraldMint,
                        unit = "g"
                    )

                    // Carbs
                    MacroProgressBar(
                        label = "Carbohydrates",
                        current = totalCarbs,
                        target = goal.targetCarbs.toDouble(),
                        color = NeonIndigo,
                        unit = "g"
                    )

                    // Fat
                    MacroProgressBar(
                        label = "Fat",
                        current = totalFat,
                        target = goal.targetFat.toDouble(),
                        color = AmberGold,
                        unit = "g"
                    )
                }
            }
        }

        // 5. Weight Progress & Historical Sparkline (Premium Cal AI metric logger)
        item {
            val weightRemaining = (goal.weightKg - goal.targetWeightKg).coerceAtLeast(0.0)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SlateMuted)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AmberGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonitorWeight,
                                    contentDescription = "Weight Track",
                                    tint = AmberGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Body Weight Monitor",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                )
                                Text(
                                    text = "Target: ${goal.targetWeightKg} kg",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                                )
                            }
                        }

                        Button(
                            onClick = { showWeightDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("+ Log", fontWeight = FontWeight.Bold, color = SlateDark, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Current",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                            Text(
                                text = "${String.format("%.1f", goal.weightKg)} kg",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = TextPrimaryDark,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                            Text(
                                text = if (weightRemaining <= 0.0) "Goal Reached! 🎉" else "${String.format("%.1f", weightRemaining)} kg",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = if (weightRemaining <= 0.0) EmeraldMint else AmberGold,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }

                    // Weight Sparkline Graph using Canvas
                    if (weightLogs.size >= 2) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Weight Trend (Recent Logs)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val recentLogs = weightLogs.take(8).reversed()
                                val minWeight = recentLogs.minOf { it.weightKg }
                                val maxWeight = recentLogs.maxOf { it.weightKg }
                                val weightRange = (maxWeight - minWeight).coerceAtLeast(0.1)

                                val paddingY = 8.dp.toPx()
                                val heightUsable = size.height - (paddingY * 2)
                                val widthStep = size.width / (recentLogs.size - 1)

                                val points = recentLogs.mapIndexed { index, item ->
                                    val x = index * widthStep
                                    val y = size.height - paddingY - (((item.weightKg - minWeight) / weightRange) * heightUsable).toFloat()
                                    androidx.compose.ui.geometry.Offset(x, y)
                                }

                                // Draw gradient baseline shadow fill
                                val shadowPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(points.first().x, size.height)
                                    points.forEach { lineTo(it.x, it.y) }
                                    lineTo(points.last().x, size.height)
                                    close()
                                }
                                drawPath(
                                    path = shadowPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(AmberGold.copy(alpha = 0.25f), Color.Transparent)
                                    )
                                )

                                // Draw trend lines
                                for (i in 0 until points.size - 1) {
                                    drawLine(
                                        color = AmberGold,
                                        start = points[i],
                                        end = points[i + 1],
                                        strokeWidth = 3.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }

                                // Draw circular node points
                                points.forEach { node ->
                                    drawCircle(
                                        color = TextPrimaryDark,
                                        radius = 4.dp.toPx(),
                                        center = node
                                    )
                                    drawCircle(
                                        color = AmberGold,
                                        radius = 2.dp.toPx(),
                                        center = node
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Inline weight history list
                    if (weightLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            weightLogs.take(3).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SlateDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = TextSecondaryDark,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = log.dateString,
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${log.weightKg} kg",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextPrimaryDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Weight Log",
                                            tint = CoralRed,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.deleteWeightLog(log.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Custom Water intake tracker card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SlateMuted),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NeonIndigo.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = "Water Tracker",
                                tint = NeonIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Water Log",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimaryDark
                                )
                            )
                            Text(
                                text = "$totalWaterMl / ${goal.targetWaterMl} ml",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (totalWaterMl > 0) {
                            TextButton(
                                onClick = { viewModel.removeLastWater() },
                                colors = ButtonDefaults.textButtonColors(contentColor = CoralRed)
                            ) {
                                Text("-250ml", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.addWater(250) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_water_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+250ml", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 7. Food Logs categorised by meals (Breakfast, Lunch, Dinner, Snacks)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meals Today",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimaryDark
                    )
                )

                TextButton(
                    onClick = { onNavigateToScan() },
                    colors = ButtonDefaults.textButtonColors(contentColor = EmeraldMint)
                ) {
                    Text("💡 Snapped a Photo? Use AI Scan", fontWeight = FontWeight.Bold)
                }
            }
        }

        val mealsList = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        mealsList.forEach { mealType ->
            item {
                val mealsOfThisType = loggedFoods.filter { it.mealType.lowercase() == mealType.lowercase() }
                val subtotalCalories = mealsOfThisType.sumOf { it.calories }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SlateMuted),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mealType,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimaryDark
                                    )
                                )
                                Text(
                                    text = "$subtotalCalories kcal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (subtotalCalories > 0) EmeraldMint else TextSecondaryDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            IconButton(
                                onClick = {
                                    activeMealTypeForManualLog = mealType
                                    showAddManualDialog = true
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SlateMuted)
                                    .testTag("add_${mealType.lowercase()}_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Food",
                                    tint = TextPrimaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (mealsOfThisType.isEmpty()) {
                            Text(
                                text = "No logged items. Tap + or use Quick Describe above.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondaryDark,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            mealsOfThisType.forEach { food ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(SlateDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = food.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimaryDark
                                            )
                                        )
                                        Text(
                                            text = "${food.quantity} • P:${food.protein}g, C:${food.carbs}g, F:${food.fat}g",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${food.calories} kcal",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = TextPrimaryDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteFood(food.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete Food Log",
                                                tint = CoralRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Modal Dialog to enter food details manually
    if (showAddManualDialog) {
        ManualLogFoodDialog(
            mealType = activeMealTypeForManualLog,
            onDismiss = { showAddManualDialog = false },
            onConfirm = { name, cal, pro, carb, fat, qty ->
                viewModel.logManualFood(name, activeMealTypeForManualLog, cal, pro, carb, fat, qty)
                showAddManualDialog = false
            }
        )
    }

    // Modal Dialog to log current body weight
    if (showWeightDialog) {
        Dialog(onDismissRequest = { showWeightDialog = false }) {
            var weightValText by remember { mutableStateOf(goal.weightKg.toString()) }
            var parseError by remember { mutableStateOf(false) }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CardSlate,
                border = BorderStroke(1.dp, SlateMuted),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Log Body Weight",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    )

                    Text(
                        text = "Enter your physical scale reading today. We'll update your daily calories, BMR, and track your history trend.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )

                    OutlinedTextField(
                        value = weightValText,
                        onValueChange = {
                            weightValText = it
                            parseError = false
                        },
                        label = { Text("Weight (kg)", color = TextSecondaryDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = SlateMuted
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (parseError) {
                        Text(
                            text = "Please enter a valid weight number (e.g. 74.5)",
                            color = CoralRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showWeightDialog = false }) {
                            Text("Cancel", color = TextSecondaryDark)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val wVal = weightValText.toDoubleOrNull()
                                if (wVal == null || wVal <= 0.0) {
                                    parseError = true
                                } else {
                                    viewModel.logWeight(wVal)
                                    showWeightDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                        ) {
                            Text("Log Weight", fontWeight = FontWeight.Bold, color = SlateDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroProgressBar(
    label: String,
    current: Double,
    target: Double,
    color: Color,
    unit: String
) {
    val progress = if (target > 0) (current / target).toFloat().coerceIn(0f, 1f) else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            )
            Text(
                text = "${String.format("%.1f", current)} / ${target.toInt()} $unit",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = SlateMuted
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualLogFoodDialog(
    mealType: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Double, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1 serving") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }

    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardSlate,
            border = BorderStroke(1.dp, SlateMuted),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Log $mealType Meal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name", color = TextSecondaryDark) },
                    placeholder = { Text("e.g. Avocado Toast", color = TextSecondaryDark.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = EmeraldMint,
                        unfocusedBorderColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Portion / Quantity", color = TextSecondaryDark) },
                    placeholder = { Text("e.g. 1 slice, 150g", color = TextSecondaryDark.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = EmeraldMint,
                        unfocusedBorderColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = caloriesStr,
                    onValueChange = { caloriesStr = it },
                    label = { Text("Calories (kcal)", color = TextSecondaryDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = EmeraldMint,
                        unfocusedBorderColor = SlateMuted
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        label = { Text("Protein (g)", color = TextSecondaryDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = EmeraldMint,
                            unfocusedBorderColor = SlateMuted
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it },
                        label = { Text("Carbs (g)", color = TextSecondaryDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = EmeraldMint,
                            unfocusedBorderColor = SlateMuted
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it },
                        label = { Text("Fat (g)", color = TextSecondaryDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = EmeraldMint,
                            unfocusedBorderColor = SlateMuted
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (hasError) {
                    Text(
                        text = "Please fill in at least the Food Name and Calories.",
                        color = CoralRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val calories = caloriesStr.toIntOrNull()
                            if (name.isBlank() || calories == null) {
                                hasError = true
                            } else {
                                val protein = proteinStr.toDoubleOrNull() ?: 0.0
                                val carbs = carbsStr.toDoubleOrNull() ?: 0.0
                                val fat = fatStr.toDoubleOrNull() ?: 0.0
                                onConfirm(name, calories, protein, carbs, fat, quantity)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldMint)
                    ) {
                        Text("Save Log", fontWeight = FontWeight.Bold, color = SlateDark)
                    }
                }
            }
        }
    }
}
