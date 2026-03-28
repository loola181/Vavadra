package com.vavarda.clicker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScreenRoutingTest {
    @Test
    fun parseGameScreenExtra_supportsPrimaryNamesAndAliases() {
        assertEquals(GameScreen.CORE, parseGameScreenExtra("CORE"))
        assertEquals(GameScreen.CORE, parseGameScreenExtra("home"))
        assertEquals(GameScreen.CORE, parseGameScreenExtra("dom"))

        assertEquals(GameScreen.GROWTH, parseGameScreenExtra("GROWTH"))
        assertEquals(GameScreen.GROWTH, parseGameScreenExtra("rost"))

        assertEquals(GameScreen.EVENTS, parseGameScreenExtra("EVENTS"))
        assertEquals(GameScreen.EVENTS, parseGameScreenExtra("battle"))
        assertEquals(GameScreen.EVENTS, parseGameScreenExtra("boy"))

        assertEquals(GameScreen.PATH, parseGameScreenExtra("PATH"))
        assertEquals(GameScreen.PATH, parseGameScreenExtra("put"))
    }

    @Test
    fun parseGameScreenExtra_returnsNullForUnknownValues() {
        assertNull(parseGameScreenExtra(null))
        assertNull(parseGameScreenExtra(""))
        assertNull(parseGameScreenExtra("settings"))
    }
}
