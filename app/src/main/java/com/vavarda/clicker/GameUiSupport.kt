package com.vavarda.clicker

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.sqrt

internal enum class FtueStep {
    FIRST_CLICK,
    FIRST_UPGRADE,
    FIRST_RITUAL,
    DONE
}

internal enum class GameScreen(val labelRes: Int) {
    CORE(R.string.nav_core),
    GROWTH(R.string.nav_growth),
    EVENTS(R.string.nav_events),
    PATH(R.string.nav_path)
}

internal fun parseGameScreenExtra(rawValue: String?): GameScreen? {
    return when (rawValue?.trim()?.uppercase(Locale.ROOT)) {
        "CORE", "HOME", "DOM" -> GameScreen.CORE
        "GROWTH", "ROST" -> GameScreen.GROWTH
        "EVENTS", "BATTLE", "BOY" -> GameScreen.EVENTS
        "PATH", "PUT" -> GameScreen.PATH
        else -> null
    }
}

internal enum class GrowthActionKind {
    TAP,
    AUTO,
    RITUAL
}

internal enum class ActionButtonStyle {
    CLAIM,
    PURCHASE,
    HIT,
    PATH,
    NEUTRAL
}

internal fun resolveFtueStep(ftue: FtueProgress): FtueStep {
    return when {
        !ftue.firstClickDone -> FtueStep.FIRST_CLICK
        !ftue.firstUpgradeDone -> FtueStep.FIRST_UPGRADE
        !ftue.firstRitualDone -> FtueStep.FIRST_RITUAL
        else -> FtueStep.DONE
    }
}

internal fun applyReward(state: GameState, reward: RewardConfig): GameState {
    val newClaimed = state.claimedRewardLevels + reward.level
    return when (reward.kind) {
        RewardKind.ESSENCE -> state.copy(
            essence = state.essence + reward.amount.toLong(),
            claimedRewardLevels = newClaimed
        )

        RewardKind.TAP_POWER -> state.copy(
            tapPower = state.tapPower + reward.amount,
            claimedRewardLevels = newClaimed
        )

        RewardKind.AUTO_POWER -> state.copy(
            autoPower = state.autoPower + reward.amount,
            claimedRewardLevels = newClaimed
        )
    }
}

internal fun calculateLevel(essence: Long, rituals: Int, config: EconomyConfig): Int {
    val ritualBonus = rituals * config.prestige.levelBonusPerRitual
    val essencePart = sqrt((essence / config.levels.essenceFactor.toDouble()).coerceAtLeast(0.0)).toInt()
    return (1 + ritualBonus + essencePart).coerceIn(1, config.levels.maxLevel)
}

internal fun essenceForLevel(level: Int, rituals: Int, config: EconomyConfig): Long {
    val ritualBonus = rituals * config.prestige.levelBonusPerRitual
    val effective = (level - 1 - ritualBonus).coerceAtLeast(0).toLong()
    return effective * effective * config.levels.essenceFactor
}

internal fun formatNumber(value: Long, compact: Boolean): String {
    if (!compact) {
        return DecimalFormat("#,###").format(value).replace(',', ' ')
    }

    return when {
        value >= 1_000_000_000 -> String.format(Locale("ru", "RU"), "%.1f млрд", value / 1_000_000_000.0)
        value >= 1_000_000 -> String.format(Locale("ru", "RU"), "%.1f млн", value / 1_000_000.0)
        value >= 1_000 -> String.format(Locale("ru", "RU"), "%.1f тыс.", value / 1_000.0)
        else -> value.toString()
    }
}

internal fun formatShortDuration(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val seconds = safeSeconds % 60
    return when {
        hours > 0 -> String.format(Locale("ru", "RU"), "%dч %02dм", hours, minutes)
        minutes > 0 -> String.format(Locale("ru", "RU"), "%dм %02dс", minutes, seconds)
        else -> String.format(Locale("ru", "RU"), "%dс", seconds)
    }
}

internal fun formatWeekKeyForUi(weekKey: String): String {
    val parts = weekKey.split("-W")
    if (parts.size != 2) return weekKey
    val year = parts[0]
    val week = parts[1]
    return "$week/$year"
}

internal fun formatNumber(value: Number, compact: Boolean): String {
    return formatNumber(value.toLong(), compact)
}

internal fun formatMultiplier(value: Double): String {
    return String.format(Locale("ru", "RU"), "%.2f", value)
}

internal fun formatPercent(value: Double): String {
    return String.format(Locale("ru", "RU"), "%.1f", value)
}

