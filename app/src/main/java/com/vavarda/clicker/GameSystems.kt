package com.vavarda.clicker

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.IsoFields
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt
import kotlin.random.Random

enum class DailyMissionType {
    TAPS,
    UPGRADES,
    ESSENCE_EARNED
}

data class DailyMission(
    val slot: Int,
    val type: DailyMissionType,
    val target: Long,
    val rewardEssence: Long
)

data class ReturnStreakMilestoneReward(
    val title: String,
    val rewardEssence: Long,
    val rewardShards: Int
)

enum class BossTickEvent {
    SPAWNED,
    EXPIRED
}

fun currentDayKey(zoneId: ZoneId = ZoneId.systemDefault()): String {
    return LocalDate.now(zoneId).toString()
}

fun currentWeekKey(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val now = LocalDate.now(zoneId)
    return "${now.get(IsoFields.WEEK_BASED_YEAR)}-W${now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
}

fun normalizeDailyProgress(state: GameState, dayKey: String): GameState {
    if (state.daily.dayKey == dayKey) {
        return state
    }
    return state.copy(
        daily = DailyProgress(
            dayKey = dayKey,
            taps = 0,
            upgrades = 0,
            essenceEarned = 0L,
            claimedMissionSlots = emptySet()
        )
    )
}

fun normalizeAltarProgress(state: GameState, weekKey: String): GameState {
    if (state.retention.altarWeekKey == weekKey) {
        return state
    }
    return state.copy(
        retention = state.retention.copy(
            altarWeekKey = weekKey,
            altarFavor = 0,
            altarClaimedTiers = emptySet()
        )
    )
}

fun generateDailyMissions(dayKey: String, config: DailyConfig): List<DailyMission> {
    val seed = dayKey.hashCode().toLong().absolute()

    val tapTarget = config.tapBaseTarget + ((seed % 5L).toInt() * config.tapStep)
    val upgradesTarget = config.upgradeBaseTarget + (((seed / 7L) % 4L).toInt() * config.upgradeStep)
    val essenceTarget = config.essenceBaseTarget + (((seed / 13L) % 5L) * config.essenceStep)

    return listOf(
        DailyMission(
            slot = 1,
            type = DailyMissionType.TAPS,
            target = tapTarget.toLong(),
            rewardEssence = (tapTarget * 12L).coerceAtLeast(1200L)
        ),
        DailyMission(
            slot = 2,
            type = DailyMissionType.UPGRADES,
            target = upgradesTarget.toLong(),
            rewardEssence = (upgradesTarget * 850L).coerceAtLeast(2000L)
        ),
        DailyMission(
            slot = 3,
            type = DailyMissionType.ESSENCE_EARNED,
            target = essenceTarget,
            rewardEssence = (essenceTarget / 2L).coerceAtLeast(2500L)
        )
    ).take(config.missionCount.coerceIn(1, 3))
}

fun addTapProgress(state: GameState): GameState {
    return state.copy(
        daily = state.daily.copy(taps = state.daily.taps + 1)
    )
}

fun addUpgradeProgress(state: GameState): GameState {
    return state.copy(
        daily = state.daily.copy(upgrades = state.daily.upgrades + 1)
    )
}

fun addEssenceProgress(state: GameState, amount: Long): GameState {
    if (amount <= 0L) {
        return state
    }
    return state.copy(
        daily = state.daily.copy(essenceEarned = state.daily.essenceEarned + amount)
    )
}

fun missionProgress(state: GameState, mission: DailyMission): Long {
    return when (mission.type) {
        DailyMissionType.TAPS -> state.daily.taps.toLong()
        DailyMissionType.UPGRADES -> state.daily.upgrades.toLong()
        DailyMissionType.ESSENCE_EARNED -> state.daily.essenceEarned
    }
}

fun isMissionCompleted(state: GameState, mission: DailyMission): Boolean {
    return missionProgress(state, mission) >= mission.target
}

fun isMissionClaimed(state: GameState, mission: DailyMission): Boolean {
    return mission.slot in state.daily.claimedMissionSlots
}

