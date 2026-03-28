package com.vavarda.clicker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ConfigParsingTest {
    @Test
    fun parseEconomyConfig_appliesDefaultsForOptionalSections() {
        val config = parseEconomyConfig(
            """
            {
              "tap": {
                "startingPower": 1,
                "baseCost": 20,
                "costOffset": 0,
                "powerPerUpgrade": 1
              },
              "auto": {
                "startingPower": 0,
                "baseCost": 100,
                "costOffset": 1,
                "powerPerUpgrade": 1
              },
              "prestige": {
                "baseCost": 5000,
                "levelBonusPerRitual": 3,
                "resetTapPower": 1,
                "resetAutoPower": 0
              },
              "levels": {
                "maxLevel": 250,
                "essenceFactor": 40,
                "targetLevels": [10, 25],
                "artMilestones": [
                  {
                    "level": 1,
                    "drawableName": "vavarda_lvl_1",
                    "title": "Пробуждение"
                  }
                ]
              },
              "rewards": [
                {
                  "level": 10,
                  "title": "Награда",
                  "description": "+1500 Искр",
                  "kind": "essence",
                  "amount": 1500
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals(250, config.levels.maxLevel)
        assertEquals(3, config.daily.missionCount)
        assertEquals(180, config.boss.spawnIntervalSeconds)
        assertEquals("Тьма", config.factions.getValue(AlignmentPath.SHADOW).name)
        assertEquals(7, config.retention.returnStreak.milestones.first().day)
        assertEquals(48, config.retention.altar.tiers.first().requiredFavor)
    }

    @Test
    fun parseUiConfig_mergesPartialOverridesWithDefaults() {
        val config = parseUiConfig(
            """
            {
              "sounds": {
                "upgrade": {
                  "volume": 0.91
                }
              },
              "layoutProfiles": {
                "compact": {
                  "bottomBarHeight": 77
                }
              }
            }
            """.trimIndent()
        )

        assertEquals(0.91f, config.sounds.upgrade.volume, 0.0001f)
        assertEquals(1.02f, config.sounds.upgrade.rate, 0.0001f)
        assertEquals(77f, config.layoutProfiles.compact.bottomBarHeight, 0.0001f)
        assertEquals(58f, config.layoutProfiles.standard.bottomBarHeight, 0.0001f)
    }

    @Test
    fun parseNarrativePack_ignoresInvalidMilestoneKeys() {
        val pack = parseNarrativePack(
            """
            {
              "ru": {
                "intro": {
                  "title": "Старт",
                  "body": "Начало пути"
                },
                "boss": {
                  "idle": "Ждет",
                  "spawn": "Пришел",
                  "wounded": "Ранен",
                  "defeated": "Пал"
                },
                "milestones": {
                  "10": {
                    "title": "Десятый",
                    "body": "Текст"
                  },
                  "oops": {
                    "title": "Сломанный",
                    "body": "Игнор"
                  }
                }
              }
            }
            """.trimIndent()
        )

        assertEquals("Старт", pack.intro.title)
        assertEquals(1, pack.milestones.size)
        val milestone = pack.milestones[10]
        assertNotNull(milestone)
        assertEquals("Десятый", milestone?.title)
        assertEquals("Пал", pack.boss.defeated)
    }
}
