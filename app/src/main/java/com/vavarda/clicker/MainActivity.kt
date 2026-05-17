package com.vavarda.clicker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vavarda.clicker.ui.theme.VavardaTheme
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.sqrt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var nextOpenScreenRequestId = 0L
    private var openScreenRequest by mutableStateOf<ScreenOpenRequest?>(null)
    private var remoteGateState by mutableStateOf(RemoteGatePhase.CHECKING)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(forceRussianContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateOpenScreenRequest(intent)
        setContent {
            VavardaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (remoteGateState) {
                        RemoteGatePhase.CHECKING -> RemoteGateLoadingScreen()
                        RemoteGatePhase.READY -> VavardaClickerScreen(openScreenRequest = openScreenRequest)
                    }
                }
            }
        }
        lifecycleScope.launch {
            val result = RemoteExperimentClient.resolve(this@MainActivity.applicationContext)
            if (result is RemoteExperimentResult.WelcomeUrl) {
                launchRemoteWebActivity(this@MainActivity, result.url)
            }
            remoteGateState = RemoteGatePhase.READY
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateOpenScreenRequest(intent)
    }

    private fun updateOpenScreenRequest(intent: Intent?) {
        val targetScreen = parseGameScreenExtra(intent?.getStringExtra("open_screen")) ?: return
        nextOpenScreenRequestId += 1L
        openScreenRequest = ScreenOpenRequest(
            id = nextOpenScreenRequestId,
            screen = targetScreen
        )
    }
}

private fun forceRussianContext(base: Context): Context {
    val locale = Locale("ru", "RU")
    Locale.setDefault(locale)
    val config = Configuration(base.resources.configuration)
    config.setLocale(locale)
    return base.createConfigurationContext(config)
}

private fun emitUiInteractionFeedback(
    view: View,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    soundEffect: Int = SoundEffectConstants.CLICK,
    hapticFeedback: Int = HapticFeedbackConstants.CONTEXT_CLICK
) {
    if (soundEnabled) {
        view.playSoundEffect(soundEffect)
    }
    if (vibrationEnabled) {
        view.performHapticFeedback(hapticFeedback)
    }
}

private data class GoalThresholdSpec(
    val value: String,
    val label: String,
    val accentColor: Color,
    val ready: Boolean
)

private data class ScreenOpenRequest(
    val id: Long,
    val screen: GameScreen
)

private data class LoadedGameResources(
    val config: EconomyConfig,
    val uiConfig: UiConfig,
    val narrativePack: NarrativeLocalePack
)

private data class ClaimAllReadySummary(
    val state: GameState,
    val claimedCount: Int,
    val totalEssence: Long,
    val totalShards: Int,
    val returnClaim: ReturnStreakClaimResult?,
    val claimedAltarTiers: List<SeasonalAltarTierConfig>
)

private data class ClickBurst(
    val id: Long,
    val amount: Long,
    val horizontalDrift: Float
)

private data class UpgradeCelebration(
    val id: Long,
    val message: String,
    val accentColor: Color
)

private data class BossImpactBurst(
    val id: Long,
    val damage: Long,
    val rewardEssence: Long?,
    val horizontalDrift: Float,
    val verticalLift: Float,
    val accentColor: Color,
    val finishingBlow: Boolean
)

private data class EpicMomentOverlayState(
    val title: String,
    val body: String,
    val effect: String,
    val drawableName: String,
    val accentColor: Color
)

