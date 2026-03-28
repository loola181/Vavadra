package com.vavarda.clicker

import android.content.Context
import org.json.JSONObject

data class NarrativeEntry(
    val title: String,
    val body: String
)

data class BossNarrative(
    val idle: String,
    val spawn: String,
    val wounded: String,
    val defeated: String
)

data class NarrativeLocalePack(
    val intro: NarrativeEntry,
    val milestones: Map<Int, NarrativeEntry>,
    val boss: BossNarrative
)

fun loadNarrativePack(context: Context): NarrativeLocalePack {
    return loadJsonAssetOrDefault(
        context = context,
        assetName = "narrative_pack.json",
        defaultValue = ::defaultNarrativePack,
        parser = ::parseNarrativePack
    )
}

fun narrativeForLevel(
    pack: NarrativeLocalePack,
    level: Int
): NarrativeEntry? {
    return pack.milestones[level]
}

internal fun parseNarrativePack(raw: String): NarrativeLocalePack {
    val root = JSONObject(raw)
    val localeRoot = root.getJSONObject("ru")
    val intro = localeRoot.getJSONObject("intro")
    val boss = localeRoot.getJSONObject("boss")
    val milestonesRoot = localeRoot.getJSONObject("milestones")

    val milestoneEntries = buildMap {
        milestonesRoot.keys().forEach { key ->
            val value = milestonesRoot.getJSONObject(key)
            val level = key.toIntOrNull() ?: return@forEach
            put(
                level,
                NarrativeEntry(
                    title = value.getString("title"),
                    body = value.getString("body")
                )
            )
        }
    }

    return NarrativeLocalePack(
        intro = NarrativeEntry(
            title = intro.getString("title"),
            body = intro.getString("body")
        ),
        milestones = milestoneEntries,
        boss = BossNarrative(
            idle = boss.getString("idle"),
            spawn = boss.getString("spawn"),
            wounded = boss.getString("wounded"),
            defeated = boss.getString("defeated")
        )
    )
}

private fun defaultNarrativePack(): NarrativeLocalePack {
    return NarrativeLocalePack(
        intro = NarrativeEntry(
            title = "Пробуждение Искры",
            body = "Башня Ваварды снова отвечает на зов. Один клик разбудит печать, и ритуал начнет новый виток."
        ),
        milestones = mapOf(
            10 to NarrativeEntry(
                title = "Вестник Сердца",
                body = "Искры впервые складываются в ритм. Башня замечает вас и открывает следующий круг силы."
            )
        ),
        boss = BossNarrative(
            idle = "В тенях слышен тяжелый шаг. Башня готовит следующую встречу.",
            spawn = "Незваный хранитель выходит из разлома: «Докажи, что достоин печати».",
            wounded = "Босс ревет: «Еще один удар, и зал вспыхнет вместе с тобой!»",
            defeated = "Разлом захлопывается. В воздухе оседает горький пепел победы."
        )
    )
}
