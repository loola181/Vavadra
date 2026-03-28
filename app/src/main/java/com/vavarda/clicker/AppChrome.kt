package com.vavarda.clicker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
internal fun BottomScreenBar(
    activeScreen: GameScreen,
    eventsReadyCount: Int,
    textScale: Float,
    onScreenChange: (GameScreen) -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC0B1018)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xD6111822),
                        Color(0xD2090D15)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(layoutProfile.bottomBarInnerPadding()),
                horizontalArrangement = Arrangement.spacedBy(layoutProfile.chipSpacing())
            ) {
                GameScreen.values().forEach { screen ->
                    val isSelected = screen == activeScreen
                    val badgeCount = if (screen == GameScreen.EVENTS) eventsReadyCount.coerceAtMost(99) else 0
                    val iconSpec = bottomNavIconSpec(screen)
                    val iconSlotSize = layoutProfile.bottomBarIconSize() + 4.dp
                    val buttonColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> Color(0xFF8A6435)
                            badgeCount > 0 -> Color(0xFF253247)
                            else -> Color(0xFF141B26)
                        },
                        animationSpec = tween(durationMillis = 220),
                        label = "switch_button_color_${screen.name.lowercase(Locale.ROOT)}"
                    )
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(layoutProfile.bottomBarHeight() - 4.dp)
                            .testTag("bottom_nav_${screen.name.lowercase(Locale.ROOT)}")
                            .clickable { onScreenChange(screen) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0x80FFD89A) else Color.White.copy(alpha = 0.05f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            buttonColor,
                                            if (isSelected) {
                                                buttonColor.copy(alpha = 0.92f)
                                            } else {
                                                buttonColor.copy(alpha = 0.98f)
                                            }
                                        )
                                    )
                                )
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 5.dp)
                                        .width(24.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFFFFD89A))
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = layoutProfile.bottomBarContentHorizontalPadding(),
                                        vertical = layoutProfile.bottomBarContentVerticalPadding()
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(modifier = Modifier.size(iconSlotSize)) {
                                    Icon(
                                        imageVector = iconSpec.imageVector,
                                        contentDescription = stringResource(screen.labelRes),
                                        tint = if (isSelected) Color(0xFFFFF4DA) else Color(0xFFE3EAF3),
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .offset(
                                                x = iconSpec.opticalOffset.x,
                                                y = iconSpec.opticalOffset.y
                                            )
                                            .graphicsLayer(
                                                scaleX = iconSpec.iconScale,
                                                scaleY = iconSpec.iconScale
                                            )
                                            .size(layoutProfile.bottomBarIconSize())
                                    )
                                    if (badgeCount > 0) {
                                        Card(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(
                                                    x = iconSpec.badgeOffset.x,
                                                    y = iconSpec.badgeOffset.y
                                                ),
                                            shape = RoundedCornerShape(999.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC857))
                                        ) {
                                            Text(
                                                text = badgeCount.toString(),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                color = Color(0xFF2A173D),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = scaledSp(9f, textScale)
                                            )
                                        }
                                    }
                                }
                                Spacer(
                                    modifier = Modifier.height(
                                        (layoutProfile.bottomBarLabelSpacing() - 1.dp).coerceAtLeast(2.dp)
                                    )
                                )
                                Text(
                                    text = bottomNavLabel(screen),
                                    color = if (isSelected) Color(0xFFFFF3D5) else Color(0xFFD7E0EC),
                                    fontSize = scaledSp(9.5f, textScale),
                                    fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip,
                                    softWrap = false
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
internal fun HomeTopBar(
    level: Int,
    path: AlignmentPath,
    profileName: String,
    profileImageUri: String,
    textScale: Float,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val contentSpacing = (layoutProfile.chipSpacing() + 2.dp).coerceAtLeast(10.dp)
    val titleSpacing = (layoutProfile.bottomBarLabelSpacing() - 1.dp).coerceAtLeast(4.dp)

    HeaderFrame(
        minHeight = 72.dp,
        backgroundBrush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xE3151A24),
                Color(0xDD121721),
                Color(0xD70F131C)
            )
        ),
        borderColor = Color.White.copy(alpha = 0.08f),
        horizontalPadding = layoutProfile.headerHorizontalPadding(),
        verticalPadding = (layoutProfile.headerVerticalPadding() - 2.dp).coerceAtLeast(9.dp),
        contentSpacing = contentSpacing
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(titleSpacing)
        ) {
            Text(
                text = stringResource(R.string.title_game),
                color = Color(0xFFFFF2D7),
                fontWeight = FontWeight.ExtraBold,
                fontSize = scaledSp(14.5f, textScale),
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily
            )
            InfoChip(
                text = "${stringResource(R.string.label_level_short, level)} • ${localizedFactionName(path)}",
                containerColor = Color(0xFF1A2230),
                contentColor = Color(0xFFF4E9D1),
                textScale = textScale
            )
        }

        HeaderActions(
            profileName = profileName,
            profileImageUri = profileImageUri,
            textScale = textScale,
            onOpenGuide = onOpenGuide,
            onOpenSettings = onOpenSettings
        )
    }
}