@Composable
private fun VavardaClickerScreen(
    openScreenRequest: ScreenOpenRequest? = null
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val startupStartedAt = remember { SystemClock.elapsedRealtime() }
    var loadedResources by remember(appContext) { mutableStateOf<LoadedGameResources?>(null) }
    var startupPhase by remember(appContext) { mutableStateOf(StartupPhase.BOOTSTRAP) }

    LaunchedEffect(appContext) {
        startupPhase = StartupPhase.BOOTSTRAP
        loadedResources = withContext(Dispatchers.IO) {
            LoadedGameResources(
                config = loadEconomyConfig(appContext),
                uiConfig = loadUiConfig(appContext),
                narrativePack = loadNarrativePack(appContext)
            )
        }
        startupPhase = StartupPhase.RESTORING
    }

    val resources = loadedResources
    if (resources == null) {
        StartupLoadingScreen(phase = startupPhase)
        return
    }

    val config = resources.config
    val uiConfig = resources.uiConfig
    val narrativePack = resources.narrativePack
    val repository = remember { GameDataStoreRepository(appContext, config) }
    val analytics = remember { LocalAnalyticsTracker() }
    val audioPlayer = remember { GameAudioPlayer(appContext) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    val clickFlash = remember { Animatable(0f) }
    val clickBursts = remember { mutableStateListOf<ClickBurst>() }
    val upgradeCelebrations = remember { mutableStateListOf<UpgradeCelebration>() }
    val bossImpactBursts = remember { mutableStateListOf<BossImpactBurst>() }
    val initialScreen = openScreenRequest?.screen ?: GameScreen.CORE

    var hydrated by remember { mutableStateOf(false) }
    var gameState by remember { mutableStateOf(defaultGameState(config)) }
    var settings by remember { mutableStateOf(GameSettings()) }
    var activeRewardLevel by remember { mutableStateOf<Int?>(null) }
    var offlineIncomeBanner by remember { mutableLongStateOf(0L) }
    var bossRewardBanner by remember { mutableLongStateOf(0L) }
    var clickBurstId by remember { mutableLongStateOf(0L) }
    var upgradeCelebrationId by remember { mutableLongStateOf(0L) }
    var bossImpactBurstId by remember { mutableLongStateOf(0L) }
    var showSettingsOverlay by remember { mutableStateOf(false) }
    var showGuideOverlay by remember { mutableStateOf(false) }
    var bossNarrativeMessage by remember { mutableStateOf(narrativePack.boss.idle) }
    var activeScreen by remember { mutableStateOf(initialScreen) }
    var epicMomentOverlay by remember { mutableStateOf<EpicMomentOverlayState?>(null) }
    var tapUpgradeSuccessTick by remember { mutableIntStateOf(0) }
    var autoUpgradeSuccessTick by remember { mutableIntStateOf(0) }
    var ritualSuccessTick by remember { mutableIntStateOf(0) }
    var critUpgradeSuccessTick by remember { mutableIntStateOf(0) }
    var streakUpgradeSuccessTick by remember { mutableIntStateOf(0) }
    var offlineUpgradeSuccessTick by remember { mutableIntStateOf(0) }
    var cacheUpgradeSuccessTick by remember { mutableIntStateOf(0) }
    var bossImpactTick by remember { mutableIntStateOf(0) }
    var bossDefeatTick by remember { mutableIntStateOf(0) }
    val currentGameState by rememberUpdatedState(gameState)
    val currentSettings by rememberUpdatedState(settings)

    LaunchedEffect(openScreenRequest?.id) {
        val targetScreen = openScreenRequest?.screen ?: return@LaunchedEffect
        activeScreen = targetScreen
    }

    suspend fun persistSnapshot() {
        repository.save(
            GameSnapshot(
                state = currentGameState.copy(lastActiveAtMillis = System.currentTimeMillis()),
                settings = currentSettings
            )
        )
    }

    LaunchedEffect(Unit) {
        startupPhase = StartupPhase.RESTORING
        val snapshot = repository.snapshots.first()
        val now = System.currentTimeMillis()
        val today = currentDayKey()
        val week = currentWeekKey()

        val normalized = snapshot.copy(
            state = normalizeAltarProgress(
                normalizeDailyProgress(snapshot.state, today),
                week
            )
        )
        val (withOffline, offlineIncome) = applyOfflineIncome(
            snapshot = normalized,
            config = config,
            nowMillis = now,
            dayKey = today
        )

        gameState = withOffline.state
        settings = withOffline.settings
        showGuideOverlay = !withOffline.settings.guideCompleted
        bossNarrativeMessage = if (withOffline.state.boss.isActive) {
            narrativePack.boss.spawn
        } else {
            narrativePack.boss.idle
        }

        if (!withOffline.settings.guideCompleted) {
            analytics.track(
                name = "guide_shown",
                params = mapOf("source" to "first_launch")
            )
        }

        if (offlineIncome > 0L) {
            offlineIncomeBanner = offlineIncome
            analytics.track(
                name = "offline_income_granted",
                params = mapOf(
                    "income" to offlineIncome,
                    "auto_power" to withOffline.state.autoPower
                )
            )
        }

        analytics.track(
            name = "session_start",
            params = mapOf(
                "level" to calculateLevel(withOffline.state.essence, withOffline.state.rituals, config),
                "path" to withOffline.state.path.name
            )
        )

        val elapsed = SystemClock.elapsedRealtime() - startupStartedAt
        val splashHoldMillis = (180L - elapsed).coerceAtLeast(0L)
        if (splashHoldMillis > 0L) {
            delay(splashHoldMillis)
        }
        hydrated = true
    }

    if (!hydrated) {
        StartupLoadingScreen(phase = startupPhase)
        return
    }

    val level = remember(gameState.essence, gameState.rituals, config) {
        calculateLevel(
            essence = gameState.essence,
            rituals = gameState.rituals,
            config = config
        )
    }

    val currentArt = remember(level, config) {
        config.levels.artMilestones
            .lastOrNull { milestone -> level >= milestone.level }
            ?: config.levels.artMilestones.first()
    }
    val nextArt = remember(level, config) {
        config.levels.artMilestones.firstOrNull { milestone -> milestone.level > level }
    }
    val activeFactionBonus = remember(gameState.path, config) {
        factionBonus(config, gameState.path)
    }
    val bossHitPreview = remember(
        gameState.tapPower,
        gameState.retention.relicLevel,
        activeFactionBonus,
        config.retention
    ) {
        (
            bossHitDamage(gameState.tapPower, activeFactionBonus).toDouble() *
                relicBossMultiplier(gameState.retention.relicLevel, config.retention)
            ).toLong().coerceAtLeast(1L)
    }
    val bossRewardPreview = remember(gameState.boss.maxHp, level, config.boss) {
        val rewardHp = if (gameState.boss.maxHp > 0L) {
            gameState.boss.maxHp
        } else {
            (config.boss.baseHp + level.toLong() * config.boss.hpPerLevel).coerceAtLeast(1L)
        }
        (rewardHp * config.boss.rewardPerHp).coerceAtLeast(1L)
    }
    val bossHitsToDefeat = remember(gameState.boss.isActive, gameState.boss.currentHp, bossHitPreview) {
        if (!gameState.boss.isActive) {
            null
        } else {
            ((gameState.boss.currentHp + bossHitPreview - 1L) / bossHitPreview)
                .coerceAtLeast(1L)
                .toInt()
        }
    }
    val dailyDayKey = remember(gameState.daily.dayKey) {
        if (gameState.daily.dayKey.isBlank()) currentDayKey() else gameState.daily.dayKey
    }
    val currentAltarWeekKey = remember(gameState.retention.altarWeekKey) {
        if (gameState.retention.altarWeekKey.isBlank()) currentWeekKey() else gameState.retention.altarWeekKey
    }
    val dailyMissions = remember(dailyDayKey, config.daily) {
        generateDailyMissions(dailyDayKey, config.daily)
    }
    val nextLevelEssence = remember(level, gameState.rituals, config) {
        essenceForLevel(level = level + 1, rituals = gameState.rituals, config = config)
    }
    val currentLevelEssence = remember(level, gameState.rituals, config) {
        essenceForLevel(level = level, rituals = gameState.rituals, config = config)
    }
    val progressToNext = if (nextLevelEssence <= currentLevelEssence) {
        1f
    } else {
        (
            (gameState.essence - currentLevelEssence).toFloat() /
                (nextLevelEssence - currentLevelEssence).toFloat()
            ).coerceIn(0f, 1f)
    }
    val tapUpgradeCost = remember(gameState.tapPower, config) {
        upgradeCost(config.tap.baseCost, gameState.tapPower, config.tap.costOffset)
    }
    val autoUpgradeCost = remember(gameState.autoPower, config) {
        upgradeCost(config.auto.baseCost, gameState.autoPower, config.auto.costOffset)
    }
    val ritualCost = remember(gameState.rituals, config) {
        config.prestige.baseCost * (gameState.rituals + 1)
    }
    val critUpgradeCost = remember(gameState.retention.critLevel, config.retention) {
        retentionUpgradeCost(
            baseCost = config.retention.crit.baseCost,
            costGrowth = config.retention.crit.costGrowth,
            currentLevel = gameState.retention.critLevel
        )
    }
    val streakUpgradeCost = remember(gameState.retention.streakLevel, config.retention) {
        retentionUpgradeCost(
            baseCost = config.retention.streak.baseCost,
            costGrowth = config.retention.streak.costGrowth,
            currentLevel = gameState.retention.streakLevel
        )
    }
    val offlineBoostUpgradeCost = remember(gameState.retention.offlineBoostLevel, config.retention) {
        retentionUpgradeCost(
            baseCost = config.retention.offlineBoost.baseCost,
            costGrowth = config.retention.offlineBoost.costGrowth,
            currentLevel = gameState.retention.offlineBoostLevel
        )
    }
    val cacheUpgradeCost = remember(gameState.retention.cacheLevel, config.retention) {
        retentionUpgradeCost(
            baseCost = config.retention.cache.baseCost,
            costGrowth = config.retention.cache.costGrowth,
            currentLevel = gameState.retention.cacheLevel
        )
    }
    val nowMillis = System.currentTimeMillis()
    val cacheReadySeconds = remember(
        gameState.retention.chestReadyAtMillis,
        gameState.retention.cacheLevel,
        nowMillis
    ) {
        cacheReadyInSeconds(gameState, nowMillis)
    }
    val cacheRewardPreview = remember(
        gameState.retention.cacheLevel,
        level,
        config.retention
    ) {
        cacheReward(
            level = gameState.retention.cacheLevel,
            playerLevel = level,
            retention = config.retention
        )
    }
    val cacheCooldownPreviewMinutes = remember(gameState.retention.cacheLevel, config.retention) {
        cacheCooldownMinutes(gameState.retention.cacheLevel, config.retention)
    }
    val returnStreakAvailable = remember(gameState.retention.lastDailyReturnClaimDayKey, dailyDayKey) {
        isReturnStreakClaimAvailable(gameState, dailyDayKey)
    }
    val returnStreakPreviewDay = remember(gameState.retention.lastDailyReturnClaimDayKey, gameState.retention.returnStreak, dailyDayKey) {
        predictedReturnStreakDay(gameState, dailyDayKey)
    }
    val returnStreakRewardPreview = remember(returnStreakPreviewDay, config.retention) {
        returnStreakRewardForDay(returnStreakPreviewDay, config.retention)
    }
    val currentReturnMilestone = remember(returnStreakPreviewDay, config.retention) {
        returnStreakMilestoneForDay(returnStreakPreviewDay, config.retention)
    }
    val nextRelicRequirement = remember(gameState.retention.relicLevel, gameState.retention.relicShards, config.retention) {
        relicShardsNeeded(gameState.retention.relicLevel, config.retention)
    }
    val altarProgress = remember(gameState.retention.altarFavor, config.retention) {
        seasonalAltarProgress(gameState, config.retention)
    }
    val altarClaimableTiers = remember(gameState.retention.altarFavor, gameState.retention.altarClaimedTiers, currentAltarWeekKey, config.retention) {
        config.retention.altar.tiers.filter { tier ->
            canClaimSeasonalAltarTier(gameState, tier, currentAltarWeekKey)
        }
    }
    val claimableMissionCount = remember(gameState.daily, dailyMissions) {
        dailyMissions.count { mission ->
            isMissionCompleted(gameState, mission) && !isMissionClaimed(gameState, mission)
        }
    }
    val eventsReadyCount = remember(
        claimableMissionCount,
        returnStreakAvailable,
        cacheReadySeconds,
        altarClaimableTiers
    ) {
        claimableMissionCount +
            (if (returnStreakAvailable) 1 else 0) +
            (if (cacheReadySeconds <= 0) 1 else 0) +
            altarClaimableTiers.size
    }
    val tapPreview = remember(gameState.tapPower, activeFactionBonus) {
        (
            manualTapGain(gameState.tapPower, activeFactionBonus).toDouble() *
                relicTapMultiplier(gameState.retention.relicLevel, config.retention)
            ).toLong().coerceAtLeast(1L)
    }
    val autoPreview = remember(gameState.autoPower, activeFactionBonus, gameState.retention.relicLevel) {
        (
            autoTickGain(gameState.autoPower, activeFactionBonus).toDouble() *
                relicAutoMultiplier(gameState.retention.relicLevel, config.retention)
            ).toLong().coerceAtLeast(0L)
    }
    val canUpgradeTap = gameState.essence >= tapUpgradeCost
    val canUpgradeAuto = gameState.essence >= autoUpgradeCost
    val canPerformRitual = gameState.essence >= ritualCost
    val ftueStep = remember(gameState.ftue) { resolveFtueStep(gameState.ftue) }
    val recommendedGrowthAction = remember(
        ftueStep,
        canPerformRitual,
        tapUpgradeCost,
        autoUpgradeCost
    ) {
        resolveRecommendedGrowthAction(
            ftueStep = ftueStep,
            canPerformRitual = canPerformRitual,
            tapUpgradeCost = tapUpgradeCost,
            autoUpgradeCost = autoUpgradeCost
        )
    }
    val nextMilestoneReward = remember(level, config.rewards) {
        config.rewards.firstOrNull { reward -> reward.level > level }
    }
    val nextMilestoneRemaining = remember(
        nextMilestoneReward,
        gameState.essence,
        gameState.rituals,
        config
    ) {
        nextMilestoneReward?.let { reward ->
            (essenceForLevel(reward.level, gameState.rituals, config) - gameState.essence).coerceAtLeast(0L)
        }
    }
    val pendingRewards = remember(level, gameState.claimedRewardLevels, config) {
        config.rewards
            .filter { reward -> level >= reward.level && reward.level !in gameState.claimedRewardLevels }
            .sortedBy { reward -> reward.level }
    }

    LaunchedEffect(pendingRewards, activeRewardLevel) {
        if (activeRewardLevel == null && pendingRewards.isNotEmpty()) {
            activeRewardLevel = pendingRewards.first().level
        }
    }

    val activeReward = remember(activeRewardLevel, config.rewards) {
        val rewardLevel = activeRewardLevel ?: return@remember null
        config.rewards.firstOrNull { reward -> reward.level == rewardLevel }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)

            val today = currentDayKey()
            val week = currentWeekKey()
            var nextState = normalizeAltarProgress(
                normalizeDailyProgress(gameState, today),
                week
            )
            val previousState = nextState

            val autoGain = (
                autoTickGain(nextState.autoPower, factionBonus(config, nextState.path)).toDouble() *
                    relicAutoMultiplier(nextState.retention.relicLevel, config.retention)
                ).toLong().coerceAtLeast(0L)
            if (autoGain > 0L) {
                nextState = nextState.copy(essence = nextState.essence + autoGain)
                nextState = addEssenceProgress(nextState, autoGain)
            }

            val liveLevel = calculateLevel(nextState.essence, nextState.rituals, config)
            val (afterBossTick, bossEvent) = advanceBossState(nextState, liveLevel, config)
            nextState = afterBossTick

            if (nextState != previousState) {
                gameState = nextState
            }

            when (bossEvent) {
                BossTickEvent.SPAWNED -> {
                    bossNarrativeMessage = narrativePack.boss.spawn
                    analytics.track(
                        name = "boss_spawned",
                        params = mapOf("level" to liveLevel, "hp" to nextState.boss.maxHp)
                    )
                }

                BossTickEvent.EXPIRED -> {
                    bossNarrativeMessage = narrativePack.boss.idle
                    analytics.track(name = "boss_expired")
                }

                null -> Unit
            }
        }
    }

    LaunchedEffect(hydrated) {
        if (!hydrated) return@LaunchedEffect
        while (true) {
            delay(5000)
            persistSnapshot()
        }
    }

    DisposableEffect(lifecycleOwner, hydrated) {
        if (!hydrated) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    scope.launch {
                        persistSnapshot()
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    fun emitTapFeedback(gain: Long) {
        emitUiInteractionFeedback(
            view = view,
            soundEnabled = settings.soundEnabled,
            vibrationEnabled = settings.vibrationEnabled,
            soundEffect = SoundEffectConstants.CLICK,
            hapticFeedback = HapticFeedbackConstants.KEYBOARD_TAP
        )

        val burst = ClickBurst(
            id = clickBurstId,
            amount = gain,
            horizontalDrift = listOf(-38f, -12f, 16f, 34f)[(clickBurstId % 4L).toInt()]
        )
        clickBurstId += 1L
        clickBursts.add(burst)

        scope.launch {
            clickFlash.snapTo(0.26f)
            clickFlash.animateTo(0f, animationSpec = tween(280))
        }
        scope.launch {
            delay(720)
            clickBursts.remove(burst)
        }
    }

    fun playGameSound(sound: GameSound, rateStep: Int = 0) {
        if (settings.soundEnabled) {
            val cue = uiConfig.sounds.forSound(sound)
            audioPlayer.play(
                sound = sound,
                volume = cue.volume,
                rate = (cue.rate + (cue.rateJitter * rateStep)).coerceIn(0.5f, 1.5f)
            )
        }
    }

    fun emitUpgradeFeedback(
        message: String,
        accentColor: Color,
        sound: GameSound = GameSound.UPGRADE,
        hapticFeedback: Int = HapticFeedbackConstants.CONTEXT_CLICK
    ) {
        playGameSound(sound)
        emitUiInteractionFeedback(
            view = view,
            soundEnabled = false,
            vibrationEnabled = settings.vibrationEnabled,
            hapticFeedback = hapticFeedback
        )

        val celebration = UpgradeCelebration(
            id = upgradeCelebrationId,
            message = message,
            accentColor = accentColor
        )
        upgradeCelebrationId += 1L
        upgradeCelebrations.add(celebration)
        scope.launch {
            delay(980)
            upgradeCelebrations.remove(celebration)
        }
    }

    fun maybeCelebrateRelicGrowth(previousState: GameState, nextState: GameState) {
        if (nextState.retention.relicLevel > previousState.retention.relicLevel) {
            emitUpgradeFeedback(
                message = appContext.getString(
                    R.string.feedback_relic_level_up,
                    nextState.retention.relicLevel
                ),
                accentColor = Color(0xFF73E6FF),
                sound = GameSound.REWARD,
                hapticFeedback = HapticFeedbackConstants.LONG_PRESS
            )
        }
    }

    fun emitBossHitFeedback(damage: Long, finishingBlow: Boolean, rewardEssence: Long? = null) {
        playGameSound(
            sound = if (finishingBlow) GameSound.BOSS_DEFEAT else GameSound.BOSS_HIT,
            rateStep = if (finishingBlow) 0 else ((bossImpactBurstId % 3L).toInt() - 1)
        )
        emitUiInteractionFeedback(
            view = view,
            soundEnabled = false,
            vibrationEnabled = settings.vibrationEnabled,
            hapticFeedback = if (finishingBlow) {
                HapticFeedbackConstants.LONG_PRESS
            } else {
                HapticFeedbackConstants.KEYBOARD_TAP
            }
        )

        bossImpactTick += 1
        if (finishingBlow) {
            bossDefeatTick += 1
        }

        val burst = BossImpactBurst(
            id = bossImpactBurstId,
            damage = damage,
            rewardEssence = rewardEssence,
            horizontalDrift = listOf(-34f, -16f, 14f, 30f)[(bossImpactBurstId % 4L).toInt()],
            verticalLift = listOf(0f, 10f, -8f, 14f)[(bossImpactBurstId % 4L).toInt()],
            accentColor = if (finishingBlow) Color(0xFFFFD978) else Color(0xFFFF8A8A),
            finishingBlow = finishingBlow
        )
        bossImpactBurstId += 1L
        bossImpactBursts.add(burst)
        scope.launch {
            delay(if (finishingBlow) 980 else 780)
            bossImpactBursts.remove(burst)
        }
    }

    fun handleArtTap() {
        val (nextState, gain) = performManualTap(gameState, config)
        gameState = nextState
        emitTapFeedback(gain)
        analytics.track(
            name = "tap",
            params = mapOf("gain" to gain, "path" to gameState.path.name)
        )
    }

    fun handleTapUpgrade() {
        val result = buyTapUpgrade(gameState, config)
        if (result != null) {
            gameState = result.first
            tapUpgradeSuccessTick += 1
            emitUpgradeFeedback(
                message = appContext.getString(R.string.feedback_tap_upgraded),
                accentColor = Color(0xFFFFD978),
                sound = GameSound.UPGRADE,
                hapticFeedback = HapticFeedbackConstants.CONTEXT_CLICK
            )
            analytics.track(
                name = "upgrade_bought",
                params = mapOf("kind" to "tap", "cost" to result.second, "new_power" to result.first.tapPower)
            )
        }
    }

    fun handleAutoUpgrade() {
        val result = buyAutoUpgrade(gameState, config)
        if (result != null) {
            gameState = result.first
            autoUpgradeSuccessTick += 1
            emitUpgradeFeedback(
                message = appContext.getString(R.string.feedback_auto_upgraded),
                accentColor = Color(0xFF9C6BFF),
                sound = GameSound.UPGRADE,
                hapticFeedback = HapticFeedbackConstants.CONTEXT_CLICK
            )
            analytics.track(
                name = "upgrade_bought",
                params = mapOf("kind" to "auto", "cost" to result.second, "new_power" to result.first.autoPower)
            )
        }
    }

    fun handleRitual() {
        val result = performRitual(gameState, config)
        if (result != null) {
            gameState = result.first
            ritualSuccessTick += 1
            emitUpgradeFeedback(
                message = appContext.getString(R.string.feedback_ritual_performed),
                accentColor = Color(0xFFFF8A8A),
                sound = GameSound.RITUAL,
                hapticFeedback = HapticFeedbackConstants.LONG_PRESS
            )
            analytics.track(
                name = "ritual_performed",
                params = mapOf("rituals_total" to result.first.rituals)
            )
        }
    }

    fun handleCritUpgrade() {
        val result = buyCritUpgrade(gameState, config)
        if (result != null) {
            gameState = result.first
            critUpgradeSuccessTick += 1
            emitUpgradeFeedback(
                message = "Крит усилен",
                accentColor = Color(0xFFB681FF)
            )
            analytics.track(
                name = "upgrade_bought",
                params = mapOf(
                    "kind" to "retention_crit",
                    "cost" to result.second,
                    "level" to result.first.retention.critLevel
                )
            )
        }
    }

    fun handleStreakUpgrade() {
        val result = buyStreakUpgrade(gameState, config)
        if (result != null) {
            gameState = result.first
            streakUpgradeSuccessTick += 1
            emitUpgradeFeedback(
                message = "Серия усилена",
                accentColor = Color(0xFF7DE3D0)
            )
            analytics.track(
                name = "upgrade_bought",
                params = mapOf(
                    "kind" to "retention_streak",
                    "cost" to result.second,
                    "level" to result.first.retention.streakLevel
                )
            )
        }
    }

    fun handleOfflineBoostUpgrade() {
        val result = buyOfflineBoostUpgrade(gameState, config)
        if (result != null) {
            gameState = result.first
            offlineUpgradeSuccessTick += 1
            emitUpgradeFeedback(
                message = "Оффлайн-буст усилен",
                accentColor = Color(0xFFFFC857)
            )
            analytics.track(
                name = "upgrade_bought",
                params = mapOf(
                    "kind" to "retention_offline",
                    "cost" to result.second,
                    "level" to result.first.retention.offlineBoostLevel
                )
            )
        }
    }

    fun handleCacheUpgrade() {
        val result = buyCacheUpgrade(gameState, config)
        if (result != null) {
            gameState = result.first
            cacheUpgradeSuccessTick += 1
            emitUpgradeFeedback(
                message = appContext.getString(R.string.feedback_cache_upgraded),
                accentColor = Color(0xFF6FE0FF)
            )
            analytics.track(
                name = "upgrade_bought",
                params = mapOf(
                    "kind" to "retention_cache",
                    "cost" to result.second,
                    "level" to result.first.retention.cacheLevel
                )
            )
        }
    }

    fun handleCacheClaim() {
        val previousState = gameState
        val result = claimArcaneCache(gameState, config, System.currentTimeMillis()) ?: return
        gameState = awardSeasonalAltarFavor(
            result.state,
            amount = config.retention.altar.favorFromCache,
            weekKey = currentAltarWeekKey
        )
        emitUpgradeFeedback(
            message = appContext.getString(
                R.string.feedback_cache_claimed,
                formatNumber(result.rewardEssence, settings.compactNumbers)
            ),
            accentColor = Color(0xFF6FE0FF),
            sound = GameSound.REWARD,
            hapticFeedback = HapticFeedbackConstants.CONTEXT_CLICK
        )
        maybeCelebrateRelicGrowth(previousState, gameState)
        analytics.track(
            name = "arcane_cache_claimed",
            params = mapOf(
                "reward" to result.rewardEssence,
                "shards" to result.shardReward,
                "cache_level" to result.state.retention.cacheLevel
            )
        )
    }

    fun handleReturnStreakClaim() {
        val previousState = gameState
        val result = claimReturnStreak(gameState, config, dailyDayKey) ?: return
        gameState = awardSeasonalAltarFavor(
            result.state,
            amount = config.retention.altar.favorFromReturn,
            weekKey = currentAltarWeekKey
        )
        emitUpgradeFeedback(
            message = appContext.getString(
                R.string.feedback_return_streak_claimed,
                result.streakDay,
                formatNumber(result.rewardEssence, settings.compactNumbers)
            ),
            accentColor = Color(0xFFFFC857),
            sound = GameSound.DAILY,
            hapticFeedback = HapticFeedbackConstants.CONTEXT_CLICK
        )
        if (result.milestone != null) {
            emitUpgradeFeedback(
                message = appContext.getString(
                    R.string.feedback_return_milestone_claimed,
                    result.milestone.title
                ),
                accentColor = Color(0xFFFFD978),
                sound = GameSound.RETURN_MILESTONE,
                hapticFeedback = HapticFeedbackConstants.LONG_PRESS
            )
            if (result.streakDay == 14 || result.streakDay == 30) {
                epicMomentOverlay = EpicMomentOverlayState(
                    title = appContext.getString(R.string.epic_overlay_return_title, result.streakDay),
                    body = result.milestone.title,
                    effect = appContext.getString(
                        R.string.epic_overlay_return_effect,
                        formatNumber(result.rewardEssence, settings.compactNumbers),
                        result.shardReward
                    ),
                    drawableName = "vavarda_return_streak",
                    accentColor = Color(0xFFFFC857)
                )
            }
        }
        maybeCelebrateRelicGrowth(previousState, gameState)
        analytics.track(
            name = "return_streak_claimed",
            params = mapOf(
                "streak_day" to result.streakDay,
                "reward" to result.rewardEssence,
                "shards" to result.shardReward
            )
        )
    }

    fun handleSeasonalAltarClaim(tier: SeasonalAltarTierConfig) {
        val previousState = gameState
        val result = claimSeasonalAltarTier(
            state = gameState,
            tier = tier,
            config = config,
            weekKey = currentAltarWeekKey
        ) ?: return
        gameState = result.state
        emitUpgradeFeedback(
            message = appContext.getString(
                R.string.feedback_altar_claimed,
                result.tier.title
            ),
            accentColor = Color(0xFFFFB85E),
            sound = GameSound.ALTAR,
            hapticFeedback = HapticFeedbackConstants.LONG_PRESS
        )
        if (result.tier.tier >= 3) {
            epicMomentOverlay = EpicMomentOverlayState(
                title = appContext.getString(R.string.epic_overlay_altar_title),
                body = result.tier.title,
                effect = appContext.getString(
                    R.string.epic_overlay_altar_effect,
                    formatNumber(result.tier.rewardEssence, settings.compactNumbers),
                    result.tier.rewardShards
                ),
                drawableName = "vavarda_season_altar",
                accentColor = Color(0xFFFFB85E)
            )
        }
        maybeCelebrateRelicGrowth(previousState, result.state)
        analytics.track(
            name = "seasonal_altar_claimed",
            params = mapOf(
                "tier" to result.tier.tier,
                "favor" to result.state.retention.altarFavor
            )
        )
    }

    fun buildClaimAllReadySummary(): ClaimAllReadySummary? {
        var nextState = gameState
        var claimedCount = 0
        var totalEssence = 0L
        var totalShards = 0
        var returnClaim: ReturnStreakClaimResult? = null
        val claimedAltarTiers = mutableListOf<SeasonalAltarTierConfig>()

        dailyMissions.forEach { mission ->
            if (isMissionCompleted(nextState, mission) && !isMissionClaimed(nextState, mission)) {
                nextState = claimDailyMission(nextState, mission, config)
                nextState = awardSeasonalAltarFavor(
                    nextState,
                    amount = config.retention.altar.favorFromDaily,
                    weekKey = currentAltarWeekKey
                )
                claimedCount += 1
                totalEssence += mission.rewardEssence
                totalShards += 1
            }
        }

        val returnResult = claimReturnStreak(nextState, config, dailyDayKey)
        if (returnResult != null) {
            nextState = awardSeasonalAltarFavor(
                returnResult.state,
                amount = config.retention.altar.favorFromReturn,
                weekKey = currentAltarWeekKey
            )
            claimedCount += 1
            totalEssence += returnResult.rewardEssence
            totalShards += returnResult.shardReward
            returnClaim = returnResult
        }

        val cacheResult = claimArcaneCache(nextState, config, System.currentTimeMillis())
        if (cacheResult != null) {
            nextState = awardSeasonalAltarFavor(
                cacheResult.state,
                amount = config.retention.altar.favorFromCache,
                weekKey = currentAltarWeekKey
            )
            claimedCount += 1
            totalEssence += cacheResult.rewardEssence
            totalShards += cacheResult.shardReward
        }

        config.retention.altar.tiers
            .sortedBy { tier -> tier.tier }
            .forEach { tier ->
                val result = claimSeasonalAltarTier(
                    state = nextState,
                    tier = tier,
                    config = config,
                    weekKey = currentAltarWeekKey
                ) ?: return@forEach
                nextState = result.state
                claimedCount += 1
                totalEssence += result.tier.rewardEssence
                totalShards += result.tier.rewardShards
                claimedAltarTiers += result.tier
            }

        if (claimedCount == 0) return null

        return ClaimAllReadySummary(
            state = nextState,
            claimedCount = claimedCount,
            totalEssence = totalEssence,
            totalShards = totalShards,
            returnClaim = returnClaim,
            claimedAltarTiers = claimedAltarTiers
        )
    }

    fun handleClaimAllReady() {
        val previousState = gameState
        val summary = buildClaimAllReadySummary() ?: return
        gameState = summary.state
        emitUpgradeFeedback(
            message = appContext.getString(
                R.string.feedback_claim_all_ready,
                summary.claimedCount,
                formatNumber(summary.totalEssence, settings.compactNumbers)
            ),
            accentColor = Color(0xFFFFC857),
            sound = GameSound.REWARD,
            hapticFeedback = HapticFeedbackConstants.LONG_PRESS
        )
        maybeCelebrateRelicGrowth(previousState, summary.state)

        val epicAltarTier = summary.claimedAltarTiers
            .filter { tier -> tier.tier >= 3 }
            .maxByOrNull { tier -> tier.tier }
        when {
            summary.returnClaim?.milestone != null &&
                (summary.returnClaim.streakDay == 14 || summary.returnClaim.streakDay == 30) -> {
                epicMomentOverlay = EpicMomentOverlayState(
                    title = appContext.getString(
                        R.string.epic_overlay_return_title,
                        summary.returnClaim.streakDay
                    ),
                    body = summary.returnClaim.milestone.title,
                    effect = appContext.getString(
                        R.string.epic_overlay_return_effect,
                        formatNumber(summary.returnClaim.rewardEssence, settings.compactNumbers),
                        summary.returnClaim.shardReward
                    ),
                    drawableName = "vavarda_return_streak",
                    accentColor = Color(0xFFFFC857)
                )
            }

            epicAltarTier != null -> {
                epicMomentOverlay = EpicMomentOverlayState(
                    title = appContext.getString(R.string.epic_overlay_altar_title),
                    body = epicAltarTier.title,
                    effect = appContext.getString(
                        R.string.epic_overlay_altar_effect,
                        formatNumber(epicAltarTier.rewardEssence, settings.compactNumbers),
                        epicAltarTier.rewardShards
                    ),
                    drawableName = "vavarda_season_altar",
                    accentColor = Color(0xFFFFB85E)
                )
            }
        }

        analytics.track(
            name = "claim_all_ready",
            params = mapOf(
                "count" to summary.claimedCount,
                "essence" to summary.totalEssence,
                "shards" to summary.totalShards
            )
        )
    }

    val backgroundShift by rememberInfiniteTransition(label = "background_shift_transition")
        .animateFloat(
            initialValue = -180f,
            targetValue = 180f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "background_shift"
        )
    val currentArtTitle = localizedArtTitle(currentArt.level, currentArt.title)
    val hasVisibleAlerts = offlineIncomeBanner > 0L || bossRewardBanner > 0L

    fun emitUiActionFeedback(
        soundEnabledOverride: Boolean = settings.soundEnabled,
        vibrationEnabledOverride: Boolean = settings.vibrationEnabled,
        hapticFeedback: Int = HapticFeedbackConstants.CONTEXT_CLICK
    ) {
        emitUiInteractionFeedback(
            view = view,
            soundEnabled = soundEnabledOverride,
            vibrationEnabled = vibrationEnabledOverride,
            hapticFeedback = hapticFeedback
        )
    }

    val openGuideOverlay = {
        emitUiActionFeedback()
        showGuideOverlay = true
        analytics.track(
            name = "guide_shown",
            params = mapOf("source" to "hud_button")
        )
    }
    val openSettingsOverlay = {
        emitUiActionFeedback()
        showSettingsOverlay = true
    }

    CompositionLocalProvider(LocalUiConfig provides uiConfig) {
        val layoutProfile = rememberScreenLayoutMetrics()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0B0715),
                            Color(0xFF17122C),
                            Color(0xFF05040B)
                        ),
                        start = Offset(0f, backgroundShift),
                        end = Offset(1200f, 1600f + backgroundShift)
                    )
                )
        ) {
            VavardaBackdrop(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaledDp(14f))
            ) {
                AnimatedContent(
                    targetState = activeScreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    transitionSpec = {
                        (
                            fadeIn(animationSpec = tween(260)) +
                                slideInHorizontally(
                                    animationSpec = tween(260),
                                    initialOffsetX = { fullWidth -> fullWidth / 8 }
                                )
                            ) togetherWith (
                            fadeOut(animationSpec = tween(220)) +
                                slideOutHorizontally(
                                    animationSpec = tween(220),
                                    targetOffsetX = { fullWidth -> -fullWidth / 10 }
                                )
                            )
                    },
                    label = "screen_content_switch"
                ) { screen ->
                    when (screen) {
                    GameScreen.CORE -> HomeScreenPage(
                        level = level,
                        essence = gameState.essence,
                        rituals = gameState.rituals,
                        path = gameState.path,
                        profileName = settings.profileName,
                        profileImageUri = settings.profileImageUri,
                        tapGain = tapPreview,
                        autoGain = autoPreview,
                        progressToNext = progressToNext,
                        nextLevelEssence = nextLevelEssence,
                        compactNumbers = settings.compactNumbers,
                        textScale = settings.textScale,
                        offlineIncomeBanner = offlineIncomeBanner,
                        bossRewardBanner = bossRewardBanner,
                        playerLevel = level,
                        artTitle = currentArtTitle,
                        artMilestoneLevel = currentArt.level,
                        nextArtUnlockLevel = nextArt?.level,
                        artDrawableName = currentArt.drawableName,
                        clickBursts = clickBursts,
                        flashAlpha = clickFlash.value,
                        showIntro = !gameState.ftue.firstClickDone,
                        intro = narrativePack.intro,
                        showFtueHints = settings.showFtueHints,
                        ftueStep = ftueStep,
                        canUpgradeTap = canUpgradeTap,
                        canUpgradeAuto = canUpgradeAuto,
                        canPerformRitual = canPerformRitual,
                        tapUpgradeCost = tapUpgradeCost,
                        autoUpgradeCost = autoUpgradeCost,
                        nextUpgradeCost = minOf(tapUpgradeCost, autoUpgradeCost),
                        ritualCost = ritualCost,
                        hasVisibleAlerts = hasVisibleAlerts,
                        onDismissOfflineIncome = { offlineIncomeBanner = 0L },
                        onDismissBossReward = { bossRewardBanner = 0L },
                        onOpenGuide = openGuideOverlay,
                        onOpenSettings = openSettingsOverlay,
                        onOpenGrowth = { activeScreen = GameScreen.GROWTH },
                        onTapArt = { handleArtTap() }
                    )

                    GameScreen.GROWTH -> SecondaryScreenPage(
                        stringResource(R.string.nav_growth),
                        null,
                        settings.profileName,
                        settings.profileImageUri,
                        settings.textScale,
                        offlineIncomeBanner,
                        bossRewardBanner,
                        settings.compactNumbers,
                        hasVisibleAlerts,
                        { offlineIncomeBanner = 0L },
                        { bossRewardBanner = 0L },
                        openGuideOverlay,
                        openSettingsOverlay
                    ) {
                        var growthDetailsExpanded by rememberSaveable("growth_details_expanded") {
                            mutableStateOf(false)
                        }

                        ActionFocusCard(
                            title = stringResource(R.string.growth_goal_title),
                            body = localizedGrowthGoalText(
                                recommendedAction = recommendedGrowthAction,
                                canPerformRitual = canPerformRitual,
                                tapUpgradeCost = tapUpgradeCost,
                                autoUpgradeCost = autoUpgradeCost,
                                ritualCost = ritualCost,
                                compactNumbers = settings.compactNumbers
                            ),
                            textScale = settings.textScale,
                            compact = true,
                            maxBodyLines = 1
                        )

                        UpgradeRow(
                            essence = gameState.essence,
                            tapPower = gameState.tapPower,
                            autoPower = gameState.autoPower,
                            rituals = gameState.rituals,
                            tapUpgradeCost = tapUpgradeCost,
                            autoUpgradeCost = autoUpgradeCost,
                            ritualCost = ritualCost,
                            canUpgradeTap = canUpgradeTap,
                            canUpgradeAuto = canUpgradeAuto,
                            canPerformRitual = canPerformRitual,
                            compactNumbers = settings.compactNumbers,
                            textScale = settings.textScale,
                            recommendedAction = recommendedGrowthAction,
                            tapSuccessTick = tapUpgradeSuccessTick,
                            autoSuccessTick = autoUpgradeSuccessTick,
                            ritualSuccessTick = ritualSuccessTick,
                            onTapUpgrade = { handleTapUpgrade() },
                            onAutoUpgrade = { handleAutoUpgrade() },
                            onPerformRitual = { handleRitual() }
                        )

                        Spacer(modifier = Modifier.height(rememberScreenLayoutMetrics().minorSpacing() + 2.dp))

                        ShortTermTargetsRow(
                            essence = gameState.essence,
                            recommendedAction = recommendedGrowthAction,
                            tapUpgradeCost = tapUpgradeCost,
                            autoUpgradeCost = autoUpgradeCost,
                            ritualCost = ritualCost,
                            nextMilestoneLevel = nextMilestoneReward?.level,
                            nextMilestoneRemaining = nextMilestoneRemaining,
                            compactNumbers = settings.compactNumbers,
                            textScale = settings.textScale
                        )

                        SecondaryDetailsPanel(
                            title = stringResource(R.string.growth_systems_title),
                            subtitle = stringResource(R.string.growth_systems_subtitle),
                            expanded = growthDetailsExpanded,
                            onToggle = {
                                emitUiActionFeedback()
                                growthDetailsExpanded = !growthDetailsExpanded
                            },
                            textScale = settings.textScale
                        ) {
                            GrowthScreenContent(
                                tapUpgradeCost = tapUpgradeCost,
                                autoUpgradeCost = autoUpgradeCost,
                            showFtueHints = settings.showFtueHints,
                            ftueStep = ftueStep,
                            ritualCost = ritualCost,
                            level = level,
                            rewards = config.rewards,
                            claimedRewardLevels = gameState.claimedRewardLevels,
                            compactNumbers = settings.compactNumbers,
                            textScale = settings.textScale,
                            retention = gameState.retention,
                            retentionConfig = config.retention,
                            critUpgradeCost = critUpgradeCost,
                                streakUpgradeCost = streakUpgradeCost,
                                offlineBoostUpgradeCost = offlineBoostUpgradeCost,
                                cacheUpgradeCost = cacheUpgradeCost,
                                cacheRewardPreview = cacheRewardPreview,
                                cacheCooldownMinutes = cacheCooldownPreviewMinutes,
                                cacheReadySeconds = cacheReadySeconds,
                                nextRelicRequirement = nextRelicRequirement,
                                canUpgradeCrit = gameState.retention.critLevel < config.retention.crit.maxLevel &&
                                    gameState.essence >= critUpgradeCost,
                                canUpgradeStreak = gameState.retention.streakLevel < config.retention.streak.maxLevel &&
                                    gameState.essence >= streakUpgradeCost,
                                canUpgradeOfflineBoost = gameState.retention.offlineBoostLevel < config.retention.offlineBoost.maxLevel &&
                                    gameState.essence >= offlineBoostUpgradeCost,
                                canUpgradeCache = gameState.retention.cacheLevel < config.retention.cache.maxLevel &&
                                    gameState.essence >= cacheUpgradeCost,
                                critSuccessTick = critUpgradeSuccessTick,
                                streakSuccessTick = streakUpgradeSuccessTick,
                                offlineBoostSuccessTick = offlineUpgradeSuccessTick,
                                cacheSuccessTick = cacheUpgradeSuccessTick,
                                onUpgradeCrit = { handleCritUpgrade() },
                                onUpgradeStreak = { handleStreakUpgrade() },
                                onUpgradeOfflineBoost = { handleOfflineBoostUpgrade() },
                                onUpgradeCache = { handleCacheUpgrade() }
                            )
                        }
                    }

                    GameScreen.EVENTS -> SecondaryScreenPage(
                        stringResource(R.string.nav_events),
                        null,
                        settings.profileName,
                        settings.profileImageUri,
                        settings.textScale,
                        offlineIncomeBanner,
                        bossRewardBanner,
                        settings.compactNumbers,
                        hasVisibleAlerts,
                        { offlineIncomeBanner = 0L },
                        { bossRewardBanner = 0L },
                        openGuideOverlay,
                        openSettingsOverlay
                    ) {
                        EventsScreenContent(
                            boss = gameState.boss,
                            narrativeLine = bossNarrativeMessage,
                            missions = dailyMissions,
                            state = gameState,
                            level = level,
                            retentionConfig = config.retention,
                            cacheReadySeconds = cacheReadySeconds,
                            cacheRewardPreview = cacheRewardPreview,
                            cacheCooldownMinutes = cacheCooldownPreviewMinutes,
                            returnStreakAvailable = returnStreakAvailable,
                            returnStreakPreviewDay = returnStreakPreviewDay,
                            returnStreakRewardPreview = returnStreakRewardPreview,
                            returnMilestonePreview = currentReturnMilestone,
                            nextRelicRequirement = nextRelicRequirement,
                            altarProgress = altarProgress,
                            altarWeekKey = currentAltarWeekKey,
                            altarClaimableTiers = altarClaimableTiers,
                            compactNumbers = settings.compactNumbers,
                            textScale = settings.textScale,
                            bossRewardPreview = bossRewardPreview,
                            bossHitPreview = bossHitPreview,
                            bossHitsToDefeat = bossHitsToDefeat,
                            bossImpactBursts = bossImpactBursts,
                            bossImpactTick = bossImpactTick,
                            bossDefeatTick = bossDefeatTick,
                            onClaimAllReady = { handleClaimAllReady() },
                            onClaimArcaneCache = { handleCacheClaim() },
                            onClaimReturnStreak = { handleReturnStreakClaim() },
                            onClaimSeasonalAltarTier = { tier -> handleSeasonalAltarClaim(tier) },
                            onHitBoss = {
                                val damage = bossHitPreview
                                val previousState = gameState
                                val (afterHit, reward) = hitBoss(gameState, damage, config)
                                var nextState = afterHit
                                val finishingBlow = reward != null
                                if (reward != null) {
                                    nextState = awardSeasonalAltarFavor(
                                        nextState,
                                        amount = config.retention.altar.favorFromBoss,
                                        weekKey = currentAltarWeekKey
                                    )
                                    bossRewardBanner = reward
                                    bossNarrativeMessage = narrativePack.boss.defeated
                                    maybeCelebrateRelicGrowth(previousState, nextState)
                                    analytics.track(name = "boss_defeated", params = mapOf("reward" to reward))
                                } else if (
                                    nextState.boss.isActive &&
                                    nextState.boss.maxHp > 0L &&
                                    nextState.boss.currentHp * 2L <= nextState.boss.maxHp
                                ) {
                                    bossNarrativeMessage = narrativePack.boss.wounded
                                }
                                gameState = nextState
                                emitBossHitFeedback(
                                    damage = damage,
                                    finishingBlow = finishingBlow,
                                    rewardEssence = reward
                                )
                                analytics.track(
                                    name = "boss_hit",
                                    params = mapOf("damage" to damage, "remaining_hp" to nextState.boss.currentHp)
                                )
                            },
                            onClaimMission = { mission ->
                                val beforeEssence = gameState.essence
                                val previousState = gameState
                                val nextState = claimDailyMission(gameState, mission, config)
                                if (nextState != gameState) {
                                    gameState = awardSeasonalAltarFavor(
                                        nextState,
                                        amount = config.retention.altar.favorFromDaily,
                                        weekKey = currentAltarWeekKey
                                    )
                                    emitUpgradeFeedback(
                                        message = appContext.getString(R.string.feedback_daily_claimed),
                                        accentColor = Color(0xFF7DE3D0),
                                        sound = GameSound.DAILY,
                                        hapticFeedback = HapticFeedbackConstants.CONTEXT_CLICK
                                    )
                                    maybeCelebrateRelicGrowth(previousState, gameState)
                                    analytics.track(
                                        name = "daily_mission_claimed",
                                        params = mapOf(
                                            "slot" to mission.slot,
                                            "reward" to (nextState.essence - beforeEssence)
                                        )
                                    )
                                }
                            }
                        )
                    }

                    GameScreen.PATH -> SecondaryScreenPage(
                        stringResource(R.string.nav_path),
                        null,
                        settings.profileName,
                        settings.profileImageUri,
                        settings.textScale,
                        offlineIncomeBanner,
                        bossRewardBanner,
                        settings.compactNumbers,
                        hasVisibleAlerts,
                        { offlineIncomeBanner = 0L },
                        { bossRewardBanner = 0L },
                        openGuideOverlay,
                        openSettingsOverlay
                    ) {
                        PathScreenContent(
                            level = level,
                            path = gameState.path,
                            bonuses = config.factions,
                            textScale = settings.textScale,
                            onPathChange = { next ->
                                if (gameState.path != next) {
                                    gameState = gameState.copy(path = next)
                                    emitUpgradeFeedback(
                                        message = appContext.getString(
                                            R.string.feedback_path_selected,
                                            appContext.getString(factionNameRes(next))
                                        ),
                                        accentColor = factionAccentColor(next),
                                        sound = GameSound.ALTAR,
                                        hapticFeedback = HapticFeedbackConstants.LONG_PRESS
                                    )
                                    analytics.track(name = "faction_changed", params = mapOf("path" to next.name))
                                }
                            }
                        )
                    }
                    }
                }

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))

                BottomScreenBar(
                    activeScreen = activeScreen,
                    eventsReadyCount = eventsReadyCount,
                    textScale = settings.textScale,
                    onScreenChange = {
                        if (it != activeScreen) {
                            emitUiActionFeedback()
                            activeScreen = it
                        }
                    }
                )
            }

            upgradeCelebrations.forEachIndexed { index, celebration ->
                FloatingUpgradeCelebration(
                    celebration = celebration,
                    index = index,
                    textScale = settings.textScale
                )
            }

            if (activeReward != null) {
                val rewardClaimMessage = stringResource(R.string.feedback_reward_claimed)
                RewardOverlay(
                    reward = activeReward,
                    narrative = narrativeForLevel(narrativePack, activeReward.level),
                    textScale = settings.textScale,
                    onClaim = {
                        var nextState = applyReward(gameState, activeReward)
                        if (activeReward.kind == RewardKind.ESSENCE) {
                            nextState = addEssenceProgress(nextState, activeReward.amount.toLong())
                        }
                        gameState = nextState
                        activeRewardLevel = null
                        emitUpgradeFeedback(
                            message = rewardClaimMessage,
                            accentColor = Color(0xFFFFC857),
                            sound = GameSound.REWARD,
                            hapticFeedback = HapticFeedbackConstants.LONG_PRESS
                        )
                        analytics.track(
                            name = "milestone_reward_claimed",
                            params = mapOf(
                                "level" to activeReward.level,
                                "kind" to activeReward.kind.name,
                                "amount" to activeReward.amount
                            )
                        )
                    }
                )
            }

            if (epicMomentOverlay != null) {
                EpicMomentOverlay(
                    state = epicMomentOverlay!!,
                    textScale = settings.textScale,
                    onDismiss = { epicMomentOverlay = null }
                )
            }

            if (showSettingsOverlay) {
                SettingsOverlay(
                    settings = settings,
                    onDismiss = {
                        emitUiActionFeedback()
                        showSettingsOverlay = false
                    },
                    onSettingsChange = { nextSettings -> settings = nextSettings },
                    onUiFeedback = { soundEnabledOverride, vibrationEnabledOverride, hapticFeedback ->
                        emitUiActionFeedback(
                            soundEnabledOverride = soundEnabledOverride,
                            vibrationEnabledOverride = vibrationEnabledOverride,
                            hapticFeedback = hapticFeedback
                        )
                    }
                )
            }

            if (showGuideOverlay) {
                GuideOverlay(
                    textScale = settings.textScale,
                    onDismiss = {
                        emitUiActionFeedback()
                        val firstLaunch = !settings.guideCompleted
                        showGuideOverlay = false
                        if (firstLaunch) {
                            settings = settings.copy(guideCompleted = true)
                        }
                        analytics.track(
                            name = "guide_closed",
                            params = mapOf("first_launch" to firstLaunch)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreenPage(
    playerLevel: Int,
    level: Int,
    essence: Long,
    rituals: Int,
    path: AlignmentPath,
    profileName: String,
    profileImageUri: String,
    tapGain: Long,
    autoGain: Long,
    progressToNext: Float,
    nextLevelEssence: Long,
    compactNumbers: Boolean,
    textScale: Float,
    offlineIncomeBanner: Long,
    bossRewardBanner: Long,
    artTitle: String,
    artMilestoneLevel: Int,
    nextArtUnlockLevel: Int?,
    artDrawableName: String,
    clickBursts: List<ClickBurst>,
    flashAlpha: Float,
    showIntro: Boolean,
    intro: NarrativeEntry,
    showFtueHints: Boolean,
    ftueStep: FtueStep,
    canUpgradeTap: Boolean,
    canUpgradeAuto: Boolean,
    canPerformRitual: Boolean,
    tapUpgradeCost: Long,
    autoUpgradeCost: Long,
    nextUpgradeCost: Long,
    ritualCost: Long,
    hasVisibleAlerts: Boolean,
    onDismissOfflineIncome: () -> Unit,
    onDismissBossReward: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGrowth: () -> Unit,
    onTapArt: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val centeredContentModifier = Modifier
        .fillMaxWidth()
        .widthIn(max = layoutProfile.contentMaxWidth())
    val onboardingActive = showIntro || (showFtueHints && ftueStep == FtueStep.FIRST_CLICK)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(layoutProfile.screenOverlayPadding()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = centeredContentModifier) {
            HomeTopBar(
                level = level,
                path = path,
                profileName = profileName,
                profileImageUri = profileImageUri,
                textScale = textScale,
                onOpenGuide = onOpenGuide,
                onOpenSettings = onOpenSettings
            )

            if (hasVisibleAlerts) {
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
            }

            GameAlerts(
                offlineIncomeBanner = offlineIncomeBanner,
                bossRewardBanner = bossRewardBanner,
                compactNumbers = compactNumbers,
                onDismissOfflineIncome = onDismissOfflineIncome,
                onDismissBossReward = onDismissBossReward
            )

            Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))

            if (onboardingActive) {
                CoreScreenContent(
                    showIntro = showIntro,
                    intro = intro,
                    showFtueHints = showFtueHints,
                    ftueStep = ftueStep,
                    nextUpgradeCost = nextUpgradeCost,
                    ritualCost = ritualCost,
                    compactNumbers = compactNumbers,
                    textScale = textScale
                )

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
            } else {
                HomeStatusCard(
                    goalTitle = stringResource(R.string.goal_title_short),
                    goalBody = localizedHomeGoalShortText(
                        ftueStep = ftueStep,
                        canUpgradeTap = canUpgradeTap,
                        canUpgradeAuto = canUpgradeAuto,
                        canPerformRitual = canPerformRitual,
                        tapUpgradeCost = tapUpgradeCost,
                        autoUpgradeCost = autoUpgradeCost,
                        ritualCost = ritualCost,
                        nextArtUnlockLevel = nextArtUnlockLevel,
                        compactNumbers = compactNumbers
                    ),
                    actionLabel = stringResource(R.string.goal_action_growth_short),
                    essence = essence,
                    rituals = rituals,
                    nextLevelEssence = nextLevelEssence,
                    compactNumbers = compactNumbers,
                    textScale = textScale,
                    onAction = onOpenGrowth
                )

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
            }

            if (onboardingActive) {
                HomeProgressCard(
                    level = level,
                    essence = essence,
                    rituals = rituals,
                    tapGain = tapGain,
                    autoGain = autoGain,
                    progressToNext = progressToNext,
                    nextLevelEssence = nextLevelEssence,
                    compactNumbers = compactNumbers,
                    textScale = textScale
                )

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
            }

            ArtCard(
                playerLevel = playerLevel,
                title = artTitle,
                artMilestoneLevel = artMilestoneLevel,
                nextArtUnlockLevel = nextArtUnlockLevel,
                drawableName = artDrawableName,
                clickBursts = clickBursts,
                flashAlpha = flashAlpha,
                showTapPrompt = showFtueHints && ftueStep == FtueStep.FIRST_CLICK,
                showBottomCaption = true,
                compactNumbers = compactNumbers,
                textScale = textScale,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = if (onboardingActive) 290.dp else 320.dp,
                        max = if (onboardingActive) 390.dp else 430.dp
                    ),
                onTap = onTapArt
            )

            if (!onboardingActive) {
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))

                HomeProgressCard(
                    level = level,
                    essence = essence,
                    rituals = rituals,
                    tapGain = tapGain,
                    autoGain = autoGain,
                    progressToNext = progressToNext,
                    nextLevelEssence = nextLevelEssence,
                    compactNumbers = compactNumbers,
                    textScale = textScale
                )
            }
        }
    }
}

