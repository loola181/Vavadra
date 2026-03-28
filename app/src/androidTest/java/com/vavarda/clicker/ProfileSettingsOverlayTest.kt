package com.vavarda.clicker

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileSettingsOverlayTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsOverlay_showsProfileControls() {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasTestTag("open_settings_button")).fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Начать игру").fetchSemanticsNodes().isNotEmpty()
        }

        val guideButtons = composeRule.onAllNodesWithText("Начать игру").fetchSemanticsNodes()
        if (guideButtons.isNotEmpty()) {
            composeRule.onAllNodesWithText("Начать игру")[0].performClick()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodes(hasTestTag("open_settings_button")).fetchSemanticsNodes().isNotEmpty() &&
                    composeRule.onAllNodes(hasTestTag("header_profile_button")).fetchSemanticsNodes().isNotEmpty()
            }
        }

        composeRule.onNodeWithTag("header_profile_button").assertIsDisplayed()
        composeRule.onNodeWithTag("open_settings_button").performClick()

        composeRule.onNodeWithTag("settings_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_name_field").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Добавить фото").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Выбрать из галереи").fetchSemanticsNodes().isNotEmpty()
        }

        val galleryLabel = if (
            composeRule.onAllNodesWithText("Добавить фото").fetchSemanticsNodes().isNotEmpty()
        ) {
            "Добавить фото"
        } else {
            "Выбрать из галереи"
        }
        composeRule.onNodeWithText(galleryLabel).performScrollTo().assertIsDisplayed()

        if (deviceSupportsCameraCapture()) {
            composeRule.onNodeWithText("Сделать фото").performScrollTo().assertIsDisplayed()
        }
    }

    private fun deviceSupportsCameraCapture(): Boolean {
        val context = composeRule.activity
        val packageManager = context.packageManager
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) &&
            captureIntent.resolveActivity(packageManager) != null
    }
}
