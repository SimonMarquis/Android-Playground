# 🛝 Android Playground

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/SimonMarquis/Android-Playground?quickstart=1)

|                                      🌑                                      |                                       ☀️                                        |
|:----------------------------------------------------------------------------:|:-------------------------------------------------------------------------------:|
| [![screenshot dark](art/screenshot-thumb-dark.png)](art/screenshot-dark.png) | [![screenshot light](art/screenshot-thumb-light.png)](art/screenshot-light.png) |

#### 🐘 Gradle

| Task                                                             | Description                                                                                 |
|------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `gradlew assembleDebug`                                          | Build debug APK                                                                             |
| `gradlew assembleRelease`                                        | Build release APK (optimized & minified)                                                    |
| `gradlew apiCheck`                                               | Checks project public API ([BCV](https://github.com/Kotlin/binary-compatibility-validator)) |
| `gradlew apiDump`                                                | Dumps project public API ([BCV](https://github.com/Kotlin/binary-compatibility-validator))  |
| `gradlew dependencyGuard`                                        | Checks project dependencies                                                                 |
| `gradlew dependencyGuardBaseline`                                | Dumps project dependencies                                                                  |
| `gradlew globalCiBadging`                                        | CI badging checks                                                                           |
| `gradlew globalCiLint`                                           | CI Lint checks (html/sarif/txt/xml)                                                         |
| `gradlew globalCiUnitTest`                                       | CI unit tests (html/xml)                                                                    |
| `gradlew generateBaselineProfile`                                | Generates Baseline & Startup profiles                                                       |
| `gradlew connectedBenchmarkAndroidTest`                          | Runs benchmark tests                                                                        |
| `gradlew assembleRelease -Pplayground.compose.compilerMetrics`   | Compose compiler metrics                                                                    |
| `gradlew assembleRelease -Pplayground.compose.compilerReports`   | Compose compiler reports                                                                    |
| `gradlew --write-verification-metadata pgp,sha256 --export-keys` | Generates verification metadata & keyring                                                   |

#### 🐙 GitHub workflows

- [![🏭 CI](https://github.com/SimonMarquis/Android-Playground/actions/workflows/ci.yaml/badge.svg)](https://github.com/SimonMarquis/Android-Playground/actions/workflows/ci.yaml)
- [![♻️ Clear GitHub Actions cache](https://github.com/SimonMarquis/Android-Playground/actions/workflows/clear-cache.yaml/badge.svg)](https://github.com/SimonMarquis/Android-Playground/actions/workflows/clear-cache.yaml)
- [![🤖 Dependabot auto-merge](https://github.com/SimonMarquis/Android-Playground/actions/workflows/dependabot-auto-merge.yaml/badge.svg)](https://github.com/SimonMarquis/Android-Playground/actions/workflows/dependabot-auto-merge.yaml)
- [![🤖 Dependabot Gradle dependencies](https://github.com/SimonMarquis/Android-Playground/actions/workflows/dependabot-gradle-dependencies.yaml/badge.svg)](https://github.com/SimonMarquis/Android-Playground/actions/workflows/dependabot-gradle-dependencies.yaml)
- [![🐘 Gradle dependency submission](https://github.com/SimonMarquis/Android-Playground/actions/workflows/gradle-dependency-submission.yaml/badge.svg)](https://github.com/SimonMarquis/Android-Playground/actions/workflows/gradle-dependency-submission.yaml)
- [![🐘 Gradle experiments](https://github.com/SimonMarquis/Android-Playground/actions/workflows/gradle-experiments.yaml/badge.svg)](https://github.com/SimonMarquis/Android-Playground/actions/workflows/gradle-experiments.yaml)
- [![🐘 Gradle Wrapper updater](https://github.com/SimonMarquis/Android-Playground/actions/workflows/gradle-wrapper-updater.yaml/badge.svg)](https://github.com/SimonMarquis/Android-Playground/actions/workflows/gradle-wrapper-updater.yaml)
- [![⚡ Startup & Baseline Profiles](https://github.com/SimonMarquis/Android-Playground/actions/workflows/startup-baseline-profiles.yaml/badge.svg)](https://github.com/SimonMarquis/Android-Playground/actions/workflows/startup-baseline-profiles.yaml)

#### 🐙 GitHub composite actions

- [`📦 Archive JUnit reports`](.github/actions/archive-junit-reports/action.yaml)
- [`📦 Archive Lint reports`](.github/actions/archive-lint-reports/action.yaml)
- [`👮 Check git-lfs files`](.github/actions/check-git-lfs/action.yaml)
- [`🐘 Setup Gradle`](.github/actions/setup-gradle/action.yaml)
- [`🐘 Setup gradle.properties`](.github/actions/setup-gradle-properties/action.yaml)
- [`☕️ Setup Java`](.github/actions/setup-java/action.yaml)

<details>
<summary><h4>🏗️ Architecture…</h4></summary>

```mermaid
graph LR
  :app[app]:::android
  subgraph :feature
    :feature:home[home]:::android
  end
  subgraph :domain
    :domain:dice[dice]:::jvm
    :domain:settings[settings]:::jvm
  end
  subgraph :data
    :data:dice[dice]:::jvm
    :data:settings[settings]:::jvm
  end
  subgraph :core
    :core:android[android]:::android
    :core:datastore[datastore]:::jvm
    :core:di[di]:::jvm
    :core:ui[ui]:::android
    :core:utils[utils]:::jvm
  end

  :app -.-> :core:android
  :app -.-> :core:di
  :app -.-> :core:ui
  :app -.-> :data:dice
  :app -.-> :data:settings
  :app -.-> :feature:home
  :core:android -.-> :core:di
  :core:android -.-> :core:utils
  :core:datastore -.-> :core:di
  :core:utils -.-> :core:di
  :data:dice -.-> :core:datastore
  :data:dice -.->|test| :core:datastore
  :data:dice -.-> :core:di
  :data:dice -.->|test| :core:utils
  :data:dice -.-> :domain:dice
  :data:settings -.-> :core:datastore
  :data:settings -.->|test| :core:datastore
  :data:settings -.-> :core:di
  :data:settings -.->|test| :core:utils
  :data:settings -.-> :domain:settings
  :feature:home -.-> :core:android
  :feature:home -.-> :core:di
  :feature:home -.-> :core:ui
  :feature:home -.-> :domain:dice
  :feature:home -.-> :domain:settings

  classDef android fill:#3ddc84,stroke:#fff,stroke-width:2px,color:#fff;
  classDef jvm fill:#7F52FF,stroke:#fff,stroke-width:2px,color:#fff;
```

</details>