@Composable
private fun SecondaryScreenPage(
    title: String,
    subtitle: String?,
    profileName: String,
    profileImageUri: String,
    textScale: Float,
    offlineIncomeBanner: Long,
    bossRewardBanner: Long,
    compactNumbers: Boolean,
    hasVisibleAlerts: Boolean,
    onDismissOfflineIncome: () -> Unit,
    onDismissBossReward: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val headerBottomSpacing = (layoutProfile.minorSpacing() - 1.dp).coerceAtLeast(6.dp)
    val contentTopSpacing = if (hasVisibleAlerts) {
        layoutProfile.minorSpacing()
    } else {
        (layoutProfile.minorSpacing() + 1.dp).coerceAtLeast(7.dp)
    }
    val centeredContentModifier = Modifier
        .fillMaxWidth()
        .widthIn(max = layoutProfile.contentMaxWidth())

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = centeredContentModifier) {
            ScreenHeaderCard(
                title = title,
                subtitle = subtitle,
                profileName = profileName,
                profileImageUri = profileImageUri,
                textScale = textScale,
                onOpenGuide = onOpenGuide,
                onOpenSettings = onOpenSettings
            )
        }

        if (hasVisibleAlerts) {
            Spacer(modifier = Modifier.height(headerBottomSpacing))

            Box(modifier = centeredContentModifier) {
                GameAlerts(
                    offlineIncomeBanner = offlineIncomeBanner,
                    bossRewardBanner = bossRewardBanner,
                    compactNumbers = compactNumbers,
                    onDismissOfflineIncome = onDismissOfflineIncome,
                    onDismissBossReward = onDismissBossReward,
                    compact = true,
                    textScale = textScale
                )
            }
        }

        Spacer(modifier = Modifier.height(contentTopSpacing))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = layoutProfile.contentMaxWidth())
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(layoutProfile.sectionSpacing()),
            content = content
        )
    }
}

@Composable
private fun SectionLabel(
    title: String,
    subtitle: String? = null,
    textScale: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFFF4E8CD),
            fontWeight = FontWeight.Bold,
            fontSize = scaledSp(15f, textScale),
            fontFamily = MaterialTheme.typography.titleMedium.fontFamily
        )
        subtitle?.takeIf { it.isNotBlank() }?.let { resolvedSubtitle ->
            Text(
                text = resolvedSubtitle,
                color = Color(0xFF97A3B5),
                fontSize = scaledSp(10.8f, textScale),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpandableLoreText(
    key: String,
    text: String,
    textScale: Float,
    color: Color,
    collapsedMaxLines: Int,
    modifier: Modifier = Modifier,
    toggleColor: Color = Color(0xFFD9C3FF),
    fontSizeSp: Float = 12f,
    textAlign: TextAlign = TextAlign.Start
) {
    var expanded by rememberSaveable(key) { mutableStateOf(false) }
    var canExpand by remember(text) { mutableStateOf(false) }
    val layoutProfile = rememberScreenLayoutMetrics()

    Column(modifier = modifier) {
        Text(
            text = text,
            color = color,
            fontSize = scaledSp(fontSizeSp, textScale),
            modifier = Modifier.fillMaxWidth(),
            textAlign = textAlign,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!expanded && textLayoutResult.hasVisualOverflow) {
                    canExpand = true
                }
            }
        )
        if (canExpand || expanded) {
            Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
            Text(
                text = stringResource(
                    if (expanded) R.string.action_hide else R.string.action_expand
                ),
                color = toggleColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = scaledSp(11f, textScale),
                textAlign = textAlign,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            )
        }
    }
}

@Composable
private fun ActionFocusCard(
    title: String,
    body: String,
    textScale: Float,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    compact: Boolean = false,
    maxBodyLines: Int = Int.MAX_VALUE
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val cardPadding = if (compact) layoutProfile.cardPadding() else layoutProfile.cardPadding() + 2.dp
    val inlineActionPadding = PaddingValues(
        horizontal = layoutProfile.cardPadding() + 2.dp,
        vertical = layoutProfile.bottomBarLabelSpacing() + 4.dp
    )
    val blockActionPadding = PaddingValues(
        horizontal = layoutProfile.cardPadding() + 2.dp,
        vertical = layoutProfile.cardPadding() - 2.dp
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (compact) Color(0xD4121821) else Color(0xD9171C24)
        ),
        border = if (compact) BorderStroke(1.dp, Color(0x4FD7BC84)) else BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = if (compact) Color(0xFFF0DCAA) else Color(0xFFFFE0A3),
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(if (compact) 11f else 12f, textScale)
                )
                if (compact && actionLabel != null && onAction != null) {
                    ClaimActionButton(
                        text = actionLabel,
                        onClick = onAction,
                        enabled = true,
                        containerColor = Color(0xFF7C5A34),
                        textScale = textScale,
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = inlineActionPadding,
                        animateAttention = false,
                        showAccentIcon = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + if (compact) 2.dp else 3.dp))
            Text(
                text = body,
                color = Color(0xFFF2F4F8),
                fontSize = scaledSp(if (compact) 12f else 13f, textScale),
                maxLines = maxBodyLines,
                overflow = if (maxBodyLines == Int.MAX_VALUE) {
                    TextOverflow.Clip
                } else {
                    TextOverflow.Ellipsis
                }
            )
            if (!compact && actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                ClaimActionButton(
                    text = actionLabel,
                    onClick = onAction,
                    enabled = true,
                    containerColor = Color(0xFF84633B),
                    textScale = textScale,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = blockActionPadding,
                    fontSizeSp = 12f,
                    animateAttention = true,
                    showAccentIcon = true
                )
            }
        }
    }
}

@Composable
private fun SecondaryDetailsPanel(
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    onToggle: () -> Unit,
    textScale: Float,
    content: @Composable ColumnScope.() -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val cardPadding = layoutProfile.cardPadding() - 2.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xD4121821)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color(0xFFFFE0A3),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(12f, textScale)
                    )
                    subtitle?.takeIf { it.isNotBlank() }?.let { resolvedSubtitle ->
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                        Text(
                            text = resolvedSubtitle,
                            color = Color(0xFFD0D7E4),
                            fontSize = scaledSp(10.5f, textScale),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(layoutProfile.chipSpacing()))
                ClaimActionButton(
                    text = if (expanded) {
                        stringResource(R.string.action_hide)
                    } else {
                        stringResource(R.string.action_details)
                    },
                    onClick = onToggle,
                    enabled = true,
                    containerColor = Color(0xFF2D3643),
                    contentColor = Color(0xFFFFEEC8),
                    textScale = textScale,
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                    animateAttention = false,
                    showAccentIcon = false
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeRewardHubCard(
    retention: RetentionProgress,
    config: RetentionConfig,
    claimableMissionCount: Int,
    eventsReadyCount: Int,
    cacheReadySeconds: Int,
    returnStreakAvailable: Boolean,
    returnStreakPreviewDay: Int,
    altarClaimableTiers: List<SeasonalAltarTierConfig>,
    textScale: Float,
    onOpenEvents: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val cardPadding = layoutProfile.cardPadding() - 2.dp
    val altarMaxFavor = remember(config.altar.tiers) {
        config.altar.tiers.maxOfOrNull { it.requiredFavor } ?: 0
    }
    val rewardHubBody = localizedHomeRewardsBody(
        eventsReadyCount = eventsReadyCount,
        cacheReadySeconds = cacheReadySeconds,
        altarFavor = retention.altarFavor,
        altarMaxFavor = altarMaxFavor
    )
    val readySummaryChips = buildList {
        if (claimableMissionCount > 0) {
            add(
                Triple(
                    stringResource(R.string.home_rewards_chip_daily, claimableMissionCount),
                    Color(0xFF0E685D),
                    Color(0xFFE7FFF6)
                )
            )
        }
        if (returnStreakAvailable) {
            add(
                Triple(
                    stringResource(R.string.home_rewards_chip_return_ready, returnStreakPreviewDay),
                    Color(0xFF4D2F75),
                    Color(0xFFF1E4FF)
                )
            )
        }
        if (cacheReadySeconds <= 0) {
            add(
                Triple(
                    stringResource(R.string.home_rewards_chip_cache_ready),
                    Color(0xFF6B4E18),
                    Color(0xFFFFE8AF)
                )
            )
        }
        if (altarClaimableTiers.isNotEmpty()) {
            add(
                Triple(
                    stringResource(R.string.home_rewards_chip_altar_ready, altarClaimableTiers.size),
                    Color(0xFF214974),
                    Color(0xFFE1F1FF)
                )
            )
        }
    }
    val visibleChips = remember(readySummaryChips) {
        val leading = readySummaryChips.take(2).toMutableList()
        val hiddenCount = readySummaryChips.size - leading.size
        if (hiddenCount > 0) {
            leading += Triple(
                "+$hiddenCount",
                Color(0xFF2A1F3F),
                Color(0xFFE7D8FF)
            )
        }
        leading.toList()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xD9161127)),
        border = BorderStroke(
            1.dp,
            if (eventsReadyCount > 0) Color(0x55FFC857) else Color(0x339D83E8)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xE51E1531),
                            Color(0xE3141022)
                        )
                    )
                )
                .padding(horizontal = cardPadding, vertical = cardPadding - 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_rewards_ready_summary, eventsReadyCount),
                        color = Color(0xFFF6E6C2),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(11.5f, textScale)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rewardHubBody,
                        color = Color.White,
                        fontSize = scaledSp(11.5f, textScale),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(layoutProfile.chipSpacing() + 2.dp))

                ClaimActionButton(
                    text = stringResource(R.string.nav_events_short),
                    onClick = onOpenEvents,
                    enabled = true,
                    containerColor = Color(0xFFFFC857),
                    contentColor = Color(0xFF2A173D),
                    textScale = textScale,
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    animateAttention = false,
                    showAccentIcon = false
                )
            }

            if (visibleChips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    visibleChips.forEach { (text, containerColor, contentColor) ->
                        InfoChip(
                            text = text,
                            containerColor = containerColor,
                            contentColor = contentColor,
                            textScale = textScale
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun HeaderProfileButton(
    profileName: String,
    profileImageUri: String,
    compact: Boolean,
    textScale: Float,
    onClick: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val defaultProfileName = stringResource(R.string.profile_name_default)
    val displayName = remember(profileName, defaultProfileName) {
        resolvedProfileName(profileName, defaultProfileName)
    }
    val buttonHeight = if (compact) {
        layoutProfile.headerButtonSize() - 10.dp
    } else {
        layoutProfile.headerButtonSize() - 6.dp
    }
    val avatarSize = if (compact) 22.dp else 24.dp
    val avatarFontSize = if (compact) 10f else 10.5f
    val horizontalPadding = if (compact) 5.dp else 7.dp
    val profileContentDescription = stringResource(
        R.string.profile_header_content_description,
        displayName
    )

    Card(
        modifier = Modifier
            .height(buttonHeight)
            .defaultMinSize(minWidth = if (compact) buttonHeight else 124.dp)
            .testTag("header_profile_button")
            .semantics(mergeDescendants = true) {
                contentDescription = profileContentDescription
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF262F3D)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .height(buttonHeight)
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 0.dp else 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                imageUri = profileImageUri,
                displayName = displayName,
                textScale = textScale,
                avatarSize = avatarSize,
                initialFontSizeSp = avatarFontSize
            )

            if (!compact) {
                Text(
                    text = displayName,
                    color = Color(0xFFF7F0E2),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = scaledSp(10.5f, textScale),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 88.dp)
                )
            }
        }
    }
}

@Composable
internal fun InfoChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    textScale: Float
) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            color = contentColor,
            fontSize = scaledSp(11f, textScale),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ClaimActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    textScale: Float,
    modifier: Modifier = Modifier,
    disabledContainerColor: Color = containerColor.copy(alpha = 0.4f),
    contentColor: Color = Color.White,
    disabledContentColor: Color = contentColor.copy(alpha = 0.72f),
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    fontSizeSp: Float = 11f,
    animateAttention: Boolean = enabled,
    showAccentIcon: Boolean = animateAttention,
    style: ActionButtonStyle = ActionButtonStyle.CLAIM
) {
    val pulseTarget = when (style) {
        ActionButtonStyle.CLAIM -> 1.04f
        ActionButtonStyle.PURCHASE -> 1.025f
        ActionButtonStyle.HIT -> 1.08f
        ActionButtonStyle.PATH -> 1.018f
        ActionButtonStyle.NEUTRAL -> 1f
    }
    val pulseDuration = when (style) {
        ActionButtonStyle.CLAIM -> 900
        ActionButtonStyle.PURCHASE -> 1100
        ActionButtonStyle.HIT -> 360
        ActionButtonStyle.PATH -> 2200
        ActionButtonStyle.NEUTRAL -> 900
    }
    val glowStart = when (style) {
        ActionButtonStyle.CLAIM -> 0.12f
        ActionButtonStyle.PURCHASE -> 0.08f
        ActionButtonStyle.HIT -> 0.18f
        ActionButtonStyle.PATH -> 0.06f
        ActionButtonStyle.NEUTRAL -> 0f
    }
    val glowEnd = when (style) {
        ActionButtonStyle.CLAIM -> 0.26f
        ActionButtonStyle.PURCHASE -> 0.16f
        ActionButtonStyle.HIT -> 0.34f
        ActionButtonStyle.PATH -> 0.14f
        ActionButtonStyle.NEUTRAL -> 0f
    }
    val sheenDuration = when (style) {
        ActionButtonStyle.CLAIM -> 1700
        ActionButtonStyle.PURCHASE -> 2200
        ActionButtonStyle.HIT -> 950
        ActionButtonStyle.PATH -> 2600
        ActionButtonStyle.NEUTRAL -> 1700
    }
    val icon = when (style) {
        ActionButtonStyle.CLAIM -> Icons.Default.CheckCircle
        ActionButtonStyle.PURCHASE -> Icons.Rounded.BarChart
        ActionButtonStyle.HIT -> Icons.Default.Bolt
        ActionButtonStyle.PATH -> Icons.Rounded.Map
        ActionButtonStyle.NEUTRAL -> Icons.Default.AutoAwesome
    }
    val showMotion = enabled && animateAttention && style != ActionButtonStyle.NEUTRAL
    val showSheen = showMotion && style != ActionButtonStyle.PATH

    val pulseScale by rememberInfiniteTransition(label = "claim_action_button_pulse")
        .animateFloat(
            initialValue = 1f,
            targetValue = pulseTarget,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "claim_action_button_pulse_value"
        )
    val glowAlpha by rememberInfiniteTransition(label = "claim_action_button_glow")
        .animateFloat(
            initialValue = glowStart,
            targetValue = glowEnd,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = pulseDuration + 80, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "claim_action_button_glow_value"
        )
    val sheenProgress by rememberInfiniteTransition(label = "claim_action_button_sheen")
        .animateFloat(
            initialValue = -0.8f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = sheenDuration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "claim_action_button_sheen_value"
        )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (showMotion) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(alpha = 0.98f)
            ) {
                drawCircle(
                    color = containerColor.copy(alpha = glowAlpha),
                    radius = size.maxDimension * if (style == ActionButtonStyle.HIT) 0.68f else 0.62f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            }
        }

        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.graphicsLayer(
                scaleX = if (showMotion) pulseScale else 1f,
                scaleY = if (showMotion) pulseScale else 1f
            ),
            shape = shape,
            border = BorderStroke(
                1.dp,
                if (enabled) Color.White.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.08f)
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = disabledContainerColor,
                disabledContentColor = disabledContentColor
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier.padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                if (showSheen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.16f),
                                        Color.Transparent
                                    ),
                                    start = Offset(120f * sheenProgress, 0f),
                                    end = Offset(120f * sheenProgress + 90f, 140f)
                                )
                            )
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (enabled && showAccentIcon) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.92f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = text,
                        fontSize = scaledSp(fontSizeSp, textScale),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidanceActionPill(
    text: String,
    textScale: Float,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF7A49D1),
    contentColor: Color = Color.White,
    emphasized: Boolean = true
) {
    val pulseScale by rememberInfiniteTransition(label = "guidance_action_pill_pulse")
        .animateFloat(
            initialValue = 1f,
            targetValue = 1.025f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "guidance_action_pill_pulse_value"
        )
    val sheenProgress by rememberInfiniteTransition(label = "guidance_action_pill_sheen")
        .animateFloat(
            initialValue = -0.8f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "guidance_action_pill_sheen_value"
        )

    Card(
        modifier = modifier.graphicsLayer(
            scaleX = if (emphasized) pulseScale else 1f,
            scaleY = if (emphasized) pulseScale else 1f
        ),
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.92f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (emphasized) 0.22f else 0.12f))
    ) {
        Box {
            if (emphasized) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.14f),
                                    Color.Transparent
                                ),
                                start = Offset(120f * sheenProgress, 0f),
                                end = Offset(120f * sheenProgress + 90f, 140f)
                            )
                        )
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.92f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = text,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(11f, textScale)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeProgressCard(
    level: Int,
    essence: Long,
    rituals: Int,
    tapGain: Long,
    autoGain: Long,
    progressToNext: Float,
    nextLevelEssence: Long,
    compactNumbers: Boolean,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val remainingToNextLevel = (nextLevelEssence - essence).coerceAtLeast(0L)
    val animatedProgress by animateFloatAsState(
        targetValue = progressToNext.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 280),
        label = "home_progress_value"
    )
    val progressColor by animateColorAsState(
        targetValue = when {
            progressToNext >= 0.9f -> Color(0xFFFFD36B)
            progressToNext >= 0.55f -> Color(0xFFF3C26C)
            else -> Color(0xFFB78DFF)
        },
        animationSpec = tween(durationMillis = 260),
        label = "home_progress_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC111720)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xE3141B27),
                        Color(0xD6111721),
                        Color(0xD40C1017)
                    )
                )
            )
        ) {
            Column(modifier = Modifier.padding(layoutProfile.cardPadding())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.hud_card_essence),
                            color = Color(0xFFAEB8C6),
                            fontSize = scaledSp(10.5f, textScale)
                        )
                        Text(
                            text = formatNumber(essence, compactNumbers),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = scaledSp(29f, textScale)
                        )
                    }
                    InfoChip(
                        text = "${stringResource(R.string.label_level_short, level)} • ${stringResource(R.string.label_rituals_short, rituals)}",
                        containerColor = Color(0xFF18202C),
                        contentColor = Color(0xFFF4E9D1),
                        textScale = textScale
                    )
                }
                Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = progressColor,
                    trackColor = Color(0xFF2A3240)
                )
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                Text(
                    text = stringResource(
                        R.string.label_next_level_short,
                        formatNumber(remainingToNextLevel, compactNumbers)
                    ),
                    color = Color(0xFFC9D3DF),
                    fontSize = scaledSp(10.8f, textScale)
                )
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    StatPill(
                        title = stringResource(R.string.label_tap_rate),
                        value = "+${formatNumber(tapGain, compactNumbers)}"
                    )
                    StatPill(
                        title = stringResource(R.string.label_auto_rate),
                        value = "${formatNumber(autoGain, compactNumbers)}/s"
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeStatusCard(
    goalTitle: String,
    goalBody: String,
    actionLabel: String,
    essence: Long,
    rituals: Int,
    nextLevelEssence: Long,
    compactNumbers: Boolean,
    textScale: Float,
    onAction: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val cardPadding = layoutProfile.cardPadding() - 2.dp
    val sectionSpacing = layoutProfile.sectionSpacing() - 2.dp
    val remainingToNextLevel = (nextLevelEssence - essence).coerceAtLeast(0L)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0x4FD7BC84))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xE61A1D28),
                        Color(0xE0131720),
                        Color(0xDE0D1017)
                    )
                )
            )
        ) {
            Column(modifier = Modifier.padding(cardPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goalTitle,
                            color = Color(0xFFE7CF96),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = scaledSp(10f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() - 1.dp))
                        Text(
                            text = goalBody,
                            color = Color(0xFFF3F6FA),
                            fontSize = scaledSp(11.6f, textScale),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(layoutProfile.chipSpacing() + 2.dp))
                    ClaimActionButton(
                        text = actionLabel,
                        onClick = onAction,
                        enabled = true,
                        containerColor = Color(0xFF7C5A34),
                        textScale = textScale,
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(
                            horizontal = cardPadding + 1.dp,
                            vertical = layoutProfile.bottomBarLabelSpacing() + 1.dp
                        ),
                        animateAttention = false,
                        showAccentIcon = false
                    )
                }

                Spacer(modifier = Modifier.height(sectionSpacing))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.label_next_level_short,
                            formatNumber(remainingToNextLevel, compactNumbers)
                        ),
                        color = Color(0xFFC5CFDA),
                        fontSize = scaledSp(10.3f, textScale)
                    )
                    Text(
                        text = stringResource(R.string.label_rituals_short, rituals),
                        color = Color(0xFFE1CA93),
                        fontSize = scaledSp(10.2f, textScale),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShortTermTargetsRow(
    essence: Long,
    recommendedAction: GrowthActionKind,
    tapUpgradeCost: Long,
    autoUpgradeCost: Long,
    ritualCost: Long,
    nextMilestoneLevel: Int?,
    nextMilestoneRemaining: Long?,
    compactNumbers: Boolean,
    textScale: Float,
    modifier: Modifier = Modifier
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val upgradeCost = when (recommendedAction) {
        GrowthActionKind.TAP -> tapUpgradeCost
        GrowthActionKind.AUTO -> autoUpgradeCost
        GrowthActionKind.RITUAL -> minOf(tapUpgradeCost, autoUpgradeCost)
    }
    val thresholdItems = buildList {
        add(
            GoalThresholdSpec(
                value = if (upgradeCost <= essence) {
                    stringResource(R.string.state_ready_short)
                } else {
                    formatNumber((upgradeCost - essence).coerceAtLeast(0L), compactNumbers)
                },
                label = stringResource(R.string.goal_threshold_upgrade),
                accentColor = Color(0xFF9C6BFF),
                ready = upgradeCost <= essence
            )
        )
        add(
            GoalThresholdSpec(
                value = if (ritualCost <= essence) {
                    stringResource(R.string.state_ready_short)
                } else {
                    formatNumber((ritualCost - essence).coerceAtLeast(0L), compactNumbers)
                },
                label = stringResource(R.string.goal_threshold_ritual),
                accentColor = Color(0xFFFF8A8A),
                ready = ritualCost <= essence
            )
        )
        if (nextMilestoneLevel != null && nextMilestoneRemaining != null) {
            add(
                GoalThresholdSpec(
                    value = if (nextMilestoneRemaining <= 0L) {
                        stringResource(R.string.state_ready_short)
                    } else {
                        formatNumber(nextMilestoneRemaining, compactNumbers)
                    },
                    label = stringResource(R.string.goal_threshold_milestone, nextMilestoneLevel),
                    accentColor = Color(0xFF5BCBF0),
                    ready = nextMilestoneRemaining <= 0L
                )
            )
        }
    }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
        verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
    ) {
        thresholdItems.forEach { item ->
            ShortTermTargetChip(
                spec = item,
                textScale = textScale
            )
        }
    }
}

