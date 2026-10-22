package com.archi.dreaming

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
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
import com.archi.dreaming.ui.dreams.DreamsTab
import com.archi.dreaming.ui.dreams.DreamsViewModel
import com.archi.dreaming.ui.dreams.PhotoChange
import com.archi.dreaming.ui.theme.DreamingTheme

class MainActivity : ComponentActivity() {

    private var dreamToOpen by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) dreamToOpen = intent.dreamId()
        enableEdgeToEdge()
        setContent {
            DreamingTheme {
                val viewModel: DreamsViewModel = viewModel()
                val dreams by viewModel.dreams.collectAsStateWithLifecycle()
                val progress by viewModel.progress.collectAsStateWithLifecycle()
                var addingDream by rememberSaveable { mutableStateOf(false) }
                var editingDreamId by rememberSaveable { mutableStateOf<Long?>(null) }
                val editingDream = editingDreamId?.let { id -> dreams.find { it.id == id } }
                var tab by rememberSaveable { mutableStateOf(DreamsTab.Dreams) }
                var openedDreamId by rememberSaveable { mutableStateOf<Long?>(null) }
                val openedDream = openedDreamId?.let { id -> dreams.find { it.id == id } }

                LaunchedEffect(dreamToOpen) {
                    dreamToOpen?.let {
                        addingDream = false
                        editingDreamId = null
                        tab = DreamsTab.Dreams
                        openedDreamId = it
                        dreamToOpen = null
                    }
                }

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
                            onEditDream = { editingDreamId = openedDream.id },
                            onAddStep = { viewModel.addStep(openedDream.id, it) },
                            onRenameStep = viewModel::renameStep,
                            onStepDoneChange = viewModel::setStepDone,
                            onDeleteStep = viewModel::deleteStep,
                            onFulfill = { date ->
                                viewModel.fulfill(openedDream, date)
                                openedDreamId = null
                                tab = DreamsTab.Fulfilled
                            },
                            onReturnToDreams = {
                                viewModel.returnToDreams(openedDream)
                                openedDreamId = null
                                tab = DreamsTab.Dreams
                            },
                        )
                    }

                    else -> DreamsScreen(
                        tab = tab,
                        onTabChange = { tab = it },
                        dreams = dreams.filter { (it.fulfilledAt != null) == (tab == DreamsTab.Fulfilled) },
                        progress = progress,
                        onAddClick = { addingDream = true },
                        onDreamClick = { openedDreamId = it.id },
                        onEditClick = { editingDreamId = it.id },
                        onDeleteClick = viewModel::deleteDream,
                        onReturnToDreams = viewModel::returnToDreams,
                        onReorder = viewModel::reorder,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        dreamToOpen = intent.dreamId()
    }

    private fun Intent.dreamId(): Long? = getLongExtra(EXTRA_DREAM_ID, -1).takeIf { it > 0 }

    companion object {
        private const val EXTRA_DREAM_ID = "dream_id"

        fun openDreamIntent(context: Context, dreamId: Long?): Intent =
            Intent(context, MainActivity::class.java).apply {
                if (dreamId != null) putExtra(EXTRA_DREAM_ID, dreamId)
            }
    }
}
