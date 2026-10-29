package com.jahanmusic.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jahanmusic.app.components.GlassmorphismNavigation
import com.jahanmusic.app.components.glassCard
import com.jahanmusic.app.ui.theme.*

/**
 * Settings rebuilt to match setting.png:
 * - header: back arrow FIRST at the top-left, then the page name
 * - six glass cards, each with a purple-gradient icon tile on the RIGHT
 *   (RTL) and title + subtitle to its left, in this order:
 *   appearance → language → default tab → rate → update → version
 * - the card list stops above the floating player/menu chrome.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAllSongs: () -> Unit = {},
    onNavigateToAlbums: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    showFooter: Boolean = true
) {
    var selectedNavIndex by remember { mutableIntStateOf(3) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(containerColor = Color.Transparent,
            contentColor = TextPrimary,
            bottomBar = {
                if (showFooter) {
                GlassmorphismNavigation(
                    selectedIndex = selectedNavIndex,
                    onItemSelected = { index ->
                        selectedNavIndex = index
                        when (index) {
                            0 -> onNavigateToHome()
                            1 -> onNavigateToAllSongs()
                            2 -> onNavigateToAlbums()
                        }
                    }
                )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                // Header: back arrow FIRST at the top-left, then the page name
                // (Row forced LTR so the arrow is on the left, like the others)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "تنظیمات",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                }

                // The six cards from setting.png (scrollable on small screens;
                // bottom inset keeps the last card above the floating chrome)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = SettingsBottomInset)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingCard(Icons.Default.Palette, "تنظیمات ظاهری", "تغییر تم، رنگ‌ها و ظاهر برنامه", onClick = onNavigateToAppearance)
                    SettingCard(Icons.Default.Translate, "زبان", "انتخاب زبان برنامه")
                    SettingCard(Icons.Default.GridView, "تب پیش‌فرض", "انتخاب تب مورد نظر در شروع برنامه")
                    SettingCard(Icons.Default.Star, "امتیاز به برنامه", "با حمایت شما، بهتر میشویم")
                    SettingCard(Icons.Default.Sync, "به روزرسانی", "بررسی و نصب نسخه جدید")
                    SettingCard(Icons.Default.Info, "نسخه فعلی", "v1.1.0")
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * One settings card from setting.png: glass card with a purple-gradient
 * rounded-square icon tile on the right (RTL first child), title + subtitle
 * right-aligned next to it.
 */
@Composable
private fun SettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .glassCard(RoundedCornerShape(18.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Purple-gradient icon tile (light violet → deep purple, like the image)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFB98CFF), Color(0xFF7A3FD0))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = TextSecondary)
        }
    }
}
