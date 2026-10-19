package com.archi.dreaming.ui.dreams

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.archi.dreaming.data.DreamDatabase
import com.archi.dreaming.data.DreamEntity
import com.archi.dreaming.data.StepEntity
import com.archi.dreaming.data.StepProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DreamsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DreamDatabase.get(application).dreamDao()
    private val stepDao = DreamDatabase.get(application).stepDao()

    val dreams: StateFlow<List<DreamEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Прогресс шагов по каждой мечте: dreamId → сколько выполнено из скольких. */
    val progress: StateFlow<Map<Long, StepProgress>> = stepDao.observeProgress()
        .map { list -> list.associateBy { it.dreamId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** Копирует обрезанное фото (если есть) во внутреннее хранилище и сохраняет мечту в базу. */
    fun addDream(title: String, croppedPhoto: Uri?, onSaved: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val path = croppedPhoto?.let(::copyPhoto)
                // Новая мечта встаёт первой
                val position = (dao.minPosition() ?: 0) - 1
                dao.insert(DreamEntity(title = title.trim(), imagePath = path, position = position))
            }
            onSaved()
        }
    }

    /** Обновляет название и фото; старый файл картинки удаляется, если фото заменили или убрали. */
    fun updateDream(id: Long, title: String, photo: PhotoChange, onSaved: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dream = dao.getById(id) ?: return@withContext
                val path = when (photo) {
                    PhotoChange.Unchanged -> dream.imagePath
                    PhotoChange.Removed -> null
                    is PhotoChange.New -> copyPhoto(photo.uri)
                }
                dao.update(dream.copy(title = title.trim(), imagePath = path))
                if (path != dream.imagePath) dream.imagePath?.let { File(it).delete() }
            }
            onSaved()
        }
    }

    /** Сохраняет новый порядок мечт после перетаскивания. */
    fun reorder(dreams: List<DreamEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.reorder(dreams.map { it.id })
        }
    }

    /** Удаляет мечту из базы вместе с файлом её фото. */
    fun deleteDream(dream: DreamEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(dream)
            dream.imagePath?.let { File(it).delete() }
        }
    }

    fun steps(dreamId: Long): Flow<List<StepEntity>> = stepDao.observeForDream(dreamId)

    /** Добавляет шаг в конец списка. */
    fun addStep(dreamId: Long, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val position = (stepDao.maxPosition(dreamId) ?: -1) + 1
            stepDao.insert(StepEntity(dreamId = dreamId, text = text.trim(), position = position))
        }
    }

    fun renameStep(step: StepEntity, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            stepDao.update(step.copy(text = text.trim()))
        }
    }

    fun setStepDone(step: StepEntity, done: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            stepDao.update(step.copy(done = done))
        }
    }

    fun deleteStep(step: StepEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            stepDao.delete(step)
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
