package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.MainViewModel
import com.example.ui.components.NavigationDrawerContent
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ChatHistoryScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HelpGuideScreen
import com.example.ui.screens.ModelScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val modelStatus by viewModel.modelStatus.collectAsState()
    val activeModel by viewModel.currentModelInfo.collectAsState()

    var currentScreen by remember { mutableStateOf("chat") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen || currentScreen != "chat") {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen != "chat") {
            currentScreen = "chat"
        }
    }

    if (!isOnboardingCompleted) {
        WelcomeScreen(
            onGetStarted = {
                viewModel.completeOnboarding()
            }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = DarkCanvas
                ) {
                    NavigationDrawerContent(
                        currentRoute = currentScreen,
                        modelStatus = modelStatus,
                        activeModel = activeModel,
                        onNewChatClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.startNewChat()
                            currentScreen = "chat"
                        },
                        onHistoryClick = {
                            coroutineScope.launch { drawerState.close() }
                            currentScreen = "history"
                        },
                        onModelSettingsClick = {
                            coroutineScope.launch { drawerState.close() }
                            currentScreen = "model_settings"
                        },
                        onToolsClick = {
                            coroutineScope.launch { drawerState.close() }
                            currentScreen = "tools"
                        },
                        onHelpClick = {
                            coroutineScope.launch { drawerState.close() }
                            currentScreen = "help"
                        },
                        onAboutClick = {
                            coroutineScope.launch { drawerState.close() }
                            currentScreen = "about"
                        }
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepBlack)
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        "chat" -> ChatScreen(
                            viewModel = viewModel,
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            },
                            onOpenSettings = { currentScreen = "model_settings" },
                            onOpenAbout = { currentScreen = "about" },
                            onOpenTools = { currentScreen = "tools" }
                        )
                        "history" -> ChatHistoryScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "chat" },
                            onSelectChat = { currentScreen = "chat" }
                        )
                        "model_settings" -> ModelScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "chat" }
                        )
                        "tools" -> ToolsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "chat" }
                        )
                        "help" -> HelpGuideScreen(
                            onBack = { currentScreen = "chat" }
                        )
                        "about" -> AboutScreen(
                            onBack = { currentScreen = "chat" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        color = TextPrimary,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}
