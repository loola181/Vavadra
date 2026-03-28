package com.vavarda.clicker

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.gameDataStore by preferencesDataStore(name = "vavarda_progress")

class GameDataStoreRepository(
    private val context: Context,
    private val config: EconomyConfig
) {
    private val dataStore = context.gameDataStore

    val snapshots: Flow<GameSnapshot> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            val pathName = preferences[PATH]
            val path = runCatching { AlignmentPath.valueOf(pathName ?: AlignmentPath.SHADOW.name) }
                .getOrElse { AlignmentPath.SHADOW }

            val claimedLevels = preferences[CLAIMED_REWARDS]
                .orEmpty()
                .mapNotNull { it.toIntOrNull() }
                .toSet()

            val claimedDailySlots = preferences[DAILY_CLAIMED_SLOTS]
                .orEmpty()
                .mapNotNull { it.toIntOrNull() }
                .toSet()

            val claimedAltarTiers = preferences[RETENTION_ALTAR_CLAIMED_TIERS]
                .orEmpty()
                .mapNotNull { it.toIntOrNull() }
                .toSet()

            GameSnapshot(
                state = GameState(
                    essence = preferences[ESSENCE] ?: 0L,
                    tapPower = preferences[TAP_POWER] ?: config.tap.startingPower,
                    autoPower = preferences[AUTO_POWER] ?: config.auto.startingPower,
                    rituals = preferences[RITUALS] ?: 0,
                    path = path,
                    ftue = FtueProgress(
                        firstClickDone = preferences[FTUE_CLICK_DONE] ?: false,
                        firstUpgradeDone = preferences[FTUE_UPGRADE_DONE] ?: false,
                        firstRitualDone = preferences[FTUE_RITUAL_DONE] ?: false
                    ),
                    claimedRewardLevels = claimedLevels,
                    daily = DailyProgress(
                        dayKey = preferences[DAILY_DAY_KEY] ?: "",
                        taps = preferences[DAILY_TAPS] ?: 0,
                        upgrades = preferences[DAILY_UPGRADES] ?: 0,
                        essenceEarned = preferences[DAILY_ESSENCE_EARNED] ?: 0L,
                        claimedMissionSlots = claimedDailySlots
                    ),
                    boss = BossState(
                        isActive = preferences[BOSS_IS_ACTIVE] ?: false,
                        currentHp = preferences[BOSS_CURRENT_HP] ?: 0L,
                        maxHp = preferences[BOSS_MAX_HP] ?: 0L,
                        secondsUntilSpawn = preferences[BOSS_SECONDS_UNTIL_SPAWN] ?: config.boss.spawnIntervalSeconds,
                        secondsLeft = preferences[BOSS_SECONDS_LEFT] ?: 0
                    ),
                    retention = RetentionProgress(
                        critLevel = preferences[RETENTION_CRIT_LEVEL] ?: 0,
                        streakLevel = preferences[RETENTION_STREAK_LEVEL] ?: 0,
                        offlineBoostLevel = preferences[RETENTION_OFFLINE_LEVEL] ?: 0,
                        streakCharge = preferences[RETENTION_STREAK_CHARGE] ?: 0,
                        cacheLevel = preferences[RETENTION_CACHE_LEVEL] ?: 0,
                        chestReadyAtMillis = preferences[RETENTION_CHEST_READY_AT] ?: 0L,
                        returnStreak = preferences[RETENTION_RETURN_STREAK] ?: 0,
                        bestReturnStreak = preferences[RETENTION_BEST_RETURN_STREAK] ?: 0,
                        lastDailyReturnClaimDayKey = preferences[RETENTION_LAST_RETURN_DAY_KEY] ?: "",
                        relicShards = preferences[RETENTION_RELIC_SHARDS] ?: 0,
                        relicLevel = preferences[RETENTION_RELIC_LEVEL] ?: 0,
                        altarWeekKey = preferences[RETENTION_ALTAR_WEEK_KEY] ?: "",
                        altarFavor = preferences[RETENTION_ALTAR_FAVOR] ?: 0,
                        altarClaimedTiers = claimedAltarTiers
                    ),
                    lastActiveAtMillis = preferences[LAST_ACTIVE_AT_MILLIS] ?: 0L
                ),
                settings = GameSettings(
                    showFtueHints = preferences[SETTING_SHOW_FTUE_HINTS] ?: true,
                    compactNumbers = preferences[SETTING_COMPACT_NUMBERS] ?: true,
                    offlineIncomeEnabled = preferences[SETTING_OFFLINE_INCOME_ENABLED] ?: true,
                    soundEnabled = preferences[SETTING_SOUND_ENABLED] ?: true,
                    vibrationEnabled = preferences[SETTING_VIBRATION_ENABLED] ?: true,
                    textScale = preferences[SETTING_TEXT_SCALE] ?: 1f,
                    guideCompleted = preferences[SETTING_GUIDE_COMPLETED] ?: false,
                    profileName = normalizeProfileName(preferences[SETTING_PROFILE_NAME].orEmpty()),
                    profileImageUri = preferences[SETTING_PROFILE_IMAGE_URI].orEmpty().trim()
                )
            )
        }

    suspend fun save(snapshot: GameSnapshot) {
        val state = snapshot.state
        val settings = snapshot.settings

        dataStore.edit { preferences ->
            preferences[ESSENCE] = state.essence
            preferences[TAP_POWER] = state.tapPower
            preferences[AUTO_POWER] = state.autoPower
            preferences[RITUALS] = state.rituals
            preferences[PATH] = state.path.name

            preferences[FTUE_CLICK_DONE] = state.ftue.firstClickDone
            preferences[FTUE_UPGRADE_DONE] = state.ftue.firstUpgradeDone
            preferences[FTUE_RITUAL_DONE] = state.ftue.firstRitualDone
            preferences[CLAIMED_REWARDS] = state.claimedRewardLevels.map(Int::toString).toSet()

            preferences[DAILY_DAY_KEY] = state.daily.dayKey
            preferences[DAILY_TAPS] = state.daily.taps
            preferences[DAILY_UPGRADES] = state.daily.upgrades
            preferences[DAILY_ESSENCE_EARNED] = state.daily.essenceEarned
            preferences[DAILY_CLAIMED_SLOTS] = state.daily.claimedMissionSlots.map(Int::toString).toSet()

            preferences[BOSS_IS_ACTIVE] = state.boss.isActive
            preferences[BOSS_CURRENT_HP] = state.boss.currentHp
            preferences[BOSS_MAX_HP] = state.boss.maxHp
            preferences[BOSS_SECONDS_UNTIL_SPAWN] = state.boss.secondsUntilSpawn
            preferences[BOSS_SECONDS_LEFT] = state.boss.secondsLeft

            preferences[RETENTION_CRIT_LEVEL] = state.retention.critLevel
            preferences[RETENTION_STREAK_LEVEL] = state.retention.streakLevel
            preferences[RETENTION_OFFLINE_LEVEL] = state.retention.offlineBoostLevel
            preferences[RETENTION_STREAK_CHARGE] = state.retention.streakCharge
            preferences[RETENTION_CACHE_LEVEL] = state.retention.cacheLevel
            preferences[RETENTION_CHEST_READY_AT] = state.retention.chestReadyAtMillis
            preferences[RETENTION_RETURN_STREAK] = state.retention.returnStreak
            preferences[RETENTION_BEST_RETURN_STREAK] = state.retention.bestReturnStreak
            preferences[RETENTION_LAST_RETURN_DAY_KEY] = state.retention.lastDailyReturnClaimDayKey
            preferences[RETENTION_RELIC_SHARDS] = state.retention.relicShards
            preferences[RETENTION_RELIC_LEVEL] = state.retention.relicLevel
            preferences[RETENTION_ALTAR_WEEK_KEY] = state.retention.altarWeekKey
            preferences[RETENTION_ALTAR_FAVOR] = state.retention.altarFavor
            preferences[RETENTION_ALTAR_CLAIMED_TIERS] = state.retention.altarClaimedTiers.map(Int::toString).toSet()

            preferences[LAST_ACTIVE_AT_MILLIS] = state.lastActiveAtMillis

            preferences[SETTING_SHOW_FTUE_HINTS] = settings.showFtueHints
            preferences[SETTING_COMPACT_NUMBERS] = settings.compactNumbers
            preferences[SETTING_OFFLINE_INCOME_ENABLED] = settings.offlineIncomeEnabled
            preferences[SETTING_SOUND_ENABLED] = settings.soundEnabled
            preferences[SETTING_VIBRATION_ENABLED] = settings.vibrationEnabled
            preferences[SETTING_TEXT_SCALE] = settings.textScale
            preferences[SETTING_GUIDE_COMPLETED] = settings.guideCompleted
            preferences[SETTING_PROFILE_NAME] = normalizeProfileName(settings.profileName)
            preferences[SETTING_PROFILE_IMAGE_URI] = settings.profileImageUri.trim()
        }
    }

    private companion object {
        val ESSENCE = longPreferencesKey("essence")
        val TAP_POWER = intPreferencesKey("tap_power")
        val AUTO_POWER = intPreferencesKey("auto_power")
        val RITUALS = intPreferencesKey("rituals")
        val PATH = stringPreferencesKey("path")

        val FTUE_CLICK_DONE = booleanPreferencesKey("ftue_click_done")
        val FTUE_UPGRADE_DONE = booleanPreferencesKey("ftue_upgrade_done")
        val FTUE_RITUAL_DONE = booleanPreferencesKey("ftue_ritual_done")
        val CLAIMED_REWARDS = stringSetPreferencesKey("claimed_rewards")

        val DAILY_DAY_KEY = stringPreferencesKey("daily_day_key")
        val DAILY_TAPS = intPreferencesKey("daily_taps")
        val DAILY_UPGRADES = intPreferencesKey("daily_upgrades")
        val DAILY_ESSENCE_EARNED = longPreferencesKey("daily_essence_earned")
        val DAILY_CLAIMED_SLOTS = stringSetPreferencesKey("daily_claimed_slots")

        val BOSS_IS_ACTIVE = booleanPreferencesKey("boss_is_active")
        val BOSS_CURRENT_HP = longPreferencesKey("boss_current_hp")
        val BOSS_MAX_HP = longPreferencesKey("boss_max_hp")
        val BOSS_SECONDS_UNTIL_SPAWN = intPreferencesKey("boss_seconds_until_spawn")
        val BOSS_SECONDS_LEFT = intPreferencesKey("boss_seconds_left")

        val RETENTION_CRIT_LEVEL = intPreferencesKey("retention_crit_level")
        val RETENTION_STREAK_LEVEL = intPreferencesKey("retention_streak_level")
        val RETENTION_OFFLINE_LEVEL = intPreferencesKey("retention_offline_level")
        val RETENTION_STREAK_CHARGE = intPreferencesKey("retention_streak_charge")
        val RETENTION_CACHE_LEVEL = intPreferencesKey("retention_cache_level")
        val RETENTION_CHEST_READY_AT = longPreferencesKey("retention_chest_ready_at")
        val RETENTION_RETURN_STREAK = intPreferencesKey("retention_return_streak")
        val RETENTION_BEST_RETURN_STREAK = intPreferencesKey("retention_best_return_streak")
        val RETENTION_LAST_RETURN_DAY_KEY = stringPreferencesKey("retention_last_return_day_key")
        val RETENTION_RELIC_SHARDS = intPreferencesKey("retention_relic_shards")
        val RETENTION_RELIC_LEVEL = intPreferencesKey("retention_relic_level")
        val RETENTION_ALTAR_WEEK_KEY = stringPreferencesKey("retention_altar_week_key")
        val RETENTION_ALTAR_FAVOR = intPreferencesKey("retention_altar_favor")
        val RETENTION_ALTAR_CLAIMED_TIERS = stringSetPreferencesKey("retention_altar_claimed_tiers")

        val LAST_ACTIVE_AT_MILLIS = longPreferencesKey("last_active_at_millis")

        val SETTING_SHOW_FTUE_HINTS = booleanPreferencesKey("setting_show_ftue_hints")
        val SETTING_COMPACT_NUMBERS = booleanPreferencesKey("setting_compact_numbers")
        val SETTING_OFFLINE_INCOME_ENABLED = booleanPreferencesKey("setting_offline_income_enabled")
        val SETTING_SOUND_ENABLED = booleanPreferencesKey("setting_sound_enabled")
        val SETTING_VIBRATION_ENABLED = booleanPreferencesKey("setting_vibration_enabled")
        val SETTING_TEXT_SCALE = floatPreferencesKey("setting_text_scale")
        val SETTING_GUIDE_COMPLETED = booleanPreferencesKey("setting_guide_completed")
        val SETTING_PROFILE_NAME = stringPreferencesKey("setting_profile_name")
        val SETTING_PROFILE_IMAGE_URI = stringPreferencesKey("setting_profile_image_uri")
    }
}
