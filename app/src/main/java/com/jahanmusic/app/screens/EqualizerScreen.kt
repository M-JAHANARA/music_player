package com.jahanmusic.app.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jahanmusic.app.components.createKnobStrip
import com.jahanmusic.app.components.GlassmorphismNavigation
import com.jahanmusic.app.player.EqualizerController
import com.jahanmusic.app.ui.theme.*
import org.androidaudioplugin.composeaudiocontrols.ImageStripKnob

/**
 * Equalizer matches equalizer.jpg (modified):
 * preset dropdown, 10 vertical sliders 31..16k left->right,
 * Bass + Virtualizer knobs left->right. No extra rectangular bar.
 * Footer nav kept (only Playback hides footer per spec).
 */
@Composable
fun EqualizerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToAllSongs: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val bands by EqualizerController.bands.collectAsState()
    val bass by EqualizerController.bass.collectAsState()
    val virt by EqualizerController.virtualizerStrength.collectAsState()
    var preset by remember { mutableStateOf("Flat") }
    var presetOpen by remember { mutableStateOf(false) }
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    val presets = listOf("Flat", "Pop", "Rock", "Jazz", "Bass", "Treble")

    // Knob image strips (KnobMan style) for compose-audio-controls' knobs
    val bassStrip = remember { createKnobStrip(AndroidColor.rgb(179, 136, 255)) }
    val virtStrip = remember { createKnobStrip(AndroidColor.rgb(224, 64, 251)) }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackground(LocalContext.current)

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
                bottomBar = {
                    GlassmorphismNavigation(
                        selectedIndex = selectedNavIndex,
                        onItemSelected = { index ->
                            selectedNavIndex = index
                            when (index) {
                                0 -> onNavigateToHome()
                                1 -> onNavigateToAllSongs()
                                3 -> onNavigateToSettings()
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                        }
                        Text("اکولایزر", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
                    }
                    // Preset row (RTL: label right, dropdown left)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("پروفایل صوتی", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Box {
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface)
                                    .clickable { presetOpen = true }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(preset, fontSize = 14.sp, color = AccentPurple)
                            }
                            DropdownMenu(expanded = presetOpen, onDismissRequest = { presetOpen = false }) {
                                presets.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p) },
                                        onClick = {
                                            preset = p
                                            EqualizerController.applyPreset(p)
                                            presetOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // 10 bands — LTR order 31..16k inside RTL screen
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EqualizerController.bandLabels.forEachIndexed { index, label ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Vertical slider via rotated horizontal slider
                                    Box(
                                        modifier = Modifier.height(150.dp).width(36.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Slider(
                                            value = bands[index],
                                            onValueChange = { EqualizerController.setBand(index, it) },
                                            modifier = Modifier
                                                .width(130.dp)
                                                .rotate(-90f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = AccentPink,
                                                activeTrackColor = AccentPurple,
                                                inactiveTrackColor = AccentPurple.copy(alpha = 0.2f)
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(label, fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Bass (left) + Virtualizer (right) — LTR
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(DarkSurface)
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ImageStripKnob(
                                    imageBitmap = bassStrip,
                                    value = bass,
                                    valueRange = 0f..1f,
                                    explicitSizeInDp = 104.dp,
                                    tooltipColor = AccentPurple,
                                    onValueChange = { EqualizerController.setBass(it) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("باس", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Bass", fontSize = 11.sp, color = TextSecondary)
                            }
                            Column(
                                modifier = Modifier.weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(DarkSurface)
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ImageStripKnob(
                                    imageBitmap = virtStrip,
                                    value = virt,
                                    valueRange = 0f..1f,
                                    explicitSizeInDp = 104.dp,
                                    tooltipColor = AccentMagenta,
                                    onValueChange = { EqualizerController.setVirtualizer(it) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("مجازی‌ساز", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Virtualizer", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