@Composable
private fun ShortTermTargetChip(
    spec: GoalThresholdSpec,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    Card(
        modifier = Modifier.widthIn(min = scaledDp(92f)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = spec.accentColor.copy(alpha = if (spec.ready) 0.18f else 0.12f)
        ),
        border = BorderStroke(
            1.dp,
            spec.accentColor.copy(alpha = if (spec.ready) 0.5f else 0.24f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = layoutProfile.cardPadding() - 2.dp,
                vertical = layoutProfile.bottomBarLabelSpacing() + 6.dp
            )
        ) {
            Text(
                text = spec.value,
                color = if (spec.ready) Color.White else spec.accentColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = scaledSp(12.5f, textScale)
            )
            Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
            Text(
                text = spec.label,
                color = Color(0xFFE9DDFF),
                fontSize = scaledSp(9.5f, textScale),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ScreenStatsCard(
    level: Int,
    essence: Long,
    rituals: Int,
    path: AlignmentPath,
    tapGain: Long,
    autoGain: Long,
    progressToNext: Float,
    nextLevelEssence: Long,
    compactNumbers: Boolean,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val horizontalPadding = layoutProfile.cardPadding() - 3.dp
    val remainingToNextLevel = (nextLevelEssence - essence).coerceAtLeast(0L)
    val progressColor by animateColorAsState(
        targetValue = when {
            progressToNext >= 0.9f -> Color(0xFFFFD36B)
            progressToNext >= 0.55f -> Color(0xFFF3C26C)
            else -> Color(0xFFB78DFF)
        },
        animationSpec = tween(durationMillis = 260),
        label = "screen_stats_progress_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171126)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing().coerceAtLeast(6.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactStatusBadge(
                    label = stringResource(R.string.label_essence),
                    value = formatNumber(essence, compactNumbers),
                    textScale = textScale,
                    emphasized = true
                )
                CompactStatusBadge(
                    label = stringResource(R.string.label_level_short, level),
                    textScale = textScale
                )
                CompactStatusBadge(
                    label = localizedFactionName(path),
                    textScale = textScale,
                    containerColor = Color(0xFF2A1A41),
                    labelColor = Color(0xFFF4E9FF)
                )
                CompactStatusBadge(
                    label = stringResource(R.string.label_rituals_short, rituals),
                    textScale = textScale
                )
                CompactStatusBadge(
                    label = stringResource(R.string.label_tap_rate),
                    value = "+${formatNumber(tapGain, compactNumbers)}",
                    textScale = textScale
                )
                CompactStatusBadge(
                    label = stringResource(R.string.label_auto_rate),
                    value = "${formatNumber(autoGain, compactNumbers)}/s",
                    textScale = textScale
                )
                CompactStatusBadge(
                    label = stringResource(
                        R.string.label_next_level_micro,
                        formatNumber(remainingToNextLevel, compactNumbers)
                    ),
                    textScale = textScale
                )
            }

            LinearProgressIndicator(
                progress = { progressToNext.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(2.dp)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                color = progressColor,
                trackColor = Color(0xFF2A1D41)
            )
        }
    }
}

@Composable
private fun CompactStatusBadge(
    label: String,
    textScale: Float,
    value: String? = null,
    emphasized: Boolean = false,
    containerColor: Color = Color(0xFF241735),
    labelColor: Color = Color(0xFFBCA9E9),
    valueColor: Color = Color.White
) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) Color(0xFF2D1A46) else containerColor
        ),
        border = if (emphasized) BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (emphasized) Color(0xFFE1CFFF) else labelColor,
                fontSize = scaledSp(9.5f, textScale),
                fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (value != null) {
                Text(
                    text = value,
                    color = valueColor,
                    fontSize = scaledSp(10f, textScale),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GameAlerts(
    offlineIncomeBanner: Long,
    bossRewardBanner: Long,
    compactNumbers: Boolean,
    onDismissOfflineIncome: () -> Unit,
    onDismissBossReward: () -> Unit,
    compact: Boolean = false,
    textScale: Float = 1f
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val spacing = if (compact) layoutProfile.bottomBarLabelSpacing() + 1.dp else layoutProfile.bottomBarLabelSpacing()
    val showOffline = offlineIncomeBanner > 0L
    val showBossReward = bossRewardBanner > 0L

    if (compact && showOffline && showBossReward) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            AnimatedVisibility(
                visible = showOffline,
                modifier = Modifier.weight(1f),
                enter = fadeIn(animationSpec = tween(260)) + slideInVertically(
                    animationSpec = tween(260),
                    initialOffsetY = { fullHeight -> -fullHeight / 3 }
                ),
                exit = fadeOut(animationSpec = tween(220)) + slideOutVertically(
                    animationSpec = tween(220),
                    targetOffsetY = { fullHeight -> -fullHeight / 4 }
                )
            ) {
                MicroAlertStatus(
                    icon = Icons.Filled.AccessTime,
                    title = stringResource(R.string.offline_toast_title),
                    description = stringResource(R.string.offline_toast_body),
                    accentColor = Color(0xFF8F69E9),
                    textScale = textScale,
                    onDismiss = onDismissOfflineIncome
                )
            }

            AnimatedVisibility(
                visible = showBossReward,
                modifier = Modifier.weight(1f),
                enter = fadeIn(animationSpec = tween(260)) + slideInVertically(
                    animationSpec = tween(260),
                    initialOffsetY = { fullHeight -> -fullHeight / 3 }
                ),
                exit = fadeOut(animationSpec = tween(220)) + slideOutVertically(
                    animationSpec = tween(220),
                    targetOffsetY = { fullHeight -> -fullHeight / 4 }
                )
            ) {
                BossVictoryToast(
                    rewardEssence = bossRewardBanner,
                    compactNumbers = compactNumbers,
                    textScale = textScale,
                    compact = true,
                    onDismiss = onDismissBossReward
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        AnimatedVisibility(
            visible = showOffline,
            enter = fadeIn(animationSpec = tween(260)) + slideInVertically(
                animationSpec = tween(260),
                initialOffsetY = { fullHeight -> -fullHeight / 3 }
            ),
            exit = fadeOut(animationSpec = tween(220)) + slideOutVertically(
                animationSpec = tween(220),
                targetOffsetY = { fullHeight -> -fullHeight / 4 }
            )
        ) {
            if (compact) {
                MicroAlertStatus(
                    icon = Icons.Filled.AccessTime,
                    title = stringResource(R.string.offline_toast_title),
                    description = stringResource(R.string.offline_toast_body),
                    accentColor = Color(0xFF8F69E9),
                    textScale = textScale,
                    onDismiss = onDismissOfflineIncome
                )
            } else {
                CompactAlertToast(
                    icon = Icons.Filled.AccessTime,
                    title = stringResource(R.string.offline_toast_title),
                    description = stringResource(R.string.offline_toast_body),
                    accentColor = Color(0xFF8F69E9),
                    onDismiss = onDismissOfflineIncome
                )
            }
        }

        AnimatedVisibility(
            visible = showBossReward,
            enter = fadeIn(animationSpec = tween(260)) + slideInVertically(
                animationSpec = tween(260),
                initialOffsetY = { fullHeight -> -fullHeight / 3 }
            ),
            exit = fadeOut(animationSpec = tween(220)) + slideOutVertically(
                animationSpec = tween(220),
                targetOffsetY = { fullHeight -> -fullHeight / 4 }
            )
        ) {
            BossVictoryToast(
                rewardEssence = bossRewardBanner,
                compactNumbers = compactNumbers,
                textScale = textScale,
                compact = compact,
                onDismiss = onDismissBossReward
            )
        }
    }
}

@Composable
private fun CoreScreenContent(
    showIntro: Boolean,
    intro: NarrativeEntry,
    showFtueHints: Boolean,
    ftueStep: FtueStep,
    nextUpgradeCost: Long,
    ritualCost: Long,
    compactNumbers: Boolean,
    textScale: Float
) {
    if (showIntro) {
        NarrativeIntroCard(
            intro = intro,
            textScale = textScale
        )
    } else if (showFtueHints && ftueStep == FtueStep.FIRST_CLICK) {
        FtueCard(
            step = ftueStep,
            nextUpgradeCost = nextUpgradeCost,
            ritualCost = ritualCost,
            compactNumbers = compactNumbers,
            textScale = textScale
        )
    }
}

@Composable
private fun GrowthScreenContent(
    tapUpgradeCost: Long,
    autoUpgradeCost: Long,
    showFtueHints: Boolean,
    ftueStep: FtueStep,
    ritualCost: Long,
    level: Int,
    rewards: List<RewardConfig>,
    claimedRewardLevels: Set<Int>,
    compactNumbers: Boolean,
    textScale: Float,
    retention: RetentionProgress,
    retentionConfig: RetentionConfig,
    critUpgradeCost: Long,
    streakUpgradeCost: Long,
    offlineBoostUpgradeCost: Long,
    cacheUpgradeCost: Long,
    cacheRewardPreview: Long,
    cacheCooldownMinutes: Int,
    cacheReadySeconds: Int,
    nextRelicRequirement: Int,
    canUpgradeCrit: Boolean,
    canUpgradeStreak: Boolean,
    canUpgradeOfflineBoost: Boolean,
    canUpgradeCache: Boolean,
    critSuccessTick: Int,
    streakSuccessTick: Int,
    offlineBoostSuccessTick: Int,
    cacheSuccessTick: Int,
    onUpgradeCrit: () -> Unit,
    onUpgradeStreak: () -> Unit,
    onUpgradeOfflineBoost: () -> Unit,
    onUpgradeCache: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val sectionSpacing = layoutProfile.sectionSpacing() - 2.dp

    if (showFtueHints && ftueStep != FtueStep.DONE && ftueStep != FtueStep.FIRST_CLICK) {
        FtueCard(
            step = ftueStep,
            nextUpgradeCost = minOf(tapUpgradeCost, autoUpgradeCost),
            ritualCost = ritualCost,
            compactNumbers = compactNumbers,
            textScale = textScale
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
    }

    MilestoneStatusCard(
        level = level,
        rewards = rewards,
        claimedRewardLevels = claimedRewardLevels,
        textScale = textScale
    )

    Spacer(modifier = Modifier.height(sectionSpacing))

    RetentionUpgradePanel(
        retention = retention,
        config = retentionConfig,
        critUpgradeCost = critUpgradeCost,
        streakUpgradeCost = streakUpgradeCost,
        offlineBoostUpgradeCost = offlineBoostUpgradeCost,
        cacheUpgradeCost = cacheUpgradeCost,
        cacheRewardPreview = cacheRewardPreview,
        cacheCooldownMinutes = cacheCooldownMinutes,
        cacheReadySeconds = cacheReadySeconds,
        canUpgradeCrit = canUpgradeCrit,
        canUpgradeStreak = canUpgradeStreak,
        canUpgradeOfflineBoost = canUpgradeOfflineBoost,
        canUpgradeCache = canUpgradeCache,
        critSuccessTick = critSuccessTick,
        streakSuccessTick = streakSuccessTick,
        offlineBoostSuccessTick = offlineBoostSuccessTick,
        cacheSuccessTick = cacheSuccessTick,
        compactNumbers = compactNumbers,
        textScale = textScale,
        onUpgradeCrit = onUpgradeCrit,
        onUpgradeStreak = onUpgradeStreak,
        onUpgradeOfflineBoost = onUpgradeOfflineBoost,
        onUpgradeCache = onUpgradeCache
    )

    Spacer(modifier = Modifier.height(sectionSpacing))

    RelicVaultCard(
        retention = retention,
        config = retentionConfig,
        nextRelicRequirement = nextRelicRequirement,
        compactNumbers = compactNumbers,
        textScale = textScale
    )
}

@Composable
private fun RetentionUpgradePanel(
    retention: RetentionProgress,
    config: RetentionConfig,
    critUpgradeCost: Long,
    streakUpgradeCost: Long,
    offlineBoostUpgradeCost: Long,
    cacheUpgradeCost: Long,
    cacheRewardPreview: Long,
    cacheCooldownMinutes: Int,
    cacheReadySeconds: Int,
    canUpgradeCrit: Boolean,
    canUpgradeStreak: Boolean,
    canUpgradeOfflineBoost: Boolean,
    canUpgradeCache: Boolean,
    critSuccessTick: Int,
    streakSuccessTick: Int,
    offlineBoostSuccessTick: Int,
    cacheSuccessTick: Int,
    compactNumbers: Boolean,
    textScale: Float,
    onUpgradeCrit: () -> Unit,
    onUpgradeStreak: () -> Unit,
    onUpgradeOfflineBoost: () -> Unit,
    onUpgradeCache: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val cardPadding = layoutProfile.cardPadding()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0x4FD7BC84))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF171C24), Color(0xFF11161D))
                )
            )
        ) {
            Column(modifier = Modifier.padding(cardPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.retention_panel_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(14f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                        ExpandableLoreText(
                            key = "retention_panel_subtitle",
                            text = stringResource(R.string.retention_panel_subtitle),
                            textScale = textScale,
                            color = Color(0xFFD3DDE8),
                            collapsedMaxLines = 1,
                            fontSizeSp = 11f,
                            toggleColor = Color(0xFFE2C688)
                        )
                    }
                    Spacer(modifier = Modifier.width(layoutProfile.chipSpacing()))
                    InfoChip(
                        text = stringResource(R.string.growth_card_long_hint),
                        containerColor = Color(0xFF262E37),
                        contentColor = Color(0xFFFFE8BE),
                        textScale = textScale
                    )
                }
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))

                RetentionUpgradeRow(
                    title = stringResource(R.string.retention_crit_title),
                    description = stringResource(
                        R.string.retention_crit_desc,
                        formatPercent(critChance(retention.critLevel, config) * 100.0),
                        formatMultiplier(critMultiplier(retention.critLevel, config))
                    ),
                    level = retention.critLevel,
                    maxLevel = config.crit.maxLevel,
                    cost = critUpgradeCost,
                    enabled = canUpgradeCrit,
                    successTick = critSuccessTick,
                    compactNumbers = compactNumbers,
                    textScale = textScale,
                    tag = "retention_crit_upgrade_button",
                    onClick = onUpgradeCrit
                )

                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))

                RetentionUpgradeRow(
                    title = stringResource(R.string.retention_streak_title),
                    description = stringResource(
                        R.string.retention_streak_desc,
                        streakThreshold(retention.streakLevel, config),
                        formatMultiplier(streakBurstMultiplier(retention.streakLevel, config))
                    ),
                    level = retention.streakLevel,
                    maxLevel = config.streak.maxLevel,
                    cost = streakUpgradeCost,
                    enabled = canUpgradeStreak,
                    successTick = streakSuccessTick,
                    compactNumbers = compactNumbers,
                    textScale = textScale,
                    tag = "retention_streak_upgrade_button",
                    extra = stringResource(
                        R.string.retention_charge,
                        retention.streakCharge,
                        streakThreshold(retention.streakLevel, config)
                    ),
                    onClick = onUpgradeStreak
                )

                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))

                RetentionUpgradeRow(
                    title = stringResource(R.string.retention_offline_title),
                    description = stringResource(
                        R.string.retention_offline_desc,
                        formatMultiplier(offlineBoostMultiplier(retention.offlineBoostLevel, config))
                    ),
                    level = retention.offlineBoostLevel,
                    maxLevel = config.offlineBoost.maxLevel,
                    cost = offlineBoostUpgradeCost,
                    enabled = canUpgradeOfflineBoost,
                    successTick = offlineBoostSuccessTick,
                    compactNumbers = compactNumbers,
                    textScale = textScale,
                    tag = "retention_offline_upgrade_button",
                    onClick = onUpgradeOfflineBoost
                )

                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))

                RetentionUpgradeRow(
                    title = stringResource(R.string.retention_cache_title),
                    description = stringResource(
                        R.string.retention_cache_desc,
                        cacheCooldownMinutes,
                        formatNumber(cacheRewardPreview, compactNumbers)
                    ),
                    level = retention.cacheLevel,
                    maxLevel = config.cache.maxLevel,
                    cost = cacheUpgradeCost,
                    enabled = canUpgradeCache,
                    successTick = cacheSuccessTick,
                    compactNumbers = compactNumbers,
                    textScale = textScale,
                    tag = "retention_cache_upgrade_button",
                    extra = if (cacheReadySeconds <= 0) {
                        stringResource(R.string.retention_cache_ready_now)
                    } else {
                        stringResource(
                            R.string.arcane_cache_timer,
                            formatShortDuration(cacheReadySeconds)
                        )
                    },
                    onClick = onUpgradeCache
                )
            }
        }
    }
}

@Composable
private fun RetentionUpgradeRow(
    title: String,
    description: String,
    level: Int,
    maxLevel: Int,
    cost: Long,
    enabled: Boolean,
    successTick: Int,
    compactNumbers: Boolean,
    textScale: Float,
    tag: String,
    onClick: () -> Unit,
    extra: String? = null
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val cardPadding = layoutProfile.cardPadding() - 2.dp
    val isActionable = level < maxLevel && enabled
    val accentColor = if (isActionable) Color(0xFF7A8DA3) else Color(0xFF556273)
    var successVisible by remember { mutableStateOf(false) }
    var lastHandledSuccessTick by remember { mutableIntStateOf(successTick) }
    val cardColor by animateColorAsState(
        targetValue = if (isActionable) Color(0xFF1E2731) else Color(0xFF171D25),
        animationSpec = tween(durationMillis = 220),
        label = "retention_card_color_$tag"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (successVisible) 1.018f else 1f,
        animationSpec = tween(durationMillis = 280),
        label = "retention_card_scale_$tag"
    )
    val successOverlayAlpha by animateFloatAsState(
        targetValue = if (successVisible) 0.22f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "retention_card_success_$tag"
    )
    LaunchedEffect(successTick) {
        if (successTick <= lastHandledSuccessTick) return@LaunchedEffect
        lastHandledSuccessTick = successTick
        successVisible = true
        delay(420)
        successVisible = false
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = cardScale,
                scaleY = cardScale
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (isActionable) 0.4f else 0.2f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(accentColor.copy(alpha = 0.12f), cardColor)
                )
            )
        ) {
            if (successOverlayAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(accentColor.copy(alpha = successOverlayAlpha))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color(0xFFFFF1D7),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = scaledSp(12.5f, textScale)
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                    ExpandableLoreText(
                        key = "${tag}_description",
                        text = description,
                        textScale = textScale,
                        color = Color(0xFFD3DDE8),
                        collapsedMaxLines = 1,
                        fontSizeSp = 11f,
                        toggleColor = Color(0xFFE2C688)
                    )
                    if (extra != null) {
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                        Text(
                            text = extra,
                            color = Color(0xFFFFE8BE),
                            fontSize = scaledSp(10.5f, textScale)
                        )
                    }
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                    InfoChip(
                        text = stringResource(R.string.retention_level_label, level, maxLevel),
                        containerColor = Color(0xFF262E37),
                        contentColor = Color(0xFFFFE8BE),
                        textScale = textScale
                    )
                }
                Spacer(modifier = Modifier.width(layoutProfile.chipSpacing() + 2.dp))
                ClaimActionButton(
                    text = if (level >= maxLevel) {
                        stringResource(R.string.state_max_level)
                    } else {
                        formatNumber(cost, compactNumbers)
                    },
                    onClick = onClick,
                    enabled = isActionable,
                    containerColor = accentColor,
                    disabledContainerColor = Color(0xFF2A323C),
                    textScale = textScale,
                    modifier = Modifier.testTag(tag),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(
                        horizontal = cardPadding,
                        vertical = layoutProfile.bottomBarLabelSpacing() + 3.dp
                    ),
                    fontSizeSp = 11f,
                    animateAttention = isActionable,
                    showAccentIcon = isActionable,
                    style = ActionButtonStyle.PURCHASE
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelicVaultCard(
    retention: RetentionProgress,
    config: RetentionConfig,
    nextRelicRequirement: Int,
    compactNumbers: Boolean,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val progress = if (nextRelicRequirement <= 0) {
        1f
    } else {
        (retention.relicShards.toFloat() / nextRelicRequirement.toFloat()).coerceIn(0f, 1f)
    }
    val glowPulse by rememberInfiniteTransition(label = "relic_vault_glow")
        .animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "relic_vault_glow_value"
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF5BCBF0).copy(alpha = 0.42f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF121A31), Color(0xFF101628), Color(0xFF0C111E))
                )
            )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFF5BCBF0).copy(alpha = 0.18f),
                    radius = size.minDimension * 0.28f * glowPulse,
                    center = Offset(size.width * 0.88f, size.height * 0.22f)
                )
                drawCircle(
                    color = Color(0xFF9C6BFF).copy(alpha = 0.14f),
                    radius = size.minDimension * 0.24f,
                    center = Offset(size.width * 0.16f, size.height * 0.78f)
                )
            }

            Column(modifier = Modifier.padding(layoutProfile.cardPadding())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.sectionSpacing() + 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .width(scaledDp(96f))
                            .height(scaledDp(108f))
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        ResourceOrPlaceholderArt(
                            drawableName = "vavarda_relic_vault",
                            placeholderTitle = stringResource(R.string.relic_vault_title),
                            placeholderSubtitle = stringResource(R.string.relic_vault_subtitle),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.relic_vault_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(14f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                        ExpandableLoreText(
                            key = "relic_vault_subtitle",
                            text = stringResource(R.string.relic_vault_subtitle),
                            textScale = textScale,
                            color = Color(0xFFD5EFFF),
                            collapsedMaxLines = 1,
                            fontSizeSp = 11f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    InfoChip(
                        text = stringResource(R.string.relic_level_short, retention.relicLevel),
                        containerColor = Color(0xFF1C2742),
                        contentColor = Color(0xFFE1F8FF),
                        textScale = textScale
                    )
                    InfoChip(
                        text = if (nextRelicRequirement > 0) {
                            stringResource(
                                R.string.relic_shards_progress,
                                retention.relicShards,
                                nextRelicRequirement
                            )
                        } else {
                            stringResource(R.string.state_max_level)
                        },
                        containerColor = Color(0xFF20253D),
                        contentColor = Color(0xFFD7D7FF),
                        textScale = textScale
                    )
                }

                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = Color(0xFF5BCBF0),
                    trackColor = Color(0xFF24304D)
                )
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    StatPill(
                        title = stringResource(R.string.faction_bonus_tap_short),
                        value = "×${formatMultiplier(relicTapMultiplier(retention.relicLevel, config))}"
                    )
                    StatPill(
                        title = stringResource(R.string.faction_bonus_auto_short),
                        value = "×${formatMultiplier(relicAutoMultiplier(retention.relicLevel, config))}"
                    )
                    StatPill(
                        title = stringResource(R.string.faction_bonus_boss_short),
                        value = "×${formatMultiplier(relicBossMultiplier(retention.relicLevel, config))}"
                    )
                }
                Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                ExpandableLoreText(
                    key = "relic_vault_hint_${retention.relicLevel}_$nextRelicRequirement",
                    text = if (nextRelicRequirement > 0) {
                        stringResource(
                            R.string.relic_vault_hint,
                            formatNumber((nextRelicRequirement - retention.relicShards).coerceAtLeast(0), compactNumbers)
                        )
                    } else {
                        stringResource(R.string.relic_vault_max_hint)
                    },
                    textScale = textScale,
                    color = Color(0xFFD4EAFE),
                    collapsedMaxLines = 2,
                    fontSizeSp = 11f
                )
            }
        }
    }
}

@Composable
private fun EventsScreenContent(
    boss: BossState,
    narrativeLine: String,
    missions: List<DailyMission>,
    state: GameState,
    level: Int,
    retentionConfig: RetentionConfig,
    cacheReadySeconds: Int,
    cacheRewardPreview: Long,
    cacheCooldownMinutes: Int,
    returnStreakAvailable: Boolean,
    returnStreakPreviewDay: Int,
    returnStreakRewardPreview: Long,
    returnMilestonePreview: ReturnStreakMilestoneConfig?,
    nextRelicRequirement: Int,
    altarProgress: Float,
    altarWeekKey: String,
    altarClaimableTiers: List<SeasonalAltarTierConfig>,
    compactNumbers: Boolean,
    textScale: Float,
    bossRewardPreview: Long,
    bossHitPreview: Long,
    bossHitsToDefeat: Int?,
    bossImpactBursts: List<BossImpactBurst>,
    bossImpactTick: Int,
    bossDefeatTick: Int,
    onClaimAllReady: () -> Unit,
    onClaimArcaneCache: () -> Unit,
    onClaimReturnStreak: () -> Unit,
    onClaimSeasonalAltarTier: (SeasonalAltarTierConfig) -> Unit,
    onHitBoss: () -> Unit,
    onClaimMission: (DailyMission) -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val claimableMissions = missions.count { mission ->
        isMissionCompleted(state, mission) && !isMissionClaimed(state, mission)
    }
    val firstClaimableMission = missions.firstOrNull { mission ->
        isMissionCompleted(state, mission) && !isMissionClaimed(state, mission)
    }
    val readyRewardCount = claimableMissions +
        (if (returnStreakAvailable) 1 else 0) +
        (if (cacheReadySeconds <= 0) 1 else 0) +
        altarClaimableTiers.size
    var detailsExpanded by rememberSaveable("events_details_expanded") { mutableStateOf(false) }

    if (boss.isActive) {
        BossEventCard(
            boss = boss,
            narrativeLine = narrativeLine,
            compactNumbers = compactNumbers,
            textScale = textScale,
            bossRewardPreview = bossRewardPreview,
            bossHitPreview = bossHitPreview,
            bossHitsToDefeat = bossHitsToDefeat,
            bossImpactBursts = bossImpactBursts,
            bossImpactTick = bossImpactTick,
            bossDefeatTick = bossDefeatTick,
            onHitBoss = onHitBoss
        )
    } else {
        val primaryBody = when {
            readyRewardCount > 1 -> stringResource(
                R.string.goal_events_claim_all_ready,
                localizedReadyRewardsSummaryText(
                    claimableMissions = claimableMissions,
                    returnStreakAvailable = returnStreakAvailable,
                    cacheReadySeconds = cacheReadySeconds,
                    altarClaimableTierCount = altarClaimableTiers.size
                )
            )
            returnStreakAvailable && returnMilestonePreview != null -> stringResource(
                R.string.home_rewards_body_return_milestone,
                returnStreakPreviewDay,
                formatNumber(
                    returnStreakRewardPreview + returnMilestonePreview.rewardEssence,
                    compactNumbers
                ),
                returnMilestonePreview.title
            )
            returnStreakAvailable -> stringResource(
                R.string.home_rewards_body_return_ready,
                returnStreakPreviewDay,
                formatNumber(returnStreakRewardPreview, compactNumbers)
            )
            cacheReadySeconds <= 0 -> stringResource(
                R.string.home_rewards_body_cache_ready,
                formatNumber(cacheRewardPreview, compactNumbers)
            )
            altarClaimableTiers.isNotEmpty() -> stringResource(
                R.string.home_rewards_body_altar_ready,
                altarClaimableTiers.size
            )
            claimableMissions > 0 -> stringResource(R.string.goal_events_daily_ready, claimableMissions)
            else -> stringResource(R.string.goal_events_boss_countdown, boss.secondsUntilSpawn)
        }
        val primaryAction: (() -> Unit)? = when {
            readyRewardCount > 1 -> onClaimAllReady
            returnStreakAvailable -> onClaimReturnStreak
            cacheReadySeconds <= 0 -> onClaimArcaneCache
            altarClaimableTiers.isNotEmpty() -> ({ onClaimSeasonalAltarTier(altarClaimableTiers.first()) })
            firstClaimableMission != null -> ({ onClaimMission(firstClaimableMission) })
            else -> null
        }

        ActionFocusCard(
            title = stringResource(R.string.goal_title),
            body = primaryBody,
            textScale = textScale,
            actionLabel = primaryAction?.let {
                if (readyRewardCount > 1) {
                    stringResource(R.string.action_claim_all)
                } else {
                    stringResource(R.string.action_claim)
                }
            },
            onAction = primaryAction
        )

        Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
        EventsHeroCard(
            boss = boss,
            readyRewardCount = readyRewardCount,
            compactNumbers = compactNumbers,
            textScale = textScale
        )

        Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
        EventsCycleSnapshotCard(
            boss = boss,
            readyRewardCount = readyRewardCount,
            claimableMissions = claimableMissions,
            returnStreakAvailable = returnStreakAvailable,
            returnStreakPreviewDay = returnStreakPreviewDay,
            cacheReadySeconds = cacheReadySeconds,
            cacheRewardPreview = cacheRewardPreview,
            altarClaimableTierCount = altarClaimableTiers.size,
            compactNumbers = compactNumbers,
            textScale = textScale
        )
    }

    SecondaryDetailsPanel(
        title = stringResource(R.string.events_rewards_summary_title),
        subtitle = stringResource(R.string.screen_events_subtitle),
        expanded = detailsExpanded,
        onToggle = { detailsExpanded = !detailsExpanded },
        textScale = textScale
    ) {
        EventsRewardSummaryCard(
            claimableMissions = claimableMissions,
            returnStreakAvailable = returnStreakAvailable,
            returnStreakPreviewDay = returnStreakPreviewDay,
            cacheReadySeconds = cacheReadySeconds,
            cacheRewardPreview = cacheRewardPreview,
            altarClaimableTiers = altarClaimableTiers,
            altarFavor = state.retention.altarFavor,
            altarConfig = retentionConfig.altar,
            readyRewardCount = readyRewardCount,
            compactNumbers = compactNumbers,
            textScale = textScale,
            onClaimAllReady = onClaimAllReady
        )

        Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
        SectionLabel(
            title = stringResource(R.string.events_retention_section_title),
            textScale = textScale
        )
        Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))

        ReturnStreakCard(
            retention = state.retention,
            config = retentionConfig,
            available = returnStreakAvailable,
            previewDay = returnStreakPreviewDay,
            rewardPreview = returnStreakRewardPreview,
            milestonePreview = returnMilestonePreview,
            textScale = textScale,
            compactNumbers = compactNumbers,
            onClaim = onClaimReturnStreak
        )

        Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
        ArcaneCacheCard(
            retention = state.retention,
            config = retentionConfig,
            cacheReadySeconds = cacheReadySeconds,
            rewardPreview = cacheRewardPreview,
            cooldownMinutes = cacheCooldownMinutes,
            playerLevel = level,
            compactNumbers = compactNumbers,
            textScale = textScale,
            onClaim = onClaimArcaneCache
        )

        Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
        RelicPulseCard(
            retention = state.retention,
            config = retentionConfig,
            nextRelicRequirement = nextRelicRequirement,
            compactNumbers = compactNumbers,
            textScale = textScale
        )

        Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
        SeasonalAltarCard(
            retention = state.retention,
            config = retentionConfig,
            progress = altarProgress,
            weekKey = altarWeekKey,
            claimableTiers = altarClaimableTiers,
            compactNumbers = compactNumbers,
            textScale = textScale,
            onClaimTier = onClaimSeasonalAltarTier
        )

        Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
        SectionLabel(
            title = stringResource(R.string.events_daily_section_title),
            textScale = textScale
        )
        Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))

        DailyMissionsCard(
            missions = missions,
            state = state,
            compactNumbers = compactNumbers,
            textScale = textScale,
            onClaimMission = onClaimMission
        )
    }
}

