package com.vavarda.clicker

enum class AlignmentPath {
    SHADOW,
    LIGHT
}

data class FtueProgress(
    val firstClickDone: Boolean = false,
    val firstUpgradeDone: Boolean = false,
    val firstRitualDone: Boolean = false
)

data class DailyProgress(
    val dayKey: String,
    val taps: Int,
    val upgrades: Int,
    val essenceEarned: Long,
    val claimedMissionSlots: Set<Int>
)

data class BossState(
    val isActive: Boolean,
    val currentHp: Long,
    val maxHp: Long,
    val secondsUntilSpawn: Int,
    val secondsLeft: Int
)

data class RetentionProgress(
    val critLevel: Int = 0,
    val streakLevel: Int = 0,
    val offlineBoostLevel: Int = 0,
    val streakCharge: Int = 0,
    val cacheLevel: Int = 0,
    val chestReadyAtMillis: Long = 0L,
    val returnStreak: Int = 0,
    val bestReturnStreak: Int = 0,
    val lastDailyReturnClaimDayKey: String = "",
    val relicShards: Int = 0,
    val relicLevel: Int = 0,
    val altarWeekKey: String = "",
    val altarFavor: Int = 0,
    val altarClaimedTiers: Set<Int> = emptySet()
)

data class GameState(
    val essence: Long,
    val tapPower: Int,
    val autoPower: Int,
    val rituals: Int,
    val path: AlignmentPath,
    val ftue: FtueProgress,
    val claimedRewardLevels: Set<Int>,
    val daily: DailyProgress,
    val boss: BossState,
    val retention: RetentionProgress,
    val lastActiveAtMillis: Long
)

data class GameSettings(
    val showFtueHints: Boolean = true,
    val compactNumbers: Boolean = true,
    val offlineIncomeEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val textScale: Float = 1f,
    val guideCompleted: Boolean = false,
    val profileName: String = "",
    val profileImageUri: String = ""
)

data class GameSnapshot(
    val state: GameState,
    val settings: GameSettings
)

fun defaultGameState(config: EconomyConfig): GameState {
    return GameState(
        essence = 0L,
        tapPower = config.tap.startingPower,
        autoPower = config.auto.startingPower,
        rituals = 0,
        path = AlignmentPath.SHADOW,
        ftue = FtueProgress(),
        claimedRewardLevels = emptySet(),
        daily = DailyProgress(
            dayKey = "",
            taps = 0,
            upgrades = 0,
            essenceEarned = 0L,
            claimedMissionSlots = emptySet()
        ),
        boss = BossState(
            isActive = false,
            currentHp = 0L,
            maxHp = 0L,
            secondsUntilSpawn = config.boss.spawnIntervalSeconds,
            secondsLeft = 0
        ),
        retention = RetentionProgress(),
        lastActiveAtMillis = 0L
    )
}
