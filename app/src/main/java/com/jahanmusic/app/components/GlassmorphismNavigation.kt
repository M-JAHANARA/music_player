package com.jahanmusic.app.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jahanmusic.app.ui.theme.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

// Left-to-right order: home, songs, albums, settings
val navItems = listOf(
    NavItem("خانه", Icons.Filled.Home, Icons.Outlined.Home, "home"),
    NavItem("آهنگ‌ها", Icons.Filled.MusicNote, Icons.Outlined.MusicNote, "songs"),
    NavItem("آلبوم‌ها", Icons.Filled.Album, Icons.Outlined.Album, "albums"),
    NavItem("تنظیمات", Icons.Filled.Settings, Icons.Outlined.Settings, "settings")
)

/**
 * Xiaomi/Miuix-style glassmorphism footer:
 * rounded 28dp pill, translucent gradient + sliding selected indicator.
 * Shown on all pages except Loading/Access/Playback.
 */
@Composable
fun GlassmorphismNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // LTR: home left … settings right (matches swipeable pager)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(20.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x40B388FF),
                            Color(0x20E040FB),
                            Color(0x40B388FF)
                        )
                    )
                )
                .background(Color(0xCC1A1A2E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                AccentPurple.copy(alpha = 0.35f),
                                                AccentMagenta.copy(alpha = 0.25f)
                                            )
                                        )
                                    )
                                } else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onItemSelected(index) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