fun claimDailyMission(state: GameState, mission: DailyMission, config: EconomyConfig): GameState {
    if (!isMissionCompleted(state, mission) || isMissionClaimed(state, mission)) {
        return state
    }

    val rewardedState = state.copy(
        essence = state.essence + mission.rewardEssence,
        daily = state.daily.copy(
            claimedMissionSlots = state.daily.claimedMissionSlots + mission.slot
        )
    )
    return awardRelicShards(rewardedState, amount = 1, config = config).first
}

fun factionBonus(config: EconomyConfig, path: AlignmentPath): FactionBonusConfig {
    return config.factions[path] ?: config.factions[AlignmentPath.SHADOW]!!
}

fun manualTapGain(tapPower: Int, faction: FactionBonusConfig): Long {
    return (tapPower * faction.tapMultiplier).roundToLong().coerceAtLeast(1L)
}

fun autoTickGain(autoPower: Int, faction: FactionBonusConfig): Long {
    return (autoPower * faction.autoMultiplier).roundToLong().coerceAtLeast(0L)
}

fun critChance(level: Int, retention: RetentionConfig): Double {
    val chance = retention.crit.baseChance + level * retention.crit.chancePerLevel
    return chance.coerceIn(0.0, 0.85)
}

fun critMultiplier(level: Int, retention: RetentionConfig): Double {
    return (retention.crit.baseMultiplier + level * retention.crit.multiplierPerLevel).coerceAtLeast(1.0)
}

fun streakThreshold(level: Int, retention: RetentionConfig): Int {
    val threshold = retention.streak.baseThreshold - level * retention.streak.thresholdReductionPerLevel
    return threshold.roundToLong().toInt().coerceAtLeast(3)
}

fun streakBurstMultiplier(level: Int, retention: RetentionConfig): Double {
    return (retention.streak.baseBurstMultiplier + level * retention.streak.burstMultiplierPerLevel).coerceAtLeast(1.0)
}

fun offlineBoostMultiplier(level: Int, retention: RetentionConfig): Double {
    return (retention.offlineBoost.baseMultiplier + level * retention.offlineBoost.multiplierPerLevel).coerceAtLeast(1.0)
}

fun cacheCooldownMinutes(level: Int, retention: RetentionConfig): Int {
    val config = retention.cache
    return (config.baseCooldownMinutes - level * config.cooldownReductionPerLevel)
        .coerceAtLeast(config.minCooldownMinutes)
}

fun cacheReward(level: Int, playerLevel: Int, retention: RetentionConfig): Long {
    val config = retention.cache
    return (
        config.baseReward +
            level.toLong() * config.rewardPerLevel +
            playerLevel.toLong() * config.rewardPerPlayerLevel
        ).coerceAtLeast(config.baseReward)
}

fun returnStreakMilestoneForDay(day: Int, retention: RetentionConfig): ReturnStreakMilestoneConfig? {
    return retention.returnStreak.milestones.firstOrNull { it.day == day }
}

fun relicShardsNeeded(level: Int, retention: RetentionConfig): Int {
    val config = retention.relic
    if (level >= config.maxLevel) return 0
    return (config.baseShardsForLevel + level * config.shardsGrowthPerLevel).coerceAtLeast(1)
}

fun relicTapMultiplier(level: Int, retention: RetentionConfig): Double {
    return (1.0 + level * retention.relic.tapMultiplierPerLevel).coerceAtLeast(1.0)
}

fun relicAutoMultiplier(level: Int, retention: RetentionConfig): Double {
    return (1.0 + level * retention.relic.autoMultiplierPerLevel).coerceAtLeast(1.0)
}

fun relicBossMultiplier(level: Int, retention: RetentionConfig): Double {
    return (1.0 + level * retention.relic.bossMultiplierPerLevel).coerceAtLeast(1.0)
}

fun retentionUpgradeCost(baseCost: Long, costGrowth: Double, currentLevel: Int): Long {
    val normalizedLevel = currentLevel.coerceAtLeast(0)
    return (baseCost * costGrowth.pow(normalizedLevel.toDouble())).roundToLong().coerceAtLeast(baseCost)
}

