# Mizo Keyboard - Android Studio Project (IME)

Welcome! This is the complete, production-ready Android Keyboard (Input Method Service) project specially engineered for the Mizo language.

## Key Features
- **Native Mizo Unicode Support**: Dedicated and popup keys for `Ṭ / ṭ` and `Ṅ / ṅ`, plus circumflex vowels (`â, ê, î, ô, û`).
- **Embedded Offline Dictionary**: Bundled `mizo_dictionary.json` directly inside `app/src/main/assets/`. Zero downloads or manual TXT file imports needed!
- **Intelligent Autocorrect & Typos**: Automatically converts common phonetic typos (e.g. `tha` -> `ṭha`, `thian` -> `ṭhian`, `nhal` -> `ṅhal`).
- **Next-Word Prediction**: Anticipates common Mizo phrases (e.g. `ka` -> `lawm e`, `dam` -> `em?`).
- **Personal Dictionary**: Add custom words directly from the keyboard candidate bar or the Settings app.
- **100% Privacy Offline Guarantee**: No `android.permission.INTERNET` in `AndroidManifest.xml`. Your typing never leaves your device.

---

## Easiest Way to Build the APK (Choose Option A or Option B)

### Option A: Free 1-Click Cloud Build (No Android Studio or Coding Required!)
1. Create a free account at [GitHub.com](https://github.com) if you don't already have one.
2. Click **New Repository** and name it `mizo-keyboard`.
3. Upload all the files from this unzipped folder into your repository.
4. Click on the **Actions** tab at the top of your GitHub repository.
5. GitHub will automatically run the pre-configured workflow (`.github/workflows/build-apk.yml`) and build `app-debug.apk` in ~2 minutes!
6. Click on the completed run and download your installable `MizoKeyboard-Debug-APK` directly to your phone.

### Option B: Build on your PC using Android Studio
1. Download and install [Android Studio](https://developer.android.com/studio) (free).
2. Open Android Studio and choose **File > Open**.
3. Select this unzipped `MizoKeyboard-Android` folder.
4. Wait for Gradle to finish syncing (it takes ~1-2 minutes).
5. In the top menu, click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. When done, Android Studio will show a popup at the bottom right: click **"locate"** to find your `app-debug.apk`.

---

## How to Install and Activate on your Android Phone
1. Transfer the `app-debug.apk` to your phone (via USB, Google Drive, WhatsApp, or email).
2. Tap the APK file to install. If your phone asks to **"Allow installation from unknown sources"**, tap **Allow**.
3. Open the installed **Mizo Keyboard** app from your home screen.
4. Follow the 2-step setup:
   - **Step 1**: Tap "Enable in Settings" and toggle **Mizo Keyboard** ON.
   - **Step 2**: Tap "Select Mizo Keyboard" and choose it as your active keyboard.
5. You can now type in any app (WhatsApp, Facebook, Messages, Notes) with full Mizo dictionary suggestions and `Ṭ` / `Ṅ` keys!
