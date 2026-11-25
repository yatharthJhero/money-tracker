# Getting started on Windows

## Install / verify tools

- [ ] Install Android Studio from the official Android developer website.
- [ ] In Android Studio, install the recommended Android SDK platform and SDK Build-Tools using SDK Manager.
- [ ] Install Git for Windows.
- [ ] Create or sign in to a GitHub account.
- [ ] Verify `git --version` in PowerShell.
- [ ] Verify Android Studio can create and run a new Empty Activity project on an emulator or USB-debugging-enabled Android phone.

Do not independently install a random JDK version. Use the JDK bundled with Android Studio unless the selected Android Gradle Plugin explicitly requires another setup.

## Create the repository

1. Create a GitHub repository named `money-tracker`.
2. Set it to **Private** initially.
3. Add the README and `.gitignore` from this starter pack, or push the local folder.
4. Keep credentials, signing keys, local SDK paths and private participant notes out of Git.
5. Make the first commit: `chore: initialize Money Tracker project`.

## Create the Android app

In Android Studio:
1. Choose **New Project** → **Empty Activity** (Compose-based template).
2. Name: `Money Tracker`.
3. Package/application ID: choose a stable reverse-domain ID you control; do not copy a placeholder blindly.
4. Language: Kotlin.
5. Minimum SDK: choose after checking the target devices you want to support; record the decision.
6. Finish and wait for Gradle sync.
7. Run the starter app once before adding features.

Record Android Studio, Android Gradle Plugin, Gradle, Kotlin, Compose and minimum SDK versions in `docs/TECH_STACK.md`.

## First local verification

- [ ] Gradle sync succeeds.
- [ ] App launches on emulator or physical device.
- [ ] No secrets or local machine paths are committed.
- [ ] First commit is pushed to GitHub.
