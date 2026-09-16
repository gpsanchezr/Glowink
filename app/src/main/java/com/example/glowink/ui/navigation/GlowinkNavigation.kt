package com.example.glowink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.glowink.data.GameStats
import com.google.firebase.auth.FirebaseAuth
import com.example.glowink.ui.auth.LoginScreen
import com.example.glowink.ui.auth.RegisterScreen
import com.example.glowink.ui.auth.WelcomeScreen
import com.example.glowink.ui.avatar.AvatarEditorScreen
import com.example.glowink.ui.games.DuelGameScreen
import com.example.glowink.ui.games.QuizGameScreen
import com.example.glowink.ui.games.RaceGameScreen
import com.example.glowink.ui.games.SnakeGameScreen
import com.example.glowink.ui.screens.Avatar3DViewerScreen
import com.example.glowink.ui.screens.CameraScreen
import com.example.glowink.ui.screens.ChatScreen
import com.example.glowink.ui.screens.ChatsListScreen
import com.example.glowink.ui.screens.ContactsScreen
import com.example.glowink.ui.screens.DiscoverScreen
import com.example.glowink.ui.screens.FriendProfileScreen
import com.example.glowink.ui.screens.GamesScreen
import com.example.glowink.ui.screens.GlowFeedScreen
import com.example.glowink.ui.screens.ProfileScreen
import com.example.glowink.ui.viewmodel.ChatViewModel

/**
 * Constantes de Rutas para la Navegación de Jetpack Compose en Glowink.
 */
object GlowinkRoutes {
    const val WELCOME = "welcome"
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SIGN_UP = "register"
    const val AVATAR_EDITOR = "avatar_editor"
    const val CHATS_LIST = "chats_list"
    const val CONTACTS = "contacts"
    const val CHAT_SCREEN = "chat_screen"
    const val FRIEND_PROFILE = "friend_profile"
    const val CAMERA = "camera"
    const val GLOW_FEED = "glow_feed"
    const val PROFILE = "profile"
    const val GAMES = "games"
    const val DISCOVER = "discover"
    const val COMMUNITY_DETAIL = "community_detail/{id}"
    const val CLAN_RANKING = "clan_ranking"
    const val AVATAR_3D_VIEWER = "avatar_3d_viewer"
    const val SHOP = "shop"
    const val GAME_SNAKE = "game_snake"
    const val GAME_DUEL = "game_duel"
    const val GAME_RACE = "game_race"
    const val GAME_QUIZ = "game_quiz"
}

/** Navega a una pestaña inferior compartida por Chats/Juegos/Perfil/Descubrir. */
private fun NavHostController.navigateToTab(index: Int) {
    val route = when (index) {
        0 -> GlowinkRoutes.CHATS_LIST
        1 -> GlowinkRoutes.GAMES
        2 -> GlowinkRoutes.PROFILE
        3 -> GlowinkRoutes.DISCOVER
        else -> return
    }
    navigate(route) {
        launchSingleTop = true
        popUpTo(GlowinkRoutes.CHATS_LIST) { saveState = true }
        restoreState = true
    }
}

/**
 * Motor de Navegación Principal (NavHost) para la aplicación Glowink.
 *
 * Flujo de navegación independiente:
 * WelcomeScreen -> LoginScreen (o RegisterScreen) -> AvatarEditorScreen -> Pantalla principal de la app (ChatsList)
 */