@Composable
internal fun ScreenHeaderCard(
    title: String,
    subtitle: String?,
    profileName: String,
    profileImageUri: String,
    textScale: Float,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val contentSpacing = (layoutProfile.chipSpacing() + 1.dp).coerceAtLeast(8.dp)
    val titleSpacing = if (subtitle.isNullOrBlank()) 0.dp else 3.dp

    HeaderFrame(
        minHeight = 64.dp,
        backgroundBrush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xE2141924),
                Color(0xDD10151E),
                Color(0xD50D1118)
            )
        ),
        borderColor = Color.White.copy(alpha = 0.06f),
        horizontalPadding = layoutProfile.headerHorizontalPadding(),
        verticalPadding = (layoutProfile.headerVerticalPadding() - 2.dp).coerceAtLeast(8.dp),
        contentSpacing = contentSpacing
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(titleSpacing)
        ) {
            Text(
                text = title,
                color = Color(0xFFFFF4DE),
                fontWeight = FontWeight.ExtraBold,
                fontSize = scaledSp(15f, textScale),
                fontFamily = MaterialTheme.typography.displayMedium.fontFamily
            )
            subtitle?.takeIf { it.isNotBlank() }?.let { resolvedSubtitle ->
                Text(
                    text = resolvedSubtitle,
                    color = Color(0xFFD1D7E3),
                    fontSize = scaledSp(9.5f, textScale),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HeaderActions(
            compact = true,
            profileName = profileName,
            profileImageUri = profileImageUri,
            textScale = textScale,
            onOpenGuide = onOpenGuide,
            onOpenSettings = onOpenSettings
        )
    }
}

@Composable
internal fun HeaderActions(
    compact: Boolean = false,
    profileName: String,
    profileImageUri: String,
    textScale: Float,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val buttonSize = headerButtonSize(layoutProfile, compact)
    val railHeight = buttonSize + if (compact) 10.dp else 12.dp
    val actionSpacing = if (compact) 5.dp else 6.dp
    val railShape = RoundedCornerShape(if (compact) 16.dp else 18.dp)

    Card(
        modifier = Modifier.height(railHeight),
        shape = railShape,
        colors = CardDefaults.cardColors(containerColor = Color(0x66101924)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier
                .height(railHeight)
                .padding(horizontal = if (compact) 5.dp else 6.dp),
            horizontalArrangement = Arrangement.spacedBy(actionSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderProfileButton(
                profileName = profileName,
                profileImageUri = profileImageUri,
                compact = compact,
                textScale = textScale,
                onClick = onOpenSettings
            )
            HeaderIconButton(
                onClick = onOpenGuide,
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.action_guide),
                containerColor = Color(0xFF273748),
                testTag = "open_guide_button",
                compact = compact,
                textScale = textScale,
                iconScale = 0.98f
            )
            HeaderIconButton(
                onClick = onOpenSettings,
                icon = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.action_settings),
                containerColor = Color(0xFF332B38),
                testTag = "open_settings_button",
                compact = compact,
                textScale = textScale,
                iconScale = 0.98f
            )
        }
    }
}

@Composable
internal fun HeaderIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    testTag: String,
    compact: Boolean,
    textScale: Float,
    iconScale: Float = 1f
) {
    val layoutProfile = rememberScreenLayoutMetrics()
    val buttonSize = headerButtonSize(layoutProfile, compact)
    val iconSize = if (compact) {
        (layoutProfile.headerIconSize(textScale) - 3.dp).coerceAtLeast(12.dp)
    } else {
        layoutProfile.headerIconSize(textScale)
    } * iconScale

    Card(
        modifier = Modifier
            .size(buttonSize)
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun HeaderFrame(
    minHeight: Dp,
    backgroundBrush: Brush,
    borderColor: Color,
    horizontalPadding: Dp,
    verticalPadding: Dp,
    contentSpacing: Dp,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xC9111620)),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(modifier = Modifier.background(brush = backgroundBrush)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .padding(
                        horizontal = horizontalPadding,
                        vertical = verticalPadding
                    ),
                horizontalArrangement = Arrangement.spacedBy(contentSpacing),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

private fun headerButtonSize(
    layoutProfile: LayoutMetrics,
    compact: Boolean
): Dp {
    return if (compact) {
        layoutProfile.headerButtonSize() - 10.dp
    } else {
        layoutProfile.headerButtonSize() - 6.dp
    }
}