@Composable
private fun PathScreenContent(
    level: Int,
    path: AlignmentPath,
    bonuses: Map<AlignmentPath, FactionBonusConfig>,
    textScale: Float,
    onPathChange: (AlignmentPath) -> Unit
) {
    FactionPanel(
        level = level,
        path = path,
        bonuses = bonuses,
        textScale = textScale,
        onPathChange = onPathChange
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HudPanel(
    level: Int,
    essence: Long,
    rituals: Int,
    path: AlignmentPath,
    tapGain: Long,
    autoGain: Long,
    progressToNext: Float,
    nextLevelEssence: Long,
    compactNumbers: Boolean,
    textScale: Float,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val levelLine = stringResource(R.string.label_level, level)
    val ritualsLabel = stringResource(R.string.label_rituals)
    val factionName = localizedFactionName(path)
    val remainingToNextLevel = (nextLevelEssence - essence).coerceAtLeast(0L)
    val progressColor by animateColorAsState(
        targetValue = when {
            progressToNext >= 0.9f -> Color(0xFFFFD36B)
            progressToNext >= 0.55f -> Color(0xFFF3C26C)
            else -> Color(0xFFB78DFF)
        },
        animationSpec = tween(durationMillis = 260),
        label = "hud_progress_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF1A112D), Color(0xFF291943), Color(0xFF130D20))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.title_game),
                            fontSize = scaledSp(28f, textScale),
                            color = Color(0xFFF6E9C9),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = MaterialTheme.typography.displayLarge.fontFamily
                        )
                        Text(
                            text = stringResource(R.string.hud_stats_subtitle, factionName),
                            fontSize = scaledSp(12f, textScale),
                            color = Color(0xFFD9CBFF)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClaimActionButton(
                            text = stringResource(R.string.action_guide),
                            onClick = onOpenGuide,
                            enabled = true,
                            containerColor = Color(0xFF634097),
                            textScale = textScale,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            fontSizeSp = 12f,
                            animateAttention = false,
                            showAccentIcon = false
                        )
                        ClaimActionButton(
                            text = stringResource(R.string.action_settings),
                            onClick = onOpenSettings,
                            enabled = true,
                            containerColor = Color(0xFF47306F),
                            textScale = textScale,
                            modifier = Modifier.testTag("open_settings_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            fontSizeSp = 12f,
                            animateAttention = false,
                            showAccentIcon = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.hud_card_essence),
                    color = Color(0xFFBAA7E5),
                    fontSize = scaledSp(12f, textScale)
                )
                AnimatedContent(
                    targetState = essence,
                    transitionSpec = {
                        (
                            fadeIn(animationSpec = tween(180)) +
                                slideInVertically(
                                    animationSpec = tween(180),
                                    initialOffsetY = { fullHeight -> fullHeight / 4 }
                                )
                            ) togetherWith (
                            fadeOut(animationSpec = tween(140)) +
                                slideOutVertically(
                                    animationSpec = tween(140),
                                    targetOffsetY = { fullHeight -> -fullHeight / 5 }
                                )
                            )
                    },
                    label = "essence_counter"
                ) { currentEssence ->
                    Text(
                        text = formatNumber(currentEssence, compactNumbers),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = scaledSp(34f, textScale)
                    )
                }
                Text(
                    text = buildString {
                        append(levelLine)
                        append("  •  ")
                        append(ritualsLabel)
                        append(": ")
                        append(rituals)
                    },
                    color = Color(0xFFE6DAFF),
                    fontSize = scaledSp(13f, textScale)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.hud_card_progress),
                    color = Color(0xFFBAA7E5),
                    fontSize = scaledSp(12f, textScale)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressToNext },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = progressColor,
                    trackColor = Color(0xFF382955)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.label_next_level_cost,
                        formatNumber(remainingToNextLevel, compactNumbers)
                    ),
                    color = Color(0xFFD9CBFF),
                    fontSize = scaledSp(12f, textScale)
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPill(
                        title = stringResource(R.string.label_tap_rate),
                        value = "+${formatNumber(tapGain, compactNumbers)}"
                    )
                    StatPill(
                        title = stringResource(R.string.label_auto_rate),
                        value = "${formatNumber(autoGain, compactNumbers)}/s"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(title: String, value: String) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF241735))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$title: ", color = Color(0xFFBCA9E9), fontSize = 10.5.sp)
            Text(text = value, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NarrativeIntroCard(
    intro: NarrativeEntry,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0x4FD7BC84))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1D202B), Color(0xFF171A23), Color(0xFF10131A))
                )
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                InfoChip(
                    text = stringResource(R.string.guide_title),
                    containerColor = Color(0xFF252C39),
                    contentColor = Color(0xFFFFE7B2),
                    textScale = textScale
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = intro.title,
                    color = Color(0xFFFFF2D6),
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(15f, textScale),
                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = intro.body,
                    color = Color(0xFFD4DBE6),
                    fontSize = scaledSp(12f, textScale),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 3.dp))
                GuidanceActionPill(
                    text = stringResource(R.string.intro_card_cta),
                    textScale = textScale,
                    modifier = Modifier.widthIn(min = scaledDp(188f)),
                    containerColor = Color(0xFF2B3544),
                    contentColor = Color(0xFFFFF0CF),
                    emphasized = true
                )
            }
        }
    }
}

