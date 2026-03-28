package com.vavarda.clicker

import android.content.Context
import org.json.JSONObject

data class EconomyConfig(
    val tap: TapEconomyConfig,
    val auto: AutoEconomyConfig,
    val prestige: PrestigeEconomyConfig,
    val levels: LevelEconomyConfig,
    val rewards: List<RewardConfig>,
    val factions: Map<AlignmentPath, FactionBonusConfig>,
    val daily: DailyConfig,
    val boss: BossConfig,
    val offline: OfflineIncomeConfig,
    val retention: RetentionConfig
)

data class TapEconomyConfig(
    val startingPower: Int,
    val baseCost: Long,
    val costOffset: Int,
    val powerPerUpgrade: Int
)

data class AutoEconomyConfig(
    val startingPower: Int,
    val baseCost: Long,
    val costOffset: Int,
    val powerPerUpgrade: Int
)

data class PrestigeEconomyConfig(
    val baseCost: Long,
    val levelBonusPerRitual: Int,
    val resetTapPower: Int,
    val resetAutoPower: Int
)

data class LevelEconomyConfig(
    val maxLevel: Int,
    val essenceFactor: Long,
    val targetLevels: List<Int>,
    val artMilestones: List<ArtMilestone>
)

data class ArtMilestone(
    val level: Int,
    val drawableName: String,
    val title: String
)

data class FactionBonusConfig(
    val name: String,
    val description: String,
    val tapMultiplier: Double,
    val autoMultiplier: Double,
    val bossDamageMultiplier: Double
)

data class DailyConfig(
    val missionCount: Int,
    val tapBaseTarget: Int,
    val tapStep: Int,
    val upgradeBaseTarget: Int,
    val upgradeStep: Int,
    val essenceBaseTarget: Long,
    val essenceStep: Long
)

data class BossConfig(
    val spawnIntervalSeconds: Int,
    val fightDurationSeconds: Int,
    val baseHp: Long,
    val hpPerLevel: Long,
    val rewardPerHp: Long
)

data class OfflineIncomeConfig(
    val enabled: Boolean,
    val maxSeconds: Int
)

data class RetentionConfig(
    val crit: CritRetentionConfig,
    val streak: StreakRetentionConfig,
    val offlineBoost: OfflineBoostRetentionConfig,
    val cache: CacheRetentionConfig,
    val returnStreak: ReturnStreakRetentionConfig,
    val relic: RelicRetentionConfig,
    val altar: SeasonalAltarConfig
)

data class CritRetentionConfig(
    val baseCost: Long,
    val costGrowth: Double,
    val maxLevel: Int,
    val baseChance: Double,
    val chancePerLevel: Double,
    val baseMultiplier: Double,
    val multiplierPerLevel: Double
)

data class StreakRetentionConfig(
    val baseCost: Long,
    val costGrowth: Double,
    val maxLevel: Int,
    val baseThreshold: Int,
    val thresholdReductionPerLevel: Double,
    val baseBurstMultiplier: Double,
    val burstMultiplierPerLevel: Double
)

data class OfflineBoostRetentionConfig(
    val baseCost: Long,
    val costGrowth: Double,
    val maxLevel: Int,
    val baseMultiplier: Double,
    val multiplierPerLevel: Double
)

data class CacheRetentionConfig(
    val baseCost: Long,
    val costGrowth: Double,
    val maxLevel: Int,
    val baseCooldownMinutes: Int,
    val cooldownReductionPerLevel: Int,
    val minCooldownMinutes: Int,
    val baseReward: Long,
    val rewardPerLevel: Long,
    val rewardPerPlayerLevel: Long,
    val shardReward: Int
)

data class ReturnStreakRetentionConfig(
    val baseReward: Long,
    val rewardPerDay: Long,
    val shardEveryDays: Int,
    val shardsPerMilestone: Int,
    val showcaseDays: Int,
    val milestones: List<ReturnStreakMilestoneConfig>
)

data class RelicRetentionConfig(
    val maxLevel: Int,
    val baseShardsForLevel: Int,
    val shardsGrowthPerLevel: Int,
    val tapMultiplierPerLevel: Double,
    val autoMultiplierPerLevel: Double,
    val bossMultiplierPerLevel: Double
)

