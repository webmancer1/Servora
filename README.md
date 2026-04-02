#  Servora

**Real-Time Server Monitoring Dashboard for Android**

Servora is a sleek, dark-themed Android app that provides real-time monitoring of your server infrastructure. Built with Jetpack Compose and a cyberpunk-inspired UI, it delivers live-updating metrics, animated gauges, and instant alert visibility — all at a glance.

---

##  Features

- **Live Dashboard** — Monitor all servers in one view with auto-refreshing data (every 3 seconds)
- **Animated Gauges** — Circular CPU, Memory, and Disk gauges with color-coded thresholds (green → amber → red)
- **Server Detail View** — Drill into any server for full metrics, network I/O charts, and process lists
- **Pulsing Status Indicators** — Animated status badges for Online, Warning, Critical, and Offline states
- **Network I/O Charts** — Live sparkline charts tracking inbound and outbound traffic
- **Process Monitor** — Top processes with CPU and memory progress bars
- **Alert System** — Real-time alerts with severity levels (Info, Warning, Critical)
- **Dark Cyberpunk Theme** — Deep navy backgrounds, neon cyan/green accents, glassmorphism cards

---

##  Screenshots

> coming soon 

---

##  Architecture

Servora follows **MVVM** (Model-View-ViewModel) with clean separation of concerns:

```
com.example.servora/
├── ServoraApp.kt                       # Hilt Application entry point
├── MainActivity.kt                     # Navigation host
├── data/
│   ├── model/ServerModels.kt           # Data classes
│   └── repository/ServerRepository.kt  # Data source (mock → API ready)
├── di/AppModule.kt                     # Hilt dependency injection
└── ui/
    ├── theme/                          # Color, Typography, Theme
    ├── components/                     # Reusable UI components
    │   ├── GaugeChart.kt               # Animated circular gauge
    │   ├── MetricCard.kt               # Glassmorphism metric card
    │   ├── StatusBadge.kt              # Pulsing status indicator
    │   └── MiniLineChart.kt            # Sparkline chart
    ├── dashboard/                      # Dashboard screen + ViewModel
    └── detail/                         # Server detail screen + ViewModel
```

---

##  Tech Stack

| Layer | Technology |
|---|---|
| **UI** | Jetpack Compose + Material 3 |
| **Navigation** | Navigation Compose |
| **DI** | Dagger Hilt |
| **Networking** | Retrofit + Kotlinx Serialization |
| **Async** | Kotlin Coroutines + Flow |
| **Build** | Kotlin DSL + Version Catalog |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |

---

##  Getting Started

### Prerequisites

- **Android Studio** Meerkat (2024.3.1) or newer
- **JDK 11** or higher
- **Android SDK 36**

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Servora.git
   ```

2. Open the project in Android Studio

3. Sync Gradle and wait for dependencies to download

4. Run on an emulator or physical device:
   ```bash
   ./gradlew installDebug
   ```

---

##  Design System

### Color Palette

| Color | Hex | Usage |
|---|---|---|
| Deep Navy | `#0A0E1A` | Background |
| Charcoal | `#141824` | Surface |
| Neon Cyan | `#00E5FF` | Primary accent |
| Mint Green | `#00E676` | Success / Online |
| Amber | `#FFB300` | Warning |
| Coral Red | `#FF5252` | Critical / Error |

### Typography

- **UI Text**: System Sans-Serif (clean, modern)
- **Metric Values**: Monospace (data-dashboard feel)

---

##  Roadmap

- [ ] Connect to real server APIs via Retrofit
- [ ] Add authentication (API key / OAuth)
- [ ] Push notifications for critical alerts
- [ ] Historical data with persistent storage (Room)
- [ ] Server grouping and filtering
- [ ] Widget for home screen monitoring
- [ ] Multi-theme support (light mode)

---




