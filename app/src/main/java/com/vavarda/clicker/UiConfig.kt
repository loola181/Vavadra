package com.vavarda.clicker

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.json.JSONObject

data class UiConfig(
    val sounds: UiSoundConfig,
    val layoutProfiles: LayoutProfilesConfig
)

data class UiSoundConfig(
    val upgrade: SoundCueConfig,
    val ritual: SoundCueConfig,
    val bossHit: SoundCueConfig,
    val bossDefeat: SoundCueConfig,
    val reward: SoundCueConfig,
    val daily: SoundCueConfig,
    val returnMilestone: SoundCueConfig,
    val altar: SoundCueConfig
) {
    fun forSound(sound: GameSound): SoundCueConfig {
        return when (sound) {
            GameSound.UPGRADE -> upgrade
            GameSound.RITUAL -> ritual
            GameSound.BOSS_HIT -> bossHit
            GameSound.BOSS_DEFEAT -> bossDefeat
            GameSound.REWARD -> reward
            GameSound.DAILY -> daily
            GameSound.RETURN_MILESTONE -> returnMilestone
            GameSound.ALTAR -> altar
        }
    }
}

data class SoundCueConfig(
    val volume: Float,
    val rate: Float,
    val rateJitter: Float
)

data class LayoutProfilesConfig(
    val compactMaxWidthDp: Int,
    val compactMaxHeightDp: Int,
    val expandedMinWidthDp: Int,
    val expandedMinHeightDp: Int,
    val compact: LayoutProfileMetricsConfig,
    val standard: LayoutProfileMetricsConfig,
    val expanded: LayoutProfileMetricsConfig
)

data class LayoutProfileMetricsConfig(
    val typographyScale: Float,
    val baseSpacingScale: Float,
    val contentMaxWidth: Float,
    val modalMaxWidth: Float,
    val sheetMaxWidth: Float,
    val screenOverlayPadding: Float,
    val cardPadding: Float,
    val headerHorizontalPadding: Float,
    val headerVerticalPadding: Float,
    val headerButtonSize: Float,
    val headerIconBaseSize: Float,
    val bottomBarHeight: Float,
    val bottomBarInnerPadding: Float,
    val bottomBarContentHorizontalPadding: Float,
    val bottomBarContentVerticalPadding: Float,
    val bottomBarIconSize: Float,
    val bottomBarLabelSpacing: Float,
    val minorSpacing: Float,
    val sectionSpacing: Float,
    val chipSpacing: Float,
    val bossButtonHeight: Float,
    val bossIconSize: Float
)

class LayoutMetrics(private val config: LayoutProfileMetricsConfig) {
    fun typographyScale(): Float = config.typographyScale
    fun baseSpacingScale(): Float = config.baseSpacingScale
    fun contentMaxWidth(): Dp = config.contentMaxWidth.dp
    fun modalMaxWidth(): Dp = config.modalMaxWidth.dp
    fun sheetMaxWidth(): Dp = config.sheetMaxWidth.dp
    fun screenOverlayPadding(): Dp = config.screenOverlayPadding.dp
    fun cardPadding(): Dp = config.cardPadding.dp
    fun headerHorizontalPadding(): Dp = config.headerHorizontalPadding.dp
    fun headerVerticalPadding(): Dp = config.headerVerticalPadding.dp
    fun headerButtonSize(): Dp = config.headerButtonSize.dp
    fun headerIconSize(textScale: Float): Dp = (config.headerIconBaseSize * textScale.coerceAtLeast(0.9f)).dp
    fun bottomBarHeight(): Dp = config.bottomBarHeight.dp
    fun bottomBarInnerPadding(): Dp = config.bottomBarInnerPadding.dp
    fun bottomBarContentHorizontalPadding(): Dp = config.bottomBarContentHorizontalPadding.dp
    fun bottomBarContentVerticalPadding(): Dp = config.bottomBarContentVerticalPadding.dp
    fun bottomBarIconSize(): Dp = config.bottomBarIconSize.dp
    fun bottomBarLabelSpacing(): Dp = config.bottomBarLabelSpacing.dp
    fun minorSpacing(): Dp = config.minorSpacing.dp
    fun sectionSpacing(): Dp = config.sectionSpacing.dp
    fun chipSpacing(): Dp = config.chipSpacing.dp
    fun bossButtonHeight(): Dp = config.bossButtonHeight.dp
    fun bossIconSize(): Dp = config.bossIconSize.dp
}

