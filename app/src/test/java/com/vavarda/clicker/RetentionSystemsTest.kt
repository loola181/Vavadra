package com.vavarda.clicker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RetentionSystemsTest {
    private val config = defaultEconomyConfig()

    @Test
    fun claimReturnStreak_onSeventhDay_appliesMilestoneRewards() {
        val state = defaultGameState(config).copy(
            essence = 500L,
            retention = RetentionProgress(
                returnStreak = 6,
                bestReturnStreak = 6,
                lastDailyReturnClaimDayKey = "2026-03-18"
            )
        )

        val result = claimReturnStreak(
            state = state,
            config = config,
            dayKey = "2026-03-19"
        )

        assertNotNull(result)
        result!!
        assertEquals(7, result.streakDay)
        assertEquals(14_000L + 900L + 6 * 700L, result.rewardEssence)
        assertEquals(3, result.shardReward)
        assertEquals(7, result.state.retention.returnStreak)
        assertEquals(7, result.state.retention.bestReturnStreak)
        assertEquals("2026-03-19", result.state.retention.lastDailyReturnClaimDayKey)
        assertEquals(3, result.state.retention.relicShards)
    }

    @Test
    fun claimReturnStreak_afterGap_resetsToDayOne() {
        val state = defaultGameState(config).copy(
            retention = RetentionProgress(
                returnStreak = 6,
                bestReturnStreak = 6,
                lastDailyReturnClaimDayKey = "2026-03-17"
            )
        )

        val result = claimReturnStreak(
            state = state,
            config = config,
            dayKey = "2026-03-19"
        )

        assertNotNull(result)
        result!!
        assertEquals(1, result.streakDay)
        assertEquals(config.retention.returnStreak.baseReward, result.rewardEssence)
        assertEquals(0, result.shardReward)
        assertEquals(1, result.state.retention.returnStreak)
        assertEquals(6, result.state.retention.bestReturnStreak)
    }

    @Test
    fun claimArcaneCache_levelsRelicWhenShardThresholdIsReached() {
        val nowMillis = 10_000L
        val state = defaultGameState(config).copy(
            retention = RetentionProgress(
                cacheLevel = 0,
                chestReadyAtMillis = 0L,
                relicShards = 4
            )
        )

        val result = claimArcaneCache(
            state = state,
            config = config,
            nowMillis = nowMillis
        )

        assertNotNull(result)
        result!!
        assertEquals(cacheReward(level = 0, playerLevel = 1, retention = config.retention), result.rewardEssence)
        assertEquals(1, result.shardReward)
        assertEquals(result.rewardEssence, result.state.essence)
        assertEquals(1, result.state.retention.relicLevel)
        assertEquals(0, result.state.retention.relicShards)
        assertEquals(nowMillis + 52 * 60_000L, result.state.retention.chestReadyAtMillis)
    }

    @Test
    fun claimSeasonalAltarTier_marksTierClaimedAndCannotRepeat() {
        val weekKey = "2026-W12"
        val tier = config.retention.altar.tiers.first()
        val state = defaultGameState(config).copy(
            retention = RetentionProgress(
                altarWeekKey = weekKey,
                altarFavor = tier.requiredFavor,
                relicShards = 4
            )
        )

        val result = claimSeasonalAltarTier(
            state = state,
            tier = tier,
            config = config,
            weekKey = weekKey
        )

        assertNotNull(result)
        result!!
        assertEquals(tier.rewardEssence, result.state.essence)
        assertEquals(1, result.state.retention.relicLevel)
        assertEquals(1, result.state.retention.relicShards)
        assertTrue(tier.tier in result.state.retention.altarClaimedTiers)
        assertNull(
            claimSeasonalAltarTier(
                state = result.state,
                tier = tier,
                config = config,
                weekKey = weekKey
            )
        )
    }
}
