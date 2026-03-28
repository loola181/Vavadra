package com.vavarda.clicker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrimaryNavigationFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigation_switchesAcrossPrimaryScreens() {
        waitForAppShell()

        composeRule.onNodeWithTag("bottom_nav_growth").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Главный рывок").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Главный рывок").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_events").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("boss_hit_button").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Сводка цикла").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithTag("boss_hit_button").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("boss_hit_button").assertIsDisplayed()
        } else {
            composeRule.onNodeWithText("Сводка цикла").assertIsDisplayed()
        }

        composeRule.onNodeWithTag("bottom_nav_path").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Альтернативная ветвь").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Альтернативная ветвь").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_core").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("art_card").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("art_card").assertIsDisplayed()
    }

    @Test
    fun growthScreen_showsUpgradeActionsAndThresholds() {
        waitForAppShell()

        composeRule.onNodeWithTag("bottom_nav_growth").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("tap_upgrade_button").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("tap_upgrade_button").assertIsDisplayed()
        composeRule.onNodeWithTag("auto_upgrade_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("ritual_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("До апгрейда").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("До ритуала").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun settingsOverlay_showsComfortAndAppearanceSections() {
        waitForAppShell()

        composeRule.onNodeWithTag("open_settings_button").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("settings_overlay").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("settings_overlay").assertIsDisplayed()
        composeRule.onNodeWithText("Комфорт").assertIsDisplayed()
        composeRule.onNodeWithText("Звук").assertIsDisplayed()

        repeat(4) {
            if (composeRule.onAllNodesWithText("Вид и читаемость").fetchSemanticsNodes().isNotEmpty()) {
                return@repeat
            }
            composeRule.onNodeWithTag("settings_overlay").performTouchInput { swipeUp() }
            composeRule.waitForIdle()
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Вид и читаемость").fetchSemanticsNodes().isNotEmpty()
        }
        assertTrue(
            composeRule.onAllNodesWithText("Вид и читаемость").fetchSemanticsNodes().isNotEmpty()
        )
        assertTrue(
            composeRule.onAllNodesWithText("Стандартный").fetchSemanticsNodes().isNotEmpty()
        )
    }

    private fun waitForAppShell() {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithTag("bottom_nav_core").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Начать игру").fetchSemanticsNodes().isNotEmpty()
        }

        val guideButtons = composeRule.onAllNodesWithText("Начать игру").fetchSemanticsNodes()
        if (guideButtons.isNotEmpty()) {
            composeRule.onAllNodesWithText("Начать игру")[0].performClick()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithTag("bottom_nav_core").fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
}
