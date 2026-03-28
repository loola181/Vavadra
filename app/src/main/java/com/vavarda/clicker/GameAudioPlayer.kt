package com.vavarda.clicker

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

enum class GameSound(
    val resId: Int
) {
    UPGRADE(R.raw.upgrade_buy),
    RITUAL(R.raw.ritual_cast),
    BOSS_HIT(R.raw.boss_hit),
    BOSS_DEFEAT(R.raw.boss_defeat),
    REWARD(R.raw.reward_claim),
    DAILY(R.raw.daily_claim),
    RETURN_MILESTONE(R.raw.return_milestone),
    ALTAR(R.raw.altar_resonance)
}

class GameAudioPlayer(context: Context) {
    private val soundPool: SoundPool
    private val soundIds: Map<GameSound, Int>
    private val loadedIds = mutableSetOf<Int>()

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        soundIds = GameSound.values().associateWith { sound ->
            soundPool.load(context, sound.resId, 1)
        }

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedIds += sampleId
            }
        }
    }

    fun play(sound: GameSound, volume: Float, rate: Float) {
        val sampleId = soundIds.getValue(sound)
        if (sampleId !in loadedIds) return
        soundPool.play(
            sampleId,
            volume,
            volume,
            1,
            0,
            rate
        )
    }

    fun release() {
        soundPool.release()
    }
}
