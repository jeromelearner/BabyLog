




Role & Objective: You are an expert Android Developer specializing in Modern Android Development (MAD). Your task is to generate the complete, error-free source code for a simple Baby Feeding Tracker app. I am a beginner and will not do any debugging; the code must work out of the box.

Project Name: BabyLog Target SDK: Android 14, 15, 16 (API Level 34+) Language: Kotlin UI Framework: Jetpack Compose (Material 3) Architecture: MVVM (Model-View-ViewModel) Local Storage: Room Database (SQLite) - Strictly Offline, No Internet Permissions.

1. Functional Requirements (The "What")
   The app has 4 main recording features. All data must be persisted locally.

Milk Feeding: Record Start Time, Amount (ml).

Diaper Change: Record Time, Pee (Boolean), Pee Amount (Low/Medium/High), Poop (Boolean), Poop Color/Consistency (Text).

Solids: Record Time, Content (Text), Amount (Text/approx).

Sleep: Record Start Time, End Time (Duration calculated automatically in UI).

CRUD Operations: The user must be able to View a history list, Add new entries, and Delete entries (long press on list item).

2. Technical Stack & Constraints (The "How")
   Build: Use Kotlin DSL (build.gradle.kts).

UI: Use Jetpack Compose only (No XML). Use Material3 design components.

Navigation: Use androidx.navigation:navigation-compose.

Database: Use Room Database with KSP (Kotlin Symbol Processing).

Date/Time: Use java.time (LocalDateTime) and store as Long (Epoch/Timestamp) in the database for easy sorting.

State Management: Use ViewModel with StateFlow.

Edge-to-Edge: Ensure the app handles Android 14+ edge-to-edge display correctly (handle WindowInsets).

3. Database Schema (Room)
   Create a single Entity class named BabyLog.

Table: baby_logs

id: Int (PrimaryKey, AutoGenerate)

type: String (Enum: MILK, DIAPER, SOLIDS, SLEEP)

startTime: Long (Timestamp)

endTime: Long? (Nullable, used for Sleep)

amountMl: Int? (Nullable, for Milk)

hasPee: Boolean? (For Diaper)

peeAmount: String? (Enum/String: "Small", "Medium", "Large")

hasPoop: Boolean? (For Diaper)

poopDetails: String? (Color/Texture for Diaper)

foodContent: String? (For Solids)

foodAmount: String? (For Solids)

4. UI Structure
   Screen 1: Home (Timeline)

Show a list of logs sorted by startTime DESC (newest on top).

Each list item should clearly show the Type (Icon + Text), Time, and Summary of details.

Floating Action Button (FAB) or a Bottom Bar to trigger "Add New Log".

Screen 2: Add Log Dialog/Sheet

When adding, ask user to select Type first (Milk, Diaper, Solids, Sleep).

Based on type, show relevant input fields (e.g., if Milk -> Show Number Input for ML).

"Save" button to commit to Room DB.

5. Implementation Instructions for Junie
   Please generate the code in the following order. Do not use placeholders. Provide full content for each file.

Step 1: Dependencies (libs.versions.toml and build.gradle.kts)

Provide the updated libs.versions.toml ensuring support for Room, Compose, Navigation, and Serialization.

Provide the app/build.gradle.kts with necessary plugins (ksp) and dependencies.

Step 2: Data Layer

Create BabyLog.kt (Entity).

Create LogDao.kt (Interface with Insert, Delete, GetAll flows).

Create AppDatabase.kt (Abstract class).

Create LogRepository.kt.

Step 3: ViewModel

Create HomeViewModel.kt that holds the list state and functions to add/delete logs.

Step 4: UI Components

Create composables for the List Items (LogItemView).

Create composables for the Input Forms (AddLogScreen).

Step 5: Main Activity

Set up the MainActivity.kt with Navigation and the Database instance creation.

IMPORTANT: Since I cannot debug, please ensure imports are correct and logic is simple and robust. Use standard Android patterns.