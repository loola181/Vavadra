# Vavarda Clicker (Android)

Минимальный Android-кликер на Kotlin + Jetpack Compose с поддержкой ваших артов по уровням.

## Что уже реализовано

- Кликер-цикл: сбор Искр, прокачка силы клика и авто-прироста.
- Смена визуала героя по уровням.
- Фракции (Тьма/Свет) с отдельными иллюстрациями.
- Ритуал Отката (престиж), который обнуляет прогресс, но увеличивает рост уровня.
- Безопасная загрузка изображений по имени файла (если файла нет, показывается плейсхолдер).

## Куда положить ваши изображения

Скопируйте PNG/JPG в папку:

`/Users/andrejivliev/Desktop/Vavadra/app/src/main/res/drawable-nodpi`

## Как назвать файлы (точно)

- `vavarda_lvl_1`
- `vavarda_lvl_2`
- `vavarda_lvl_3`
- `vavarda_lvl_4`
- `vavarda_lvl_5`
- `vavarda_lvl_6`
- `vavarda_lvl_7`
- `vavarda_lvl_8`
- `vavarda_lvl_9`
- `vavarda_lvl_10`
- `vavarda_lvl_25`
- `vavarda_lvl_50`
- `vavarda_lvl_75`
- `vavarda_lvl_100`
- `vavarda_dark_legion` (картинка с темной командой/демонами)
- `vavarda_light_guardians` (картинка со светлой командой)

Расширение можно `.png` или `.webp`, главное сохранить имена в lower_snake_case.

## Запуск

1. Откройте проект в Android Studio.
2. Дождитесь `Gradle Sync`.
3. Запустите на эмуляторе или устройстве Android.

## Где логика

- Экран игры: `app/src/main/java/com/vavarda/clicker/MainActivity.kt`
- Тема: `app/src/main/java/com/vavarda/clicker/ui/theme/Theme.kt`

## Конфиг экономики

Экономика вынесена в JSON:

`app/src/main/assets/economy_config.json`

Там настраиваются:
- параметры tap/auto апгрейдов
- престиж (ритуал)
- уровни, milestones и награды
- бонусы фракций
- параметры дейликов, босса и оффлайн-дохода

## Новые механики

- Оффлайн-доход при возвращении в игру (с лимитом из конфига).
- Дейлики: 3 миссии в день с наградами.
- Фракционные бонусы Тьма/Свет (модификаторы клика, авто-прироста и урона по боссу).
- Циклическое событие босса с наградой за победу.
- Базовая локальная аналитика событий через интерфейс трекера (`Logcat`).
- UI-фидбек клика: вспышка, летящие цифры, haptic и системный click-sound.
- Отдельный экран настроек: звук, вибрация, оффлайн-доход, размер текста.
- Полностью русская локализация UI и narrative.
- Narrative-пакет в `app/src/main/assets/narrative_pack.json`.
- Smoke-тесты основных сценариев в `app/src/test/java/com/vavarda/clicker/GameSmokeTest.kt`.

## Симулятор прогрессии

Скрипт считает время до уровней (по умолчанию 10/25/50/75/100) с той же экономикой, что в игре:

```bash
./scripts/progression_simulator.py
```

Полезные параметры:

```bash
./scripts/progression_simulator.py --clicks-per-second 5
./scripts/progression_simulator.py --targets 10,25,50,75,100 --no-prestige
```
