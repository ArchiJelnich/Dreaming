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
import com.archi.dreaming.ui.dreams.AddDreamScreen
import com.archi.dreaming.ui.dreams.DreamsScreen
import com.archi.dreaming.ui.dreams.DreamsViewModel
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

                if (addingDream) {
                    AddDreamScreen(
                        onSave = viewModel::addDream,
                        onBack = { addingDream = false },
                    )
                } else {
                    DreamsScreen(
                        dreams = dreams,
                        onAddClick = { addingDream = true },
                    )
                }
            }
        }
    }
}