internal fun resolveRecommendedGrowthAction(
    ftueStep: FtueStep,
    canPerformRitual: Boolean,
    tapUpgradeCost: Long,
    autoUpgradeCost: Long
): GrowthActionKind {
    return when {
        ftueStep == FtueStep.FIRST_RITUAL || canPerformRitual -> GrowthActionKind.RITUAL
        tapUpgradeCost <= autoUpgradeCost -> GrowthActionKind.TAP
        else -> GrowthActionKind.AUTO
    }
}

@Composable
internal fun localizedHomeGoalText(
    ftueStep: FtueStep,
    canUpgradeTap: Boolean,
    canUpgradeAuto: Boolean,
    canPerformRitual: Boolean,
    tapUpgradeCost: Long,
    autoUpgradeCost: Long,
    ritualCost: Long,
    nextArtUnlockLevel: Int?,
    compactNumbers: Boolean
): String {
    return when (ftueStep) {
        FtueStep.FIRST_CLICK -> stringResource(R.string.goal_home_first_click)
        FtueStep.FIRST_UPGRADE -> stringResource(
            R.string.goal_home_first_upgrade,
            formatNumber(minOf(tapUpgradeCost, autoUpgradeCost), compactNumbers)
        )
        FtueStep.FIRST_RITUAL -> stringResource(
            R.string.goal_home_first_ritual,
            formatNumber(ritualCost, compactNumbers)
        )
        FtueStep.DONE -> when {
            canPerformRitual -> stringResource(R.string.goal_home_ritual_ready)
            canUpgradeTap && tapUpgradeCost <= autoUpgradeCost -> stringResource(
                R.string.goal_home_tap_ready,
                formatNumber(tapUpgradeCost, compactNumbers)
            )
            canUpgradeAuto -> stringResource(
                R.string.goal_home_auto_ready,
                formatNumber(autoUpgradeCost, compactNumbers)
            )
            nextArtUnlockLevel != null -> stringResource(R.string.goal_home_next_art, nextArtUnlockLevel)
            else -> stringResource(R.string.goal_home_default)
        }
    }
}

@Composable
internal fun localizedHomeGoalShortText(
    ftueStep: FtueStep,
    canUpgradeTap: Boolean,
    canUpgradeAuto: Boolean,
    canPerformRitual: Boolean,
    tapUpgradeCost: Long,
    autoUpgradeCost: Long,
    ritualCost: Long,
    nextArtUnlockLevel: Int?,
    compactNumbers: Boolean
): String {
    return when (ftueStep) {
        FtueStep.FIRST_CLICK -> stringResource(R.string.goal_home_first_click_short)
        FtueStep.FIRST_UPGRADE -> stringResource(
            R.string.goal_home_first_upgrade_short,
            formatNumber(minOf(tapUpgradeCost, autoUpgradeCost), compactNumbers)
        )
        FtueStep.FIRST_RITUAL -> stringResource(
            R.string.goal_home_first_ritual_short,
            formatNumber(ritualCost, compactNumbers)
        )
        FtueStep.DONE -> when {
            canPerformRitual -> stringResource(R.string.goal_home_ritual_ready_short)
            canUpgradeTap && tapUpgradeCost <= autoUpgradeCost -> stringResource(
                R.string.goal_home_tap_ready_short,
                formatNumber(tapUpgradeCost, compactNumbers)
            )
            canUpgradeAuto -> stringResource(
                R.string.goal_home_auto_ready_short,
                formatNumber(autoUpgradeCost, compactNumbers)
            )
            nextArtUnlockLevel != null -> stringResource(R.string.goal_home_next_art_short, nextArtUnlockLevel)
            else -> stringResource(R.string.goal_home_default_short)
        }
    }
}

@Composable
internal fun localizedReadyRewardsSummaryText(
    claimableMissions: Int,
    returnStreakAvailable: Boolean,
    cacheReadySeconds: Int,
    altarClaimableTierCount: Int
): String {
    val readyItems = buildList {
        if (claimableMissions > 0) {
            add(stringResource(R.string.events_reward_chip_daily, claimableMissions))
        }
        if (returnStreakAvailable) {
            add(stringResource(R.string.events_reward_chip_return))
        }
        if (cacheReadySeconds <= 0) {
            add(stringResource(R.string.events_reward_chip_cache))
        }
        if (altarClaimableTierCount > 0) {
            add(stringResource(R.string.events_reward_chip_altar, altarClaimableTierCount))
        }
    }

    return readyItems.joinToString(separator = " • ")
}

