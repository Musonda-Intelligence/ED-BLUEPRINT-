package com.edblueprint.app.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edblueprint.app.data.remote.MaterialDto
import com.edblueprint.app.ui.app.rememberContainer
import com.edblueprint.app.ui.app.rememberLoader
import com.edblueprint.app.ui.components.DetailScreen
import com.edblueprint.app.ui.components.EdCard
import com.edblueprint.app.ui.components.EmptyState
import com.edblueprint.app.ui.components.LoadContent
import com.edblueprint.app.ui.components.SectionTitle
import com.edblueprint.app.ui.components.TabScreen
import com.edblueprint.app.ui.theme.EdOrange
import com.edblueprint.app.ui.theme.EdTextPrimary
import com.edblueprint.app.ui.theme.EdTextSecondary

@Composable
fun CoursesScreen(onOpenTopic: (id: Long, title: String) -> Unit) {
    val repository = rememberContainer().contentRepository
    val loader = rememberLoader("topics") { repository.topics() }
    val state by loader.state.collectAsStateWithLifecycle()

    TabScreen(title = "Courses", subtitle = "ED 111 Engineering Drawing") {
        LoadContent(state = state, onRetry = loader::reload) { topics ->
            if (topics.isEmpty()) {
                EmptyState(
                    title = "No topics yet",
                    message = "Topics and their lessons appear here once they are published."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topics.forEach { topic ->
                        EdCard(onClick = { onOpenTopic(topic.id, topic.title) }) {
                            Text(
                                text = topic.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = EdTextPrimary
                            )
                            if (!topic.description.isNullOrBlank()) {
                                Text(
                                    text = topic.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EdTextSecondary,
                                    maxLines = 2,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Text(
                                text = "${topic.materialCount} lessons  ·  ${topic.quizCount} quizzes",
                                style = MaterialTheme.typography.labelSmall,
                                color = EdOrange,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicScreen(
    topicId: Long,
    title: String,
    onBack: () -> Unit,
    onOpenMaterial: (MaterialDto) -> Unit
) {
    val repository = rememberContainer().contentRepository
    val loader = rememberLoader("topic-$topicId") { repository.topicMaterials(topicId) }
    val state by loader.state.collectAsStateWithLifecycle()

    DetailScreen(title = title, onBack = onBack) {
        LoadContent(state = state, onRetry = loader::reload) { materials ->
            MaterialList(
                materials = materials,
                emptyTitle = "Nothing here yet",
                emptyMessage = "Lessons for this topic appear here once they are published.",
                onOpen = onOpenMaterial
            )
        }
    }
}

@Composable
fun PastPapersScreen(onOpenMaterial: (MaterialDto) -> Unit) {
    val repository = rememberContainer().contentRepository
    val loader = rememberLoader("papers") { repository.papers() }
    val state by loader.state.collectAsStateWithLifecycle()

    TabScreen(title = "Past Papers", subtitle = "Free for every registered student") {
        LoadContent(state = state, onRetry = loader::reload) { papers ->
            MaterialList(
                materials = papers,
                emptyTitle = "No papers yet",
                emptyMessage = "Past examination papers and marking schemes appear here once they are uploaded.",
                onOpen = onOpenMaterial
            )
        }
    }
}

/** Shown when a student taps locked content, or the server answers 403. */
@Composable
fun SubscriptionRequiredScreen(onBack: () -> Unit) {
    DetailScreen(title = "Subscription required", onBack = onBack) {
        SectionTitle("Premium content")
        EdCard(borderColor = EdOrange) {
            Text(
                text = "Videos and course documents are for subscribed students.",
                style = MaterialTheme.typography.titleMedium,
                color = EdTextPrimary
            )
            Text(
                text = "Past papers stay free. To unlock everything else, subscribe and your " +
                    "account will be upgraded as soon as ED Blueprint confirms your payment. " +
                    "Online payment inside the app is not connected yet, so a subscription " +
                    "cannot be started from here.",
                style = MaterialTheme.typography.bodyMedium,
                color = EdTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