fun bossHitDamage(tapPower: Int, faction: FactionBonusConfig): Long {
    return (tapPower * faction.bossDamageMultiplier).roundToLong().coerceAtLeast(1L)
}

fun upgradeCost(baseCost: Long, currentPower: Int, offset: Int): Long {
    val power = (currentPower + offset).coerceAtLeast(1).toLong()
    return baseCost * power * power
}

fun advanceBossState(state: GameState, level: Int, config: EconomyConfig): Pair<GameState, BossTickEvent?> {
    val boss = state.boss

    if (boss.isActive) {
        val nextLeft = boss.secondsLeft - 1
        if (nextLeft <= 0) {
            return state.copy(
                boss = boss.copy(
                    isActive = false,
                    currentHp = 0L,
                    maxHp = 0L,
                    secondsUntilSpawn = config.boss.spawnIntervalSeconds,
                    secondsLeft = 0
                )
            ) to BossTickEvent.EXPIRED
        }

        return state.copy(
            boss = boss.copy(secondsLeft = nextLeft)
        ) to null
    }

    val nextSpawn = boss.secondsUntilSpawn - 1
    if (nextSpawn > 0) {
        return state.copy(
            boss = boss.copy(secondsUntilSpawn = nextSpawn)
        ) to null
    }

    val maxHp = (config.boss.baseHp + level.toLong() * config.boss.hpPerLevel).coerceAtLeast(1L)
    return state.copy(
        boss = BossState(
            isActive = true,
            currentHp = maxHp,
            maxHp = maxHp,
            secondsUntilSpawn = 0,
            secondsLeft = config.boss.fightDurationSeconds
        )
    ) to BossTickEvent.SPAWNED
}

fun hitBoss(state: GameState, damage: Long, config: EconomyConfig): Pair<GameState, Long?> {
    val boss = state.boss
    if (!boss.isActive || boss.currentHp <= 0L) {
        return state to null
    }

    val nextHp = (boss.currentHp - damage).coerceAtLeast(0L)
    if (nextHp > 0L) {
        return state.copy(
            boss = boss.copy(currentHp = nextHp)
        ) to null
    }

    val reward = (boss.maxHp * config.boss.rewardPerHp).coerceAtLeast(1L)
    var defeatedState = state.copy(
        essence = state.essence + reward,
        boss = BossState(
            isActive = false,
            currentHp = 0L,
            maxHp = 0L,
            secondsUntilSpawn = config.boss.spawnIntervalSeconds,
            secondsLeft = 0
        )
    )
    defeatedState = addEssenceProgress(defeatedState, reward)
    return awardRelicShards(defeatedState, amount = 2, config = config).first to reward
}

fun applyOfflineIncome(
    snapshot: GameSnapshot,
    config: EconomyConfig,
    nowMillis: Long,
    dayKey: String
): Pair<GameSnapshot, Long> {
    var state = primeFreshRetentionState(snapshot.state, config, nowMillis, dayKey)
    val settings = snapshot.settings

    val lastActive = state.lastActiveAtMillis
    if (!settings.offlineIncomeEnabled || !config.offline.enabled || lastActive <= 0L) {
        state = state.copy(lastActiveAtMillis = nowMillis)
        return GameSnapshot(state = state, settings = settings) to 0L
    }

    val elapsedSeconds = ((nowMillis - lastActive) / 1000L)
        .coerceAtLeast(0L)
        .coerceAtMost(config.offline.maxSeconds.toLong())

    if (elapsedSeconds <= 0L || state.autoPower <= 0) {
        state = state.copy(lastActiveAtMillis = nowMillis)
        return GameSnapshot(state = state, settings = settings) to 0L
    }

    val faction = factionBonus(config, state.path)
    val perSecond = (
        autoTickGain(state.autoPower, faction).toDouble() *
            relicAutoMultiplier(state.retention.relicLevel, config.retention)
        ).roundToLong()
    val total = (
        perSecond.toDouble() *
            elapsedSeconds.toDouble() *
            offlineBoostMultiplier(state.retention.offlineBoostLevel, config.retention)
        ).roundToLong()

    if (total <= 0L) {
        state = state.copy(lastActiveAtMillis = nowMillis)
        return GameSnapshot(state = state, settings = settings) to 0L
    }

    state = state.copy(
        essence = state.essence + total,
        lastActiveAtMillis = nowMillis
    )
    state = addEssenceProgress(state, total)

    return GameSnapshot(state = state, settings = settings) to total
}