@Composable
private fun CompactAlertToast(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notice_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDE191226)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    color = Color(0xFFDCCBFF),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = Color(0xFFD0C2F3),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun BossVictoryToast(
    rewardEssence: Long,
    compactNumbers: Boolean,
    textScale: Float,
    compact: Boolean,
    onDismiss: () -> Unit
) {
    val glowPulse by rememberInfiniteTransition(label = "boss_victory_glow")
        .animateFloat(
            initialValue = 0.94f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "boss_victory_glow_value"
        )
    val shape = if (compact) RoundedCornerShape(999.dp) else RoundedCornerShape(18.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notice_card"),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFFFFD978).copy(alpha = 0.34f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF7E6035),
                        Color(0xFF2B241A),
                        Color(0xFF181D25)
                    )
                )
            )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFFFFD978).copy(alpha = if (compact) 0.16f else 0.2f),
                    radius = size.minDimension * (if (compact) 0.42f else 0.34f) * glowPulse,
                    center = Offset(size.width * 0.88f, size.height * 0.22f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (compact) 8.dp else 12.dp,
                        vertical = if (compact) 6.dp else 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x33FFF0C8))
                        .padding(if (compact) 4.dp else 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFE7A4),
                        modifier = Modifier.size(if (compact) 12.dp else 16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(if (compact) 7.dp else 10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.boss_victory_title),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = scaledSp(if (compact) 10f else 12.5f, textScale),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (compact) {
                            stringResource(
                                R.string.boss_victory_reward,
                                formatNumber(rewardEssence, compactNumbers)
                            )
                        } else {
                            stringResource(R.string.boss_victory_body)
                        },
                        color = Color(0xFFF6E9C8),
                        fontSize = scaledSp(if (compact) 9f else 10.5f, textScale),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!compact) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Card(
                        shape = RoundedCornerShape(999.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x33FFF0C8))
                    ) {
                        Text(
                            text = stringResource(
                                R.string.boss_victory_reward,
                                formatNumber(rewardEssence, compactNumbers)
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(10.5f, textScale)
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(if (compact) 18.dp else 26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.action_close),
                        tint = Color(0xFFFFF1D7),
                        modifier = Modifier.size(if (compact) 12.dp else 15.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MicroAlertStatus(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    textScale: Float,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notice_card"),
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xC9161124)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .padding(3.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(11.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = scaledSp(9.5f, textScale),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = description,
                modifier = Modifier.weight(1f),
                color = Color(0xFFDCCBFF),
                fontSize = scaledSp(9f, textScale),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.action_close),
                    tint = Color(0xFFD0C2F3),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun BoxScope.FloatingUpgradeCelebration(
    celebration: UpgradeCelebration,
    index: Int,
    textScale: Float
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(celebration.id) {
        shown = true
    }
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 920),
        label = "upgrade_celebration_progress_${celebration.id}"
    )
    val density = LocalDensity.current
    val celebrationOffsetYPx = with(density) { (-(22f * progress)).dp.roundToPx() }

    Card(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = scaledDp(20f + index * 44f))
            .offset { IntOffset(0, celebrationOffsetYPx) }
            .graphicsLayer(
                scaleX = 0.94f + (0.08f * (1f - (progress - 0.2f).coerceAtLeast(0f))),
                scaleY = 0.94f + (0.08f * (1f - (progress - 0.2f).coerceAtLeast(0f)))
            )
            .alpha(1f - (progress * 0.92f)),
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE161027)),
        border = BorderStroke(1.dp, celebration.accentColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = scaledDp(12f), vertical = scaledDp(8f)),
            horizontalArrangement = Arrangement.spacedBy(scaledDp(8f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = celebration.accentColor,
                modifier = Modifier.size(scaledDp(16f))
            )
            Text(
                text = celebration.message,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(12f, textScale)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArtCard(
    playerLevel: Int,
    title: String,
    artMilestoneLevel: Int,
    nextArtUnlockLevel: Int?,
    drawableName: String,
    clickBursts: List<ClickBurst>,
    flashAlpha: Float,
    showTapPrompt: Boolean,
    showBottomCaption: Boolean,
    compactNumbers: Boolean,
    textScale: Float,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val artPulse by rememberInfiniteTransition(label = "art_pulse_transition")
        .animateFloat(
            initialValue = 1f,
            targetValue = 1.025f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "art_pulse"
        )
    val tapCompression by animateFloatAsState(
        targetValue = if (flashAlpha > 0f) 0.985f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "art_tap_compression"
    )
    val sheenShift by rememberInfiniteTransition(label = "art_sheen_transition")
        .animateFloat(
            initialValue = -260f,
            targetValue = 980f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "art_sheen"
        )
    val hintPulse by rememberInfiniteTransition(label = "art_hint_transition")
        .animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "art_hint_pulse"
        )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onTap() }
            .testTag("art_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171126)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = tapCompression,
                    scaleY = tapCompression
                )
        ) {
            ResourceOrPlaceholderArt(
                drawableName = drawableName,
                placeholderTitle = title,
                placeholderSubtitle = stringResource(R.string.art_placeholder_body),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = artPulse,
                        scaleY = artPulse
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            start = Offset(sheenShift, 0f),
                            end = Offset(sheenShift + 240f, 920f)
                        )
                    )
            )

            if (flashAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = flashAlpha))
                )
            }

            clickBursts.forEach { burst ->
                FloatingTapBurst(
                    burst = burst,
                    compactNumbers = compactNumbers,
                    textScale = textScale
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(
                        horizontal = layoutProfile.cardPadding(),
                        vertical = layoutProfile.cardPadding()
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    InfoChip(
                        text = stringResource(R.string.art_stage_label, artMilestoneLevel),
                        containerColor = Color(0xB2140E22),
                        contentColor = Color(0xFFF6E8C9),
                        textScale = textScale
                    )
                    InfoChip(
                        text = stringResource(R.string.art_player_level, playerLevel),
                        containerColor = Color(0xA2171C28),
                        contentColor = Color.White,
                        textScale = textScale
                    )
                }
            }

            if (showTapPrompt || showBottomCaption) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xD7120D1F))
                            )
                        )
                        .padding(
                            horizontal = layoutProfile.cardPadding(),
                            vertical = layoutProfile.cardPadding() - 2.dp
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xBA121722))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (showBottomCaption) {
                                Text(
                                    text = stringResource(R.string.art_current_manifestation),
                                    color = Color(0xFFE7CF96),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = scaledSp(9.8f, textScale)
                                )
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = scaledSp(18.6f, textScale),
                                    fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (nextArtUnlockLevel != null) {
                                        stringResource(R.string.art_next_unlock_polish, nextArtUnlockLevel)
                                    } else {
                                        stringResource(R.string.art_stage_label, artMilestoneLevel)
                                    },
                                    color = Color(0xFFD6C6F2),
                                    fontSize = scaledSp(10.6f, textScale),
                                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (showTapPrompt) {
                                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                                Text(
                                    text = stringResource(R.string.tap_hint),
                                    color = Color(0xFFD6DEEA),
                                    fontSize = scaledSp(10.4f, textScale),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (showTapPrompt) {
                            Card(
                                modifier = Modifier
                                    .padding(bottom = 2.dp)
                                    .graphicsLayer(
                                        scaleX = hintPulse,
                                        scaleY = hintPulse
                                    ),
                                shape = RoundedCornerShape(999.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xA81B1430))
                            ) {
                                Text(
                                    text = stringResource(R.string.art_touch_chip),
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    color = Color(0xFFF4E9FF),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = scaledSp(10.5f, textScale)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.FloatingTapBurst(
    burst: ClickBurst,
    compactNumbers: Boolean,
    textScale: Float
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(burst.id) {
        started = true
    }
    val animationProgress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "tap_burst_progress"
    )
    val density = LocalDensity.current
    val burstOffsetXPx = with(density) { burst.horizontalDrift.dp.roundToPx() }
    val burstOffsetYPx = with(density) { (-56f * animationProgress).dp.roundToPx() }

    Text(
        text = "+${formatNumber(burst.amount, compactNumbers)}",
        color = Color(0xFFFFE8A8),
        fontWeight = FontWeight.ExtraBold,
        fontSize = scaledSp(18f, textScale),
        modifier = Modifier
            .align(Alignment.Center)
            .offset { IntOffset(burstOffsetXPx, burstOffsetYPx) }
            .alpha(1f - animationProgress)
    )
}

@Composable
private fun ResourceOrPlaceholderArt(
    drawableName: String,
    placeholderTitle: String,
    placeholderSubtitle: String,
    modifier: Modifier = Modifier
) {
    val (resourceId, artBitmap) = rememberArtBitmap(drawableName)

    if (resourceId != 0) {
        Box(
            modifier = modifier.background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF281A3A),
                        Color(0xFF161922),
                        Color(0xFF0D1016)
                    ),
                    radius = 1300f
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            val imageModifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp)
            if (artBitmap != null) {
                Image(
                    bitmap = artBitmap,
                    contentDescription = drawableName,
                    contentScale = ContentScale.Fit,
                    modifier = imageModifier
                )
            } else {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = drawableName,
                    contentScale = ContentScale.Fit,
                    modifier = imageModifier
                )
            }
        }
    } else {
        Box(
            modifier = modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF151720),
                        Color(0xFF1B1323),
                        Color(0xFF0E1016)
                    )
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0x253FDBFF),
                    radius = size.minDimension * 0.28f,
                    center = Offset(size.width * 0.24f, size.height * 0.22f)
                )
                drawCircle(
                    color = Color(0x22FFCC73),
                    radius = size.minDimension * 0.16f,
                    center = Offset(size.width * 0.78f, size.height * 0.26f)
                )
                drawCircle(
                    color = Color(0x1838D39F),
                    radius = size.minDimension * 0.24f,
                    center = Offset(size.width * 0.70f, size.height * 0.84f)
                )
            }
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xB5151722)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFD88F),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = placeholderTitle,
                    color = Color(0xFFFFF1D8),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = placeholderSubtitle,
                    color = Color(0xFFD2D7E1),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun FtueCard(
    step: FtueStep,
    nextUpgradeCost: Long,
    ritualCost: Long,
    compactNumbers: Boolean,
    textScale: Float
) {
    val title = when (step) {
        FtueStep.FIRST_CLICK -> stringResource(R.string.ftue_step_1_title)
        FtueStep.FIRST_UPGRADE -> stringResource(R.string.ftue_step_2_title)
        FtueStep.FIRST_RITUAL -> stringResource(R.string.ftue_step_3_title)
        FtueStep.DONE -> ""
    }
    val body = when (step) {
        FtueStep.FIRST_CLICK -> stringResource(R.string.ftue_step_1_body)
        FtueStep.FIRST_UPGRADE -> stringResource(
            R.string.ftue_step_2_body,
            formatNumber(nextUpgradeCost, compactNumbers)
        )
        FtueStep.FIRST_RITUAL -> stringResource(
            R.string.ftue_step_3_body,
            formatNumber(ritualCost, compactNumbers)
        )
        FtueStep.DONE -> ""
    }
    val actionText = when (step) {
        FtueStep.FIRST_CLICK -> stringResource(R.string.ftue_action_step_1)
        FtueStep.FIRST_UPGRADE -> stringResource(R.string.ftue_action_step_2)
        FtueStep.FIRST_RITUAL -> stringResource(R.string.ftue_action_step_3)
        FtueStep.DONE -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0x4DAF86FF))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF23163A), Color(0xFF171126))
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF33214D))
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        color = Color(0xFFEAD8FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(12f, textScale)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = body,
                        color = Color(0xFFDCCBFF),
                        fontSize = scaledSp(11f, textScale),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GuidanceActionPill(
                        text = actionText,
                        textScale = textScale,
                        containerColor = Color(0xFF3A245F),
                        contentColor = Color(0xFFF0E2FF),
                        emphasized = step != FtueStep.DONE
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeRow(
    essence: Long,
    tapPower: Int,
    autoPower: Int,
    rituals: Int,
    tapUpgradeCost: Long,
    autoUpgradeCost: Long,
    ritualCost: Long,
    canUpgradeTap: Boolean,
    canUpgradeAuto: Boolean,
    canPerformRitual: Boolean,
    compactNumbers: Boolean,
    textScale: Float,
    recommendedAction: GrowthActionKind,
    tapSuccessTick: Int,
    autoSuccessTick: Int,
    ritualSuccessTick: Int,
    onTapUpgrade: () -> Unit,
    onAutoUpgrade: () -> Unit,
    onPerformRitual: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    data class GrowthUpgradeEntry(
        val kind: GrowthActionKind,
        val title: String,
        val description: String,
        val focusHint: String?,
        val successLabel: String,
        val current: Int,
        val next: Int,
        val cost: Long,
        val enabled: Boolean,
        val missingAmount: Long,
        val successTick: Int,
        val buttonLabel: String,
        val tag: String,
        val onClick: () -> Unit
    )
    var rowVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        rowVisible = true
    }
    val rowAlpha by animateFloatAsState(
        targetValue = if (rowVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 360),
        label = "upgrade_row_alpha"
    )
    val rowOffset by animateFloatAsState(
        targetValue = if (rowVisible) 0f else 20f,
        animationSpec = tween(durationMillis = 360),
        label = "upgrade_row_offset"
    )
    val density = LocalDensity.current
    val rowOffsetYPx = with(density) { rowOffset.dp.roundToPx() }
    val upgradeEntries = listOf(
        GrowthUpgradeEntry(
            kind = GrowthActionKind.TAP,
            title = stringResource(R.string.label_tap_rate),
            description = stringResource(R.string.upgrade_tap_desc),
            focusHint = stringResource(R.string.growth_focus_tap_hint),
            successLabel = stringResource(R.string.growth_success_tap),
            current = tapPower,
            next = tapPower + 1,
            cost = tapUpgradeCost,
            enabled = canUpgradeTap,
            missingAmount = (tapUpgradeCost - essence).coerceAtLeast(0L),
            successTick = tapSuccessTick,
            buttonLabel = stringResource(R.string.action_upgrade_tap),
            tag = "tap_upgrade_button",
            onClick = onTapUpgrade
        ),
        GrowthUpgradeEntry(
            kind = GrowthActionKind.AUTO,
            title = stringResource(R.string.label_auto_rate),
            description = stringResource(R.string.upgrade_auto_desc),
            focusHint = stringResource(R.string.growth_focus_auto_hint),
            successLabel = stringResource(R.string.growth_success_auto),
            current = autoPower,
            next = autoPower + 1,
            cost = autoUpgradeCost,
            enabled = canUpgradeAuto,
            missingAmount = (autoUpgradeCost - essence).coerceAtLeast(0L),
            successTick = autoSuccessTick,
            buttonLabel = stringResource(R.string.action_upgrade_auto),
            tag = "auto_upgrade_button",
            onClick = onAutoUpgrade
        ),
        GrowthUpgradeEntry(
            kind = GrowthActionKind.RITUAL,
            title = stringResource(R.string.label_ritual),
            description = stringResource(R.string.upgrade_ritual_desc),
            focusHint = stringResource(R.string.growth_focus_ritual_hint),
            successLabel = stringResource(R.string.growth_success_ritual),
            current = rituals,
            next = rituals + 1,
            cost = ritualCost,
            enabled = canPerformRitual,
            missingAmount = (ritualCost - essence).coerceAtLeast(0L),
            successTick = ritualSuccessTick,
            buttonLabel = stringResource(R.string.action_do),
            tag = "ritual_button",
            onClick = onPerformRitual
        )
    )
    val featuredEntry = upgradeEntries.firstOrNull { it.kind == recommendedAction } ?: upgradeEntries.first()
    val secondaryEntries = upgradeEntries.filterNot { it.kind == featuredEntry.kind }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .offset { IntOffset(0, rowOffsetYPx) },
        verticalArrangement = Arrangement.spacedBy(layoutProfile.bottomBarLabelSpacing())
    ) {
        UpgradeActionCard(
            modifier = Modifier.fillMaxWidth(),
            title = featuredEntry.title,
            description = featuredEntry.description,
            focusHint = featuredEntry.focusHint,
            successLabel = featuredEntry.successLabel,
            current = featuredEntry.current,
            next = featuredEntry.next,
            cost = featuredEntry.cost,
            enabled = featuredEntry.enabled,
            missingAmount = featuredEntry.missingAmount,
            recommended = true,
            featured = true,
            successTick = featuredEntry.successTick,
            compactNumbers = compactNumbers,
            textScale = textScale,
            buttonLabel = featuredEntry.buttonLabel,
            tag = featuredEntry.tag,
            lockedHint = if (featuredEntry.enabled) {
                null
            } else {
                stringResource(
                    R.string.growth_locked_featured_hint,
                    formatNumber(featuredEntry.missingAmount, compactNumbers)
                )
            },
            onClick = featuredEntry.onClick
        )

        SectionLabel(
            title = stringResource(R.string.growth_support_section_title),
            subtitle = stringResource(R.string.growth_support_section_subtitle),
            textScale = textScale
        )

        secondaryEntries.forEach { entry ->
            UpgradeActionCard(
                modifier = Modifier.fillMaxWidth(),
                title = entry.title,
                description = entry.description,
                focusHint = null,
                successLabel = entry.successLabel,
                current = entry.current,
                next = entry.next,
                cost = entry.cost,
                enabled = entry.enabled,
                missingAmount = entry.missingAmount,
                recommended = false,
                featured = false,
                successTick = entry.successTick,
                compactNumbers = compactNumbers,
                textScale = textScale,
                buttonLabel = entry.buttonLabel,
                tag = entry.tag,
                lockedHint = if (entry.enabled) {
                    null
                } else {
                    stringResource(
                        R.string.growth_locked_support_hint,
                        formatNumber(entry.missingAmount, compactNumbers)
                    )
                },
                onClick = entry.onClick
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UpgradeActionCard(
    modifier: Modifier,
    title: String,
    description: String,
    focusHint: String?,
    successLabel: String,
    current: Int,
    next: Int,
    cost: Long,
    enabled: Boolean,
    missingAmount: Long,
    recommended: Boolean,
    featured: Boolean,
    successTick: Int,
    compactNumbers: Boolean,
    textScale: Float,
    buttonLabel: String,
    tag: String,
    lockedHint: String? = null,
    onClick: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val cardPadding = if (featured) {
        layoutProfile.cardPadding() - 4.dp
    } else {
        layoutProfile.cardPadding() - 6.dp
    }
    var successVisible by remember { mutableStateOf(false) }
    var lastHandledSuccessTick by remember { mutableIntStateOf(successTick) }
    val accentColor = when {
        recommended && enabled -> Color(0xFFFFD978)
        featured && enabled -> Color(0xFF7E9ED4)
        enabled -> Color(0xFF6B809A)
        else -> Color(0xFF6A7488)
    }
    val cardColor by animateColorAsState(
        targetValue = when {
            recommended && enabled -> Color(0xFF2B241E)
            featured && enabled -> Color(0xFF171F2A)
            enabled -> Color(0xFF141B23)
            else -> Color(0xFF121821)
        },
        animationSpec = tween(durationMillis = 220),
        label = "upgrade_card_color_$tag"
    )
    val successScale by animateFloatAsState(
        targetValue = if (successVisible) {
            if (featured) 1.02f else 1.01f
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 280),
        label = "upgrade_card_scale_$tag"
    )
    val successOverlayAlpha by animateFloatAsState(
        targetValue = if (successVisible) 0.22f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "upgrade_card_success_$tag"
    )
    val successBannerAlpha by animateFloatAsState(
        targetValue = if (successVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "upgrade_card_banner_alpha_$tag"
    )
    LaunchedEffect(successTick) {
        if (successTick <= lastHandledSuccessTick) return@LaunchedEffect
        lastHandledSuccessTick = successTick
        successVisible = true
        delay(440)
        successVisible = false
    }

    Card(
        modifier = modifier.graphicsLayer(
            scaleX = successScale,
            scaleY = successScale
        ),
        shape = RoundedCornerShape(if (featured) 16.dp else 15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            1.dp,
            if (recommended) accentColor.copy(alpha = 0.56f) else Color.White.copy(alpha = 0.06f)
        )
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(accentColor.copy(alpha = 0.14f), cardColor)
                )
            )
        ) {
            if (successOverlayAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(accentColor.copy(alpha = successOverlayAlpha))
                )
            }
            Column(modifier = Modifier.padding(cardPadding)) {
                AnimatedVisibility(
                    visible = successVisible,
                    enter = fadeIn(animationSpec = tween(180)) + slideInVertically(
                        animationSpec = tween(220),
                        initialOffsetY = { fullHeight -> -fullHeight / 3 }
                    ),
                    exit = fadeOut(animationSpec = tween(180))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(successBannerAlpha)
                            .clip(RoundedCornerShape(999.dp))
                            .background(accentColor.copy(alpha = 0.22f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = successLabel,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(10.5f, textScale)
                        )
                    }
                }
                if (successVisible) {
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(if (featured) 12.5f else 12f, textScale),
                            fontFamily = MaterialTheme.typography.titleMedium.fontFamily
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() - 1.dp))
                        Text(
                            text = description,
                            color = Color(0xFFD0D7E4),
                            fontSize = scaledSp(if (featured) 10f else 9.6f, textScale),
                            maxLines = if (featured) 2 else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (featured && !focusHint.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                            Text(
                                text = focusHint,
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = scaledSp(10f, textScale),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (recommended) {
                        Card(
                            shape = RoundedCornerShape(999.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD978))
                        ) {
                            Text(
                                text = stringResource(R.string.state_recommended),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                color = Color(0xFF241735),
                                fontWeight = FontWeight.Bold,
                                fontSize = scaledSp(9.5f, textScale)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    InfoChip(
                        text = stringResource(R.string.growth_upgrade_level, current, next),
                        containerColor = Color(0xFF232A36),
                        contentColor = Color(0xFFEAEFF8),
                        textScale = textScale
                    )
                    InfoChip(
                        text = stringResource(
                            R.string.growth_upgrade_cost,
                            formatNumber(cost, compactNumbers)
                        ),
                        containerColor = if (enabled) {
                            accentColor.copy(alpha = 0.2f)
                        } else {
                            Color(0xFF202734)
                        },
                        contentColor = if (enabled) Color.White else Color(0xFFBFC8D5),
                        textScale = textScale
                    )
                }
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                ClaimActionButton(
                    text = if (enabled) {
                        buttonLabel
                    } else {
                        stringResource(
                            R.string.upgrade_missing_short,
                            formatNumber(missingAmount, compactNumbers)
                        )
                    },
                    onClick = onClick,
                    enabled = enabled,
                    containerColor = when {
                        recommended && enabled -> Color(0xFF7E5A18)
                        featured && enabled -> Color(0xFF355F73)
                        enabled -> Color(0xFF314352)
                        else -> Color(0xFF26303B)
                    },
                    disabledContainerColor = Color(0xFF26303B),
                    textScale = textScale,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(tag),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(
                        horizontal = cardPadding,
                        vertical = if (featured) {
                            layoutProfile.bottomBarLabelSpacing() + 2.dp
                        } else {
                            layoutProfile.bottomBarLabelSpacing() + 1.dp
                        }
                    ),
                    fontSizeSp = if (featured) 10f else 9.5f,
                    animateAttention = featured && enabled,
                    showAccentIcon = featured && enabled,
                    style = ActionButtonStyle.PURCHASE
                )
                if (!enabled && !lockedHint.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                    Text(
                        text = lockedHint,
                        color = Color(0xFF9DAAB9),
                        fontSize = scaledSp(if (featured) 10.4f else 10f, textScale),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyMissionsCard(
    missions: List<DailyMission>,
    state: GameState,
    compactNumbers: Boolean,
    textScale: Float,
    onClaimMission: (DailyMission) -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171C24))
    ) {
        Column(modifier = Modifier.padding(layoutProfile.cardPadding())) {
            Text(
                text = stringResource(R.string.daily_title),
                color = Color(0xFFFFF1D7),
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(15f, textScale)
            )
            Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))

            missions.forEach { mission ->
                val progress = missionProgress(state, mission)
                val completed = isMissionCompleted(state, mission)
                val claimed = isMissionClaimed(state, mission)
                val fraction = (progress.toFloat() / mission.target.toFloat()).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = layoutProfile.minorSpacing()),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242D))
                ) {
                    Column(modifier = Modifier.padding(layoutProfile.cardPadding() - 2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dailyMissionTitle(mission),
                                    color = Color(0xFFFFF1D7),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = scaledSp(13f, textScale)
                                )
                                Text(
                                    text = dailyMissionDescription(mission, compactNumbers),
                                    color = Color(0xFFD3DDE8),
                                    fontSize = scaledSp(11f, textScale),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(layoutProfile.chipSpacing()))
                            Card(
                                shape = RoundedCornerShape(999.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF735730))
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.daily_reward_short,
                                        formatNumber(mission.rewardEssence, compactNumbers)
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    color = Color(0xFFFFE8BE),
                                    fontSize = scaledSp(10f, textScale),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 4.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = Color(0xFF7A8DA3),
                            trackColor = Color(0xFF2A323C)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${formatNumber(progress, compactNumbers)} / ${formatNumber(mission.target, compactNumbers)}",
                                color = Color(0xFFD3DDE8),
                                fontSize = scaledSp(11f, textScale)
                            )
                            if (claimed) {
                                Text(
                                    text = stringResource(R.string.state_reward_claimed),
                                    color = Color(0xFFB7C9A9),
                                    fontSize = scaledSp(11f, textScale),
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                ClaimActionButton(
                                    onClick = { onClaimMission(mission) },
                                    enabled = completed,
                                    containerColor = Color(0xFF84633B),
                                    disabledContainerColor = Color(0xFF2A323C),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    textScale = textScale,
                                    text = if (completed) {
                                        stringResource(R.string.daily_claim_ready)
                                    } else {
                                        stringResource(R.string.daily_claim_locked)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReturnStreakCard(
    retention: RetentionProgress,
    config: RetentionConfig,
    available: Boolean,
    previewDay: Int,
    rewardPreview: Long,
    milestonePreview: ReturnStreakMilestoneConfig?,
    textScale: Float,
    compactNumbers: Boolean,
    onClaim: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    var detailsExpanded by rememberSaveable("events_return_details") { mutableStateOf(false) }
    val pulse by rememberInfiniteTransition(label = "return_streak_pulse")
        .animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "return_streak_pulse_value"
        )
    val highlightedMilestoneDay = remember(available, previewDay, milestonePreview, config.returnStreak.milestones) {
        milestonePreview?.day
            ?: config.returnStreak.milestones.firstOrNull { previewDay <= it.day }?.day
            ?: config.returnStreak.milestones.lastOrNull()?.day
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = if (available) pulse else 1f,
                scaleY = if (available) pulse else 1f
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFFFFC857).copy(alpha = 0.42f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2E1A12), Color(0xFF1A1123), Color(0xFF100B18))
                )
            )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFFFFC857).copy(alpha = 0.14f),
                    radius = size.minDimension * 0.34f,
                    center = Offset(size.width * 0.88f, size.height * 0.24f)
                )
            }

            Column(modifier = Modifier.padding(layoutProfile.cardPadding() - 2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.sectionSpacing() + 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(scaledDp(72f))
                            .height(scaledDp(88f))
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        ResourceOrPlaceholderArt(
                            drawableName = "vavarda_return_streak",
                            placeholderTitle = stringResource(R.string.return_streak_title),
                            placeholderSubtitle = stringResource(R.string.return_streak_subtitle),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.return_streak_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(14f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                        Text(
                            text = stringResource(
                                R.string.return_streak_reward_preview,
                                formatNumber(rewardPreview, compactNumbers)
                            ),
                            color = Color(0xFFFFE7AF),
                            fontSize = scaledSp(10.5f, textScale),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                        ExpandableLoreText(
                            key = "return_streak_subtitle",
                            text = stringResource(R.string.return_streak_subtitle),
                            textScale = textScale,
                            color = Color(0xFFFFE8BF),
                            collapsedMaxLines = 1,
                            fontSizeSp = 10.5f
                        )
                    }
                    InfoChip(
                        text = stringResource(R.string.return_streak_best, retention.bestReturnStreak),
                        containerColor = Color(0xFF3A2618),
                        contentColor = Color(0xFFFFE7AF),
                        textScale = textScale
                    )
                }
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.bottomBarLabelSpacing() + 4.dp)
                ) {
                    repeat(config.returnStreak.showcaseDays) { index ->
                        val day = index + 1
                        val active = day <= previewDay.coerceAtMost(config.returnStreak.showcaseDays)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(
                                    if (active) Color(0xFFFFC857) else Color(0xFF4A2D1B)
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(layoutProfile.minorSpacing() + 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.return_streak_day_label, previewDay),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = scaledSp(13f, textScale)
                        )
                        if (milestonePreview != null) {
                            Text(
                                text = milestonePreview.title,
                                color = Color(0xFFFFE7AF),
                                fontSize = scaledSp(10.5f, textScale),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClaimActionButton(
                            text = if (detailsExpanded) {
                                stringResource(R.string.action_hide)
                            } else {
                                stringResource(R.string.action_expand)
                            },
                            onClick = { detailsExpanded = !detailsExpanded },
                            enabled = true,
                            containerColor = Color(0xFF3A2618),
                            contentColor = Color(0xFFFFE7AF),
                            textScale = textScale,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            animateAttention = false,
                            showAccentIcon = false,
                            style = ActionButtonStyle.NEUTRAL
                        )
                        if (available) {
                            ClaimActionButton(
                                onClick = onClaim,
                                enabled = true,
                                containerColor = Color(0xFFD48C2E),
                                textScale = textScale,
                                text = stringResource(R.string.action_claim),
                                style = ActionButtonStyle.CLAIM
                            )
                        } else {
                            InfoChip(
                                text = stringResource(R.string.return_streak_claimed_today),
                                containerColor = Color(0xFF362315),
                                contentColor = Color(0xFFD9B778),
                                textScale = textScale
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = detailsExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                        Text(
                            text = stringResource(R.string.return_streak_milestones_title),
                            color = Color(0xFFFFE5B5),
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(11f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                            verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                        ) {
                            config.returnStreak.milestones.forEach { milestone ->
                                ReturnMilestoneHighlightCard(
                                    milestone = milestone,
                                    reached = retention.returnStreak >= milestone.day,
                                    highlighted = milestone.day == highlightedMilestoneDay,
                                    claimableToday = available && milestonePreview?.day == milestone.day,
                                    compactNumbers = compactNumbers,
                                    textScale = textScale
                                )
                            }
                        }
                        if (milestonePreview != null) {
                            Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                            Text(
                                text = stringResource(
                                    R.string.return_streak_milestone_preview,
                                    milestonePreview.title,
                                    formatNumber(milestonePreview.rewardEssence, compactNumbers),
                                    milestonePreview.rewardShards
                                ),
                                color = Color(0xFFFFE7AF),
                                fontSize = scaledSp(11f, textScale)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReturnMilestoneHighlightCard(
    milestone: ReturnStreakMilestoneConfig,
    reached: Boolean,
    highlighted: Boolean,
    claimableToday: Boolean,
    compactNumbers: Boolean,
    textScale: Float
) {
    val activeMotion = claimableToday || highlighted
    val borderColor = when {
        claimableToday -> Color(0xFFFFC857)
        reached -> Color(0xFFD48C2E)
        highlighted -> Color(0xFFB77D3E)
        else -> Color(0x554A2D1B)
    }
    val containerColor = when {
        claimableToday -> Color(0x523D240B)
        reached -> Color(0x40311D0E)
        highlighted -> Color(0x33291B12)
        else -> Color(0x22170F14)
    }
    val pulseScale by rememberInfiniteTransition(label = "return_milestone_card_motion")
        .animateFloat(
            initialValue = 1f,
            targetValue = if (claimableToday) 1.04f else 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (claimableToday) 920 else 1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "return_milestone_card_pulse"
        )
    val glowAlpha by rememberInfiniteTransition(label = "return_milestone_card_glow")
        .animateFloat(
            initialValue = if (claimableToday) 0.10f else 0.05f,
            targetValue = if (claimableToday) 0.24f else 0.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (claimableToday) 960 else 1700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "return_milestone_card_glow_alpha"
        )
    val sheenProgress by rememberInfiniteTransition(label = "return_milestone_card_sheen")
        .animateFloat(
            initialValue = -0.8f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (claimableToday) 1800 else 2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "return_milestone_card_sheen_progress"
        )

    Card(
        modifier = Modifier.graphicsLayer(
            scaleX = if (activeMotion) pulseScale else 1f,
            scaleY = if (activeMotion) pulseScale else 1f
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            if (activeMotion) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = borderColor.copy(alpha = glowAlpha),
                        radius = size.minDimension * if (claimableToday) 0.44f else 0.36f,
                        center = Offset(size.width * 0.84f, size.height * 0.2f)
                    )
                    val startX = size.width * sheenProgress - (size.width * 0.38f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (claimableToday) 0.16f else 0.08f),
                                Color.Transparent
                            ),
                            start = Offset(startX, 0f),
                            end = Offset(startX + size.width * 0.5f, size.height)
                        )
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = stringResource(R.string.return_streak_milestone_day_short, milestone.day),
                    color = if (claimableToday || reached) Color(0xFFFFE7AF) else Color(0xFFD3B58B),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = scaledSp(14f, textScale)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = milestone.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = scaledSp(11f, textScale),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.return_streak_milestone_reward_short,
                        formatNumber(milestone.rewardEssence, compactNumbers),
                        milestone.rewardShards
                    ),
                    color = Color(0xFFFFE7AF),
                    fontSize = scaledSp(10f, textScale)
                )
                if (claimableToday) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.return_streak_milestone_today),
                        color = Color(0xFFFFC857),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(10f, textScale)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArcaneCacheCard(
    retention: RetentionProgress,
    config: RetentionConfig,
    cacheReadySeconds: Int,
    rewardPreview: Long,
    cooldownMinutes: Int,
    playerLevel: Int,
    compactNumbers: Boolean,
    textScale: Float,
    onClaim: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val ready = cacheReadySeconds <= 0
    val chestPulse by rememberInfiniteTransition(label = "arcane_cache_pulse")
        .animateFloat(
            initialValue = 1f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (ready) 980 else 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "arcane_cache_pulse_value"
        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = if (ready) chestPulse else 1f,
                scaleY = if (ready) chestPulse else 1f
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF6FE0FF).copy(alpha = 0.44f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF111E30), Color(0xFF11142A), Color(0xFF0A101D))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(layoutProfile.cardPadding())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.sectionSpacing() + 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(scaledDp(72f))
                            .height(scaledDp(88f))
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        ResourceOrPlaceholderArt(
                            drawableName = "vavarda_arcane_cache",
                            placeholderTitle = stringResource(R.string.arcane_cache_title),
                            placeholderSubtitle = stringResource(R.string.arcane_cache_desc, cooldownMinutes, formatNumber(rewardPreview, compactNumbers)),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.arcane_cache_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(14f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                            verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                        ) {
                            InfoChip(
                                text = stringResource(R.string.retention_level_label, retention.cacheLevel, config.cache.maxLevel),
                                containerColor = Color(0xFF1B2842),
                                contentColor = Color(0xFFE0F7FF),
                                textScale = textScale
                            )
                            InfoChip(
                                text = if (ready) {
                                    stringResource(
                                        R.string.events_reward_cache_ready,
                                        formatNumber(rewardPreview, compactNumbers)
                                    )
                                } else {
                                    stringResource(R.string.arcane_cache_timer, formatShortDuration(cacheReadySeconds))
                                },
                                containerColor = if (ready) Color(0xFF2B6A7C) else Color(0xFF1A263A),
                                contentColor = Color(0xFFE0F7FF),
                                textScale = textScale
                            )
                        }
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                        ExpandableLoreText(
                            key = "arcane_cache_desc_${retention.cacheLevel}_$ready",
                            text = stringResource(
                                R.string.arcane_cache_desc,
                                cooldownMinutes,
                                formatNumber(rewardPreview, compactNumbers)
                            ),
                            textScale = textScale,
                            color = Color(0xFFD8F6FF),
                            collapsedMaxLines = 1,
                            fontSizeSp = 10.5f
                        )
                    }
                }
                Spacer(modifier = Modifier.height(layoutProfile.minorSpacing() + 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.arcane_cache_player_scale, playerLevel),
                        color = Color(0xFFDAD7FF),
                        fontSize = scaledSp(10.5f, textScale),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (ready) {
                        ClaimActionButton(
                            onClick = onClaim,
                            enabled = true,
                            containerColor = Color(0xFF3AAFD3),
                            textScale = textScale,
                            text = stringResource(R.string.action_claim),
                            style = ActionButtonStyle.CLAIM
                        )
                    } else {
                        InfoChip(
                            text = stringResource(R.string.arcane_cache_timer, formatShortDuration(cacheReadySeconds)),
                            containerColor = Color(0xFF1A263A),
                            contentColor = Color(0xFFB5DFFF),
                            textScale = textScale
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelicPulseCard(
    retention: RetentionProgress,
    config: RetentionConfig,
    nextRelicRequirement: Int,
    compactNumbers: Boolean,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val progress = if (nextRelicRequirement <= 0) {
        1f
    } else {
        (retention.relicShards.toFloat() / nextRelicRequirement.toFloat()).coerceIn(0f, 1f)
    }
    val shimmer by rememberInfiniteTransition(label = "relic_pulse_shimmer")
        .animateFloat(
            initialValue = -160f,
            targetValue = 460f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "relic_pulse_shimmer_value"
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF5BCBF0).copy(alpha = 0.36f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF11182C), Color(0xFF0F1424), Color(0xFF0A0F1C))
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.06f), Color.Transparent),
                            start = Offset(shimmer, 0f),
                            end = Offset(shimmer + 180f, 420f)
                        )
                    )
            )
            Column(modifier = Modifier.padding(layoutProfile.cardPadding())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.sectionSpacing() + 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(scaledDp(72f))
                            .height(scaledDp(88f))
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        ResourceOrPlaceholderArt(
                            drawableName = "vavarda_relic_vault",
                            placeholderTitle = stringResource(R.string.relic_pulse_title),
                            placeholderSubtitle = stringResource(R.string.relic_pulse_subtitle),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.relic_pulse_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(14f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                        Text(
                            text = if (nextRelicRequirement > 0) {
                                stringResource(
                                    R.string.relic_pulse_progress,
                                    retention.relicShards,
                                    nextRelicRequirement,
                                    formatNumber((nextRelicRequirement - retention.relicShards).coerceAtLeast(0), compactNumbers)
                                )
                            } else {
                                stringResource(R.string.relic_vault_max_hint)
                            },
                            color = Color(0xFFD5EEFF),
                            fontSize = scaledSp(10.5f, textScale),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                        ExpandableLoreText(
                            key = "relic_pulse_subtitle_${retention.relicLevel}",
                            text = stringResource(R.string.relic_pulse_subtitle),
                            textScale = textScale,
                            color = Color(0xFFD5EEFF),
                            collapsedMaxLines = 1,
                            fontSizeSp = 10.5f
                        )
                    }
                    InfoChip(
                        text = stringResource(R.string.relic_level_short, retention.relicLevel),
                        containerColor = Color(0xFF1B2740),
                        contentColor = Color(0xFFE0F8FF),
                        textScale = textScale
                    )
                }
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = Color(0xFF5BCBF0),
                    trackColor = Color(0xFF233149)
                )
                Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    InfoChip(
                        text = stringResource(
                            R.string.relic_pulse_bonus,
                            formatPercent((relicTapMultiplier(retention.relicLevel, config) - 1.0) * 100.0)
                        ),
                        containerColor = Color(0xFF17253D),
                        contentColor = Color(0xFFE0F8FF),
                        textScale = textScale
                    )
                    InfoChip(
                        text = stringResource(
                            R.string.relic_pulse_bonus_auto,
                            formatPercent((relicAutoMultiplier(retention.relicLevel, config) - 1.0) * 100.0)
                        ),
                        containerColor = Color(0xFF1C2840),
                        contentColor = Color(0xFFD8E7FF),
                        textScale = textScale
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeasonalAltarCard(
    retention: RetentionProgress,
    config: RetentionConfig,
    progress: Float,
    weekKey: String,
    claimableTiers: List<SeasonalAltarTierConfig>,
    compactNumbers: Boolean,
    textScale: Float,
    onClaimTier: (SeasonalAltarTierConfig) -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    var detailsExpanded by rememberSaveable("events_altar_details") { mutableStateOf(false) }
    val sweep by rememberInfiniteTransition(label = "altar_sweep")
        .animateFloat(
            initialValue = -220f,
            targetValue = 720f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "altar_sweep_value"
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFFFFB85E).copy(alpha = 0.42f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF25170F), Color(0xFF1A1225), Color(0xFF120C18))
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.06f), Color.Transparent),
                            start = Offset(sweep, 0f),
                            end = Offset(sweep + 180f, 460f)
                        )
                    )
            )
            Column(modifier = Modifier.padding(layoutProfile.cardPadding())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.sectionSpacing() + 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(scaledDp(76f))
                            .height(scaledDp(92f))
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        ResourceOrPlaceholderArt(
                            drawableName = "vavarda_season_altar",
                            placeholderTitle = stringResource(R.string.altar_title),
                            placeholderSubtitle = stringResource(R.string.altar_subtitle),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.altar_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(14f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                            verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                        ) {
                            InfoChip(
                                text = stringResource(R.string.altar_week_label, formatWeekKeyForUi(weekKey)),
                                containerColor = Color(0xFF3A2418),
                                contentColor = Color(0xFFFFE1BC),
                                textScale = textScale
                            )
                            InfoChip(
                                text = stringResource(R.string.altar_favor_label, retention.altarFavor),
                                containerColor = Color(0xFF31204A),
                                contentColor = Color(0xFFE8D8FF),
                                textScale = textScale
                            )
                        }
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                        ExpandableLoreText(
                            key = "altar_subtitle_${retention.altarFavor}",
                            text = stringResource(R.string.altar_subtitle),
                            textScale = textScale,
                            color = Color(0xFFFFE1BC),
                            collapsedMaxLines = 1,
                            fontSizeSp = 10.5f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing() + 2.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = Color(0xFFFFB85E),
                    trackColor = Color(0xFF4A2C1E)
                )
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                Text(
                    text = stringResource(R.string.altar_tiers_title),
                    color = Color(0xFFFFE1BC),
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(11f, textScale)
                )
                Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    config.altar.tiers.forEach { tier ->
                        val claimable = tier in claimableTiers
                        val claimed = tier.tier in retention.altarClaimedTiers
                        AltarTierHighlightCard(
                            tier = tier,
                            favor = retention.altarFavor,
                            claimed = claimed,
                            claimable = claimable,
                            compactNumbers = compactNumbers,
                            textScale = textScale
                        )
                    }
                }
                Spacer(modifier = Modifier.height(layoutProfile.minorSpacing() + 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    ClaimActionButton(
                        text = if (detailsExpanded) {
                            stringResource(R.string.action_hide)
                        } else {
                            stringResource(R.string.action_expand)
                        },
                        onClick = { detailsExpanded = !detailsExpanded },
                        enabled = true,
                        containerColor = Color(0xFF3A2418),
                        contentColor = Color(0xFFFFE1BC),
                        textScale = textScale,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        animateAttention = false,
                        showAccentIcon = false,
                        style = ActionButtonStyle.NEUTRAL
                    )
                }

                AnimatedVisibility(visible = detailsExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing() + 2.dp))
                        config.altar.tiers.forEach { tier ->
                            val claimable = tier in claimableTiers
                            val claimed = tier.tier in retention.altarClaimedTiers
                            val tierIndex = config.altar.tiers.indexOfFirst { it.tier == tier.tier }
                            val previousFavor = if (tierIndex > 0) config.altar.tiers[tierIndex - 1].requiredFavor else 0
                            val tierProgress = when {
                                claimed -> 1f
                                tier.requiredFavor <= previousFavor -> 1f
                                else -> (
                                    (retention.altarFavor - previousFavor).toFloat() /
                                        (tier.requiredFavor - previousFavor).toFloat()
                                ).coerceIn(0f, 1f)
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = layoutProfile.minorSpacing()),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x33261A12)),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        claimed -> Color(0x55B58A4A)
                                        claimable -> Color(0x88FFB85E)
                                        else -> Color(0x334A2C1E)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(layoutProfile.cardPadding() - 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            InfoChip(
                                                text = tierRoman(tier.tier),
                                                containerColor = if (claimable) Color(0xFFCF7A32) else Color(0xFF3A2418),
                                                contentColor = if (claimable) Color.White else Color(0xFFFFE1BC),
                                                textScale = textScale
                                            )
                                            Text(
                                                text = tier.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = scaledSp(13f, textScale)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 1.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.altar_tier_desc,
                                                tier.requiredFavor,
                                                formatNumber(tier.rewardEssence, compactNumbers),
                                                tier.rewardShards
                                            ),
                                            color = Color(0xFFFFE1BC),
                                            fontSize = scaledSp(11f, textScale)
                                        )
                                        Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                                        LinearProgressIndicator(
                                            progress = { tierProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(99.dp)),
                                            color = if (claimable) Color(0xFFFFB85E) else Color(0xFFB57C47),
                                            trackColor = Color(0xFF3A2418)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(layoutProfile.chipSpacing() + 2.dp))
                                    when {
                                        claimed -> InfoChip(
                                            text = stringResource(R.string.state_reward_claimed),
                                            containerColor = Color(0xFF3A2A1D),
                                            contentColor = Color(0xFFD8B47A),
                                            textScale = textScale
                                        )
                                        claimable -> ClaimActionButton(
                                            onClick = { onClaimTier(tier) },
                                            enabled = true,
                                            containerColor = Color(0xFFCF7A32),
                                            textScale = textScale,
                                            text = stringResource(R.string.action_claim),
                                            style = ActionButtonStyle.CLAIM
                                        )
                                        else -> InfoChip(
                                            text = stringResource(
                                                R.string.altar_tier_locked,
                                                (tier.requiredFavor - retention.altarFavor).coerceAtLeast(0)
                                            ),
                                            containerColor = Color(0xFF2A1C16),
                                            contentColor = Color(0xFFC29C6C),
                                            textScale = textScale
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AltarTierHighlightCard(
    tier: SeasonalAltarTierConfig,
    favor: Int,
    claimed: Boolean,
    claimable: Boolean,
    compactNumbers: Boolean,
    textScale: Float
) {
    val remaining = (tier.requiredFavor - favor).coerceAtLeast(0)
    val activeMotion = claimable || (!claimed && remaining <= 24)
    val borderColor = when {
        claimable -> Color(0xFFFFB85E)
        claimed -> Color(0xFFB58A4A)
        else -> Color(0x554A2C1E)
    }
    val containerColor = when {
        claimable -> Color(0x523D240B)
        claimed -> Color(0x38291B10)
        else -> Color(0x22170F14)
    }
    val pulseScale by rememberInfiniteTransition(label = "altar_tier_card_motion")
        .animateFloat(
            initialValue = 1f,
            targetValue = if (claimable) 1.04f else 1.018f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (claimable) 960 else 1700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "altar_tier_card_pulse"
        )
    val glowAlpha by rememberInfiniteTransition(label = "altar_tier_card_glow")
        .animateFloat(
            initialValue = if (claimable) 0.10f else 0.04f,
            targetValue = if (claimable) 0.22f else 0.10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (claimable) 1000 else 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "altar_tier_card_glow_alpha"
        )
    val sheenProgress by rememberInfiniteTransition(label = "altar_tier_card_sheen")
        .animateFloat(
            initialValue = -0.9f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (claimable) 1900 else 2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "altar_tier_card_sheen_progress"
        )

    Card(
        modifier = Modifier.graphicsLayer(
            scaleX = if (activeMotion) pulseScale else 1f,
            scaleY = if (activeMotion) pulseScale else 1f
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            if (activeMotion) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = borderColor.copy(alpha = glowAlpha),
                        radius = size.minDimension * if (claimable) 0.44f else 0.34f,
                        center = Offset(size.width * 0.82f, size.height * 0.18f)
                    )
                    val startX = size.width * sheenProgress - (size.width * 0.42f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (claimable) 0.15f else 0.07f),
                                Color.Transparent
                            ),
                            start = Offset(startX, 0f),
                            end = Offset(startX + size.width * 0.48f, size.height)
                        )
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = tierRoman(tier.tier),
                    color = if (claimable || claimed) Color(0xFFFFE1BC) else Color(0xFFD1B693),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = scaledSp(14f, textScale)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tier.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = scaledSp(11f, textScale),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.altar_tier_reward_short,
                        formatNumber(tier.rewardEssence, compactNumbers),
                        tier.rewardShards
                    ),
                    color = Color(0xFFFFE1BC),
                    fontSize = scaledSp(10f, textScale)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        claimed -> stringResource(R.string.state_reward_claimed)
                        claimable -> stringResource(R.string.altar_tier_highlight_ready)
                        else -> stringResource(R.string.altar_tier_highlight_locked, remaining)
                    },
                    color = if (claimable) Color(0xFFFFB85E) else Color(0xFFD4B58E),
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(10f, textScale)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventsRewardSummaryCard(
    claimableMissions: Int,
    returnStreakAvailable: Boolean,
    returnStreakPreviewDay: Int,
    cacheReadySeconds: Int,
    cacheRewardPreview: Long,
    altarClaimableTiers: List<SeasonalAltarTierConfig>,
    altarFavor: Int,
    altarConfig: SeasonalAltarConfig,
    readyRewardCount: Int,
    compactNumbers: Boolean,
    textScale: Float,
    onClaimAllReady: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val cardPadding = layoutProfile.cardPadding() - 4.dp
    val nextLockedTier = remember(altarFavor, altarClaimableTiers, altarConfig.tiers) {
        altarConfig.tiers
            .sortedBy { it.requiredFavor }
            .firstOrNull { tier -> altarFavor < tier.requiredFavor && tier !in altarClaimableTiers }
    }
    val summaryChips = buildList {
        add(
            Triple(
                if (claimableMissions > 0) {
                    stringResource(R.string.events_reward_chip_daily, claimableMissions)
                } else {
                    "${stringResource(R.string.events_reward_daily_title)} ${stringResource(R.string.events_reward_daily_idle)}"
                },
                if (claimableMissions > 0) Color(0xFF0D8B79) else Color(0xFF2A213F),
                Color.White
            )
        )
        add(
            Triple(
                if (returnStreakAvailable) {
                    "${stringResource(R.string.events_reward_return_title)} ${returnStreakPreviewDay}"
                } else {
                    "${stringResource(R.string.events_reward_return_title)} ${stringResource(R.string.events_reward_return_idle)}"
                },
                if (returnStreakAvailable) Color(0xFF6E46A5) else Color(0xFF2A213F),
                Color.White
            )
        )
        add(
            Triple(
                if (cacheReadySeconds <= 0) {
                    "${stringResource(R.string.events_reward_cache_title)} +${formatNumber(cacheRewardPreview, compactNumbers)}"
                } else {
                    "${stringResource(R.string.events_reward_cache_title)} ${formatShortDuration(cacheReadySeconds)}"
                },
                if (cacheReadySeconds <= 0) Color(0xFF8A6417) else Color(0xFF2A213F),
                Color.White
            )
        )
        add(
            Triple(
                if (altarClaimableTiers.isNotEmpty()) {
                    stringResource(R.string.events_reward_chip_altar, altarClaimableTiers.size)
                } else {
                    stringResource(
                        R.string.events_reward_altar_idle,
                        (nextLockedTier?.requiredFavor?.minus(altarFavor))?.coerceAtLeast(0) ?: 0
                    )
                },
                if (altarClaimableTiers.isNotEmpty()) Color(0xFF245D92) else Color(0xFF2A213F),
                Color.White
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xD9161B24)),
        border = BorderStroke(1.dp, Color(0x4FD7BC84))
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.events_rewards_summary_title),
                        color = Color(0xFFF5E8C6),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(11.5f, textScale)
                    )
                    Spacer(modifier = Modifier.height((layoutProfile.bottomBarLabelSpacing() - 1.dp).coerceAtLeast(2.dp)))
                    Text(
                        text = if (readyRewardCount > 0) {
                            stringResource(R.string.events_rewards_summary_ready_hint)
                        } else {
                            stringResource(R.string.events_rewards_summary_idle_hint)
                        },
                        color = if (readyRewardCount > 0) Color(0xFFD7E1EC) else Color(0xFFB7C2CF),
                        fontSize = scaledSp(9.5f, textScale),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (readyRewardCount > 0) {
                    Spacer(modifier = Modifier.width(layoutProfile.chipSpacing()))
                    InfoChip(
                        text = stringResource(R.string.home_rewards_ready_badge, readyRewardCount),
                        containerColor = Color(0xFF735730),
                        contentColor = Color(0xFFF5E8C6),
                        textScale = textScale
                    )
                }
            }
            if (readyRewardCount > 1) {
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                ClaimActionButton(
                    text = stringResource(R.string.action_claim_all),
                    onClick = onClaimAllReady,
                    enabled = true,
                    containerColor = Color(0xFFFFC857),
                    contentColor = Color(0xFF2A173D),
                    textScale = textScale,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                    fontSizeSp = 12.5f,
                    animateAttention = true,
                    showAccentIcon = true
                )
            }
            Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
            ) {
                summaryChips.forEach { (text, containerColor, contentColor) ->
                    InfoChip(
                        text = text,
                        containerColor = containerColor,
                        contentColor = contentColor,
                        textScale = textScale
                    )
                }
            }
        }
    }
}

@Composable
private fun EventRewardStatusTile(
    title: String,
    value: String,
    ready: Boolean,
    readyColor: Color,
    idleColor: Color,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (ready) readyColor.copy(alpha = 0.18f) else idleColor
        ),
        border = BorderStroke(
            1.dp,
            if (ready) readyColor.copy(alpha = 0.44f) else Color(0x3DD7C6FF)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = layoutProfile.cardPadding(), vertical = layoutProfile.cardPadding() - 2.dp)) {
            Text(
                text = title,
                color = Color(0xFFCDBEFF),
                fontSize = scaledSp(10f, textScale),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(12f, textScale),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventsHeroCard(
    boss: BossState,
    readyRewardCount: Int,
    compactNumbers: Boolean,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val heroPadding = layoutProfile.cardPadding() + 1.dp
    val accentColor = if (boss.isActive) Color(0xFFE07B63) else Color(0xFFD8BC7D)
    val glowPulse by rememberInfiniteTransition(label = "events_hero_glow")
        .animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (boss.isActive) 1800 else 3200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "events_hero_glow_value"
        )
    val sweepOffset by rememberInfiniteTransition(label = "events_hero_sweep")
        .animateFloat(
            initialValue = -280f,
            targetValue = 920f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "events_hero_sweep_value"
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.34f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = if (boss.isActive) {
                            listOf(Color(0xFF251816), Color(0xFF1A1F27), Color(0xFF0F131A))
                        } else {
                            listOf(Color(0xFF282114), Color(0xFF1A1F27), Color(0xFF0F131A))
                        }
                    )
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.95f)
            ) {
                drawCircle(
                    color = accentColor.copy(alpha = if (boss.isActive) 0.28f else 0.2f),
                    radius = size.minDimension * 0.42f * glowPulse,
                    center = Offset(size.width * 0.84f, size.height * 0.26f)
                )
                drawCircle(
                    color = Color(0x24D7BC84),
                    radius = size.minDimension * 0.24f,
                    center = Offset(size.width * 0.18f, size.height * 0.78f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            start = Offset(sweepOffset, 0f),
                            end = Offset(sweepOffset + 220f, 720f)
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(
                    horizontal = heroPadding,
                    vertical = heroPadding
                )
            ) {
                InfoChip(
                    text = stringResource(
                        if (boss.isActive) {
                            R.string.boss_status_chip_active
                        } else {
                            R.string.boss_status_chip_idle
                        }
                    ),
                    containerColor = accentColor.copy(alpha = 0.22f),
                    contentColor = Color.White,
                    textScale = textScale
                )
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                Text(
                    text = stringResource(
                        if (boss.isActive) {
                            R.string.events_hero_active_title
                        } else {
                            R.string.events_hero_idle_title
                        }
                    ),
                    color = Color(0xFFFFF4DF),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = scaledSp(22f, textScale),
                    fontFamily = MaterialTheme.typography.displayMedium.fontFamily
                )
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                ExpandableLoreText(
                    key = "events_hero_body_${boss.isActive}",
                    text = stringResource(
                        if (boss.isActive) {
                            R.string.events_hero_active_body
                        } else {
                            R.string.events_hero_idle_body
                        }
                    ),
                    textScale = textScale,
                    color = Color(0xFFD3DDE8),
                    collapsedMaxLines = 2,
                    toggleColor = Color(0xFFE2C688)
                )
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    InfoChip(
                        text = stringResource(
                            if (boss.isActive) {
                                R.string.events_hero_timer_active
                            } else {
                                R.string.events_hero_timer_idle
                            },
                            if (boss.isActive) boss.secondsLeft else boss.secondsUntilSpawn
                        ),
                        containerColor = Color(0xFF202833),
                        contentColor = Color(0xFFF4E8CD),
                        textScale = textScale
                    )
                    InfoChip(
                        text = if (boss.isActive) {
                            stringResource(
                                R.string.events_hero_hp,
                                formatNumber(boss.currentHp, compactNumbers)
                            )
                        } else {
                            if (readyRewardCount > 0) {
                                stringResource(R.string.events_hero_claims_ready, readyRewardCount)
                            } else {
                                stringResource(R.string.events_hero_claims_idle)
                            }
                        },
                        containerColor = if (boss.isActive) {
                            accentColor.copy(alpha = 0.18f)
                        } else {
                            Color(0xFF25303A)
                        },
                        contentColor = if (boss.isActive) {
                            Color(0xFFFFE7D2)
                        } else {
                            Color(0xFFE5EEF6)
                        },
                        textScale = textScale
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventsCycleSnapshotCard(
    boss: BossState,
    readyRewardCount: Int,
    claimableMissions: Int,
    returnStreakAvailable: Boolean,
    returnStreakPreviewDay: Int,
    cacheReadySeconds: Int,
    cacheRewardPreview: Long,
    altarClaimableTierCount: Int,
    compactNumbers: Boolean,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val readyValue = when {
        readyRewardCount > 1 -> localizedReadyRewardsSummaryText(
            claimableMissions = claimableMissions,
            returnStreakAvailable = returnStreakAvailable,
            cacheReadySeconds = cacheReadySeconds,
            altarClaimableTierCount = altarClaimableTierCount
        )
        readyRewardCount == 1 && cacheReadySeconds <= 0 -> stringResource(
            R.string.events_snapshot_ready_cache,
            formatNumber(cacheRewardPreview, compactNumbers)
        )
        readyRewardCount == 1 && returnStreakAvailable -> stringResource(
            R.string.events_snapshot_ready_return,
            returnStreakPreviewDay
        )
        readyRewardCount == 1 && claimableMissions > 0 -> stringResource(
            R.string.events_snapshot_ready_daily,
            claimableMissions
        )
        readyRewardCount == 1 && altarClaimableTierCount > 0 -> stringResource(
            R.string.events_snapshot_ready_altar,
            altarClaimableTierCount
        )
        else -> stringResource(R.string.events_snapshot_ready_empty)
    }
    val nextStepValue = when {
        cacheReadySeconds <= 0 -> stringResource(
            R.string.events_snapshot_next_cache,
            formatNumber(cacheRewardPreview, compactNumbers)
        )
        returnStreakAvailable -> stringResource(
            R.string.events_snapshot_next_return,
            returnStreakPreviewDay
        )
        claimableMissions > 0 -> stringResource(
            R.string.events_snapshot_next_daily,
            claimableMissions
        )
        else -> stringResource(R.string.events_snapshot_next_prepare)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC161D26)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(layoutProfile.cardPadding() - 1.dp)) {
            SectionLabel(
                title = stringResource(R.string.events_snapshot_title),
                subtitle = stringResource(R.string.events_snapshot_subtitle),
                textScale = textScale
            )
            Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing() - 2.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
            ) {
                EventRewardStatusTile(
                    title = stringResource(R.string.events_snapshot_ready_title),
                    value = readyValue,
                    ready = readyRewardCount > 0,
                    readyColor = Color(0xFFC99754),
                    idleColor = Color(0xFF1E242D),
                    textScale = textScale
                )
                EventRewardStatusTile(
                    title = stringResource(R.string.events_snapshot_next_title),
                    value = nextStepValue,
                    ready = cacheReadySeconds <= 0 || returnStreakAvailable || claimableMissions > 0,
                    readyColor = Color(0xFF8A6AD5),
                    idleColor = Color(0xFF1E242D),
                    textScale = textScale
                )
                EventRewardStatusTile(
                    title = stringResource(R.string.events_snapshot_boss_title),
                    value = stringResource(R.string.events_snapshot_boss_value, boss.secondsUntilSpawn),
                    ready = boss.secondsUntilSpawn <= 30,
                    readyColor = Color(0xFFE18268),
                    idleColor = Color(0xFF1E242D),
                    textScale = textScale
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BossEventCard(
    boss: BossState,
    narrativeLine: String,
    compactNumbers: Boolean,
    textScale: Float,
    bossRewardPreview: Long,
    bossHitPreview: Long,
    bossHitsToDefeat: Int?,
    bossImpactBursts: List<BossImpactBurst>,
    bossImpactTick: Int,
    bossDefeatTick: Int,
    onHitBoss: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val shakeOffset = remember { Animatable(0f) }
    val impactWave = remember { Animatable(0f) }
    val defeatFlash = remember { Animatable(0f) }
    val cardGlow by rememberInfiniteTransition(label = "boss_card_glow")
        .animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (boss.isActive) 1500 else 2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "boss_card_glow_value"
        )
    val statusColor = if (boss.isActive) Color(0xFFE18268) else Color(0xFFD8BC7E)
    val impactScale = 1f + impactWave.value * 0.018f + defeatFlash.value * 0.024f

    LaunchedEffect(bossImpactTick) {
        if (bossImpactTick == 0) return@LaunchedEffect
        impactWave.snapTo(1f)
        impactWave.animateTo(0f, animationSpec = tween(durationMillis = 520))
        shakeOffset.snapTo(0f)
        listOf(-16f, 12f, -9f, 5f, -2f, 0f).forEach { target ->
            shakeOffset.animateTo(target, animationSpec = tween(durationMillis = 36))
        }
    }

    LaunchedEffect(bossDefeatTick) {
        if (bossDefeatTick == 0) return@LaunchedEffect
        defeatFlash.snapTo(1f)
        defeatFlash.animateTo(0f, animationSpec = tween(durationMillis = 760))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                translationX = shakeOffset.value,
                scaleX = impactScale,
                scaleY = impactScale
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.32f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = if (boss.isActive) {
                        listOf(Color(0xFF241817), Color(0xFF1A1F27), Color(0xFF0F131A))
                    } else {
                        listOf(Color(0xFF221D16), Color(0xFF1A1F27), Color(0xFF0F131A))
                    }
                )
            )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = statusColor.copy(alpha = if (boss.isActive) 0.18f else 0.1f),
                    radius = size.minDimension * 0.34f * cardGlow,
                    center = Offset(size.width * 0.88f, size.height * 0.16f)
                )
                if (impactWave.value > 0f) {
                    drawCircle(
                        color = statusColor.copy(alpha = impactWave.value * 0.34f),
                        radius = size.minDimension * (0.16f + (1f - impactWave.value) * 0.18f),
                        center = Offset(size.width * 0.5f, size.height * 0.72f),
                        style = Stroke(width = 16f * impactWave.value.coerceAtLeast(0.16f))
                    )
                }
                if (defeatFlash.value > 0f) {
                    drawCircle(
                        color = Color(0xFFFFE29B).copy(alpha = defeatFlash.value * 0.32f),
                        radius = size.minDimension * (0.22f + (1f - defeatFlash.value) * 0.28f),
                        center = Offset(size.width * 0.5f, size.height * 0.68f)
                    )
                }
            }
            Column(modifier = Modifier.padding(layoutProfile.cardPadding())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.boss_title),
                        color = Color(0xFFFFF4DF),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(16f, textScale)
                    )
                    Card(
                        shape = RoundedCornerShape(999.dp),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.22f))
                    ) {
                        Text(
                            text = stringResource(
                                if (boss.isActive) {
                                    R.string.boss_status_chip_active
                                } else {
                                    R.string.boss_status_chip_idle
                                }
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(11f, textScale)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(scaledDp(182f))
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    ResourceOrPlaceholderArt(
                        drawableName = "vavarda_boss_rift_warden",
                        placeholderTitle = stringResource(R.string.boss_title),
                        placeholderSubtitle = narrativeLine,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x12000000),
                                        Color.Transparent,
                                        Color(0xC811151C)
                                    )
                                )
                            )
                    )
                    FlowRow(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                        verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                    ) {
                        if (boss.isActive) {
                            InfoChip(
                                text = stringResource(R.string.boss_time_short, boss.secondsLeft),
                                containerColor = Color(0xBF30241D),
                                contentColor = Color(0xFFFFE6C7),
                                textScale = textScale
                            )
                            InfoChip(
                                text = stringResource(
                                    R.string.boss_reward_chip,
                                    formatNumber(bossRewardPreview, compactNumbers)
                                ),
                                containerColor = Color(0xB4362F21),
                                contentColor = Color(0xFFFFE8B5),
                                textScale = textScale
                            )
                        } else {
                            InfoChip(
                                text = stringResource(R.string.boss_spawn_short, boss.secondsUntilSpawn),
                                containerColor = Color(0xBF1D2631),
                                contentColor = Color(0xFFF4E8CD),
                                textScale = textScale
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (boss.isActive) {
                                stringResource(R.string.boss_overlay_title_active)
                            } else {
                                stringResource(R.string.boss_status_chip_idle)
                            },
                            color = Color(0xFFFFE6B6),
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(10.4f, textScale)
                        )
                        Text(
                            text = narrativeLine,
                            color = Color(0xFFF2F5F9),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = scaledSp(11.4f, textScale),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))

                if (boss.isActive) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                        verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                    ) {
                        InfoChip(
                            text = stringResource(R.string.boss_time_short, boss.secondsLeft),
                            containerColor = Color(0xFF3C3126),
                            contentColor = Color(0xFFFFE6C7),
                            textScale = textScale
                        )
                        InfoChip(
                            text = stringResource(
                                R.string.boss_hp_short,
                                formatNumber(boss.currentHp, compactNumbers),
                                formatNumber(boss.maxHp, compactNumbers)
                            ),
                            containerColor = Color(0xFF1F2932),
                            contentColor = Color(0xFFF7FAFD),
                            textScale = textScale
                        )
                    }
                    Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                    ) {
                        BossPressureTile(
                            title = stringResource(R.string.boss_pressure_hit_title),
                            value = formatNumber(bossHitPreview, compactNumbers),
                            accentColor = Color(0xFFCB9873),
                            textScale = textScale,
                            modifier = Modifier.weight(1f)
                        )
                        BossPressureTile(
                            title = stringResource(R.string.boss_pressure_reward_title),
                            value = "+${formatNumber(bossRewardPreview, compactNumbers)}",
                            accentColor = Color(0xFFD8BC7E),
                            textScale = textScale,
                            modifier = Modifier.weight(1f)
                        )
                        BossPressureTile(
                            title = stringResource(R.string.boss_pressure_finish_title),
                            value = stringResource(
                                R.string.boss_pressure_finish_value,
                                bossHitsToDefeat ?: 1
                            ),
                            accentColor = Color(0xFFE8DCC2),
                            textScale = textScale,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                    LinearProgressIndicator(
                        progress = {
                            if (boss.maxHp <= 0L) 0f else {
                                (boss.currentHp.toFloat() / boss.maxHp.toFloat()).coerceIn(0f, 1f)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = statusColor,
                        trackColor = Color(0xFF28313A)
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC181E26)),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.18f))
                    ) {
                        Text(
                            text = stringResource(
                                R.string.boss_active_callout,
                                formatNumber(bossRewardPreview, compactNumbers),
                                bossHitsToDefeat ?: 1
                            ),
                            modifier = Modifier.padding(
                                horizontal = layoutProfile.cardPadding() - 2.dp,
                                vertical = layoutProfile.cardPadding() - 5.dp
                            ),
                            color = Color(0xFFFFE0C7),
                            fontSize = scaledSp(11f, textScale),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                    ClaimActionButton(
                        text = stringResource(
                            R.string.action_hit_boss_preview,
                            formatNumber(bossHitPreview, compactNumbers)
                        ),
                        onClick = onHitBoss,
                        enabled = true,
                        containerColor = Color(0xFFA3623F),
                        textScale = textScale,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(layoutProfile.bossButtonHeight())
                            .testTag("boss_hit_button"),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        fontSizeSp = 14f,
                        animateAttention = boss.isActive,
                        showAccentIcon = true,
                        style = ActionButtonStyle.HIT
                    )
                } else {
                    InfoChip(
                        text = stringResource(R.string.boss_spawn_short, boss.secondsUntilSpawn),
                        containerColor = Color(0xFF202833),
                        contentColor = Color(0xFFF4E8CD),
                        textScale = textScale
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                    ExpandableLoreText(
                        key = "boss_idle_callout_${boss.secondsUntilSpawn}",
                        text = stringResource(R.string.boss_idle_callout),
                        textScale = textScale,
                        color = Color(0xFFD3DDE8),
                        collapsedMaxLines = 1,
                        fontSizeSp = 11f,
                        toggleColor = Color(0xFFE2C688)
                    )
                }
            }
            bossImpactBursts.forEach { burst ->
                FloatingBossDamageBurst(
                    burst = burst,
                    compactNumbers = compactNumbers,
                    textScale = textScale
                )
            }
        }
    }
}

