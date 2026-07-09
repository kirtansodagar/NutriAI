package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.NutriViewModel
import com.example.ui.viewmodel.ScanUiState

// High Density Theme Palette
private val HdPrimaryPurple = Color(0xFF10B981) // EmeraldMint
private val HdDarkText = Color.White
private val HdSecondaryText = Color(0xFF94A3B8)
private val HdBackground = Color(0xFF0F172A)
private val HdCardBg = Color(0xFF1E293B)
private val HdBorderColor = Color(0xFF334155)
private val HdAccentPill = Color(0xFF1E293B)
private val HdAccentGreen = Color(0xFF10B981).copy(alpha = 0.15f)
private val HdGreenText = Color(0xFF10B981)
private val EmeraldMint = Color(0xFF10B981)
private val CoralRed = Color(0xFFEF4444)
private val DeepSkyBlue = Color(0xFF6366F1) // NeonIndigo
private val AmberGold = Color(0xFFF59E0B)

data class MealPreset(
    val id: String,
    val name: String,
    val description: String,
    val baseColor: Color,
    val accentColor: Color
)

@Composable
fun ScanScreen(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanUiState by viewModel.scanUiState.collectAsState()
    val recentFeedbacks by viewModel.recentFeedbacks.collectAsState()
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedMealCategory by remember { mutableStateOf("Lunch") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Inline edit states
    var editedName by remember { mutableStateOf("") }
    var editedQuantity by remember { mutableStateOf("") }
    var editedCalories by remember { mutableStateOf("") }
    var editedProtein by remember { mutableStateOf("") }
    var editedCarbs by remember { mutableStateOf("") }
    var editedFat by remember { mutableStateOf("") }
    var editedIngredients by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }

    // Feedback States
    var feedbackComments by remember { mutableStateOf("") }
    var feedbackRating by remember { mutableStateOf<Boolean?>(null) }
    var isFeedbackSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(scanUiState) {
        if (scanUiState is ScanUiState.Success) {
            val result = (scanUiState as ScanUiState.Success).result
            editedName = result.name
            editedQuantity = result.quantity
            editedCalories = result.calories.toString()
            editedProtein = result.protein.toString()
            editedCarbs = result.carbs.toString()
            editedFat = result.fat.toString()
            editedIngredients = result.ingredients
            isEditMode = false
            feedbackComments = ""
            feedbackRating = null
            isFeedbackSubmitted = false
        }
    }

    // Meal presets list for testing
    val presets = remember {
        listOf(
            MealPreset("salmon", "Grilled Salmon", "Salmon fillet with broccoli and olive oil.", Color(0xFFF07167), Color(0xFF86B49C)),
            MealPreset("avocado_toast", "Avocado Toast", "Sourdough bread, mashed avocado, poached egg.", Color(0xFFADF7B6), Color(0xFFFFC09F)),
            MealPreset("burger", "Double Cheeseburger", "Juicy double beef patty with fries.", Color(0xFFECA135), Color(0xFFFF9F1C)),
            MealPreset("smoothie_bowl", "Acai Bowl", "Strawberry blueberry yogurt base with seeds.", Color(0xFF9B5DE5), Color(0xFFF15BB5))
        )
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    selectedBitmap = bitmap
                    viewModel.scanFoodImage(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera launcher to capture directly
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedBitmap = it
            viewModel.scanFoodImage(it)
        }
    }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            android.widget.Toast.makeText(
                context,
                "Camera permission is required to capture photos directly.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HdBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HdPrimaryPurple.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = HdPrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Visual AI Scanner",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = HdDarkText
                        )
                    )
                    Text(
                        text = "Snap or choose a meal photo to estimate nutrition",
                        style = MaterialTheme.typography.bodySmall.copy(color = HdSecondaryText)
                    )
                }
            }
        }

        val activeKey = viewModel.getApiKeyToUse()
        // Warning if key is missing
        if (activeKey.isEmpty() || activeKey == "MY_GEMINI_API_KEY") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Warning",
                            tint = CoralRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "No Gemini API Key found. Visual food scans are running in simulated demo mode. Paste your key on the Profile Screen or set it in AI Studio Secrets to enable live AI analysis.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CoralRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Camera / Image picker box
        item {
            if (selectedBitmap != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = HdCardBg),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected food",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                        )
                        // Overlay cancel button
                        IconButton(
                            onClick = {
                                selectedBitmap = null
                                viewModel.clearScanState()
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Camera card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clickable {
                                val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                )
                                if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    cameraLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            }
                            .testTag("direct_camera_capture_card"),
                        colors = CardDefaults.cardColors(containerColor = HdCardBg),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(HdPrimaryPurple.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera",
                                    tint = HdPrimaryPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Take Photo",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = HdDarkText
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Directly scan meal",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = HdSecondaryText,
                                    fontSize = 11.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Gallery Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clickable { photoPickerLauncher.launch("image/*") }
                            .testTag("gallery_picker_card"),
                        colors = CardDefaults.cardColors(containerColor = HdCardBg),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(DeepSkyBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Gallery",
                                    tint = DeepSkyBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Upload Photo",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = HdDarkText
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "From your gallery",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = HdSecondaryText,
                                    fontSize = 11.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // OR presets section
        if (selectedBitmap == null && scanUiState is ScanUiState.Idle) {
            item {
                Text(
                    text = "No picture? Try a Meal Preset",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = HdDarkText,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        OutlinedCard(
                            onClick = {
                                val generatedBitmap = createPresetBitmap(preset)
                                selectedBitmap = generatedBitmap
                                viewModel.scanFoodImage(generatedBitmap)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HdCardBg),
                            border = BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic canvas preview
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(preset.baseColor, preset.accentColor)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Restaurant,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = HdDarkText,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = HdSecondaryText,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Scan Preset",
                                    tint = HdPrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (recentFeedbacks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Calibration",
                            tint = HdPrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Calibration Memory",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = HdDarkText,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                    Text(
                        text = "The AI references these logs to calibrate its portion and calorie estimates for future scans.",
                        style = MaterialTheme.typography.bodySmall.copy(color = HdSecondaryText, fontSize = 11.sp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recentFeedbacks.forEach { fb ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = HdCardBg),
                                border = BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (fb.isPositive) Icons.Default.ThumbUp else Icons.Default.ThumbDown,
                                        contentDescription = null,
                                        tint = if (fb.isPositive) HdGreenText else Color(0xFFBA1A1A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fb.foodName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = HdDarkText)
                                        )
                                        if (fb.originalCalories != fb.correctedCalories) {
                                            Text(
                                                text = "Calibrated: ${fb.originalCalories} kcal ➡️ ${fb.correctedCalories} kcal",
                                                style = MaterialTheme.typography.labelSmall.copy(color = HdSecondaryText)
                                            )
                                        }
                                        Text(
                                            text = "Correction: \"${fb.feedbackText}\"",
                                            style = MaterialTheme.typography.labelSmall.copy(color = HdSecondaryText, fontStyle = FontStyle.Italic)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Processing / Status Card
        item {
            AnimatedVisibility(visible = scanUiState !is ScanUiState.Idle) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HdCardBg),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (val state = scanUiState) {
                            is ScanUiState.Loading -> {
                                CircularProgressIndicator(color = HdPrimaryPurple, strokeWidth = 4.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Analyzing Food Image...",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = HdDarkText,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                                Text(
                                    text = "Gemini is identifying ingredients and estimating portion macros...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = HdSecondaryText,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }

                            is ScanUiState.Error -> {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Error",
                                    tint = CoralRed,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Scan Failed",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = CoralRed,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                                Text(
                                    text = state.message,
                                    color = HdSecondaryText,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.clearScanState() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                                ) {
                                    Text("Retry Scan", fontWeight = FontWeight.Bold)
                                }
                            }

                            is ScanUiState.Success -> {
                                val food = state.result
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = HdGreenText,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "AI Scan Complete",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = HdGreenText,
                                                fontWeight = FontWeight.Black
                                            )
                                        )
                                    }
                                    if (!isEditMode) {
                                        TextButton(
                                            onClick = { isEditMode = true },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("✏️ Edit Details", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = HdPrimaryPurple))
                                        }
                                    }
                                }

                                if (isEditMode) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(HdCardBg, RoundedCornerShape(16.dp))
                                            .border(BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "Edit Nutrition Estimates",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, color = HdDarkText)
                                        )
                                        OutlinedTextField(
                                            value = editedName,
                                            onValueChange = { editedName = it },
                                            label = { Text("Food Name", style = MaterialTheme.typography.bodySmall) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HdPrimaryPurple)
                                        )
                                        OutlinedTextField(
                                            value = editedQuantity,
                                            onValueChange = { editedQuantity = it },
                                            label = { Text("Portion Size", style = MaterialTheme.typography.bodySmall) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HdPrimaryPurple)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = editedCalories,
                                                onValueChange = { editedCalories = it },
                                                label = { Text("Calories (kcal)", style = MaterialTheme.typography.bodySmall) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HdPrimaryPurple)
                                            )
                                            OutlinedTextField(
                                                value = editedProtein,
                                                onValueChange = { editedProtein = it },
                                                label = { Text("Protein (g)", style = MaterialTheme.typography.bodySmall) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HdPrimaryPurple)
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = editedCarbs,
                                                onValueChange = { editedCarbs = it },
                                                label = { Text("Carbs (g)", style = MaterialTheme.typography.bodySmall) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HdPrimaryPurple)
                                            )
                                            OutlinedTextField(
                                                value = editedFat,
                                                onValueChange = { editedFat = it },
                                                label = { Text("Fat (g)", style = MaterialTheme.typography.bodySmall) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HdPrimaryPurple)
                                            )
                                        }
                                        OutlinedTextField(
                                            value = editedIngredients,
                                            onValueChange = { editedIngredients = it },
                                            label = { Text("Ingredients (comma-separated)", style = MaterialTheme.typography.bodySmall) },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 3,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HdPrimaryPurple)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = {
                                                editedName = food.name
                                                editedQuantity = food.quantity
                                                editedCalories = food.calories.toString()
                                                editedProtein = food.protein.toString()
                                                editedCarbs = food.carbs.toString()
                                                editedFat = food.fat.toString()
                                                editedIngredients = food.ingredients
                                                isEditMode = false
                                            }) {
                                                Text("Cancel", color = HdSecondaryText)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { isEditMode = false },
                                                colors = ButtonDefaults.buttonColors(containerColor = HdPrimaryPurple)
                                            ) {
                                                Text("Done", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(HdAccentGreen, RoundedCornerShape(16.dp))
                                            .border(BorderStroke(1.dp, Color(0xFFC8E6C9)), RoundedCornerShape(16.dp))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = editedName,
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    color = HdGreenText,
                                                    fontWeight = FontWeight.Black
                                                )
                                            )
                                            Text(
                                                text = "Estimated Portion: $editedQuantity",
                                                style = MaterialTheme.typography.labelSmall.copy(color = HdGreenText.copy(alpha = 0.8f))
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            MacroBadge("Calories", "$editedCalories kcal", CoralRed)
                                            MacroBadge("Protein", "${editedProtein}g", HdGreenText)
                                            MacroBadge("Carbs", "${editedCarbs}g", AmberGold)
                                            MacroBadge("Fat", "${editedFat}g", DeepSkyBlue)
                                        }

                                        if (editedIngredients.isNotEmpty()) {
                                            Column {
                                                Text(
                                                    text = "Detected Ingredients",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = HdGreenText,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = editedIngredients,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = HdGreenText.copy(alpha = 0.9f))
                                                )
                                            }
                                        }
                                    }
                                }

                                // Meal category selector and Log button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box {
                                        OutlinedCard(
                                            onClick = { dropdownExpanded = true },
                                            modifier = Modifier.width(130.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = HdCardBg),
                                            border = BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = selectedMealCategory,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = HdDarkText,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = HdSecondaryText
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = dropdownExpanded,
                                            onDismissRequest = { dropdownExpanded = false },
                                            modifier = Modifier.background(HdCardBg)
                                        ) {
                                            listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { cat ->
                                                DropdownMenuItem(
                                                    text = { Text(cat, color = HdDarkText) },
                                                    onClick = {
                                                        selectedMealCategory = cat
                                                        dropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.logManualFood(
                                                editedName,
                                                selectedMealCategory,
                                                editedCalories.toIntOrNull() ?: food.calories,
                                                editedProtein.toDoubleOrNull() ?: food.protein,
                                                editedCarbs.toDoubleOrNull() ?: food.carbs,
                                                editedFat.toDoubleOrNull() ?: food.fat,
                                                editedQuantity
                                            )
                                            
                                            // Implicit feedback logic: if user modified AI estimation, auto-save as learning feedback
                                            val isCalorieChanged = (editedCalories.toIntOrNull() ?: food.calories) != food.calories
                                            val isIngredientChanged = editedIngredients != food.ingredients
                                            if (isCalorieChanged || isIngredientChanged) {
                                                viewModel.submitScanFeedback(
                                                    foodName = editedName,
                                                    originalCalories = food.calories,
                                                    correctedCalories = editedCalories.toIntOrNull() ?: food.calories,
                                                    originalIngredients = food.ingredients,
                                                    correctedIngredients = editedIngredients,
                                                    feedbackText = "Implicit correction via manual details editing before logging.",
                                                    isPositive = false
                                                )
                                            }

                                            viewModel.clearScanState()
                                            selectedBitmap = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HdPrimaryPurple),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp)
                                            .testTag("log_scanned_food_button")
                                    ) {
                                        Text("Log Meal", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Explicit Continuous Learning Feedback Section
                                if (!isFeedbackSubmitted) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = HdCardBg),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Tune,
                                                    contentDescription = null,
                                                    tint = HdPrimaryPurple,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Rate AI & Help It Learn",
                                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = HdDarkText)
                                                )
                                            }
                                            Text(
                                                text = "Continuous learning calibrates future food scans based on your feedback.",
                                                style = MaterialTheme.typography.bodySmall.copy(color = HdSecondaryText, fontSize = 11.sp),
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Accurate Estimate?", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = HdDarkText))
                                                IconButton(
                                                    onClick = { feedbackRating = true },
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(if (feedbackRating == true) Color(0xFFC8E6C9) else Color.Transparent, CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ThumbUp,
                                                        contentDescription = "Thumbs Up",
                                                        tint = if (feedbackRating == true) HdGreenText else HdSecondaryText,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { feedbackRating = false },
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(if (feedbackRating == false) Color(0xFFFFCDD2) else Color.Transparent, CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ThumbDown,
                                                        contentDescription = "Thumbs Down",
                                                        tint = if (feedbackRating == false) Color(0xFFB71C1C) else HdSecondaryText,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = feedbackComments,
                                                onValueChange = { feedbackComments = it },
                                                placeholder = { Text("What did we get wrong? (e.g. 'underestimated portion')", style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, color = HdSecondaryText) },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = HdDarkText,
                                                    unfocusedTextColor = HdDarkText,
                                                    focusedBorderColor = HdPrimaryPurple,
                                                    unfocusedBorderColor = HdBorderColor.copy(alpha = 0.5f),
                                                    focusedContainerColor = HdBackground,
                                                    unfocusedContainerColor = HdBackground
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    if (feedbackRating != null) {
                                                        viewModel.submitScanFeedback(
                                                            foodName = editedName.ifBlank { food.name },
                                                            originalCalories = food.calories,
                                                            correctedCalories = editedCalories.toIntOrNull() ?: food.calories,
                                                            originalIngredients = food.ingredients,
                                                            correctedIngredients = editedIngredients.ifBlank { food.ingredients },
                                                            feedbackText = feedbackComments.ifBlank { if (feedbackRating == true) "Accurate prediction" else "Needs calibration" },
                                                            isPositive = feedbackRating == true
                                                        )
                                                        isFeedbackSubmitted = true
                                                    }
                                                },
                                                enabled = feedbackRating != null,
                                                colors = ButtonDefaults.buttonColors(containerColor = HdPrimaryPurple),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Submit Feedback", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                } else {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = HdAccentGreen),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Calibrated", tint = HdGreenText, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("AI Calibration Record Saved!", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = HdGreenText))
                                                Text("The model will calibrate future estimations using this feedback.", style = MaterialTheme.typography.labelSmall.copy(color = HdGreenText.copy(alpha = 0.8f)))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                TextButton(
                                    onClick = {
                                        viewModel.clearScanState()
                                        selectedBitmap = null
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = HdSecondaryText),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Discard Scan", fontWeight = FontWeight.Bold)
                                }
                            }

                            is ScanUiState.Idle -> {
                                // Do nothing
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
}

@Composable
fun MacroBadge(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(HdBackground, RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = HdSecondaryText))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                color = color,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

// Helper: draw border stroke with brush
fun borderBrush(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)

// Helper: generate a beautiful simulated food grid texture Bitmap
private fun createPresetBitmap(preset: MealPreset): Bitmap {
    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    // Base paint background
    val bgPaint = AndroidPaint().apply {
        color = android.graphics.Color.parseColor("#1E293B")
        style = AndroidPaint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 512f, 512f, bgPaint)

    // Drawing healthy concentric food loops
    val loopPaint = AndroidPaint().apply {
        color = preset.baseColor.hashCode()
        style = AndroidPaint.Style.STROKE
        strokeWidth = 35f
        isAntiAlias = true
    }
    canvas.drawCircle(256f, 256f, 150f, loopPaint)

    // Accent inner cores
    val corePaint = AndroidPaint().apply {
        color = preset.accentColor.hashCode()
        style = AndroidPaint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(256f, 256f, 75f, corePaint)

    return bitmap
}
