# MedReminder 

An android medication reminder app build for our Mobile App Development term project 

> **Platform:** Android (API 24+) | **Language:** Java | **Team:** Pragyay · Ibrahim · Omar

## What it does 

MedReminder helps users track their daily medications with schedules alarms, dosage logging, refill tracking, and a visual consistency history. 
Built as a open source ad-free alternative to existing apps on Android. 

### Features (Subject to change)

### Core 

| Feature | Owner   |
|---|---------|
| Daily medication schedule | Omar    |
| Dosage tracking (taken / snoozed / missed) | Pragyay |
| Refill reminders when supply runs low | Ibrahim |
| Emergency contact + share med list | Ibrahim |

### Extra

| Feature | Owner   |
|---|---------|
| Alarm + notification + lock screen alert | Pragyay |
| Smart snooze with escalation | Pragyay |
| Barcode scanner to add medications | Ibrahim |
| Adherence heatmap calendar | Omar    |

### Nice to have

| Feature | Owner |
|---|---|
| Dark / light mode toggle | Omar |

git 

## Branch structure
```
main          ← stable only, never push here directly
└── dev       ← integration branch, all features merge here first
    ├── feature/today-screen        (Omar)
    ├── feature/dosage-tracking     (Pragyay)
    ├── feature/medications-tab     (Ibrahim)
    ├── feature/alarms              (Pragyay)
    ├── feature/snooze              (Pragyay)
    ├── feature/barcode             (Ibrahim)
    ├── feature/history-calendar    (Omar)
    ├── feature/settings            (Omar)
    ├── xml/layouts                 (everyone)
    └── xml/drawables               (everyone)
```

**Rules:**
- Never push directly to `main` or `dev` PLEASE; Always open a Pull Request
- Pull `dev` into your branch every morning before you start coding so everyone is working with the latest code
- One PR review required before merging into `dev`; As soon as you're done with a feature make a PR to merge into 'dev', PR requests will be reviewed by me every midnight :)

## Setup

1. Clone the repo: `git clone git@github.com:mrtonguelicker/MedReminder.git`
2. Open in Android Studio
3. Let Gradle sync
4. Checkout your assigned branch: `git checkout feature/your-branch`
5. Run on an emulator or physical device (API 24+)

## Feature to branch mapping

| Feature | Branch |
|---|---|
| Daily medication schedule | `feature/today-screen` |
| Dosage tracking (taken / snoozed / missed) | `feature/dosage-tracking` |
| Refill reminders when supply runs low | `feature/medications-tab` |
| Emergency contact + share med list | `feature/medications-tab` |
| Alarm + notification + lock screen alert | `feature/alarms` |
| Smart snooze with escalation | `feature/snooze` |
| Barcode scanner to add medications | `feature/barcode` |
| Adherence heatmap calendar | `feature/history-calendar` |
| Dark / light mode toggle | `feature/settings` |