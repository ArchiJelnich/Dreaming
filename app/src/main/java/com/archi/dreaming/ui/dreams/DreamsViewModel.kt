package com.archi.dreaming.ui.dreams

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archi.dreaming.data.DreamDatabase
import com.archi.dreaming.data.DreamEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DreamsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DreamDatabase.get(application).dreamDao()

    val dreams: StateFlow<List<DreamEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Копирует обрезанное фото во внутреннее хранилище и сохраняет мечту в базу. */
    fun addDream(title: String, croppedPhoto: Uri, onSaved: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val path = copyPhoto(croppedPhoto)
                dao.insert(DreamEntity(title = title.trim(), imagePath = path))
            }
            onSaved()
        }
    }

    private fun copyPhoto(source: Uri): String {
        val app = getApplication<Application>()
        val dir = File(app.filesDir, "dreams").apply { mkdirs() }
        val target = File(dir, "${UUID.randomUUID()}.jpg")
        app.contentResolver.openInputStream(source)!!.use { input ->
            target.outputStream().use { input.copyTo(it) }
        }
        return target.absolutePath
    }
}