fun createNewGameSnapshot(
    config: EconomyConfig,
    nowMillis: Long,
    dayKey: String
): GameSnapshot {
    return GameSnapshot(
        state = primeFreshRetentionState(defaultGameState(config), config, nowMillis, dayKey),
        settings = GameSettings()
    )
}

fun primeFreshRetentionState(
    state: GameState,
    config: EconomyConfig,
    nowMillis: Long,
    dayKey: String,
    weekKey: String = currentWeekKey()
): GameState {
    val normalized = normalizeAltarProgress(normalizeDailyProgress(state, dayKey), weekKey)
    if (normalized.lastActiveAtMillis > 0L) {
        return normalized
    }

    val seededRetention = normalized.retention.copy(
        chestReadyAtMillis = if (normalized.retention.chestReadyAtMillis > 0L) {
            normalized.retention.chestReadyAtMillis
        } else {
            nowMillis + cacheCooldownMinutes(normalized.retention.cacheLevel, config.retention) * 60_000L
        },
        lastDailyReturnClaimDayKey = normalized.retention.lastDailyReturnClaimDayKey.ifBlank { dayKey },
        altarWeekKey = if (normalized.retention.altarWeekKey.isBlank()) weekKey else normalized.retention.altarWeekKey
    )

    return normalized.copy(
        retention = seededRetention,
        lastActiveAtMillis = nowMillis
    )
}

fun performManualTap(state: GameState, config: EconomyConfig): Pair<GameState, Long> {
    val faction = factionBonus(config, state.path)
    var gain = (
        manualTapGain(state.tapPower, faction).toDouble() *
            relicTapMultiplier(state.retention.relicLevel, config.retention)
        ).roundToLong().coerceAtLeast(1L)
    var retention = state.retention

    val critTriggered = Random.nextDouble() < critChance(retention.critLevel, config.retention)
    if (critTriggered) {
        gain = (gain * critMultiplier(retention.critLevel, config.retention)).roundToLong().coerceAtLeast(1L)
    }

    val nextCharge = retention.streakCharge + 1
    val triggerThreshold = streakThreshold(retention.streakLevel, config.retention)
    val streakTriggered = nextCharge >= triggerThreshold
    if (streakTriggered) {
        gain = (gain * streakBurstMultiplier(retention.streakLevel, config.retention)).roundToLong().coerceAtLeast(1L)
    }

    retention = retention.copy(
        streakCharge = if (streakTriggered) 0 else nextCharge
    )

    var nextState = state.copy(
        essence = state.essence + gain,
        ftue = state.ftue.copy(firstClickDone = true),
        retention = retention
    )
    nextState = addTapProgress(nextState)
    nextState = addEssenceProgress(nextState, gain)
    return nextState to gain
}

fun buyTapUpgrade(state: GameState, config: EconomyConfig): Pair<GameState, Long>? {
    val cost = upgradeCost(config.tap.baseCost, state.tapPower, config.tap.costOffset)
    if (state.essence < cost) {
        return null
    }

    var nextState = state.copy(
        essence = state.essence - cost,
        tapPower = state.tapPower + config.tap.powerPerUpgrade,
        ftue = state.ftue.copy(firstUpgradeDone = true)
    )
    nextState = addUpgradeProgress(nextState)
    return nextState to cost
}

