package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModelInfo
import com.example.data.model.ModelStatus
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPurplePrimary
import com.example.ui.theme.NeonPurpleSecondary
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NavigationDrawerContent(
    currentRoute: String,
    modelStatus: ModelStatus,
    activeModel: ModelInfo?,
    onNewChatClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onModelSettingsClick: () -> Unit,
    onToolsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(DarkCanvas)
            .statusBarsPadding()
            .padding(vertical = 16.dp)
    ) {
        // Drawer Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            FuturisticAvatar(size = 46.dp, pulsing = modelStatus == ModelStatus.READY)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "TODO",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Offline AI Assistant",
                    color = NeonPurpleSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = DarkSurfaceStroke, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // New Chat prominent button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkCardElevated)
                .border(1.dp, NeonPurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .clickable { onNewChatClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("drawer_new_chat")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = NeonPurpleSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "New Chat",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Items
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            DrawerNavigationItem(
                icon = Icons.Default.History,
                label = "Chat History",
                isSelected = currentRoute == "history",
                onClick = onHistoryClick,
                tag = "drawer_history"
            )
            DrawerNavigationItem(
                icon = Icons.Default.Settings,
                label = "Model & Settings",
                isSelected = currentRoute == "model_settings",
                onClick = onModelSettingsClick,
                tag = "drawer_model_settings"
            )
            DrawerNavigationItem(
                icon = Icons.Default.Build,
                label = "Tools",
                isSelected = currentRoute == "tools",
                onClick = onToolsClick,
                tag = "drawer_tools"
            )
            DrawerNavigationItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                label = "Help & Guide",
                isSelected = currentRoute == "help",
                onClick = onHelpClick,
                tag = "drawer_help"
            )
            DrawerNavigationItem(
                icon = Icons.Default.Info,
                label = "About TODO",
                isSelected = currentRoute == "about",
                onClick = onAboutClick,
                tag = "drawer_about"
            )
        }

        // Bottom Model Status indicator card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkCardSurface)
                .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(14.dp))
                .clickable { onModelSettingsClick() }
                .padding(14.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Model Status",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val statusDotColor = when (modelStatus) {
                        ModelStatus.READY -> OnlineGreen
                        ModelStatus.LOADING -> StatusAmber
                        ModelStatus.NOT_LOADED -> StatusAmber
                        ModelStatus.NOT_INSTALLED -> ErrorRed
                        ModelStatus.ERROR -> ErrorRed
                    }

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusDotColor)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val statusDisplay = when (modelStatus) {
                    ModelStatus.READY -> "● Local AI Ready"
                    ModelStatus.LOADING -> "● Loading Model..."
                    ModelStatus.NOT_LOADED -> "● Model Not Loaded"
                    ModelStatus.NOT_INSTALLED -> "● Model Required"
                    ModelStatus.ERROR -> "● Model Error"
                }

                Text(
                    text = statusDisplay,
                    color = if (modelStatus == ModelStatus.READY) OnlineGreen else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (activeModel != null && modelStatus == ModelStatus.READY) {
                    Text(
                        text = "${activeModel.name} (${activeModel.formattedSize})",
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerNavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) DarkCardElevated else DarkCanvas)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(tag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NeonPurpleSecondary else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                color = if (isSelected) NeonPurpleSecondary else TextPrimary,
                fontSize = 14.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