val LocalUiConfig = staticCompositionLocalOf { defaultUiConfig() }

fun loadUiConfig(context: Context): UiConfig {
    return loadJsonAssetOrDefault(
        context = context,
        assetName = "ui_config.json",
        defaultValue = ::defaultUiConfig,
        parser = ::parseUiConfig
    )
}

internal fun parseUiConfig(raw: String): UiConfig {
    val root = JSONObject(raw)
    val sounds = root.optJSONObject("sounds")
    val layoutProfiles = root.optJSONObject("layoutProfiles")
    val defaultConfig = defaultUiConfig()

    return UiConfig(
        sounds = UiSoundConfig(
            upgrade = parseSoundCue(
                sounds = sounds,
                key = "upgrade",
                fallback = defaultConfig.sounds.upgrade
            ),
            ritual = parseSoundCue(
                sounds = sounds,
                key = "ritual",
                fallback = defaultConfig.sounds.ritual
            ),
            bossHit = parseSoundCue(
                sounds = sounds,
                key = "bossHit",
                fallback = defaultConfig.sounds.bossHit
            ),
            bossDefeat = parseSoundCue(
                sounds = sounds,
                key = "bossDefeat",
                fallback = defaultConfig.sounds.bossDefeat
            ),
            reward = parseSoundCue(
                sounds = sounds,
                key = "reward",
                fallback = defaultConfig.sounds.reward
            ),
            daily = parseSoundCue(
                sounds = sounds,
                key = "daily",
                fallback = defaultConfig.sounds.daily
            ),
            returnMilestone = parseSoundCue(
                sounds = sounds,
                key = "returnMilestone",
                fallback = defaultConfig.sounds.returnMilestone
            ),
            altar = parseSoundCue(
                sounds = sounds,
                key = "altar",
                fallback = defaultConfig.sounds.altar
            )
        ),
        layoutProfiles = LayoutProfilesConfig(
            compactMaxWidthDp = layoutProfiles?.optInt(
                "compactMaxWidthDp",
                defaultConfig.layoutProfiles.compactMaxWidthDp
            ) ?: defaultConfig.layoutProfiles.compactMaxWidthDp,
            compactMaxHeightDp = layoutProfiles?.optInt(
                "compactMaxHeightDp",
                defaultConfig.layoutProfiles.compactMaxHeightDp
            ) ?: defaultConfig.layoutProfiles.compactMaxHeightDp,
            expandedMinWidthDp = layoutProfiles?.optInt(
                "expandedMinWidthDp",
                defaultConfig.layoutProfiles.expandedMinWidthDp
            ) ?: defaultConfig.layoutProfiles.expandedMinWidthDp,
            expandedMinHeightDp = layoutProfiles?.optInt(
                "expandedMinHeightDp",
                defaultConfig.layoutProfiles.expandedMinHeightDp
            ) ?: defaultConfig.layoutProfiles.expandedMinHeightDp,
            compact = parseLayoutMetrics(
                layoutProfiles = layoutProfiles,
                key = "compact",
                fallback = defaultConfig.layoutProfiles.compact
            ),
            standard = parseLayoutMetrics(
                layoutProfiles = layoutProfiles,
                key = "standard",
                fallback = defaultConfig.layoutProfiles.standard
            ),
            expanded = parseLayoutMetrics(
                layoutProfiles = layoutProfiles,
                key = "expanded",
                fallback = defaultConfig.layoutProfiles.expanded
            )
        )
    )
}

private fun parseSoundCue(
    sounds: JSONObject?,
    key: String,
    fallback: SoundCueConfig
): SoundCueConfig {
    val soundJson = sounds?.optJSONObject(key)
    return SoundCueConfig(
        volume = soundJson?.optDouble("volume", fallback.volume.toDouble())?.toFloat() ?: fallback.volume,
        rate = soundJson?.optDouble("rate", fallback.rate.toDouble())?.toFloat() ?: fallback.rate,
        rateJitter = soundJson?.optDouble(
            "rateJitter",
            fallback.rateJitter.toDouble()
        )?.toFloat() ?: fallback.rateJitter
    )
}

