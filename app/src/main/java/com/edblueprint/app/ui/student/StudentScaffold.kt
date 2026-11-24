package com.edblueprint.app.ui.student

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.edblueprint.app.data.remote.MaterialDto
import com.edblueprint.app.data.remote.UserDto
import com.edblueprint.app.ui.quiz.QuizScreen
import com.edblueprint.app.ui.theme.EdBlack
import com.edblueprint.app.ui.theme.EdMaroon
import com.edblueprint.app.ui.theme.EdOrange
import com.edblueprint.app.ui.theme.EdSurface
import com.edblueprint.app.ui.theme.EdTextSecondary
import com.edblueprint.app.ui.viewer.PdfViewerScreen
import com.edblueprint.app.ui.viewer.VideoPlayerScreen

private enum class StudentTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Home("home", "Home", Icons.Filled.Home),
    Courses("courses", "Courses", Icons.AutoMirrored.Filled.MenuBook),
    PastPapers("papers", "Past Papers", Icons.Filled.Description),
    Quizzes("quizzes", "Quizzes", Icons.Filled.Quiz),
    Profile("profile", "Profile", Icons.Filled.Person)
}

private const val ROUTE_TOPIC = "topic/{id}/{title}"
private const val ROUTE_PDF = "pdf/{id}/{title}"
private const val ROUTE_VIDEO = "video/{id}/{title}"
private const val ROUTE_QUIZ = "quiz/{id}/{title}"
private const val ROUTE_SUBSCRIBE = "subscribe"

private val idAndTitleArgs = listOf(
    navArgument("id") { type = NavType.LongType },
    navArgument("title") { type = NavType.StringType }
)

private fun NavHostController.goTo(kind: String, id: Long, title: String) {
    navigate("$kind/$id/${Uri.encode(title)}")
}

/** The student app: five tabs, plus the topic, viewer, quiz and subscription screens. */
@Composable
fun StudentScaffold(user: UserDto, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = StudentTab.entries.any { it.route == currentRoute }

    // Locked items go to the subscription screen. The server also refuses the file itself.
    val openMaterial: (MaterialDto) -> Unit = { material ->
        when {
            material.locked -> navController.navigate(ROUTE_SUBSCRIBE)
            material.isVideo -> navController.goTo("video", material.id, material.title)
            else -> navController.goTo("pdf", material.id, material.title)
        }
    }
    val openQuiz: (Long, String) -> Unit = { id, title -> navController.goTo("quiz", id, title) }

    // Replaces the viewer with the subscription screen so Back does not return to a refused file.
    val subscriptionRequired: () -> Unit = {
        navController.popBackStack()
        navController.navigate(ROUTE_SUBSCRIBE)
    }

    Scaffold(
        containerColor = EdBlack,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = EdSurface) {
                    StudentTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EdOrange,
                                selectedTextColor = EdOrange,
                                indicatorColor = EdMaroon,
                                unselectedIconColor = EdTextSecondary,
                                unselectedTextColor = EdTextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = StudentTab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(StudentTab.Home.route) {
                HomeScreen(user = user, onOpenMaterial = openMaterial, onOpenQuiz = openQuiz)
            }
            composable(StudentTab.Courses.route) {
                CoursesScreen(onOpenTopic = { id, title -> navController.goTo("topic", id, title) })
            }
            composable(StudentTab.PastPapers.route) {
                PastPapersScreen(onOpenMaterial = openMaterial)
            }
            composable(StudentTab.Quizzes.route) {
                QuizzesScreen(onOpenQuiz = openQuiz)
            }
            composable(StudentTab.Profile.route) {
                ProfileScreen(user = user, onLogout = onLogout)
            }

            composable(ROUTE_TOPIC, arguments = idAndTitleArgs) { entry ->
                TopicScreen(
                    topicId = entry.arguments?.getLong("id") ?: 0L,
                    title = entry.arguments?.getString("title").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenMaterial = openMaterial
                )
            }
            composable(ROUTE_PDF, arguments = idAndTitleArgs) { entry ->
                PdfViewerScreen(
                    materialId = entry.arguments?.getLong("id") ?: 0L,
                    title = entry.arguments?.getString("title").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onSubscriptionRequired = subscriptionRequired
                )
            }
            composable(ROUTE_VIDEO, arguments = idAndTitleArgs) { entry ->
                VideoPlayerScreen(
                    materialId = entry.arguments?.getLong("id") ?: 0L,
                    title = entry.arguments?.getString("title").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onSubscriptionRequired = subscriptionRequired
                )
            }
            composable(ROUTE_QUIZ, arguments = idAndTitleArgs) { entry ->
                QuizScreen(
                    quizId = entry.arguments?.getLong("id") ?: 0L,
                    title = entry.arguments?.getString("title").orEmpty(),
                    onExit = { navController.popBackStack() }
                )
            }
            composable(ROUTE_SUBSCRIBE) {
                SubscriptionRequiredScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
