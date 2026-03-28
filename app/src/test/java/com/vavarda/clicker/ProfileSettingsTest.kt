package com.vavarda.clicker

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileSettingsTest {

    @Test
    fun normalizeProfileName_trimsCollapsesWhitespaceAndLimitsLength() {
        val raw = "   Лорд    Бездны      с очень длинным именем   "

        val normalized = normalizeProfileName(raw)

        assertEquals("Лорд Бездны с очень длин", normalized)
    }

    @Test
    fun resolvedProfileName_usesFallbackForBlankValue() {
        val resolved = resolvedProfileName("   ", fallback = "Игрок")

        assertEquals("Игрок", resolved)
    }
}