fun buyAutoUpgrade(state: GameState, config: EconomyConfig): Pair<GameState, Long>? {
    val cost = upgradeCost(config.auto.baseCost, state.autoPower, config.auto.costOffset)
    if (state.essence < cost) {
        return null
    }

    var nextState = state.copy(
        essence = state.essence - cost,
        autoPower = state.autoPower + config.auto.powerPerUpgrade,
        ftue = state.ftue.copy(firstUpgradeDone = true)
    )
    nextState = addUpgradeProgress(nextState)
    return nextState to cost
}

fun performRitual(state: GameState, config: EconomyConfig): Pair<GameState, Long>? {
    val cost = config.prestige.baseCost * (state.rituals + 1)
    if (state.essence < cost) {
        return null
    }

    return state.copy(
        essence = 0L,
        tapPower = config.prestige.resetTapPower,
        autoPower = config.prestige.resetAutoPower,
        rituals = state.rituals + 1,
        ftue = state.ftue.copy(firstRitualDone = true),
        retention = state.retention.copy(streakCharge = 0)
    ) to cost
}

fun buyCritUpgrade(state: GameState, config: EconomyConfig): Pair<GameState, Long>? {
    val retention = config.retention.crit
    val currentLevel = state.retention.critLevel
    if (currentLevel >= retention.maxLevel) {
        return null
    }

    val cost = retentionUpgradeCost(
        baseCost = retention.baseCost,
        costGrowth = retention.costGrowth,
        currentLevel = currentLevel
    )
    if (state.essence < cost) {
        return null
    }

    var nextState = state.copy(
        essence = state.essence - cost,
        retention = state.retention.copy(critLevel = currentLevel + 1),
        ftue = state.ftue.copy(firstUpgradeDone = true)
    )
    nextState = addUpgradeProgress(nextState)
    return nextState to cost
}

fun buyStreakUpgrade(state: GameState, config: EconomyConfig): Pair<GameState, Long>? {
    val retention = config.retention.streak
    val currentLevel = state.retention.streakLevel
    if (currentLevel >= retention.maxLevel) {
        return null
    }

    val cost = retentionUpgradeCost(
        baseCost = retention.baseCost,
        costGrowth = retention.costGrowth,
        currentLevel = currentLevel
    )
    if (state.essence < cost) {
        return null
    }

    var nextState = state.copy(
        essence = state.essence - cost,
        retention = state.retention.copy(
            streakLevel = currentLevel + 1,
            streakCharge = 0
        ),
        ftue = state.ftue.copy(firstUpgradeDone = true)
    )
    nextState = addUpgradeProgress(nextState)
    return nextState to cost
}

fun buyOfflineBoostUpgrade(state: GameState, config: EconomyConfig): Pair<GameState, Long>? {
    val retention = config.retention.offlineBoost
    val currentLevel = state.retention.offlineBoostLevel
    if (currentLevel >= retention.maxLevel) {
        return null
    }

    val cost = retentionUpgradeCost(
        baseCost = retention.baseCost,
        costGrowth = retention.costGrowth,
        currentLevel = currentLevel
    )
    if (state.essence < cost) {
        return null
    }

    var nextState = state.copy(
        essence = state.essence - cost,
        retention = state.retention.copy(offlineBoostLevel = currentLevel + 1),
        ftue = state.ftue.copy(firstUpgradeDone = true)
    )
    nextState = addUpgradeProgress(nextState)
    return nextState to cost
}

fun buyCacheUpgrade(state: GameState, config: EconomyConfig): Pair<GameState, Long>? {
    val retention = config.retention.cache
    val currentLevel = state.retention.cacheLevel
    if (currentLevel >= retention.maxLevel) {
        return null
    }

    val cost = retentionUpgradeCost(
        baseCost = retention.baseCost,
        costGrowth = retention.costGrowth,
        currentLevel = currentLevel
    )
    if (state.essence < cost) {
        return null
    }

    var nextState = state.copy(
        essence = state.essence - cost,
        retention = state.retention.copy(cacheLevel = currentLevel + 1),
        ftue = state.ftue.copy(firstUpgradeDone = true)
    )
    nextState = addUpgradeProgress(nextState)
    return nextState to cost
}