@Composable
private fun BossPressureTile(
    title: String,
    value: String,
    accentColor: Color,
    textScale: Float,
    modifier: Modifier = Modifier
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.13f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.26f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = layoutProfile.cardPadding() - 1.dp,
                vertical = layoutProfile.cardPadding() - 3.dp
            )
        ) {
            Text(
                text = title,
                color = Color(0xFFC9D3DE),
                fontSize = scaledSp(9.5f, textScale),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
            Text(
                text = value,
                color = Color(0xFFFFF3DE),
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(12f, textScale),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BoxScope.FloatingBossDamageBurst(
    burst: BossImpactBurst,
    compactNumbers: Boolean,
    textScale: Float
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(burst.id) {
        started = true
    }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = if (burst.finishingBlow) 900 else 760),
        label = "boss_damage_burst_${burst.id}"
    )
    val density = LocalDensity.current
    val bossBurstOffsetXPx = with(density) { burst.horizontalDrift.dp.roundToPx() }
    val bossBurstOffsetYPx = with(density) {
        (burst.verticalLift + 54f - (90f * progress)).dp.roundToPx()
    }

    val burstText = if (burst.finishingBlow && burst.rewardEssence != null) {
        "+${formatNumber(burst.rewardEssence, compactNumbers)}"
    } else {
        "-${formatNumber(burst.damage, compactNumbers)}"
    }
    Text(
        text = burstText,
        color = if (burst.finishingBlow) Color(0xFFFFEDB0) else burst.accentColor,
        fontWeight = FontWeight.ExtraBold,
        fontSize = scaledSp(if (burst.finishingBlow) 22f else 18f, textScale),
        modifier = Modifier
            .align(Alignment.Center)
            .offset { IntOffset(bossBurstOffsetXPx, bossBurstOffsetYPx) }
            .graphicsLayer(
                scaleX = 0.92f + (0.18f * (1f - progress)),
                scaleY = 0.92f + (0.18f * (1f - progress))
            )
            .alpha(1f - (progress * 0.96f))
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MilestoneStatusCard(
    level: Int,
    rewards: List<RewardConfig>,
    claimedRewardLevels: Set<Int>,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val nextReward = rewards.firstOrNull { reward -> reward.level > level }
    val collectedCount = rewards.count { reward -> reward.level in claimedRewardLevels }
    val nextRewardTitle = nextReward?.let { localizedArtTitle(it.level, it.title) }
    val nextRewardDescription = nextReward?.description
    val nextRewardEffect = nextReward?.let { localizedRewardEffect(it) }
    val nextRewardText = if (nextReward != null) {
        stringResource(R.string.state_next_reward, nextReward.level)
    } else {
        stringResource(R.string.state_all_rewards_collected)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0x4FD7BC84))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF171C24), Color(0xFF11161D))
                )
            )
        ) {
            Column(modifier = Modifier.padding(layoutProfile.cardPadding())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.milestone_panel_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(14f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() - 1.dp))
                        Text(
                            text = nextRewardText,
                            color = Color(0xFFD3DDE8),
                            fontSize = scaledSp(11f, textScale),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    InfoChip(
                        text = stringResource(R.string.growth_card_long_hint),
                        containerColor = Color(0xFF262E37),
                        contentColor = Color(0xFFFFE8BE),
                        textScale = textScale
                    )
                }
                if (nextRewardTitle != null && nextRewardDescription != null && nextRewardEffect != null) {
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                    Text(
                        text = nextRewardTitle,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = scaledSp(13f, textScale)
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                    ExpandableLoreText(
                        key = "growth_next_reward_${nextReward.level}",
                        text = nextRewardDescription,
                        textScale = textScale,
                        color = Color(0xFFD3DDE8),
                        collapsedMaxLines = 1,
                        fontSizeSp = 10.5f,
                        toggleColor = Color(0xFFE2C688)
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() - 1.dp))
                    Text(
                        text = nextRewardEffect,
                        color = Color(0xFFFFE8A8),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(11f, textScale)
                    )
                }
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.bottomBarLabelSpacing() + 2.dp),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.bottomBarLabelSpacing() + 2.dp)
                ) {
                    InfoChip(
                        text = stringResource(R.string.state_rewards_collected, collectedCount, rewards.size),
                        containerColor = Color(0xFF242C35),
                        contentColor = Color.White,
                        textScale = textScale
                    )
                    nextReward?.let {
                        InfoChip(
                            text = stringResource(R.string.state_next_reward, it.level),
                            containerColor = Color(0xFF735730),
                            contentColor = Color(0xFFFFE8BE),
                            textScale = textScale
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardOverlay(
    reward: RewardConfig,
    narrative: NarrativeEntry?,
    textScale: Float,
    onClaim: () -> Unit
) {
    var shown by remember { mutableStateOf(false) }
    val layoutProfile = rememberScreenLayoutMetrics()
    LaunchedEffect(Unit) {
        shown = true
    }
    val overlayAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "reward_overlay_alpha"
    )
    val overlayScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.94f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "reward_overlay_scale"
    )
    val rewardTitle = narrative?.title ?: localizedArtTitle(reward.level, reward.title)
    val rewardBody = narrative?.body ?: reward.description
    val rewardEffect = localizedRewardEffect(reward)
    val rewardBodyKey = "reward_overlay_${reward.level}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f * overlayAlpha))
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = layoutProfile.screenOverlayPadding())
                .fillMaxWidth()
                .widthIn(max = layoutProfile.modalMaxWidth())
                .graphicsLayer(
                    scaleX = overlayScale,
                    scaleY = overlayScale
                )
                .alpha(overlayAlpha),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171C24))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(layoutProfile.cardPadding() + 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.milestone_overlay_title),
                    color = Color(0xFFFFEAC4),
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(18f, textScale),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                Text(
                    text = rewardTitle,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = scaledSp(16f, textScale),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                ExpandableLoreText(
                    key = rewardBodyKey,
                    text = rewardBody,
                    textScale = textScale,
                    color = Color(0xFFD3DDE8),
                    collapsedMaxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    toggleColor = Color(0xFFE2C688),
                    fontSizeSp = 13f,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                Text(
                    text = rewardEffect,
                    color = Color(0xFFFFE8A8),
                    fontWeight = FontWeight.Bold,
                    fontSize = scaledSp(14f, textScale),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing() + 4.dp))
                ClaimActionButton(
                    text = stringResource(R.string.reward_button),
                    onClick = onClaim,
                    enabled = true,
                    containerColor = Color(0xFF84633B),
                    textScale = textScale,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    fontSizeSp = 13f,
                    animateAttention = true,
                    showAccentIcon = true
                )
            }
        }
    }
}

@Composable
private fun EpicMomentOverlay(
    state: EpicMomentOverlayState,
    textScale: Float,
    onDismiss: () -> Unit
) {
    var shown by remember { mutableStateOf(false) }
    val layoutProfile = rememberScreenLayoutMetrics()
    LaunchedEffect(Unit) {
        shown = true
    }
    val overlayAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "epic_overlay_alpha"
    )
    val overlayScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.92f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "epic_overlay_scale"
    )
    val glowPulse by rememberInfiniteTransition(label = "epic_overlay_glow")
        .animateFloat(
            initialValue = 0.94f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "epic_overlay_glow_value"
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f * overlayAlpha))
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = layoutProfile.screenOverlayPadding())
                .fillMaxWidth()
                .widthIn(max = layoutProfile.modalMaxWidth())
                .graphicsLayer(
                    scaleX = overlayScale,
                    scaleY = overlayScale
                )
                .alpha(overlayAlpha),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, state.accentColor.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            state.accentColor.copy(alpha = 0.14f),
                            Color(0xFF1A1027),
                            Color(0xFF120C1C)
                        )
                    )
                )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = state.accentColor.copy(alpha = 0.16f),
                        radius = size.minDimension * 0.30f * glowPulse,
                        center = Offset(size.width * 0.84f, size.height * 0.2f)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(layoutProfile.cardPadding() + 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.epic_overlay_title),
                        color = Color(0xFFF8EAC9),
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(18f, textScale),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp + layoutProfile.bottomBarLabelSpacing())
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        ResourceOrPlaceholderArt(
                            drawableName = state.drawableName,
                            placeholderTitle = state.title,
                            placeholderSubtitle = state.body,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing() + 4.dp))
                    Text(
                        text = state.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = scaledSp(19f, textScale),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 4.dp))
                    Text(
                        text = state.body,
                        color = Color(0xFFE7D9FF),
                        fontSize = scaledSp(13f, textScale),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                    Text(
                        text = state.effect,
                        color = state.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledSp(15f, textScale),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing() + 6.dp))
                    ClaimActionButton(
                        text = stringResource(R.string.action_ok),
                        onClick = onDismiss,
                        enabled = true,
                        containerColor = Color(0xFF84633B),
                        textScale = textScale,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        fontSizeSp = 13f,
                        animateAttention = true,
                        showAccentIcon = true
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideOverlay(
    textScale: Float,
    onDismiss: () -> Unit
) {
    var shown by remember { mutableStateOf(false) }
    val layoutProfile = rememberScreenLayoutMetrics()
    LaunchedEffect(Unit) {
        shown = true
    }
    val overlayAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "guide_overlay_alpha"
    )
    val overlayScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.94f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "guide_overlay_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.76f * overlayAlpha))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.72f)
                .padding(
                    horizontal = layoutProfile.screenOverlayPadding(),
                    vertical = layoutProfile.screenOverlayPadding()
                )
                .fillMaxWidth()
                .widthIn(max = layoutProfile.sheetMaxWidth())
                .graphicsLayer(
                    scaleX = overlayScale,
                    scaleY = overlayScale
                )
                .alpha(overlayAlpha)
                .clickable(onClick = {}),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF201438))
        ) {
            Column(
                modifier = Modifier
                    .padding(layoutProfile.cardPadding() + 6.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SheetHandle()
                Text(
                    text = stringResource(R.string.guide_title),
                    color = Color(0xFFF4E9FF),
                    fontSize = scaledSp(21f, textScale),
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily
                )
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                Text(
                    text = stringResource(R.string.guide_subtitle),
                    color = Color(0xFFCEB9F6),
                    fontSize = scaledSp(12f, textScale)
                )
                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))

                GuideStepText(text = stringResource(R.string.guide_step_1), textScale = textScale)
                GuideStepText(text = stringResource(R.string.guide_step_2), textScale = textScale)
                GuideStepText(text = stringResource(R.string.guide_step_3), textScale = textScale)
                GuideStepText(text = stringResource(R.string.guide_step_4), textScale = textScale)

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))

                Text(
                    text = stringResource(R.string.guide_return_hint),
                    color = Color(0xFFE9D8FF),
                    fontSize = scaledSp(12f, textScale)
                )

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing() + 4.dp))

                ClaimActionButton(
                    text = stringResource(R.string.guide_start_button),
                    onClick = onDismiss,
                    enabled = true,
                    containerColor = Color(0xFF7A49D1),
                    textScale = textScale,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    fontSizeSp = 13f,
                    animateAttention = false,
                    showAccentIcon = true
                )
            }
        }
    }
}

