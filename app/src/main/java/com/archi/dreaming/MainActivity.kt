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
                var addingDream by rememberSaveable { mutableStateOf(false) }
                var editingDreamId by rememberSaveable { mutableStateOf<Long?>(null) }
                val editingDream = editingDreamId?.let { id -> dreams.find { it.id == id } }

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

                    else -> DreamsScreen(
                        dreams = dreams,
                        onAddClick = { addingDream = true },
                        onEditClick = { editingDreamId = it.id },
                        onDeleteClick = viewModel::deleteDream,
                        onReorder = viewModel::reorder,
                    )
                }
            }
        }
    }
}