data class ReturnStreakMilestoneConfig(
    val day: Int,
    val title: String,
    val rewardEssence: Long,
    val rewardShards: Int
)

data class SeasonalAltarConfig(
    val favorFromDaily: Int,
    val favorFromBoss: Int,
    val favorFromCache: Int,
    val favorFromReturn: Int,
    val tiers: List<SeasonalAltarTierConfig>
)

data class SeasonalAltarTierConfig(
    val tier: Int,
    val title: String,
    val requiredFavor: Int,
    val rewardEssence: Long,
    val rewardShards: Int
)

enum class RewardKind {
    ESSENCE,
    TAP_POWER,
    AUTO_POWER
}

data class RewardConfig(
    val level: Int,
    val title: String,
    val description: String,
    val kind: RewardKind,
    val amount: Int
)

fun loadEconomyConfig(context: Context): EconomyConfig {
    return loadJsonAssetOrDefault(
        context = context,
        assetName = "economy_config.json",
        defaultValue = ::defaultEconomyConfig,
        parser = ::parseEconomyConfig
    )
}

internal fun parseEconomyConfig(raw: String): EconomyConfig {
    val root = JSONObject(raw)

    val tap = root.getJSONObject("tap")
    val auto = root.getJSONObject("auto")
    val prestige = root.getJSONObject("prestige")
    val levels = root.getJSONObject("levels")
    val daily = root.optJSONObject("daily")
    val boss = root.optJSONObject("boss")
    val offline = root.optJSONObject("offline")
    val retention = root.optJSONObject("retention")
    val critRetention = retention?.optJSONObject("crit")
    val streakRetention = retention?.optJSONObject("streak")
    val offlineBoostRetention = retention?.optJSONObject("offlineBoost")
    val cacheRetention = retention?.optJSONObject("cache")
    val returnStreakRetention = retention?.optJSONObject("returnStreak")
    val relicRetention = retention?.optJSONObject("relic")
    val altarRetention = retention?.optJSONObject("altar")

    val returnStreakMilestones = buildList {
        val defaultMilestones = defaultReturnStreakMilestones()
        val items = returnStreakRetention?.optJSONArray("milestones")
        if (items == null) {
            addAll(defaultMilestones)
        } else {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                add(
                    ReturnStreakMilestoneConfig(
                        day = item.optInt("day", defaultMilestones.getOrNull(index)?.day ?: 7),
                        title = item.optString("title", defaultMilestones.getOrNull(index)?.title ?: "Ритуал серии"),
                        rewardEssence = item.optLong("rewardEssence", defaultMilestones.getOrNull(index)?.rewardEssence ?: 5000L),
                        rewardShards = item.optInt("rewardShards", defaultMilestones.getOrNull(index)?.rewardShards ?: 1)
                    )
                )
            }
        }
    }.sortedBy { it.day }

    val altarTiers = buildList {
        val defaultTiers = defaultSeasonalAltarTiers()
        val items = altarRetention?.optJSONArray("tiers")
        if (items == null) {
            addAll(defaultTiers)
        } else {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                add(
                    SeasonalAltarTierConfig(
                        tier = item.optInt("tier", defaultTiers.getOrNull(index)?.tier ?: (index + 1)),
                        title = item.optString("title", defaultTiers.getOrNull(index)?.title ?: "Ступень алтаря"),
                        requiredFavor = item.optInt("requiredFavor", defaultTiers.getOrNull(index)?.requiredFavor ?: 10),
                        rewardEssence = item.optLong("rewardEssence", defaultTiers.getOrNull(index)?.rewardEssence ?: 3000L),
                        rewardShards = item.optInt("rewardShards", defaultTiers.getOrNull(index)?.rewardShards ?: 1)
                    )
                )
            }
        }
    }.sortedBy { it.requiredFavor }

    val artMilestones = buildList {
        val items = levels.getJSONArray("artMilestones")
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            add(
                ArtMilestone(
                    level = item.getInt("level"),
                    drawableName = item.getString("drawableName"),
                    title = item.getString("title")
                )
            )
        }
    }.sortedBy { it.level }

    val targetLevels = buildList {
        val items = levels.getJSONArray("targetLevels")
        for (index in 0 until items.length()) {
            add(items.getInt(index))
        }
    }.sorted()

    val rewards = buildList {
        val items = root.getJSONArray("rewards")
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            add(
                RewardConfig(
                    level = item.getInt("level"),
                    title = item.getString("title"),
                    description = item.getString("description"),
                    kind = RewardKind.valueOf(item.getString("kind").uppercase()),
                    amount = item.getInt("amount")
                )
            )
        }
    }.sortedBy { it.level }

    val defaultFactions = defaultFactionBonuses()
    val factionsJson = root.optJSONObject("factions")
    val parsedFactions = AlignmentPath.values().associateWith { path ->
        val pathJson = factionsJson?.optJSONObject(path.name)
        if (pathJson == null) {
            defaultFactions.getValue(path)
        } else {
            FactionBonusConfig(
                name = pathJson.optString("name", defaultFactions.getValue(path).name),
                description = pathJson.optString("description", defaultFactions.getValue(path).description),
                tapMultiplier = pathJson.optDouble("tapMultiplier", defaultFactions.getValue(path).tapMultiplier),
                autoMultiplier = pathJson.optDouble("autoMultiplier", defaultFactions.getValue(path).autoMultiplier),
                bossDamageMultiplier = pathJson.optDouble("bossDamageMultiplier", defaultFactions.getValue(path).bossDamageMultiplier)
            )
        }
    }

    return EconomyConfig(
        tap = TapEconomyConfig(
            startingPower = tap.getInt("startingPower"),
            baseCost = tap.getLong("baseCost"),
            costOffset = tap.getInt("costOffset"),
            powerPerUpgrade = tap.getInt("powerPerUpgrade")
        ),
        auto = AutoEconomyConfig(
            startingPower = auto.getInt("startingPower"),
            baseCost = auto.getLong("baseCost"),
            costOffset = auto.getInt("costOffset"),
            powerPerUpgrade = auto.getInt("powerPerUpgrade")
        ),
        prestige = PrestigeEconomyConfig(
            baseCost = prestige.getLong("baseCost"),
            levelBonusPerRitual = prestige.getInt("levelBonusPerRitual"),
            resetTapPower = prestige.getInt("resetTapPower"),
            resetAutoPower = prestige.getInt("resetAutoPower")
        ),
        levels = LevelEconomyConfig(
            maxLevel = levels.getInt("maxLevel"),
            essenceFactor = levels.getLong("essenceFactor"),
            targetLevels = targetLevels,
            artMilestones = artMilestones.ifEmpty {
                listOf(ArtMilestone(level = 1, drawableName = "vavarda_lvl_1", title = "Пробуждение"))
            }
        ),
        rewards = rewards,
        factions = parsedFactions,
        daily = DailyConfig(
            missionCount = daily?.optInt("missionCount", 3) ?: 3,
            tapBaseTarget = daily?.optInt("tapBaseTarget", 120) ?: 120,
            tapStep = daily?.optInt("tapStep", 35) ?: 35,
            upgradeBaseTarget = daily?.optInt("upgradeBaseTarget", 4) ?: 4,
            upgradeStep = daily?.optInt("upgradeStep", 1) ?: 1,
            essenceBaseTarget = daily?.optLong("essenceBaseTarget", 5000L) ?: 5000L,
            essenceStep = daily?.optLong("essenceStep", 2500L) ?: 2500L
        ),
        boss = BossConfig(
            spawnIntervalSeconds = boss?.optInt("spawnIntervalSeconds", 180) ?: 180,
            fightDurationSeconds = boss?.optInt("fightDurationSeconds", 20) ?: 20,
            baseHp = boss?.optLong("baseHp", 180L) ?: 180L,
            hpPerLevel = boss?.optLong("hpPerLevel", 16L) ?: 16L,
            rewardPerHp = boss?.optLong("rewardPerHp", 12L) ?: 12L
        ),
        offline = OfflineIncomeConfig(
            enabled = offline?.optBoolean("enabled", true) ?: true,
            maxSeconds = offline?.optInt("maxSeconds", 8 * 60 * 60) ?: (8 * 60 * 60)
        ),
        retention = RetentionConfig(
            crit = CritRetentionConfig(
                baseCost = critRetention?.optLong("baseCost", 450L) ?: 450L,
                costGrowth = critRetention?.optDouble("costGrowth", 1.65) ?: 1.65,
                maxLevel = critRetention?.optInt("maxLevel", 20) ?: 20,
                baseChance = critRetention?.optDouble("baseChance", 0.08) ?: 0.08,
                chancePerLevel = critRetention?.optDouble("chancePerLevel", 0.025) ?: 0.025,
                baseMultiplier = critRetention?.optDouble("baseMultiplier", 2.0) ?: 2.0,
                multiplierPerLevel = critRetention?.optDouble("multiplierPerLevel", 0.08) ?: 0.08
            ),
            streak = StreakRetentionConfig(
                baseCost = streakRetention?.optLong("baseCost", 380L) ?: 380L,
                costGrowth = streakRetention?.optDouble("costGrowth", 1.6) ?: 1.6,
                maxLevel = streakRetention?.optInt("maxLevel", 20) ?: 20,
                baseThreshold = streakRetention?.optInt("baseThreshold", 12) ?: 12,
                thresholdReductionPerLevel = streakRetention?.optDouble("thresholdReductionPerLevel", 0.4) ?: 0.4,
                baseBurstMultiplier = streakRetention?.optDouble("baseBurstMultiplier", 1.3) ?: 1.3,
                burstMultiplierPerLevel = streakRetention?.optDouble("burstMultiplierPerLevel", 0.06) ?: 0.06
            ),
            offlineBoost = OfflineBoostRetentionConfig(
                baseCost = offlineBoostRetention?.optLong("baseCost", 520L) ?: 520L,
                costGrowth = offlineBoostRetention?.optDouble("costGrowth", 1.7) ?: 1.7,
                maxLevel = offlineBoostRetention?.optInt("maxLevel", 15) ?: 15,
                baseMultiplier = offlineBoostRetention?.optDouble("baseMultiplier", 1.0) ?: 1.0,
                multiplierPerLevel = offlineBoostRetention?.optDouble("multiplierPerLevel", 0.2) ?: 0.2
            ),
            cache = CacheRetentionConfig(
                baseCost = cacheRetention?.optLong("baseCost", 760L) ?: 760L,
                costGrowth = cacheRetention?.optDouble("costGrowth", 1.76) ?: 1.76,
                maxLevel = cacheRetention?.optInt("maxLevel", 12) ?: 12,
                baseCooldownMinutes = cacheRetention?.optInt("baseCooldownMinutes", 52) ?: 52,
                cooldownReductionPerLevel = cacheRetention?.optInt("cooldownReductionPerLevel", 2) ?: 2,
                minCooldownMinutes = cacheRetention?.optInt("minCooldownMinutes", 22) ?: 22,
                baseReward = cacheRetention?.optLong("baseReward", 1200L) ?: 1200L,
                rewardPerLevel = cacheRetention?.optLong("rewardPerLevel", 620L) ?: 620L,
                rewardPerPlayerLevel = cacheRetention?.optLong("rewardPerPlayerLevel", 40L) ?: 40L,
                shardReward = cacheRetention?.optInt("shardReward", 1) ?: 1
            ),
            returnStreak = ReturnStreakRetentionConfig(
                baseReward = returnStreakRetention?.optLong("baseReward", 900L) ?: 900L,
                rewardPerDay = returnStreakRetention?.optLong("rewardPerDay", 700L) ?: 700L,
                shardEveryDays = returnStreakRetention?.optInt("shardEveryDays", 4) ?: 4,
                shardsPerMilestone = returnStreakRetention?.optInt("shardsPerMilestone", 1) ?: 1,
                showcaseDays = returnStreakRetention?.optInt("showcaseDays", 7) ?: 7,
                milestones = returnStreakMilestones
            ),
            relic = RelicRetentionConfig(
                maxLevel = relicRetention?.optInt("maxLevel", 10) ?: 10,
                baseShardsForLevel = relicRetention?.optInt("baseShardsForLevel", 5) ?: 5,
                shardsGrowthPerLevel = relicRetention?.optInt("shardsGrowthPerLevel", 3) ?: 3,
                tapMultiplierPerLevel = relicRetention?.optDouble("tapMultiplierPerLevel", 0.03) ?: 0.03,
                autoMultiplierPerLevel = relicRetention?.optDouble("autoMultiplierPerLevel", 0.04) ?: 0.04,
                bossMultiplierPerLevel = relicRetention?.optDouble("bossMultiplierPerLevel", 0.05) ?: 0.05
            ),
            altar = SeasonalAltarConfig(
                favorFromDaily = altarRetention?.optInt("favorFromDaily", 4) ?: 4,
                favorFromBoss = altarRetention?.optInt("favorFromBoss", 10) ?: 10,
                favorFromCache = altarRetention?.optInt("favorFromCache", 3) ?: 3,
                favorFromReturn = altarRetention?.optInt("favorFromReturn", 5) ?: 5,
                tiers = altarTiers
            )
        )
    )
}