fun isArcaneCacheReady(state: GameState, nowMillis: Long): Boolean {
    val readyAt = state.retention.chestReadyAtMillis
    return readyAt <= 0L || readyAt <= nowMillis
}

fun cacheReadyInSeconds(state: GameState, nowMillis: Long): Int {
    return ((state.retention.chestReadyAtMillis - nowMillis) / 1000L).coerceAtLeast(0L).toInt()
}

data class CacheClaimResult(
    val state: GameState,
    val rewardEssence: Long,
    val shardReward: Int
)

fun claimArcaneCache(
    state: GameState,
    config: EconomyConfig,
    nowMillis: Long
): CacheClaimResult? {
    if (!isArcaneCacheReady(state, nowMillis)) {
        return null
    }

    val reward = cacheReward(
        level = state.retention.cacheLevel,
        playerLevel = calculateStateLevel(state, config),
        retention = config.retention
    )
    val shardReward = config.retention.cache.shardReward
    var nextState = state.copy(
        essence = state.essence + reward,
        retention = state.retention.copy(
            chestReadyAtMillis = nowMillis + cacheCooldownMinutes(state.retention.cacheLevel, config.retention) * 60_000L
        )
    )
    nextState = addEssenceProgress(nextState, reward)
    nextState = awardRelicShards(nextState, amount = shardReward, config = config).first
    return CacheClaimResult(
        state = nextState,
        rewardEssence = reward,
        shardReward = shardReward
    )
}

fun isReturnStreakClaimAvailable(state: GameState, dayKey: String): Boolean {
    return state.retention.lastDailyReturnClaimDayKey != dayKey
}

fun predictedReturnStreakDay(state: GameState, dayKey: String): Int {
    if (!isReturnStreakClaimAvailable(state, dayKey)) {
        return state.retention.returnStreak.coerceAtLeast(1)
    }

    val previousDay = parseDayKey(state.retention.lastDailyReturnClaimDayKey)
    val today = parseDayKey(dayKey) ?: return 1
    return when {
        previousDay == null -> 1
        previousDay.plusDays(1) == today -> state.retention.returnStreak + 1
        else -> 1
    }.coerceAtLeast(1)
}

fun returnStreakRewardForDay(day: Int, retention: RetentionConfig): Long {
    return (
        retention.returnStreak.baseReward +
            (day - 1).coerceAtLeast(0).toLong() * retention.returnStreak.rewardPerDay
        ).coerceAtLeast(retention.returnStreak.baseReward)
}

data class ReturnStreakClaimResult(
    val state: GameState,
    val rewardEssence: Long,
    val streakDay: Int,
    val shardReward: Int,
    val milestone: ReturnStreakMilestoneReward? = null
)

fun claimReturnStreak(
    state: GameState,
    config: EconomyConfig,
    dayKey: String
): ReturnStreakClaimResult? {
    if (!isReturnStreakClaimAvailable(state, dayKey)) {
        return null
    }

    val previousDay = parseDayKey(state.retention.lastDailyReturnClaimDayKey)
    val today = parseDayKey(dayKey) ?: return null
    val nextStreak = when {
        previousDay == null -> 1
        previousDay.plusDays(1) == today -> state.retention.returnStreak + 1
        else -> 1
    }
    val reward = returnStreakRewardForDay(nextStreak, config.retention)
    val shardReward = if (
        config.retention.returnStreak.shardEveryDays > 0 &&
        nextStreak % config.retention.returnStreak.shardEveryDays == 0
    ) {
        config.retention.returnStreak.shardsPerMilestone
    } else {
        0
    }
    val milestone = returnStreakMilestoneForDay(nextStreak, config.retention)
    val totalReward = reward + (milestone?.rewardEssence ?: 0L)
    val totalShards = shardReward + (milestone?.rewardShards ?: 0)

    var nextState = state.copy(
        essence = state.essence + totalReward,
        retention = state.retention.copy(
            returnStreak = nextStreak,
            bestReturnStreak = maxOf(state.retention.bestReturnStreak, nextStreak),
            lastDailyReturnClaimDayKey = dayKey
        )
    )
    nextState = addEssenceProgress(nextState, totalReward)
    if (totalShards > 0) {
        nextState = awardRelicShards(nextState, amount = totalShards, config = config).first
    }

    return ReturnStreakClaimResult(
        state = nextState,
        rewardEssence = totalReward,
        streakDay = nextStreak,
        shardReward = totalShards,
        milestone = milestone?.let {
            ReturnStreakMilestoneReward(
                title = it.title,
                rewardEssence = it.rewardEssence,
                rewardShards = it.rewardShards
            )
        }
    )
}

