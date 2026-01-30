这是一项非常有挑战性但也非常可行的任务。既然你是完全的新手，并且要求 **Junie** (一个基于AI的编程辅助工具) 全权负责代码生成，那么我为你准备的这份文档必须极其**精确、全面且结构化**。

我们不能只告诉 AI "我要做一个婴儿应用"，我们必须告诉它每一个变量名、每一个数据库字段、每一个界面的逻辑，以及具体使用什么技术栈（以确保能在 Android 14-16 上运行）。

以下是你需要发给 Junie 的**完整指令文档（Master Prompt）**。请直接复制以下内容发送给 Junie。

---

### 复制以下内容发送给 Junie

**Role & Objective:**
You are an expert Android Developer specializing in **Modern Android Development (MAD)**. Your task is to generate the **complete, error-free source code** for a simple Baby Feeding Tracker app. I am a beginner and will not do any debugging; the code must work out of the box.

**Project Name:** BabyLog
**Target SDK:** Android 14, 15, 16 (API Level 34+)
**Language:** Kotlin
**UI Framework:** Jetpack Compose (Material 3)
**Architecture:** MVVM (Model-View-ViewModel)
**Local Storage:** Room Database (SQLite) - **Strictly Offline, No Internet Permissions.**

---

### 1. Functional Requirements (The "What")

The app has 4 main recording features. All data must be persisted locally.

1. **Milk Feeding:** Record Start Time, Amount (ml).
2. **Diaper Change:** Record Time, Pee (Boolean), Pee Amount (Low/Medium/High), Poop (Boolean), Poop Color/Consistency (Text).
3. **Solids:** Record Time, Content (Text), Amount (Text/approx).
4. **Sleep:** Record Start Time, End Time (Duration calculated automatically in UI).

**CRUD Operations:** The user must be able to View a history list, Add new entries, and Delete entries (long press on list item).

### 2. Technical Stack & Constraints (The "How")

* **Build:** Use Kotlin DSL (`build.gradle.kts`).
* **UI:** Use **Jetpack Compose** only (No XML). Use `Material3` design components.
* **Navigation:** Use `androidx.navigation:navigation-compose`.
* **Database:** Use **Room Database** with KSP (Kotlin Symbol Processing).
* **Date/Time:** Use `java.time` (LocalDateTime) and store as Long (Epoch/Timestamp) in the database for easy sorting.
* **State Management:** Use `ViewModel` with `StateFlow`.
* **Edge-to-Edge:** Ensure the app handles Android 14+ edge-to-edge display correctly (handle WindowInsets).

### 3. Database Schema (Room)

Create a single Entity class named `BabyLog`.

**Table: `baby_logs**`

* `id`: Int (PrimaryKey, AutoGenerate)
* `type`: String (Enum: MILK, DIAPER, SOLIDS, SLEEP)
* `startTime`: Long (Timestamp)
* `endTime`: Long? (Nullable, used for Sleep)
* `amountMl`: Int? (Nullable, for Milk)
* `hasPee`: Boolean? (For Diaper)
* `peeAmount`: String? (Enum/String: "Small", "Medium", "Large")
* `hasPoop`: Boolean? (For Diaper)
* `poopDetails`: String? (Color/Texture for Diaper)
* `foodContent`: String? (For Solids)
* `foodAmount`: String? (For Solids)

### 4. UI Structure

* **Screen 1: Home (Timeline)**
* Show a list of logs sorted by `startTime` DESC (newest on top).
* Each list item should clearly show the Type (Icon + Text), Time, and Summary of details.
* Floating Action Button (FAB) or a Bottom Bar to trigger "Add New Log".


* **Screen 2: Add Log Dialog/Sheet**
* When adding, ask user to select Type first (Milk, Diaper, Solids, Sleep).
* Based on type, show relevant input fields (e.g., if Milk -> Show Number Input for ML).
* "Save" button to commit to Room DB.



---

### 5. Implementation Instructions for Junie

Please generate the code in the following order. **Do not use placeholders.** Provide full content for each file.

**Step 1: Dependencies (`libs.versions.toml` and `build.gradle.kts`)**

* Provide the updated `libs.versions.toml` ensuring support for Room, Compose, Navigation, and Serialization.
* Provide the `app/build.gradle.kts` with necessary plugins (ksp) and dependencies.

**Step 2: Data Layer**

* Create `BabyLog.kt` (Entity).
* Create `LogDao.kt` (Interface with Insert, Delete, GetAll flows).
* Create `AppDatabase.kt` (Abstract class).
* Create `LogRepository.kt`.

**Step 3: ViewModel**

* Create `HomeViewModel.kt` that holds the list state and functions to add/delete logs.

**Step 4: UI Components**

* Create composables for the List Items (`LogItemView`).
* Create composables for the Input Forms (`AddLogScreen`).

**Step 5: Main Activity**

* Set up the `MainActivity.kt` with Navigation and the Database instance creation.

**IMPORTANT:** Since I cannot debug, please ensure imports are correct and logic is simple and robust. Use standard Android patterns.

---

### 给用户的操作指南（Read Me）

在你把上面的英文内容发给 Junie 之后，它会生成一系列的代码块。由于你是第一次做，请严格按照以下步骤操作：

1. **下载并安装 Android Studio:**
* 去谷歌搜索 "Download Android Studio Ladybug" (或者最新版本)。安装时一路点 "Next" 即可。


2. **创建新项目:**
* 打开 Android Studio -> **New Project**。
* 选择 **"Empty Activity"** (注意图标通常是 Compose 的 logo)。
* **Name:** `BabyLog`
* **Package name:** `com.example.babylog` (或者你自己喜欢的名字，但要记住它，因为代码里的 `package` 声明要对应)。
* **Language:** Kotlin.
* **Build Configuration Language:** Kotlin DSL (Recommended).
* 点击 **Finish**，等待它下载一堆东西（右下角进度条跑完）。


3. **如何使用 Junie 生成的代码:**
* Junie 会给你文件名，例如 `app/build.gradle.kts` 或 `MainActivity.kt`。
* 在 Android Studio 左侧的项目视图（Project View）中找到对应的文件。
* **全选 (Ctrl+A / Cmd+A)** 那个文件的原有内容，**删除**，然后**粘贴** Junie 给你的新代码。


4. **关键注意点:**
* **Gradle 文件:** 如果修改了 `build.gradle.kts` 或 `libs.versions.toml`，Android Studio 右上角会出现一个由大象图标组成的按钮叫 **"Sync Now"**。一定要点它，并等待同步成功。
* **包名一致性:** 确保 Junie 生成的代码第一行 `package com.example.babylog` 和你创建项目时的设置一致。如果不一致，手动改一下第一行即可。


5. **运行:**
* 用 USB 线把你的安卓手机连上电脑（确保护眼模式/开发者模式里的 USB 调试已打开）。
* 点击 Android Studio 顶部的绿色三角形 **Run** 按钮。


6. **打包 (最后一步):**
* 等你测试没问题了，点击菜单栏 **Build** -> **Generate Signed Bundle / APK**，选择 **APK**，然后创建一个新的密钥（KeyStore），按照提示填密码即可生成安装包发送到手机上。



现在，请把上面的英文 Prompt 复制发给 Junie 吧！