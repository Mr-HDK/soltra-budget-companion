# Soltra Budget Companion (Android)

Soltra is a local-first Android app for personal budget tracking. It focuses on quick daily input, budget control, account balance reconciliation, customizable charts, and reliable local export/backup workflows.

## Download APK (Recommended Distribution)

APK files should be distributed through **GitHub Releases**, not committed into the source repository.

- Latest release page: `https://github.com/<owner>/<repo>/releases/latest`
- Release assets: download the APK attached to the release (example: `soltra-v1.2.0.apk`)

### Install on Android

1. Open the latest release page.
2. Download the APK asset to your device.
3. Enable install from unknown sources (if Android asks).
4. Open the APK and complete installation.

### Optional Integrity Check (SHA-256)

When publishing a release, include a checksum file (for example `SHA256SUMS.txt`) and optionally verify:

```powershell
Get-FileHash .\soltra-vX.Y.Z.apk -Algorithm SHA256
```

## Core Features

- Quick expense logging with category, payment method, date/time, merchant label, and notes
- Monthly budget tracking with period customization (`month start day`)
- Category budgets and category-level spending visibility
- Account management and account-to-account transfer tracking
- Balance checkpoints with audit-style delta analysis
- Graph composer (multi-chart dashboard with drag-to-reorder)
- Quick templates and recurring expense rules
- CSV import, CSV export, and JSON backup/restore (with merge behavior)
- Reminder notifications for inactivity and checkpoint cadence
- Home screen widgets for budget overview, quick add, and chart snapshots

## Screen-by-Screen Feature Map

### Dashboard

- Today and month totals
- Budget usage and remaining amount
- Trend versus previous period
- Rolling totals
- Category spending breakdown
- Latest checkpoint context

### Expenses

- Create, edit, and delete expenses
- Filter by text, category, payment method, and period
- Supports manual date/time editing and optional note metadata

### Checkpoints

- Record bank and cash balances at a point in time
- Keep a checkpoint history
- Compute audit metrics between checkpoints:
  - expected balance from previous checkpoint minus expenses
  - actual recorded balance
  - uncontrolled delta (untracked inflow/outflow indicator)
- Edit or delete historical checkpoints

### Budgets Workspace

This is the central workspace for budget operations and has multiple sections.

#### Budget Section

- Set global monthly budget
- Set month start day (1..28) to align with non-calendar billing cycles
- Set currency code

#### Categories Section

- Create, rename, reorder, recolor, and re-icon categories
- Activate/deactivate categories
- Set per-category monthly budget
- Safe deletion logic:
  - if unused: direct delete
  - if linked to expenses/templates/recurring rules: requires replacement category
  - linked data is reassigned and category budgets are merged into replacement

#### Accounts Section

- Create and manage accounts (cash, bank, card, e-wallet, other)
- Track account balances and account trends
- Rename, activate/deactivate, update type and balance
- Safe deletion logic:
  - if no transfer history: direct delete
  - if transfer history exists: requires replacement account, then transfer links are reassigned

#### Transfers Section

- Record transfers between two accounts with amount, date/time, and note
- Validations include:
  - source and destination must differ
  - amount must be positive
  - both accounts must be active
  - source must have sufficient balance
- Recent transfer history is visible in the UI

### Templates Workspace

- Quick templates:
  - define default amount/category/payment/note presets
  - edit/delete templates
- Recurring rules:
  - daily/weekly/monthly frequencies
  - interval-based scheduling
  - activation flag
  - manual "run due now" generation for pending recurrences

### Graphs

- Compose multiple charts in a single dashboard
- Chart types: pie, donut, bars
- Periods: current/previous month, current/previous year, custom range
- Grouping: category, payment method, month
- Reorder widgets via long-press drag
- Persisted graph configuration and layout
- Current maximum: 12 graph widgets

### Settings

- Export folder and backup file selection (SAF)
- Manual export and restore
- Auto-backup enable/disable and interval configuration
- Reminder toggles and inactivity threshold
- Widget defaults (category/payment)
- App language preference
- Appearance mode
- App lock mode (optional PIN)
- CSV import mapping and execution

## Widgets

- `BudgetOverviewWidgetProvider`: budget status and period snapshot
- `QuickAddWidgetProvider`: fast expense entry path
- `ChartWidgetProvider`: chart snapshot rendered from configured graph inputs

## Data and Export

Primary exported files include:

- `transactions.csv`
- `checkpoints.csv`
- `budgets.csv`
- `monthly_summary.csv`
- `backup.json`

The app supports both folder-based exports and direct backup file updates. Backup restore is designed for practical merge workflows rather than blind overwrite-only behavior.

## Tech Stack

- Kotlin
- Jetpack Compose
- Room
- DataStore
- WorkManager
- Storage Access Framework (SAF)

## Project Structure

- `app/src/main/java/com/hdk/soltra/MainActivity.kt`
- `app/src/main/java/com/hdk/soltra/ui/BudgetCompanionRoot.kt`
- `app/src/main/java/com/hdk/soltra/ui/MainViewModel.kt`
- `app/src/main/java/com/hdk/soltra/data/local/`
- `app/src/main/java/com/hdk/soltra/data/repository/`
- `app/src/main/java/com/hdk/soltra/sync/`
- `app/src/main/java/com/hdk/soltra/widget/`

## Local Setup

Requirements:

1. Android SDK installed
2. Java 17
3. Local SDK path configured through `local.properties`

Create local config:

```powershell
Copy-Item local.properties.example local.properties
```

Then edit `local.properties` and set:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

## Build and Test

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

macOS/Linux:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```
