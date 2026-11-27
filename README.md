# 💰 Money Tracker

A personal finance management application designed to help users track income, monitor expenses, manage budgets, and understand their financial habits.

## 📌 Project Overview

Money Tracker is a personal finance tracking application that aims to make money management simple and organized.

The application will help users record their daily financial transactions, categorize expenses, monitor income, and gain insights into their spending patterns.

## 🎯 Project Objectives

- Track daily income and expenses.
- Monitor monthly spending.
- Organize transactions into categories.
- Manage personal budgets.
- View financial summaries and reports.
- Improve financial planning and money management.

## ✨ Planned Features

- [ ] User Registration and Login
- [ ] Dashboard with Financial Overview
- [ ] Add, Edit, and Delete Transactions
- [ ] Income and Expense Tracking
- [ ] Expense Categorization
- [ ] Monthly and Yearly Reports
- [ ] Budget Management
- [ ] Interactive Charts and Visualizations
- [ ] Transaction History
- [ ] Responsive User Interface

## 🛠️ Technology Stack

Technology choices will be finalized during development.

- **Frontend:** To be finalized
- **Backend:** To be finalized
- **Database:** To be finalized
- **Version Control:** Git and GitHub

## 📂 Project Structure

```text
money-tracker/
├── app/                    # Android application module (Kotlin, Jetpack Compose)
│   └── src/
│       ├── main/           # App source, resources and AndroidManifest.xml
│       ├── test/           # Local JVM unit tests
│       └── androidTest/    # Instrumented tests (emulator or device)
├── gradle/                 # Version catalog (libs.versions.toml) and Gradle wrapper
├── docs/                   # Project documentation (canonical)
│   └── planning/           # Archived pre-repository planning notes
├── research/               # Research guidelines (no personal data)
├── CLAUDE.md               # Working agreement for AI-assisted development
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

`docs/planning/` contains archived planning notes written before this repository existed. Where they conflict with `docs/*.md`, the `docs/*.md` files take precedence.

*The project structure will evolve as development progresses.*

## ⚙️ Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yatharthJhero/money-tracker.git
```

### 2. Navigate to the Project Directory

```bash
cd money-tracker
```

### 3. Open in Android Studio

Open the cloned folder in Android Studio and let Gradle sync finish. Android Studio creates `local.properties` with your Android SDK path; this file is machine-specific and git-ignored.

### 4. Build and Test

Use the JDK bundled with Android Studio. From the project root:

```powershell
# Windows
.\gradlew.bat assembleDebug testDebugUnitTest
```

```bash
# macOS / Linux
./gradlew assembleDebug testDebugUnitTest
```

To run the app, select the `app` run configuration in Android Studio and choose an emulator or a connected device.

## 📊 Future Enhancements

- Financial analytics and insights.
- Savings goals and tracking.
- Export financial reports.
- Recurring transaction management.
- Advanced dashboard visualizations.
- Secure cloud-based data storage.

## 🚀 Project Status

**Status:** Initial Development

This project is currently under development. Features and documentation will be updated as the application evolves.

## 👨‍💻 Author

**Yatharth Jha**

GitHub: [@yatharthJhero](https://github.com/yatharthJhero)

## 📄 License

License to be decided.

---

⭐ If you find this project interesting, feel free to explore the repository and follow its development!