private fun parseLayoutMetrics(
    layoutProfiles: JSONObject?,
    key: String,
    fallback: LayoutProfileMetricsConfig
): LayoutProfileMetricsConfig {
    val metricsJson = layoutProfiles?.optJSONObject(key)
    return LayoutProfileMetricsConfig(
        typographyScale = metricsJson?.optDouble(
            "typographyScale",
            fallback.typographyScale.toDouble()
        )?.toFloat() ?: fallback.typographyScale,
        baseSpacingScale = metricsJson?.optDouble(
            "baseSpacingScale",
            fallback.baseSpacingScale.toDouble()
        )?.toFloat() ?: fallback.baseSpacingScale,
        contentMaxWidth = metricsJson?.optDouble(
            "contentMaxWidth",
            fallback.contentMaxWidth.toDouble()
        )?.toFloat() ?: fallback.contentMaxWidth,
        modalMaxWidth = metricsJson?.optDouble(
            "modalMaxWidth",
            fallback.modalMaxWidth.toDouble()
        )?.toFloat() ?: fallback.modalMaxWidth,
        sheetMaxWidth = metricsJson?.optDouble(
            "sheetMaxWidth",
            fallback.sheetMaxWidth.toDouble()
        )?.toFloat() ?: fallback.sheetMaxWidth,
        screenOverlayPadding = metricsJson?.optDouble(
            "screenOverlayPadding",
            fallback.screenOverlayPadding.toDouble()
        )?.toFloat() ?: fallback.screenOverlayPadding,
        cardPadding = metricsJson?.optDouble("cardPadding", fallback.cardPadding.toDouble())?.toFloat()
            ?: fallback.cardPadding,
        headerHorizontalPadding = metricsJson?.optDouble(
            "headerHorizontalPadding",
            fallback.headerHorizontalPadding.toDouble()
        )?.toFloat() ?: fallback.headerHorizontalPadding,
        headerVerticalPadding = metricsJson?.optDouble(
            "headerVerticalPadding",
            fallback.headerVerticalPadding.toDouble()
        )?.toFloat() ?: fallback.headerVerticalPadding,
        headerButtonSize = metricsJson?.optDouble(
            "headerButtonSize",
            fallback.headerButtonSize.toDouble()
        )?.toFloat() ?: fallback.headerButtonSize,
        headerIconBaseSize = metricsJson?.optDouble(
            "headerIconBaseSize",
            fallback.headerIconBaseSize.toDouble()
        )?.toFloat() ?: fallback.headerIconBaseSize,
        bottomBarHeight = metricsJson?.optDouble(
            "bottomBarHeight",
            fallback.bottomBarHeight.toDouble()
        )?.toFloat() ?: fallback.bottomBarHeight,
        bottomBarInnerPadding = metricsJson?.optDouble(
            "bottomBarInnerPadding",
            fallback.bottomBarInnerPadding.toDouble()
        )?.toFloat() ?: fallback.bottomBarInnerPadding,
        bottomBarContentHorizontalPadding = metricsJson?.optDouble(
            "bottomBarContentHorizontalPadding",
            fallback.bottomBarContentHorizontalPadding.toDouble()
        )?.toFloat() ?: fallback.bottomBarContentHorizontalPadding,
        bottomBarContentVerticalPadding = metricsJson?.optDouble(
            "bottomBarContentVerticalPadding",
            fallback.bottomBarContentVerticalPadding.toDouble()
        )?.toFloat() ?: fallback.bottomBarContentVerticalPadding,
        bottomBarIconSize = metricsJson?.optDouble(
            "bottomBarIconSize",
            fallback.bottomBarIconSize.toDouble()
        )?.toFloat() ?: fallback.bottomBarIconSize,
        bottomBarLabelSpacing = metricsJson?.optDouble(
            "bottomBarLabelSpacing",
            fallback.bottomBarLabelSpacing.toDouble()
        )?.toFloat() ?: fallback.bottomBarLabelSpacing,
        minorSpacing = metricsJson?.optDouble("minorSpacing", fallback.minorSpacing.toDouble())?.toFloat()
            ?: fallback.minorSpacing,
        sectionSpacing = metricsJson?.optDouble(
            "sectionSpacing",
            fallback.sectionSpacing.toDouble()
        )?.toFloat() ?: fallback.sectionSpacing,
        chipSpacing = metricsJson?.optDouble("chipSpacing", fallback.chipSpacing.toDouble())?.toFloat()
            ?: fallback.chipSpacing,
        bossButtonHeight = metricsJson?.optDouble(
            "bossButtonHeight",
            fallback.bossButtonHeight.toDouble()
        )?.toFloat() ?: fallback.bossButtonHeight,
        bossIconSize = metricsJson?.optDouble(
            "bossIconSize",
            fallback.bossIconSize.toDouble()
        )?.toFloat() ?: fallback.bossIconSize
    )
}

