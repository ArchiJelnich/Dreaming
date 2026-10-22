package com.archi.dreaming.ui.dreams

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.archi.dreaming.R
import java.io.File

sealed interface PhotoChange {
    data object Unchanged : PhotoChange
    data object Removed : PhotoChange
    data class New(val uri: Uri) : PhotoChange
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamEditorScreen(
    @StringRes screenTitle: Int,
    onSave: (title: String, photo: PhotoChange, onSaved: () -> Unit) -> Unit,
    onBack: () -> Unit,
    initialTitle: String = "",
    initialPhoto: String? = null,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var newPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var photoRemoved by rememberSaveable { mutableStateOf(false) }
    val photoModel: Any? = newPhotoUri ?: initialPhoto?.takeUnless { photoRemoved }?.let(::File)
    var saving by rememberSaveable { mutableStateOf(false) }
    val cropTitle = stringResource(R.string.crop_title)
    val colors = MaterialTheme.colorScheme

    BackHandler(enabled = !saving, onBack = onBack)

    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            newPhotoUri = result.uriContent?.toString()
        } else if (result.error != null) {
            Toast.makeText(context, R.string.crop_failed, Toast.LENGTH_SHORT).show()
        }
    }
    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            cropLauncher.launch(
                CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions(
                        guidelines = CropImageView.Guidelines.ON,
                        outputCompressFormat = Bitmap.CompressFormat.JPEG,
                        outputCompressQuality = 90,
                        activityTitle = cropTitle,
                        toolbarColor = colors.surfaceVariant.toArgb(),
                        toolbarTitleColor = colors.onBackground.toArgb(),
                        toolbarBackButtonColor = colors.onBackground.toArgb(),
                        activityMenuIconColor = colors.onBackground.toArgb(),
                        activityMenuTextColor = colors.onBackground.toArgb(),
                        activityBackgroundColor = colors.background.toArgb(),
                        borderCornerColor = colors.primary.toArgb(),
                    ),
                ),
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(screenTitle)) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !saving) {
                        Icon(
                            painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            val hasPhoto = photoModel != null
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (hasPhoto) 4f / 5f else 2f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = !saving) {
                        pickLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
            ) {
                if (hasPhoto) {
                    AsyncImage(
                        model = photoModel,
                        contentDescription = title.ifBlank { null },
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Text(
                    text = stringResource(if (!hasPhoto) R.string.pick_photo else R.string.change_photo),
                    style = if (!hasPhoto) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(if (!hasPhoto) Alignment.Center else Alignment.BottomCenter)
                        .padding(16.dp)
                        .then(
                            if (!hasPhoto) Modifier
                            else Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ),
                )
            }

            if (hasPhoto) {
                TextButton(
                    onClick = {
                        newPhotoUri = null
                        photoRemoved = true
                    },
                    enabled = !saving,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 0.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_delete),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.remove_photo))
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.dream_title_label)) },
                singleLine = true,
                enabled = !saving,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    saving = true
                    val photo = when {
                        newPhotoUri != null -> PhotoChange.New(Uri.parse(newPhotoUri))
                        photoRemoved && initialPhoto != null -> PhotoChange.Removed
                        else -> PhotoChange.Unchanged
                    }
                    onSave(title, photo, onBack)
                },
                enabled = title.isNotBlank() && !saving,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                } else {
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