@Composable
internal fun localizedGrowthGoalText(
    recommendedAction: GrowthActionKind,
    canPerformRitual: Boolean,
    tapUpgradeCost: Long,
    autoUpgradeCost: Long,
    ritualCost: Long,
    compactNumbers: Boolean
): String {
    return when (recommendedAction) {
        GrowthActionKind.TAP -> stringResource(
            R.string.goal_growth_tap,
            formatNumber(tapUpgradeCost, compactNumbers)
        )
        GrowthActionKind.AUTO -> stringResource(
            R.string.goal_growth_auto,
            formatNumber(autoUpgradeCost, compactNumbers)
        )
        GrowthActionKind.RITUAL -> if (canPerformRitual) {
            stringResource(R.string.goal_growth_ritual_ready)
        } else {
            stringResource(
                R.string.goal_growth_ritual_progress,
                formatNumber(ritualCost, compactNumbers)
            )
        }
    }
}

@Composable
internal fun localizedEventsGoalText(
    boss: BossState,
    claimableMissions: Int
): String {
    return when {
        boss.isActive -> stringResource(R.string.goal_events_boss_active)
        claimableMissions > 0 -> stringResource(R.string.goal_events_daily_ready, claimableMissions)
        else -> stringResource(R.string.goal_events_boss_countdown, boss.secondsUntilSpawn)
    }
}

@Composable
internal fun localizedHomeRewardsBody(
    eventsReadyCount: Int,
    cacheReadySeconds: Int,
    altarFavor: Int,
    altarMaxFavor: Int
): String {
    return when {
        eventsReadyCount > 0 -> stringResource(R.string.home_rewards_body_collect)
        else -> stringResource(
            R.string.home_rewards_body_idle,
            formatShortDuration(cacheReadySeconds),
            altarFavor,
            altarMaxFavor
        )
    }
}

@Composable
internal fun localizedArtTitle(level: Int, fallback: String): String {
    val resId = when (level) {
        1 -> R.string.art_title_1
        2 -> R.string.art_title_2
        3 -> R.string.art_title_3
        4 -> R.string.art_title_4
        5 -> R.string.art_title_5
        6 -> R.string.art_title_6
        7 -> R.string.art_title_7
        8 -> R.string.art_title_8
        9 -> R.string.art_title_9
        10 -> R.string.art_title_10
        25 -> R.string.art_title_25
        50 -> R.string.art_title_50
        75 -> R.string.art_title_75
        100 -> R.string.art_title_100
        else -> return fallback
    }
    return stringResource(resId)
}

@Composable
internal fun localizedFactionName(path: AlignmentPath): String {
    return stringResource(factionNameRes(path))
}

internal fun factionNameRes(path: AlignmentPath): Int {
    return when (path) {
        AlignmentPath.SHADOW -> R.string.faction_shadow_name
        AlignmentPath.LIGHT -> R.string.faction_light_name
    }
}

internal fun localizedFactionName(context: Context, path: AlignmentPath): String {
    return context.getString(
        when (path) {
            AlignmentPath.SHADOW -> R.string.faction_shadow_name
            AlignmentPath.LIGHT -> R.string.faction_light_name
        }
    )
}

internal fun factionImageName(path: AlignmentPath): String {
    return when (path) {
        AlignmentPath.SHADOW -> "vavarda_dark_legion"
        AlignmentPath.LIGHT -> "vavarda_light_guardians"
    }
}

internal fun factionAccentColor(path: AlignmentPath): Color {
    return when (path) {
        AlignmentPath.SHADOW -> Color(0xFF7C678F)
        AlignmentPath.LIGHT -> Color(0xFFD7B573)
    }
}

@Composable
internal fun localizedFactionLoreTitle(path: AlignmentPath): String {
    return stringResource(
        when (path) {
            AlignmentPath.SHADOW -> R.string.faction_shadow_lore_title
            AlignmentPath.LIGHT -> R.string.faction_light_lore_title
        }
    )
}

@Composable
internal fun localizedFactionLoreCaption(path: AlignmentPath): String {
    return stringResource(
        when (path) {
            AlignmentPath.SHADOW -> R.string.faction_shadow_lore_caption
            AlignmentPath.LIGHT -> R.string.faction_light_lore_caption
        }
    )
}

@Composable
internal fun localizedFactionLoreBody(path: AlignmentPath): String {
    return stringResource(
        when (path) {
            AlignmentPath.SHADOW -> R.string.faction_shadow_lore_body
            AlignmentPath.LIGHT -> R.string.faction_light_lore_body
        }
    )
}

@Composable
internal fun localizedFactionPlaystyle(path: AlignmentPath): String {
    return stringResource(
        when (path) {
            AlignmentPath.SHADOW -> R.string.faction_shadow_style
            AlignmentPath.LIGHT -> R.string.faction_light_style
        }
    )
}