private fun defaultUiConfig(): UiConfig {
    return UiConfig(
        sounds = UiSoundConfig(
            upgrade = SoundCueConfig(volume = 0.62f, rate = 1.02f, rateJitter = 0f),
            ritual = SoundCueConfig(volume = 0.74f, rate = 0.98f, rateJitter = 0f),
            bossHit = SoundCueConfig(volume = 0.8f, rate = 1f, rateJitter = 0.04f),
            bossDefeat = SoundCueConfig(volume = 0.86f, rate = 1f, rateJitter = 0f),
            reward = SoundCueConfig(volume = 0.82f, rate = 1f, rateJitter = 0f),
            daily = SoundCueConfig(volume = 0.7f, rate = 1.05f, rateJitter = 0f),
            returnMilestone = SoundCueConfig(volume = 0.88f, rate = 0.96f, rateJitter = 0f),
            altar = SoundCueConfig(volume = 0.92f, rate = 0.92f, rateJitter = 0f)
        ),
        layoutProfiles = LayoutProfilesConfig(
            compactMaxWidthDp = 379,
            compactMaxHeightDp = 759,
            expandedMinWidthDp = 430,
            expandedMinHeightDp = 900,
            compact = LayoutProfileMetricsConfig(
                typographyScale = 0.94f,
                baseSpacingScale = 0.92f,
                contentMaxWidth = 480f,
                modalMaxWidth = 520f,
                sheetMaxWidth = 560f,
                screenOverlayPadding = 10f,
                cardPadding = 10f,
                headerHorizontalPadding = 10f,
                headerVerticalPadding = 8f,
                headerButtonSize = 40f,
                headerIconBaseSize = 16f,
                bottomBarHeight = 54f,
                bottomBarInnerPadding = 6f,
                bottomBarContentHorizontalPadding = 3f,
                bottomBarContentVerticalPadding = 5f,
                bottomBarIconSize = 16f,
                bottomBarLabelSpacing = 1f,
                minorSpacing = 6f,
                sectionSpacing = 8f,
                chipSpacing = 6f,
                bossButtonHeight = 44f,
                bossIconSize = 16f
            ),
            standard = LayoutProfileMetricsConfig(
                typographyScale = 1f,
                baseSpacingScale = 1f,
                contentMaxWidth = 540f,
                modalMaxWidth = 600f,
                sheetMaxWidth = 640f,
                screenOverlayPadding = 12f,
                cardPadding = 12f,
                headerHorizontalPadding = 12f,
                headerVerticalPadding = 10f,
                headerButtonSize = 44f,
                headerIconBaseSize = 18f,
                bottomBarHeight = 58f,
                bottomBarInnerPadding = 8f,
                bottomBarContentHorizontalPadding = 4f,
                bottomBarContentVerticalPadding = 6f,
                bottomBarIconSize = 18f,
                bottomBarLabelSpacing = 2f,
                minorSpacing = 8f,
                sectionSpacing = 10f,
                chipSpacing = 8f,
                bossButtonHeight = 48f,
                bossIconSize = 18f
            ),
            expanded = LayoutProfileMetricsConfig(
                typographyScale = 1.04f,
                baseSpacingScale = 1.05f,
                contentMaxWidth = 620f,
                modalMaxWidth = 680f,
                sheetMaxWidth = 720f,
                screenOverlayPadding = 16f,
                cardPadding = 14f,
                headerHorizontalPadding = 14f,
                headerVerticalPadding = 12f,
                headerButtonSize = 46f,
                headerIconBaseSize = 19f,
                bottomBarHeight = 62f,
                bottomBarInnerPadding = 10f,
                bottomBarContentHorizontalPadding = 6f,
                bottomBarContentVerticalPadding = 7f,
                bottomBarIconSize = 20f,
                bottomBarLabelSpacing = 3f,
                minorSpacing = 10f,
                sectionSpacing = 12f,
                chipSpacing = 10f,
                bossButtonHeight = 52f,
                bossIconSize = 20f
            )
        )
    )
}