@Composable
fun GlowinkAppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sharedViewModel: ChatViewModel = viewModel()
) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        GlowinkRoutes.CHATS_LIST
    } else {
        GlowinkRoutes.WELCOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(GlowinkRoutes.WELCOME) { _: NavBackStackEntry ->
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(GlowinkRoutes.LOGIN)
                },
                onNavigateToRegister = {
                    navController.navigate(GlowinkRoutes.REGISTER)
                }
            )
        }

        composable(GlowinkRoutes.LOGIN) { _: NavBackStackEntry ->
            LoginScreen(
                viewModel = sharedViewModel,
                onLoginSuccess = {
                    navController.navigate(GlowinkRoutes.CHATS_LIST) {
                        popUpTo(GlowinkRoutes.WELCOME) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(GlowinkRoutes.REGISTER)
                }
            )
        }

        composable(GlowinkRoutes.REGISTER) { _: NavBackStackEntry ->
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(GlowinkRoutes.LOGIN)
                },
                onNavigateToAvatarCreation = {
                    navController.navigate(GlowinkRoutes.AVATAR_EDITOR)
                },
                viewModel = sharedViewModel
            )
        }

        composable(GlowinkRoutes.AVATAR_EDITOR) { _: NavBackStackEntry ->
            com.example.glowink.ui.avatar.AvatarCreationScreen(
                chatViewModel = sharedViewModel,
                onDone = {
                    navController.navigate(GlowinkRoutes.CHATS_LIST) {
                        popUpTo(GlowinkRoutes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(GlowinkRoutes.CHATS_LIST) { _: NavBackStackEntry ->
            ChatsListScreen(
                viewModel = sharedViewModel,
                onChatClick = { friendId ->
                    sharedViewModel.selectChat(friendId)
                    navController.navigate(GlowinkRoutes.CHAT_SCREEN)
                },
                onAddStoryClick = {
                    navController.navigate(GlowinkRoutes.CAMERA)
                },
                onNavigateToContacts = {
                    navController.navigate(GlowinkRoutes.CONTACTS)
                },
                onNavigateToShop = {
                    navController.navigate(GlowinkRoutes.SHOP)
                },
                onNavigateToClan = {
                    navController.navigate(GlowinkRoutes.DISCOVER) // Redirigir a Descubrir como fallback del clan
                },
                onLogout = {
                    sharedViewModel.logout()
                    navController.navigate(GlowinkRoutes.WELCOME) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onTabClick = { index -> navController.navigateToTab(index) }
            )
        }

        composable(GlowinkRoutes.CONTACTS) { _: NavBackStackEntry ->
            ContactsScreen(
                viewModel = sharedViewModel,
                onBackClick = { navController.popBackStack() },
                onUserSelected = { friendId ->
                    sharedViewModel.selectChat(friendId)
                    navController.navigate(GlowinkRoutes.CHAT_SCREEN)
                }
            )
        }

        composable(GlowinkRoutes.PROFILE) { _: NavBackStackEntry ->
            ProfileScreen(
                viewModel = sharedViewModel,
                onLogout = {
                    sharedViewModel.logout()
                    navController.navigate(GlowinkRoutes.WELCOME) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onEditAvatar = {
                    navController.navigate(GlowinkRoutes.AVATAR_EDITOR)
                },
                onOpen3DViewer = {
                    navController.navigate(GlowinkRoutes.AVATAR_3D_VIEWER)
                },
                onTabClick = { index -> navController.navigateToTab(index) }
            )
        }

        composable(GlowinkRoutes.AVATAR_3D_VIEWER) { _: NavBackStackEntry ->
            Avatar3DViewerScreen(onBack = { navController.popBackStack() })
        }

        composable(GlowinkRoutes.SHOP) { _: NavBackStackEntry ->
            com.example.glowink.ui.screens.ShopScreen(
                viewModel = sharedViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(GlowinkRoutes.GAMES) { _: NavBackStackEntry ->
            GamesScreen(
                viewModel = sharedViewModel,
                onTabClick = { index -> navController.navigateToTab(index) },
                onPlayCulebra = { navController.navigate(GlowinkRoutes.GAME_SNAKE) },
                onPlayDuelo = { navController.navigate(GlowinkRoutes.GAME_DUEL) },
                onPlayCarrera = { navController.navigate(GlowinkRoutes.GAME_RACE) },
                onPlayQuiz = { navController.navigate(GlowinkRoutes.GAME_QUIZ) }
            )
        }

        composable(GlowinkRoutes.GAME_SNAKE) { _: NavBackStackEntry ->
            SnakeGameScreen(
                onExit = { navController.popBackStack() },
                onGameOver = { score ->
                    sharedViewModel.grantGameReward(coinsEarned = (score / 5).coerceAtLeast(2)) { stats ->
                        stats.copy(highScoreCulebra = maxOf(stats.highScoreCulebra, score))
                    }
                }
            )
        }

        composable(GlowinkRoutes.GAME_DUEL) { _: NavBackStackEntry ->
            DuelGameScreen(
                onExit = { navController.popBackStack() },
                onMatchOver = { topWon ->
                    sharedViewModel.grantGameReward(coinsEarned = if (topWon) 25 else 10) { stats ->
                        stats.copy(
                            partidasDuelo = stats.partidasDuelo + 1,
                            victoriasDuelo = stats.victoriasDuelo + if (topWon) 1 else 0
                        )
                    }
                }
            )
        }

        composable(GlowinkRoutes.GAME_RACE) { _: NavBackStackEntry ->
            RaceGameScreen(
                numPlayers = 2,
                onExit = { navController.popBackStack() },
                onGameOver = { won ->
                    sharedViewModel.grantGameReward(coinsEarned = if (won) 40 else 15) { stats: GameStats ->
                        stats.copy(victoriasCarrera = stats.victoriasCarrera + if (won) 1 else 0)
                    }
                }
            )
        }

        composable(GlowinkRoutes.GAME_QUIZ) { _: NavBackStackEntry ->
            QuizGameScreen(
                onExit = { navController.popBackStack() },
                onGameOver = { score ->
                    sharedViewModel.grantGameReward(coinsEarned = (score / 4).coerceAtLeast(3)) { stats ->
                        stats.copy(highScoreQuiz = maxOf(stats.highScoreQuiz, score))
                    }
                }
            )
        }

        composable(GlowinkRoutes.CHAT_SCREEN) { _: NavBackStackEntry ->
            ChatScreen(
                viewModel = sharedViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(GlowinkRoutes.CAMERA) { _: NavBackStackEntry ->
            CameraScreen(
                viewModel = sharedViewModel,
                onPublishSuccess = {
                    navController.navigate(GlowinkRoutes.GLOW_FEED) {
                        popUpTo(GlowinkRoutes.CAMERA) { inclusive = true }
                    }
                },
                onCloseClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(GlowinkRoutes.GLOW_FEED) { _: NavBackStackEntry ->
            GlowFeedScreen(
                viewModel = sharedViewModel,
                onCloseFeed = {
                    navController.popBackStack(GlowinkRoutes.CHATS_LIST, inclusive = false)
                }
            )
        }

        composable(GlowinkRoutes.DISCOVER) { _: NavBackStackEntry ->
            DiscoverScreen(
                viewModel = sharedViewModel,
                onTabClick = { index -> navController.navigateToTab(index) },
                onCommunityClick = { id -> navController.navigate("community_detail/$id") }
            )
        }

        composable(GlowinkRoutes.COMMUNITY_DETAIL) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            com.example.glowink.ui.screens.CommunityDetailScreen(
                viewModel = sharedViewModel,
                communityId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(GlowinkRoutes.CLAN_RANKING) { _: NavBackStackEntry ->
            com.example.glowink.ui.screens.ClanRankingScreen(
                viewModel = sharedViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