fun awardSeasonalAltarFavor(state: GameState, amount: Int, weekKey: String): GameState {
    if (amount <= 0) return normalizeAltarProgress(state, weekKey)
    val normalized = normalizeAltarProgress(state, weekKey)
    return normalized.copy(
        retention = normalized.retention.copy(
            altarFavor = normalized.retention.altarFavor + amount
        )
    )
}

fun seasonalAltarProgress(state: GameState, config: RetentionConfig): Float {
    val lastTier = config.altar.tiers.maxByOrNull { it.requiredFavor } ?: return 0f
    if (lastTier.requiredFavor <= 0) return 1f
    return (state.retention.altarFavor.toFloat() / lastTier.requiredFavor.toFloat()).coerceIn(0f, 1f)
}

fun canClaimSeasonalAltarTier(state: GameState, tier: SeasonalAltarTierConfig, weekKey: String): Boolean {
    val normalized = normalizeAltarProgress(state, weekKey)
    return normalized.retention.altarFavor >= tier.requiredFavor &&
        tier.tier !in normalized.retention.altarClaimedTiers
}

data class SeasonalAltarClaimResult(
    val state: GameState,
    val tier: SeasonalAltarTierConfig
)

fun claimSeasonalAltarTier(
    state: GameState,
    tier: SeasonalAltarTierConfig,
    config: EconomyConfig,
    weekKey: String
): SeasonalAltarClaimResult? {
    val normalized = normalizeAltarProgress(state, weekKey)
    if (!canClaimSeasonalAltarTier(normalized, tier, weekKey)) {
        return null
    }

    var nextState = normalized.copy(
        essence = normalized.essence + tier.rewardEssence,
        retention = normalized.retention.copy(
            altarClaimedTiers = normalized.retention.altarClaimedTiers + tier.tier
        )
    )
    nextState = addEssenceProgress(nextState, tier.rewardEssence)
    if (tier.rewardShards > 0) {
        nextState = awardRelicShards(nextState, amount = tier.rewardShards, config = config).first
    }

    return SeasonalAltarClaimResult(
        state = nextState,
        tier = tier
    )
}

private fun awardRelicShards(
    state: GameState,
    amount: Int,
    config: EconomyConfig
): Pair<GameState, Int> {
    if (amount <= 0) return state to 0

    var retention = state.retention.copy(
        relicShards = (state.retention.relicShards + amount).coerceAtLeast(0)
    )
    var levelsGained = 0
    while (retention.relicLevel < config.retention.relic.maxLevel) {
        val need = relicShardsNeeded(retention.relicLevel, config.retention)
        if (need <= 0 || retention.relicShards < need) break
        retention = retention.copy(
            relicShards = retention.relicShards - need,
            relicLevel = retention.relicLevel + 1
        )
        levelsGained += 1
    }
    return state.copy(retention = retention) to levelsGained
}

private fun parseDayKey(dayKey: String): LocalDate? {
    if (dayKey.isBlank()) return null
    return runCatching { LocalDate.parse(dayKey) }.getOrNull()
}

private fun calculateStateLevel(state: GameState, config: EconomyConfig): Int {
    val ritualBonus = state.rituals * config.prestige.levelBonusPerRitual
    val essencePart = sqrt(state.essence.toDouble() / config.levels.essenceFactor.toDouble()).toInt()
    return (1 + ritualBonus + essencePart).coerceIn(1, config.levels.maxLevel)
}

private fun Long.absolute(): Long {
    return if (this < 0) -this else this
}
