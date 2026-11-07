# TidyFlow — трекер привычек и задач с геймификацией

Учебный проект (тема 4). Нативное Android-приложение на **Kotlin** с разметкой на **XML**:
каждый экран — отдельная `Activity` со своим layout-файлом.

- `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`
- Android Gradle Plugin 8.7.3, Kotlin 2.0.21, Gradle 8.9, JDK 17
- Тёмная монохромная тема (чёрно-белые акценты), Material 3, ViewBinding, Room (KSP), Coroutines

## Как открыть
1. Распаковать архив.
2. Android Studio → **File → Open** → папка `TidyFlow`.
3. Дождаться Gradle Sync (нужен интернет) → **Run ▶**.

## Экраны (Activity ↔ layout)
| Экран по ТЗ | Activity | Layout |
|---|---|---|
| Экран загрузки | `SplashActivity` | `activity_splash.xml` |
| Авторизация | `LoginActivity` | `activity_login.xml` |
| Регистрация | `RegisterActivity` | `activity_register.xml` |
| Главный экран: сетка привычек на день / неделю + панель навигации | `MainActivity` | `activity_main.xml`, `item_habit_day.xml`, `item_habit_week.xml` |
| Создание и редактирование привычки | `HabitEditActivity` | `activity_habit_edit.xml` |
| Статистика и графики | `StatsActivity` | `activity_stats.xml` |
| Достижения (ачивки) | `AchievementsActivity` | `activity_achievements.xml`, `item_achievement.xml` |

Панель навигации (`BottomNavigationView`): Главная / Статистика / Достижения.

## Геймификация
- +10 XP за каждую отметку, каждые 100 XP — новый уровень.
- Анимация при отметке (увеличение и пружинистый возврат), сообщение «+10 XP» или «Новый уровень».
- 7 достижений: первый шаг, серии 3 и 7 дней, 10 и 50 выполнений, 3 привычки, 3 уровень.

## Структура
```
TidyFlow/
├── build.gradle.kts, settings.gradle.kts, gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts            # зависимости
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/depri/tidyflow/
        │   │   ├── data/   # Room: сущности, DAO, база
        │   │   ├── ui/     # Activity, адаптеры, BarChartView
        │   │   └── util/   # Session, PasswordHasher, Game, Achievements
        │   └── res/        # layout, values, drawable, menu, mipmap
        └── test/           # unit-тесты игровой логики
```

## Зависимости (app/build.gradle.kts)
core-ktx, appcompat, activity-ktx, lifecycle-runtime-ktx, kotlinx-coroutines-android,
material, constraintlayout, recyclerview, room-runtime, room-ktx, room-compiler (KSP), junit.