fun defaultEconomyConfig(): EconomyConfig {
    return EconomyConfig(
        tap = TapEconomyConfig(
            startingPower = 1,
            baseCost = 20L,
            costOffset = 0,
            powerPerUpgrade = 1
        ),
        auto = AutoEconomyConfig(
            startingPower = 0,
            baseCost = 100L,
            costOffset = 1,
            powerPerUpgrade = 1
        ),
        prestige = PrestigeEconomyConfig(
            baseCost = 5000L,
            levelBonusPerRitual = 3,
            resetTapPower = 1,
            resetAutoPower = 0
        ),
        levels = LevelEconomyConfig(
            maxLevel = 999,
            essenceFactor = 20L,
            targetLevels = listOf(10, 25, 50, 75, 100),
            artMilestones = listOf(
                ArtMilestone(level = 1, drawableName = "vavarda_lvl_1", title = "Пробуждение"),
                ArtMilestone(level = 2, drawableName = "vavarda_lvl_2", title = "Первые Искры"),
                ArtMilestone(level = 3, drawableName = "vavarda_lvl_3", title = "Арканист"),
                ArtMilestone(level = 4, drawableName = "vavarda_lvl_4", title = "Хранитель Томов"),
                ArtMilestone(level = 5, drawableName = "vavarda_lvl_5", title = "Жрец Пепла"),
                ArtMilestone(level = 6, drawableName = "vavarda_lvl_6", title = "Огненный Сигил"),
                ArtMilestone(level = 7, drawableName = "vavarda_lvl_7", title = "Клинок Пустоты"),
                ArtMilestone(level = 8, drawableName = "vavarda_lvl_8", title = "Владыка Гроз"),
                ArtMilestone(level = 9, drawableName = "vavarda_lvl_9", title = "Голос Бури"),
                ArtMilestone(level = 10, drawableName = "vavarda_lvl_10", title = "Вестник Сердца"),
                ArtMilestone(level = 25, drawableName = "vavarda_lvl_25", title = "Штормовой Лорд"),
                ArtMilestone(level = 50, drawableName = "vavarda_lvl_50", title = "Архон Отката"),
                ArtMilestone(level = 75, drawableName = "vavarda_lvl_75", title = "Повелитель Руин"),
                ArtMilestone(level = 100, drawableName = "vavarda_lvl_100", title = "Истинное Имя")
            )
        ),
        rewards = listOf(
            RewardConfig(level = 10, title = "Уровень 10: Всплеск Искр", description = "+1500 Искр", kind = RewardKind.ESSENCE, amount = 1500),
            RewardConfig(level = 25, title = "Уровень 25: Рунная Сила", description = "+2 к силе клика", kind = RewardKind.TAP_POWER, amount = 2),
            RewardConfig(level = 50, title = "Уровень 50: Механика Ритма", description = "+2 к авто-приросту", kind = RewardKind.AUTO_POWER, amount = 2),
            RewardConfig(level = 75, title = "Уровень 75: Дар Ковена", description = "+15000 Искр", kind = RewardKind.ESSENCE, amount = 15000),
            RewardConfig(level = 100, title = "Уровень 100: Истинный Резонанс", description = "+5 к силе клика", kind = RewardKind.TAP_POWER, amount = 5)
        ),
        factions = defaultFactionBonuses(),
        daily = DailyConfig(
            missionCount = 3,
            tapBaseTarget = 120,
            tapStep = 35,
            upgradeBaseTarget = 4,
            upgradeStep = 1,
            essenceBaseTarget = 5000L,
            essenceStep = 2500L
        ),
        boss = BossConfig(
            spawnIntervalSeconds = 180,
            fightDurationSeconds = 20,
            baseHp = 180L,
            hpPerLevel = 16L,
            rewardPerHp = 12L
        ),
        offline = OfflineIncomeConfig(
            enabled = true,
            maxSeconds = 8 * 60 * 60
        ),
        retention = RetentionConfig(
            crit = CritRetentionConfig(
                baseCost = 450L,
                costGrowth = 1.65,
                maxLevel = 20,
                baseChance = 0.08,
                chancePerLevel = 0.025,
                baseMultiplier = 2.0,
                multiplierPerLevel = 0.08
            ),
            streak = StreakRetentionConfig(
                baseCost = 380L,
                costGrowth = 1.6,
                maxLevel = 20,
                baseThreshold = 12,
                thresholdReductionPerLevel = 0.4,
                baseBurstMultiplier = 1.3,
                burstMultiplierPerLevel = 0.06
            ),
            offlineBoost = OfflineBoostRetentionConfig(
                baseCost = 520L,
                costGrowth = 1.7,
                maxLevel = 15,
                baseMultiplier = 1.0,
                multiplierPerLevel = 0.2
            ),
            cache = CacheRetentionConfig(
                baseCost = 760L,
                costGrowth = 1.76,
                maxLevel = 12,
                baseCooldownMinutes = 52,
                cooldownReductionPerLevel = 2,
                minCooldownMinutes = 22,
                baseReward = 1200L,
                rewardPerLevel = 620L,
                rewardPerPlayerLevel = 40L,
                shardReward = 1
            ),
            returnStreak = ReturnStreakRetentionConfig(
                baseReward = 900L,
                rewardPerDay = 700L,
                shardEveryDays = 4,
                shardsPerMilestone = 1,
                showcaseDays = 7,
                milestones = defaultReturnStreakMilestones()
            ),
            relic = RelicRetentionConfig(
                maxLevel = 10,
                baseShardsForLevel = 5,
                shardsGrowthPerLevel = 3,
                tapMultiplierPerLevel = 0.03,
                autoMultiplierPerLevel = 0.04,
                bossMultiplierPerLevel = 0.05
            ),
            altar = SeasonalAltarConfig(
                favorFromDaily = 4,
                favorFromBoss = 10,
                favorFromCache = 3,
                favorFromReturn = 5,
                tiers = defaultSeasonalAltarTiers()
            )
        )
    )
}

