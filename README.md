# Campus Study Group Finder (Android Client)

An Android application built with **Jetpack Compose**, **Android Credential Manager**, and **Supabase Auth / Postgrest** that helps college students discover, create, and join campus study sessions based on courses, locations, and schedules.

---

## Key Features

- **Google OAuth Authentication**: Modern native Google Sign-In powered by Android Credential Manager and Supabase Auth (`GoTrue`).
- **Supabase Backend Integration**: Authenticated user sessions and database persistence via Supabase Postgrest API.
- **Material 3 UI**: Declarative Jetpack Compose interface with theme support and responsive state management.

---

## Repository Architecture

This project is part of a multi-repository layout:
* **Frontend App (This Repo)**: [cortes9/Project02](https://github.com/cortes9/Project02)
* **Backend API / DB Repo**: [cortes9/Project02-Backend](https://github.com/cortes9/Project02-Backend)
* **Proposal & Specification**: [`docs/proposal.md`](docs/proposal.md)

---

## Getting Started & Local Setup

### 1. Prerequisites
- **Android Studio** (2024.1+ recommended) with JDK 11 or JDK 17.
- **Android Device or Emulator** running API 24+ with **Google Play Services** enabled.

---

### 2. Environment Configuration (`.env.local`)

Secrets and API keys are loaded dynamically into `BuildConfig` at compile time and are ignored by Git.

Create a file named `.env.local` in the project root directory with the following variables:

```properties
# Supabase Configuration
SUPABASE_URL=https://<your-project-ref>.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Google OAuth Web Application Client ID
WEB_CLIENT_ID=862676647140-q2pc6smjqsm3ur1u1g5652mutvuo9ba0.apps.googleusercontent.com
```

> [!CAUTION]
> Never put Supabase `service_role` keys or direct PostgreSQL database passwords in this Android repository. The Android app connects securely via HTTPS using `SUPABASE_URL` and `SUPABASE_ANON_KEY`.

---

### 3. Google OAuth & Supabase Setup for Teammates

To run Google Sign-In on a new development machine or emulator:

1. **Obtain your Debug SHA-1 Fingerprint**:
   In Android Studio Terminal, run:
   ```powershell
   .\gradlew signingReport
   ```
2. **Google Cloud Console**:
   - Ensure an **Android Client ID** exists in Google Cloud Console under the same project with:
     - Package Name: `com.example.studygroupfinder`
     - SHA-1 Certificate Fingerprint: *(Output from `signingReport`)*
   - Verify your **OAuth Consent Screen** is configured and your email is added under **Test users** if in Testing status.
3. **Supabase Dashboard**:
   - In **Authentication $\rightarrow$ Providers $\rightarrow$ Google**:
     - Enable Google Provider.
     - Add the **Web Application Client ID** and **Client Secret**.
     - Enable **Skip nonce checks** under Advanced Settings.

---

## App Code Architecture

```
app/src/main/java/com/example/studygroupfinder/
├── data/
│   ├── remote/
│   │   └── SupabaseClient.kt       # Supabase client singleton (Auth + Postgrest)
│   └── repository/
│       └── AuthRepository.kt       # Google ID token exchange with Supabase
├── ui/
│   └── auth/
│       ├── LoginScreen.kt          # Material 3 Compose Login UI
│       ├── LoginViewModel.kt       # CredentialManager & state flow handler
│       └── LoginState.kt          # UI state definitions (Idle, Loading, Success, Error)
└── MainActivity.kt                 # Application entry point
```

---

## Building & Running

To build the debug APK:
```powershell
.\gradlew assembleDebug
```

To run tests:
```powershell
.\gradlew test
```