@Composable
internal fun localizedFactionDescription(path: AlignmentPath): String {
    return stringResource(
        when (path) {
            AlignmentPath.SHADOW -> R.string.faction_shadow_desc
            AlignmentPath.LIGHT -> R.string.faction_light_desc
        }
    )
}

@Composable
internal fun dailyMissionTitle(mission: DailyMission): String {
    return stringResource(
        when (mission.type) {
            DailyMissionType.TAPS -> R.string.daily_mission_taps_title
            DailyMissionType.UPGRADES -> R.string.daily_mission_upgrades_title
            DailyMissionType.ESSENCE_EARNED -> R.string.daily_mission_essence_title
        }
    )
}

@Composable
internal fun dailyMissionDescription(mission: DailyMission, compactNumbers: Boolean): String {
    return when (mission.type) {
        DailyMissionType.TAPS -> stringResource(R.string.daily_mission_taps_desc, mission.target.toInt())
        DailyMissionType.UPGRADES -> stringResource(R.string.daily_mission_upgrades_desc, mission.target.toInt())
        DailyMissionType.ESSENCE_EARNED -> stringResource(
            R.string.daily_mission_essence_desc,
            formatNumber(mission.target, compactNumbers)
        )
    }
}

@Composable
internal fun localizedRewardEffect(reward: RewardConfig): String {
    return when (reward.kind) {
        RewardKind.ESSENCE -> stringResource(R.string.milestone_effect_essence, reward.amount)
        RewardKind.TAP_POWER -> stringResource(R.string.milestone_effect_tap_power, reward.amount)
        RewardKind.AUTO_POWER -> stringResource(R.string.milestone_effect_auto_power, reward.amount)
    }
}

@Composable
internal fun bottomNavLabel(screen: GameScreen): String {
    return stringResource(
        when (screen) {
            GameScreen.CORE -> R.string.nav_core_short
            GameScreen.GROWTH -> R.string.nav_growth_short
            GameScreen.EVENTS -> R.string.nav_events_short
            GameScreen.PATH -> R.string.nav_path_short
        }
    )
}

internal data class BottomNavIconSpec(
    val imageVector: ImageVector,
    val opticalOffset: DpOffset,
    val iconScale: Float = 1f,
    val badgeOffset: DpOffset = DpOffset(6.dp, (-4).dp)
)

internal fun bottomNavIconSpec(screen: GameScreen): BottomNavIconSpec {
    return when (screen) {
        GameScreen.CORE -> BottomNavIconSpec(
            imageVector = Icons.Rounded.Home,
            opticalOffset = DpOffset(0.dp, (-0.5f).dp),
            iconScale = 0.96f
        )
        GameScreen.GROWTH -> BottomNavIconSpec(
            imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
            opticalOffset = DpOffset(0.dp, (-0.5f).dp),
            iconScale = 0.96f
        )
        GameScreen.EVENTS -> BottomNavIconSpec(
            imageVector = Icons.Rounded.Bolt,
            opticalOffset = DpOffset(0.dp, (-0.5f).dp),
            iconScale = 0.95f,
            badgeOffset = DpOffset(4.dp, (-2).dp)
        )
        GameScreen.PATH -> BottomNavIconSpec(
            imageVector = Icons.Rounded.Explore,
            opticalOffset = DpOffset(0.dp, (-0.5f).dp),
            iconScale = 0.96f
        )
    }
}

internal fun tierRoman(value: Int): String {
    return when (value) {
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        else -> value.toString()
    }
}

@Composable
internal fun scaledSp(base: Float, scale: Float): TextUnit {
    val layoutMetrics = rememberScreenLayoutMetrics()
    return (base * scale.coerceIn(0.9f, 1.2f) * layoutMetrics.typographyScale()).sp
}

@Composable
internal fun scaledDp(base: Float): Dp {
    val layoutMetrics = rememberScreenLayoutMetrics()
    return (base * layoutMetrics.baseSpacingScale()).dp
}

@Composable
internal fun rememberScreenLayoutMetrics(): LayoutMetrics {
    val configuration = LocalConfiguration.current
    val layoutProfiles = LocalUiConfig.current.layoutProfiles
    val metricsConfig = when {
        configuration.screenWidthDp <= layoutProfiles.compactMaxWidthDp ||
            configuration.screenHeightDp <= layoutProfiles.compactMaxHeightDp -> layoutProfiles.compact
        configuration.screenWidthDp >= layoutProfiles.expandedMinWidthDp &&
            configuration.screenHeightDp >= layoutProfiles.expandedMinHeightDp -> layoutProfiles.expanded
        else -> layoutProfiles.standard
    }
    return remember(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        metricsConfig
    ) {
        LayoutMetrics(metricsConfig)
    }
}
