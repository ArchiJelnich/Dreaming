package com.archi.dreaming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.archi.dreaming.ui.dreams.DreamDetailScreen
import com.archi.dreaming.ui.dreams.DreamEditorScreen
import com.archi.dreaming.ui.dreams.DreamsScreen
import com.archi.dreaming.ui.dreams.DreamsViewModel
import com.archi.dreaming.ui.dreams.PhotoChange
import com.archi.dreaming.ui.theme.DreamingTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreamingTheme {
                val viewModel: DreamsViewModel = viewModel()
                val dreams by viewModel.dreams.collectAsStateWithLifecycle()
                val progress by viewModel.progress.collectAsStateWithLifecycle()
                var addingDream by rememberSaveable { mutableStateOf(false) }
                var editingDreamId by rememberSaveable { mutableStateOf<Long?>(null) }
                val editingDream = editingDreamId?.let { id -> dreams.find { it.id == id } }
                var openedDreamId by rememberSaveable { mutableStateOf<Long?>(null) }
                val openedDream = openedDreamId?.let { id -> dreams.find { it.id == id } }

                when {
                    addingDream -> DreamEditorScreen(
                        screenTitle = R.string.new_dream,
                        onSave = { title, photo, onSaved ->
                            viewModel.addDream(title, (photo as? PhotoChange.New)?.uri, onSaved)
                        },
                        onBack = { addingDream = false },
                    )

                    editingDream != null -> key(editingDream.id) {
                        DreamEditorScreen(
                            screenTitle = R.string.edit_dream_title,
                            initialTitle = editingDream.title,
                            initialPhoto = editingDream.imagePath,
                            onSave = { title, photo, onSaved ->
                                viewModel.updateDream(editingDream.id, title, photo, onSaved)
                            },
                            onBack = { editingDreamId = null },
                        )
                    }

                    openedDream != null -> key(openedDream.id) {
                        val steps by remember { viewModel.steps(openedDream.id) }
                            .collectAsStateWithLifecycle(initialValue = emptyList())
                        DreamDetailScreen(
                            dream = openedDream,
                            steps = steps,
                            onBack = { openedDreamId = null },
                            // После редактирования возвращаемся на экран мечты
                            onEditDream = { editingDreamId = openedDream.id },
                            onAddStep = { viewModel.addStep(openedDream.id, it) },
                            onRenameStep = viewModel::renameStep,
                            onStepDoneChange = viewModel::setStepDone,
                            onDeleteStep = viewModel::deleteStep,
                        )
                    }

                    else -> DreamsScreen(
                        dreams = dreams,
                        progress = progress,
                        onAddClick = { addingDream = true },
                        onDreamClick = { openedDreamId = it.id },
                        onEditClick = { editingDreamId = it.id },
                        onDeleteClick = viewModel::deleteDream,
                        onReorder = viewModel::reorder,
                    )
                }
            }
        }
    }
}
