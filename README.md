# Villager Panic (Fabric 1.20.1)

Мод для Minecraft 1.20.1 (Fabric): когда житель получает урон от игрока или любого другого моба,
он и все жители в радиусе 16 блоков убегают от источника опасности (8 секунд паники).

## Сборка

Нужны JDK 17 и Gradle.

```bash
gradle build
```

Готовый jar появится в `build/libs/villagerpanic-1.0.0.jar`
(файл без `-sources` — это и есть мод).

## Установка

1. Установите [Fabric Loader](https://fabricmc.net/use/) для Minecraft 1.20.1.
2. Положите в папку `mods`:
   - `villagerpanic-1.0.0.jar`
   - [Fabric API](https://modrinth.com/mod/fabric-api) для 1.20.1.

## Настройка

Константы в `src/main/java/com/example/villagerpanic/PanicManager.java`:

- `PANIC_DURATION_TICKS` — длительность паники (160 тиков = 8 секунд);
- `FLEE_SPEED` — скорость бегства;
- `SPREAD_RADIUS` — радиус, в котором паника передаётся другим жителям;
- `FLEE_DISTANCE` — на какое расстояние убегать от угрозы.
