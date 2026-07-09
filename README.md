# Cal AI — Premium AI Nutrition & Weight Tracker

A highly polished, feature-rich **Android application** crafted with **Kotlin** and **Jetpack Compose**. Cal AI combines traditional macro-tracking with state-of-the-art Generative AI to provide a frictionless meal tracking, nutrition advice, and weight monitoring experience.

---

## 🌟 Key Features

### 1. ⚡ AI Quick Describe & Log
*   **Plain English Logging**: No more tedious database searches. Just type what you ate in natural language (e.g., *"2 scrambled eggs with cheddar cheese and double slice wheat bread"*).
*   **Instant Nutrition Breakdown**: The Gemini API parses your message, computes caloric/macronutrient details, and logs it directly to your dashboard under the correct meal category.

### 2. 📸 High-Fidelity Visual AI Scanner
*   **Direct Camera Capture**: Capture a photo of your plate in real time using your device camera.
*   **Gallery Uploads**: Support for loading images from your device gallery.
*   **Smart Visual Classification**: Powered by Gemini's multimodal capabilities, it identifies ingredients, estimates portion sizes, and provides precise nutritional metrics.
*   **Interactive Review & Edit**: Review the AI's classification, adjust estimated quantities, select meal categories (Breakfast, Lunch, Dinner, Snack), and easily save the logged items.

### 3. 💬 AI Nutrition Coach Chatbot
*   **Smart Conversations**: Chat directly with an AI coach who is fully context-aware of your dynamic daily goals, consumed meals, and historical metrics.
*   **Instant Guidance**: Ask questions like *"What's a high-protein dinner option?"* or *"How can I curb my sweet tooth?"*
*   **Dynamic Feedback**: Receive custom recommendations tailored to your active budget, calorie remaining, and target milestones.

### 4. 📊 Body Weight Monitor & Canvas Sparkline
*   **Interactive Metric Logger**: Log your current weight with automatic user profile integration.
*   **Adaptive Sparkline Trend Graph**: A custom-drawn visual sparkline on Compose `Canvas` highlighting your body weight history, complete with gradients, high-contrast markers, and trend lines.
*   **Target Milestones**: Live calculation of remaining weight to target with motivational indicators.

### 5. 💧 Hydration & Macro Progress
*   **Water Log**: Tracks your daily hydration level with modular indicators.
*   **Macro Progress Indicators**: Beautiful Material 3 progress bars visualizing Protein, Carbohydrate, and Fat metrics relative to your customized goals.

### 6. ⚙️ Smart Custom Profiles
*   **Adaptive Calculations**: Dynamically computes target base calories and optimal macronutrient ratios depending on age, height, gender, weight, activity level, and target goals (e.g., Weight Loss, Muscle Gain, or Maintenance).
*   **Dynamic Secrets**: Allows you to enter your personal Gemini API key in-app, or defaults safely to your secure AI Studio environment credentials.

---

## 🎨 Design System & Theming

Cal AI follows a custom **Unified Cosmic Palette** based on modern **Material Design 3 (M3)** principles:
*   **Primary Background (`SlateDark`)**: Deep Space `#0F172A`
*   **Container Card (`CardSlate`)**: `#1E293B`
*   **Muted Accents (`SlateMuted`)**: `#334155`
*   **High-Contrast Action Colors**:
    *   **Emerald Mint (`#10B981`)**: Utilized for protein achievements, positive progress indicators, and active UI states.
    *   **Neon Indigo (`#6366F1`)**: Utilized for carbohydrates, water trackers, and AI actions.
    *   **Amber Gold (`#F59E0B`)**: Utilized for lipid indicators, warnings, and weight logging metrics.
    *   **Coral Red (`#EF4444`)**: Utilized for calorie overages, delete alerts, and system warnings.

---

## 🏗️ Architecture & Stack

The codebase is engineered with modern Android architecture standards:
*   **Programming Language**: 100% Kotlin
*   **UI Framework**: Jetpack Compose (Declarative UI)
*   **Architecture Pattern**: Model-View-ViewModel (MVVM)
*   **Local Database**: SQLite managed via **Room Database** with Kotlin Coroutines for offline-first local persistence.
*   **Asynchronous Flow**: Kotlin Coroutines & `StateFlow` to provide reactive, thread-safe UI updates.
*   **Network Client**: Retrofit for interacting with remote services and fetching content.
*   **Image Loading**: Coil (Coroutines Image Loader) for smooth image rendering.
*   **Navigation**: Type-safe Jetpack Navigation Compose.

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer)
*   JDK 17+
*   Android SDK 34+

### Setup & Installation
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
    cd YOUR_REPO_NAME
    ```
2.  **Add your Gemini API Key**:
    *   Enter your personal key securely in the **Secrets panel** within AI Studio.
    *   Alternatively, run the app and navigate to the **Profile Screen** inside the app to save your API key securely to the local state.
3.  **Build and Run**:
    *   Open the project in Android Studio.
    *   Sync Gradle files.
    *   Press the **Run** button to launch on your favorite emulator or physical device.

---

## ☁️ How to Push this Project to your GitHub Account

Since this environment operates securely in the cloud, you can upload this complete codebase directly to your GitHub repository using the built-in integrations:

1.  **Open Project Settings**: Look for the **Settings / Export Menu** in the AI Studio sidebar or top navigation.
2.  **Authenticate with GitHub**: Select the **Push to GitHub** action. You will be prompted to authorize AI Studio to access your GitHub repositories.
3.  **Choose Repo Options**: Specify whether you want to create a new private or public repository and give it a title (e.g., `Cal-AI-Nutrition-Tracker`).
4.  **Complete the Push**: AI Studio will bundle your files, build structures, and push the entire commit history directly to your profile.
5.  **Offline Access**: Alternatively, you can select **Export as ZIP** from the settings menu to download the complete Android Studio-ready project to your local computer.
