package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModelStatus
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPurplePrimary
import com.example.ui.theme.NeonPurpleSecondary
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopAppBar(
    modelStatus: ModelStatus,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onClearChat: () -> Unit,
    onExportChat: () -> Unit,
    onDeleteChat: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(DarkCardSurface)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.testTag("drawer_button")
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open Navigation Menu",
                tint = NeonPurpleSecondary
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        FuturisticAvatar(size = 36.dp, pulsing = modelStatus == ModelStatus.READY)

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TODO",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                val (statusColor, statusText) = when (modelStatus) {
                    ModelStatus.READY -> Pair(OnlineGreen, "Offline • Model Ready")
                    ModelStatus.LOADING -> Pair(StatusAmber, "Offline • Loading Model...")
                    ModelStatus.NOT_LOADED -> Pair(StatusAmber, "Offline • Model Not Loaded")
                    ModelStatus.NOT_INSTALLED -> Pair(ErrorRed, "Offline • Model Required")
                    ModelStatus.ERROR -> Pair(ErrorRed, "Offline • Model Error")
                }

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = statusText,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.testTag("search_button")
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Messages",
                tint = TextSecondary
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("more_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = TextSecondary
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(DarkCardElevated)
            ) {
                DropdownMenuItem(
                    text = { Text("Clear current chat", color = TextPrimary) },
                    leadingIcon = {
                        Icon(Icons.Default.ClearAll, contentDescription = null, tint = NeonPurplePrimary)
                    },
                    onClick = {
                        menuExpanded = false
                        onClearChat()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export chat", color = TextPrimary) },
                    leadingIcon = {
                        Icon(Icons.Default.Share, contentDescription = null, tint = NeonPurplePrimary)
                    },
                    onClick = {
                        menuExpanded = false
                        onExportChat()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete chat", color = ErrorRed) },
                    leadingIcon = {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ErrorRed)
                    },
                    onClick = {
                        menuExpanded = false
                        onDeleteChat()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Model & Settings", color = TextPrimary) },
                    leadingIcon = {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = NeonPurplePrimary)
                    },
                    onClick = {
                        menuExpanded = false
                        onOpenSettings()
                    }
                )
                DropdownMenuItem(
                    text = { Text("About TODO", color = TextPrimary) },
                    leadingIcon = {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NeonPurplePrimary)
                    },
                    onClick = {
                        menuExpanded = false
                        onOpenAbout()
                    }
                )
            }
        }
    }
}
