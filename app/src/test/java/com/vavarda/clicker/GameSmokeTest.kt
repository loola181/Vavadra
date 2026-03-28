package com.vavarda.clicker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSmokeTest {
    private val config = defaultEconomyConfig()

    @Test
    fun newGameSnapshot_usesCleanDefaults() {
        val nowMillis = 1_741_596_800_000L

        val snapshot = createNewGameSnapshot(
            config = config,
            nowMillis = nowMillis,
            dayKey = "2026-03-10"
        )

        assertEquals(0L, snapshot.state.essence)
        assertEquals(config.tap.startingPower, snapshot.state.tapPower)
        assertEquals(config.auto.startingPower, snapshot.state.autoPower)
        assertEquals("2026-03-10", snapshot.state.daily.dayKey)
        assertEquals(config.boss.spawnIntervalSeconds, snapshot.state.boss.secondsUntilSpawn)
        assertEquals(nowMillis, snapshot.state.lastActiveAtMillis)
    }

    @Test
    fun upgrades_increasePowerAndConsumeEssence() {
        val state = defaultGameState(config).copy(
            essence = 1_000L,
            daily = DailyProgress(
                dayKey = "2026-03-10",
                taps = 0,
                upgrades = 0,
                essenceEarned = 0L,
                claimedMissionSlots = emptySet()
            )
        )

        val tapUpgrade = buyTapUpgrade(state, config)
        assertTrue(tapUpgrade != null)
        assertEquals(2, tapUpgrade!!.first.tapPower)
        assertEquals(980L, tapUpgrade.first.essence)
        assertEquals(1, tapUpgrade.first.daily.upgrades)

        val autoUpgrade = buyAutoUpgrade(state, config)
        assertTrue(autoUpgrade != null)
        assertEquals(1, autoUpgrade!!.first.autoPower)
        assertEquals(900L, autoUpgrade.first.essence)
        assertEquals(1, autoUpgrade.first.daily.upgrades)
    }

    @Test
    fun ritual_resetsEconomyAndAddsRitual() {
        val state = defaultGameState(config).copy(
            essence = config.prestige.baseCost,
            tapPower = 7,
            autoPower = 3
        )

        val ritual = performRitual(state, config)

        assertTrue(ritual != null)
        assertEquals(0L, ritual!!.first.essence)
        assertEquals(config.prestige.resetTapPower, ritual.first.tapPower)
        assertEquals(config.prestige.resetAutoPower, ritual.first.autoPower)
        assertEquals(1, ritual.first.rituals)
        assertTrue(ritual.first.ftue.firstRitualDone)
    }

    @Test
    fun offlineIncome_grantsAutoEssenceAndUpdatesTimestamp() {
        val nowMillis = 1_741_596_800_000L
        val oneHourAgo = nowMillis - 3_600_000L
        val state = defaultGameState(config).copy(
            autoPower = 5,
            path = AlignmentPath.LIGHT,
            lastActiveAtMillis = oneHourAgo,
            daily = DailyProgress(
                dayKey = "2026-03-10",
                taps = 0,
                upgrades = 0,
                essenceEarned = 0L,
                claimedMissionSlots = emptySet()
            )
        )
        val snapshot = GameSnapshot(
            state = state,
            settings = GameSettings(offlineIncomeEnabled = true)
        )

        val (result, reward) = applyOfflineIncome(
            snapshot = snapshot,
            config = config,
            nowMillis = nowMillis,
            dayKey = "2026-03-10"
        )

        assertEquals(21_600L, reward)
        assertEquals(21_600L, result.state.essence)
        assertEquals(21_600L, result.state.daily.essenceEarned)
        assertEquals(nowMillis, result.state.lastActiveAtMillis)
        assertFalse(result.state.boss.isActive)
    }

    @Test
    fun hitBoss_onFinishingBlow_grantsRewardAndTracksDailyEssence() {
        val maxHp = 240L
        val state = defaultGameState(config).copy(
            daily = DailyProgress(
                dayKey = "2026-03-10",
                taps = 0,
                upgrades = 0,
                essenceEarned = 300L,
                claimedMissionSlots = emptySet()
            ),
            boss = BossState(
                isActive = true,
                currentHp = 40L,
                maxHp = maxHp,
                secondsUntilSpawn = 0,
                secondsLeft = 12
            )
        )

        val (result, reward) = hitBoss(state, damage = 50L, config = config)
        val resolvedReward = requireNotNull(reward)

        assertEquals(maxHp * config.boss.rewardPerHp, resolvedReward)
        assertEquals(resolvedReward, result.essence)
        assertEquals(300L + resolvedReward, result.daily.essenceEarned)
        assertFalse(result.boss.isActive)
        assertEquals(config.boss.spawnIntervalSeconds, result.boss.secondsUntilSpawn)
    }

    @Test
    fun hitBoss_onRegularHit_onlyReducesBossHp() {
        val state = defaultGameState(config).copy(
            essence = 120L,
            daily = DailyProgress(
                dayKey = "2026-03-10",
                taps = 0,
                upgrades = 0,
                essenceEarned = 300L,
                claimedMissionSlots = emptySet()
            ),
            boss = BossState(
                isActive = true,
                currentHp = 90L,
                maxHp = 240L,
                secondsUntilSpawn = 0,
                secondsLeft = 12
            )
        )

        val (result, reward) = hitBoss(state, damage = 50L, config = config)

        assertNull(reward)
        assertEquals(120L, result.essence)
        assertEquals(300L, result.daily.essenceEarned)
        assertTrue(result.boss.isActive)
        assertEquals(40L, result.boss.currentHp)
    }
}