private fun defaultReturnStreakMilestones(): List<ReturnStreakMilestoneConfig> {
    return listOf(
        ReturnStreakMilestoneConfig(day = 7, title = "Седьмой Знак", rewardEssence = 14_000L, rewardShards = 3),
        ReturnStreakMilestoneConfig(day = 14, title = "Двойной Обет", rewardEssence = 32_000L, rewardShards = 4),
        ReturnStreakMilestoneConfig(day = 30, title = "Тридцатая Печать", rewardEssence = 90_000L, rewardShards = 8)
    )
}

private fun defaultSeasonalAltarTiers(): List<SeasonalAltarTierConfig> {
    return listOf(
        SeasonalAltarTierConfig(tier = 1, title = "Пепельная Чаша", requiredFavor = 48, rewardEssence = 8_500L, rewardShards = 2),
        SeasonalAltarTierConfig(tier = 2, title = "Коронный Обет", requiredFavor = 108, rewardEssence = 21_000L, rewardShards = 4),
        SeasonalAltarTierConfig(tier = 3, title = "Сердце Сезона", requiredFavor = 180, rewardEssence = 52_000L, rewardShards = 8)
    )
}

private fun defaultFactionBonuses(): Map<AlignmentPath, FactionBonusConfig> {
    return mapOf(
        AlignmentPath.SHADOW to FactionBonusConfig(
            name = "Тьма",
            description = "Усиленный урон и клик, чуть слабее авто-прирост.",
            tapMultiplier = 1.18,
            autoMultiplier = 0.9,
            bossDamageMultiplier = 1.25
        ),
        AlignmentPath.LIGHT to FactionBonusConfig(
            name = "Свет",
            description = "Ускоренный авто-прирост и стабильная экономика.",
            tapMultiplier = 0.9,
            autoMultiplier = 1.2,
            bossDamageMultiplier = 0.95
        )
    )
}