@Composable
private fun GuideStepText(
    text: String,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    Text(
        text = text,
        color = Color(0xFFEBDFFF),
        fontSize = scaledSp(13f, textScale),
        modifier = Modifier.padding(bottom = layoutProfile.minorSpacing())
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsOverlay(
    settings: GameSettings,
    onDismiss: () -> Unit,
    onSettingsChange: (GameSettings) -> Unit,
    onUiFeedback: (
        soundEnabledOverride: Boolean,
        vibrationEnabledOverride: Boolean,
        hapticFeedback: Int
    ) -> Unit
) {
    var shown by remember { mutableStateOf(false) }
    val layoutProfile = rememberScreenLayoutMetrics()
    LaunchedEffect(Unit) {
        shown = true
    }
    val overlayAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "settings_overlay_alpha"
    )
    val overlayScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.95f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "settings_overlay_scale"
    )
    fun emitSettingsInteractionFeedback(
        nextSoundEnabled: Boolean = settings.soundEnabled,
        nextVibrationEnabled: Boolean = settings.vibrationEnabled,
        hapticFeedback: Int = HapticFeedbackConstants.CONTEXT_CLICK
    ) {
        onUiFeedback(
            nextSoundEnabled,
            nextVibrationEnabled,
            hapticFeedback
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.64f * overlayAlpha))
            .clickable {
                emitSettingsInteractionFeedback()
                onDismiss()
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.80f)
                .padding(
                    horizontal = layoutProfile.screenOverlayPadding(),
                    vertical = layoutProfile.screenOverlayPadding()
                )
                .fillMaxWidth()
                .widthIn(max = layoutProfile.sheetMaxWidth())
                .graphicsLayer(
                    scaleX = overlayScale,
                    scaleY = overlayScale
                )
                .alpha(overlayAlpha)
                .clickable(onClick = {})
                .testTag("settings_overlay"),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151A23)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF171C26),
                            Color(0xFF131821),
                            Color(0xFF0E1218)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(layoutProfile.cardPadding() + 6.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SheetHandle()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_title),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = scaledSp(20f, settings.textScale)
                            )
                            Text(
                                text = stringResource(R.string.settings_subtitle),
                                color = Color(0xFFD0D7E4),
                                fontSize = scaledSp(11f, settings.textScale)
                            )
                        }
                        Spacer(modifier = Modifier.width(layoutProfile.chipSpacing()))
                        ClaimActionButton(
                            text = stringResource(R.string.action_close),
                            onClick = {
                                emitSettingsInteractionFeedback()
                                onDismiss()
                            },
                            enabled = true,
                            containerColor = Color(0xFF2E3847),
                            textScale = settings.textScale,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 7.dp),
                            animateAttention = false,
                            showAccentIcon = false
                        )
                    }

                    Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing() + 4.dp))

                    ProfileSettingsSection(
                        settings = settings,
                        onSettingsChange = onSettingsChange,
                        onUiFeedback = { soundEnabled, vibrationEnabled, hapticFeedback ->
                            emitSettingsInteractionFeedback(
                                nextSoundEnabled = soundEnabled,
                                nextVibrationEnabled = vibrationEnabled,
                                hapticFeedback = hapticFeedback
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))

                    SectionLabel(
                        title = stringResource(R.string.settings_comfort_section_title),
                        subtitle = stringResource(R.string.settings_comfort_section_subtitle),
                        textScale = settings.textScale
                    )

                    Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))

                    SettingRow(
                        title = stringResource(R.string.settings_audio),
                        checked = settings.soundEnabled,
                        onCheckedChange = {
                            emitSettingsInteractionFeedback(
                                nextSoundEnabled = it,
                                nextVibrationEnabled = settings.vibrationEnabled
                            )
                            onSettingsChange(settings.copy(soundEnabled = it))
                        },
                        textScale = settings.textScale
                    )
                    SettingRow(
                        title = stringResource(R.string.settings_vibration),
                        checked = settings.vibrationEnabled,
                        onCheckedChange = {
                            emitSettingsInteractionFeedback(
                                nextSoundEnabled = settings.soundEnabled,
                                nextVibrationEnabled = it
                            )
                            onSettingsChange(settings.copy(vibrationEnabled = it))
                        },
                        textScale = settings.textScale
                    )
                    SettingRow(
                        title = stringResource(R.string.settings_offline_income),
                        checked = settings.offlineIncomeEnabled,
                        onCheckedChange = {
                            emitSettingsInteractionFeedback()
                            onSettingsChange(settings.copy(offlineIncomeEnabled = it))
                        },
                        textScale = settings.textScale
                    )
                    SettingRow(
                        title = stringResource(R.string.settings_hints),
                        checked = settings.showFtueHints,
                        onCheckedChange = {
                            emitSettingsInteractionFeedback()
                            onSettingsChange(settings.copy(showFtueHints = it))
                        },
                        textScale = settings.textScale
                    )
                    SettingRow(
                        title = stringResource(R.string.settings_compact_numbers),
                        checked = settings.compactNumbers,
                        onCheckedChange = {
                            emitSettingsInteractionFeedback()
                            onSettingsChange(settings.copy(compactNumbers = it))
                        },
                        textScale = settings.textScale
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionLabel(
                        title = stringResource(R.string.settings_appearance_section_title),
                        subtitle = stringResource(R.string.settings_appearance_section_subtitle),
                        textScale = settings.textScale
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.minorSpacing()))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                        verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                    ) {
                        TextScaleOptionButton(
                            label = stringResource(R.string.settings_text_small),
                            selected = settings.textScale <= 0.92f,
                            textScale = settings.textScale,
                            onClick = {
                                if (settings.textScale > 0.92f) {
                                    emitSettingsInteractionFeedback()
                                    onSettingsChange(settings.copy(textScale = 0.9f))
                                }
                            }
                        )
                        TextScaleOptionButton(
                            label = stringResource(R.string.settings_text_medium),
                            selected = settings.textScale > 0.92f && settings.textScale < 1.1f,
                            textScale = settings.textScale,
                            onClick = {
                                if (!(settings.textScale > 0.92f && settings.textScale < 1.1f)) {
                                    emitSettingsInteractionFeedback()
                                    onSettingsChange(settings.copy(textScale = 1.0f))
                                }
                            }
                        )
                        TextScaleOptionButton(
                            label = stringResource(R.string.settings_text_large),
                            selected = settings.textScale >= 1.1f,
                            textScale = settings.textScale,
                            onClick = {
                                if (settings.textScale < 1.1f) {
                                    emitSettingsInteractionFeedback()
                                    onSettingsChange(settings.copy(textScale = 1.15f))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileSettingsSection(
    settings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit,
    onUiFeedback: (
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        hapticFeedback: Int
    ) -> Unit
) {
    val context = LocalContext.current
    val layoutProfile = rememberScreenLayoutMetrics()
    val canTakePhoto = remember(context) {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) &&
            captureIntent.resolveActivity(context.packageManager) != null
    }
    val defaultProfileName = stringResource(R.string.profile_name_default)
    val displayName = remember(settings.profileName, defaultProfileName) {
        resolvedProfileName(settings.profileName, defaultProfileName)
    }
    var profileNameDraft by rememberSaveable { mutableStateOf(settings.profileName) }
    var pendingCameraCapturePath by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(settings.profileName) {
        if (profileNameDraft != settings.profileName) {
            profileNameDraft = settings.profileName
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        persistProfilePhotoPermission(context, uri)
        if (settings.profileImageUri != uri.toString()) {
            clearStoredProfilePhoto(context, settings.profileImageUri)
        }
        onSettingsChange(settings.copy(profileImageUri = uri.toString()))
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val finalizedUri = finalizeProfileCameraCapture(context, pendingCameraCapturePath)
            if (finalizedUri != null) {
                clearStoredProfilePhoto(context, settings.profileImageUri)
                onSettingsChange(settings.copy(profileImageUri = finalizedUri))
            }
        } else {
            discardProfileCameraCapture(pendingCameraCapturePath)
        }
        pendingCameraCapturePath = ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC171D27)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xF11A202A),
                        Color(0xE5161B24),
                        Color(0xDD11161D)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(layoutProfile.cardPadding())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.sectionSpacing())
                ) {
                    ProfileAvatar(
                        imageUri = settings.profileImageUri,
                        displayName = displayName,
                        textScale = settings.textScale,
                        avatarSize = 84.dp,
                        initialFontSizeSp = 28f
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                            verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                        ) {
                            Text(
                                text = stringResource(R.string.profile_section_title),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = scaledSp(15f, settings.textScale)
                            )
                            InfoChip(
                                text = stringResource(R.string.profile_badge_local),
                                containerColor = Color(0xFF283342),
                                contentColor = Color(0xFFFFE5B2),
                                textScale = settings.textScale
                            )
                        }
                        Text(
                            text = displayName,
                            color = Color(0xFFFFE2B0),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = scaledSp(18f, settings.textScale),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.profile_section_subtitle),
                            color = Color(0xFFD0D7E4),
                            fontSize = scaledSp(11.2f, settings.textScale),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))

                OutlinedTextField(
                    value = profileNameDraft,
                    onValueChange = { rawName ->
                        val normalized = normalizeProfileName(rawName)
                        profileNameDraft = normalized
                        onSettingsChange(settings.copy(profileName = normalized))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_field"),
                    singleLine = true,
                    label = {
                        Text(
                            text = stringResource(R.string.profile_name_label),
                            fontSize = scaledSp(11f, settings.textScale)
                        )
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.profile_name_placeholder),
                            fontSize = scaledSp(11f, settings.textScale)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontSize = scaledSp(13f, settings.textScale)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F141B),
                        unfocusedContainerColor = Color(0xFF0F141B),
                        focusedBorderColor = Color(0xFFB88A4B),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        cursorColor = Color(0xFFFFD28E),
                        focusedLabelColor = Color(0xFFFFE3B4),
                        unfocusedLabelColor = Color(0xFFC9D2DF),
                        focusedPlaceholderColor = Color(0xFF8D98A8),
                        unfocusedPlaceholderColor = Color(0xFF8D98A8)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    if (canTakePhoto) {
                        ClaimActionButton(
                            text = stringResource(R.string.profile_action_take_photo),
                            onClick = {
                                onUiFeedback(
                                    settings.soundEnabled,
                                    settings.vibrationEnabled,
                                    HapticFeedbackConstants.CONTEXT_CLICK
                                )
                                val pendingCapture = prepareProfileCameraCapture(context) ?: return@ClaimActionButton
                                pendingCameraCapturePath = pendingCapture.tempFilePath
                                cameraLauncher.launch(pendingCapture.outputUri)
                            },
                            enabled = true,
                            containerColor = Color(0xFF2F5E6A),
                            textScale = settings.textScale,
                            modifier = Modifier.testTag("profile_take_photo_button"),
                            shape = RoundedCornerShape(13.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            animateAttention = false,
                            showAccentIcon = false,
                            style = ActionButtonStyle.NEUTRAL
                        )
                    }

                    ClaimActionButton(
                        text = stringResource(
                            if (settings.profileImageUri.isBlank()) {
                                R.string.profile_action_add_photo
                            } else {
                                R.string.profile_action_change_photo
                            }
                        ),
                        onClick = {
                            onUiFeedback(
                                settings.soundEnabled,
                                settings.vibrationEnabled,
                                HapticFeedbackConstants.CONTEXT_CLICK
                            )
                            photoPicker.launch(arrayOf("image/*"))
                        },
                        enabled = true,
                        containerColor = Color(0xFF8D653C),
                        textScale = settings.textScale,
                        modifier = Modifier.testTag("profile_pick_photo_button"),
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        animateAttention = false,
                        showAccentIcon = false,
                        style = ActionButtonStyle.NEUTRAL
                    )

                    if (settings.profileImageUri.isNotBlank()) {
                        ClaimActionButton(
                            text = stringResource(R.string.profile_action_remove_photo),
                            onClick = {
                                onUiFeedback(
                                    settings.soundEnabled,
                                    settings.vibrationEnabled,
                                    HapticFeedbackConstants.CONTEXT_CLICK
                                )
                                clearStoredProfilePhoto(context, settings.profileImageUri)
                                onSettingsChange(settings.copy(profileImageUri = ""))
                            },
                            enabled = true,
                            containerColor = Color(0xFF333A48),
                            textScale = settings.textScale,
                            modifier = Modifier.testTag("profile_remove_photo_button"),
                            shape = RoundedCornerShape(13.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            animateAttention = false,
                            showAccentIcon = false,
                            style = ActionButtonStyle.NEUTRAL
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(
                        if (settings.profileImageUri.isBlank()) {
                            R.string.profile_photo_hint_empty
                        } else {
                            R.string.profile_photo_hint_selected
                        }
                    ),
                    color = Color(0xFFB7C0CF),
                    fontSize = scaledSp(10.5f, settings.textScale)
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    imageUri: String,
    displayName: String,
    textScale: Float,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 72.dp,
    initialFontSizeSp: Float = 24f,
) {
    val imageBitmap = rememberProfileImageBitmap(imageUri)
    val initial = remember(displayName) {
        displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }

    Box(
        modifier = modifier
            .size(avatarSize)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB88A4B), Color(0xFF2C3541))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = stringResource(
                    R.string.profile_avatar_content_description,
                    displayName
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = scaledSp(initialFontSizeSp, textScale)
            )
        }
    }
}

private fun persistProfilePhotoPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0x80D8C49B))
        )
    }
}

@Composable
private fun TextScaleOptionButton(
    label: String,
    selected: Boolean,
    textScale: Float,
    onClick: () -> Unit
) {
    ClaimActionButton(
        text = label,
        onClick = onClick,
        enabled = true,
        containerColor = if (selected) Color(0xFF8D653C) else Color(0xFF262D39),
        contentColor = if (selected) Color(0xFFFFF4DD) else Color(0xFFD7DEE8),
        textScale = textScale,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        animateAttention = false,
        showAccentIcon = false
    )
}

@Composable
private fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textScale: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC161D27)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color(0xFFF2F4F8),
                fontSize = scaledSp(13f, textScale)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFFE4B0),
                    checkedTrackColor = Color(0xFF8A6237),
                    uncheckedThumbColor = Color(0xFFE5E8EE),
                    uncheckedTrackColor = Color(0xFF404A59),
                    uncheckedBorderColor = Color.Transparent,
                    checkedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FactionPanel(
    level: Int,
    path: AlignmentPath,
    bonuses: Map<AlignmentPath, FactionBonusConfig>,
    textScale: Float,
    onPathChange: (AlignmentPath) -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val canSwitchPath = level >= 10
    val shadowBonus = bonuses[AlignmentPath.SHADOW] ?: bonuses.values.first()
    val lightBonus = bonuses[AlignmentPath.LIGHT] ?: shadowBonus
    val alternatePath = if (path == AlignmentPath.SHADOW) AlignmentPath.LIGHT else AlignmentPath.SHADOW
    val activeBonus = if (path == AlignmentPath.SHADOW) shadowBonus else lightBonus
    val alternateBonus = if (alternatePath == AlignmentPath.SHADOW) shadowBonus else lightBonus

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(layoutProfile.sectionSpacing())
    ) {
        PathHeroCard(
            level = level,
            path = path,
            unlocked = true,
            bonus = activeBonus,
            textScale = textScale
        )

        SectionLabel(
            title = stringResource(R.string.faction_choice_section_title),
            subtitle = stringResource(
                if (canSwitchPath) {
                    R.string.faction_choice_section_subtitle
                } else {
                    R.string.faction_choice_locked_subtitle
                }
            ),
            textScale = textScale
        )

        FactionStoryCard(
            factionPath = alternatePath,
            selected = false,
            canSwitchPath = canSwitchPath,
            bonus = alternateBonus,
            textScale = textScale,
            onSelect = { onPathChange(alternatePath) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PathHeroCard(
    level: Int,
    path: AlignmentPath,
    unlocked: Boolean,
    bonus: FactionBonusConfig,
    textScale: Float
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val accentColor = if (unlocked) factionAccentColor(path) else Color(0xFF9A8667)
    val heroPulse by rememberInfiniteTransition(label = "path_hero_pulse_${path.name.lowercase(Locale.ROOT)}")
        .animateFloat(
            initialValue = 1.02f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "path_hero_pulse_value_${path.name.lowercase(Locale.ROOT)}"
        )
    val heroDrift by rememberInfiniteTransition(label = "path_hero_drift_${path.name.lowercase(Locale.ROOT)}")
        .animateFloat(
            initialValue = -36f,
            targetValue = 36f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "path_hero_drift_value_${path.name.lowercase(Locale.ROOT)}"
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF181D24), Color(0xFF11161D))
                    )
                )
        ) {
            ResourceOrPlaceholderArt(
                drawableName = factionImageName(path),
                placeholderTitle = localizedFactionName(path),
                placeholderSubtitle = localizedFactionLoreCaption(path),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaledDp(164f))
                    .graphicsLayer(
                        scaleX = heroPulse,
                        scaleY = heroPulse,
                        alpha = if (unlocked) 0.14f else 0.08f,
                        translationX = heroDrift
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = if (unlocked) 0.22f else 0.12f), Color.Transparent),
                            center = Offset(360f + heroDrift, 140f),
                            radius = 620f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (unlocked) 0.12f else 0.08f),
                                Color(0xEA171C23),
                                Color(0xFF10141B)
                            )
                        )
                    )
            )
            Column(modifier = Modifier.padding(layoutProfile.cardPadding() + 1.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                    verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    InfoChip(
                        text = stringResource(R.string.label_level_short, level),
                        containerColor = Color(0xFF222A33),
                        contentColor = Color(0xFFF4E9D1),
                        textScale = textScale
                    )
                    InfoChip(
                        text = if (unlocked) localizedFactionName(path) else stringResource(R.string.faction_unlock_short),
                        containerColor = if (unlocked) accentColor.copy(alpha = 0.18f) else Color(0xFF28303A),
                        contentColor = if (unlocked) Color.White else Color(0xFFF0E3C7),
                        textScale = textScale
                    )
                }

                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                Text(
                    text = if (unlocked) {
                        localizedFactionLoreTitle(path)
                    } else {
                        stringResource(R.string.faction_locked_title)
                    },
                    color = Color(0xFFFFF1DA),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = scaledSp(19.5f, textScale),
                    fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                Text(
                    text = if (unlocked) {
                        localizedFactionLoreCaption(path)
                    } else {
                        stringResource(R.string.faction_panel_subtitle)
                    },
                    color = if (unlocked) accentColor else Color(0xFFD3DDE8),
                    fontWeight = FontWeight.Medium,
                    fontSize = scaledSp(10.6f, textScale),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (unlocked) {
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                    Text(
                        text = localizedFactionDescription(path),
                        color = Color(0xFFE8EEF5),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = scaledSp(11.6f, textScale),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                    ExpandableLoreText(
                        key = "path_hero_style_${path.name}",
                        text = localizedFactionPlaystyle(path),
                        textScale = textScale,
                        color = Color(0xFFC1CDD9),
                        collapsedMaxLines = 1,
                        fontSizeSp = 11.1f,
                        toggleColor = Color(0xFFE2C688)
                    )
                    Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                        verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                    ) {
                        FactionMetricChip(
                            title = stringResource(R.string.faction_bonus_tap_short),
                            value = "×${formatMultiplier(bonus.tapMultiplier)}",
                            accentColor = accentColor,
                            textScale = textScale
                        )
                        FactionMetricChip(
                            title = stringResource(R.string.faction_bonus_auto_short),
                            value = "×${formatMultiplier(bonus.autoMultiplier)}",
                            accentColor = accentColor,
                            textScale = textScale
                        )
                        FactionMetricChip(
                            title = stringResource(R.string.faction_bonus_boss_short),
                            value = "×${formatMultiplier(bonus.bossDamageMultiplier)}",
                            accentColor = accentColor,
                            textScale = textScale
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactionStoryCard(
    factionPath: AlignmentPath,
    selected: Boolean,
    canSwitchPath: Boolean,
    bonus: FactionBonusConfig,
    textScale: Float,
    onSelect: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val accentColor = factionAccentColor(factionPath)
    var detailsExpanded by rememberSaveable("faction_details_${factionPath.name}") { mutableStateOf(false) }
    val selectedScale by animateFloatAsState(
        targetValue = if (selected) 1.015f else 1f,
        animationSpec = tween(durationMillis = 260),
        label = "faction_card_scale_${factionPath.name.lowercase(Locale.ROOT)}"
    )
    val artDrift by rememberInfiniteTransition(label = "faction_card_drift_${factionPath.name.lowercase(Locale.ROOT)}")
        .animateFloat(
            initialValue = -24f,
            targetValue = 24f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "faction_card_drift_value_${factionPath.name.lowercase(Locale.ROOT)}"
        )
    val stateChipText = when {
        selected -> stringResource(R.string.faction_state_current)
        canSwitchPath -> stringResource(R.string.faction_state_available)
        else -> stringResource(R.string.faction_unlock_short)
    }
    val buttonLabel = when {
        selected -> stringResource(R.string.faction_action_current)
        !canSwitchPath -> stringResource(R.string.faction_unlock_short)
        else -> stringResource(R.string.faction_action_select)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = selectedScale,
                scaleY = selectedScale
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFF1A2028) else Color(0xFF151A21)
        ),
        border = BorderStroke(
            1.dp,
            if (selected) accentColor.copy(alpha = 0.46f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (selected) 0.1f else 0.05f),
                        Color.Transparent
                    )
                )
            )
        ) {
            Column(modifier = Modifier.padding(layoutProfile.cardPadding() + 2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.cardPadding()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                            verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                        ) {
                            InfoChip(
                                text = stateChipText,
                                containerColor = if (selected) {
                                    accentColor.copy(alpha = 0.2f)
                                } else {
                                    Color(0xFF25303A)
                                },
                                contentColor = if (selected) {
                                    Color.White
                                } else {
                                    Color(0xFFFFE8BE)
                                },
                                textScale = textScale
                            )
                        }

                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 2.dp))
                        Text(
                            text = localizedFactionName(factionPath),
                            color = Color(0xFFFFF1DA),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = scaledSp(17.5f, textScale),
                            fontFamily = MaterialTheme.typography.displayMedium.fontFamily
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                        Text(
                            text = localizedFactionDescription(factionPath),
                            color = Color(0xFFE8EEF5),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = scaledSp(11.1f, textScale),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing()))
                        Text(
                            text = stringResource(
                                R.string.faction_compare_line,
                                formatMultiplier(bonus.tapMultiplier),
                                formatMultiplier(bonus.autoMultiplier),
                                formatMultiplier(bonus.bossDamageMultiplier)
                            ),
                            color = Color(0xFFAFBDCE),
                            fontSize = scaledSp(10.5f, textScale),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(if (selected) 112.dp else 76.dp)
                            .height(if (selected) 136.dp else 92.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF10151C))
                            .border(
                                width = 1.dp,
                                color = accentColor.copy(alpha = 0.24f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        ResourceOrPlaceholderArt(
                            drawableName = factionImageName(factionPath),
                            placeholderTitle = localizedFactionName(factionPath),
                            placeholderSubtitle = localizedFactionLoreCaption(factionPath),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .graphicsLayer(
                                    scaleX = if (selected) 1.04f else 1.01f,
                                    scaleY = if (selected) 1.04f else 1.01f,
                                    translationX = if (selected) artDrift * 0.4f else artDrift * 0.2f
                                )
                        )
                    }
                }

                if (selected || canSwitchPath) {
                    Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing()),
                        verticalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                    ) {
                        FactionMetricChip(
                            title = stringResource(R.string.faction_bonus_tap_short),
                            value = "×${formatMultiplier(bonus.tapMultiplier)}",
                            accentColor = accentColor,
                            textScale = textScale
                        )
                        FactionMetricChip(
                            title = stringResource(R.string.faction_bonus_auto_short),
                            value = "×${formatMultiplier(bonus.autoMultiplier)}",
                            accentColor = accentColor,
                            textScale = textScale
                        )
                        FactionMetricChip(
                            title = stringResource(R.string.faction_bonus_boss_short),
                            value = "×${formatMultiplier(bonus.bossDamageMultiplier)}",
                            accentColor = accentColor,
                            textScale = textScale
                        )
                    }
                }

                Spacer(modifier = Modifier.height(layoutProfile.sectionSpacing()))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
                ) {
                    ClaimActionButton(
                        text = if (detailsExpanded) {
                            stringResource(R.string.faction_action_hide_details)
                        } else {
                            stringResource(R.string.faction_action_show_details)
                        },
                        onClick = { detailsExpanded = !detailsExpanded },
                        enabled = true,
                        containerColor = Color(0xFF25303A),
                        contentColor = Color(0xFFFFE8BE),
                        textScale = textScale,
                        modifier = Modifier.weight(0.82f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                        animateAttention = false,
                        showAccentIcon = false,
                        style = ActionButtonStyle.NEUTRAL
                    )
                    ClaimActionButton(
                        text = buttonLabel,
                        onClick = onSelect,
                        enabled = canSwitchPath && !selected,
                        containerColor = accentColor,
                        disabledContainerColor = if (selected) {
                            accentColor.copy(alpha = 0.45f)
                        } else {
                            Color(0xFF2A323C)
                        },
                        textScale = textScale,
                        modifier = Modifier.weight(1.18f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                        fontSizeSp = 11.5f,
                        animateAttention = canSwitchPath && !selected,
                        showAccentIcon = canSwitchPath && !selected,
                        style = ActionButtonStyle.PATH
                    )
                }

                AnimatedVisibility(visible = detailsExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(layoutProfile.minorSpacing() + 4.dp))
                        Text(
                            text = stringResource(R.string.faction_section_lore_style),
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = scaledSp(11f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.bottomBarLabelSpacing() + 1.dp))
                        Text(
                            text = localizedFactionLoreBody(factionPath),
                            color = Color(0xFFD3DDE8),
                            fontSize = scaledSp(11.5f, textScale)
                        )
                        Spacer(modifier = Modifier.height(layoutProfile.minorSpacing() + 4.dp))
                        Text(
                            text = localizedFactionPlaystyle(factionPath),
                            color = Color(0xFFFFE8BE),
                            fontSize = scaledSp(11.5f, textScale)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FactionMetricChip(
    title: String,
    value: String,
    accentColor: Color,
    textScale: Float
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.14f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.24f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)) {
            Text(
                text = title,
                color = Color(0xFFD4DEE8),
                fontSize = scaledSp(10f, textScale)
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(12f, textScale)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VavardaPreview() {
    VavardaTheme {
        VavardaClickerScreen()
    }
}
