package com.archi.dreaming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archi.dreaming.ui.dreams.DreamsScreen
import com.archi.dreaming.ui.theme.DreamingTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreamingTheme {
                DreamsScreen()
            }
        }
    }
}
