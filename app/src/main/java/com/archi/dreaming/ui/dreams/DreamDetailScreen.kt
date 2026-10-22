package com.archi.dreaming.ui.dreams

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.archi.dreaming.R
import com.archi.dreaming.data.DreamEntity
import com.archi.dreaming.data.StepEntity
import com.archi.dreaming.ui.theme.NoPhotoGradients
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamDetailScreen(
    dream: DreamEntity,
    steps: List<StepEntity>,
    onBack: () -> Unit,
    onEditDream: () -> Unit,
    onAddStep: (String) -> Unit,
    onRenameStep: (StepEntity, String) -> Unit,
    onStepDoneChange: (StepEntity, Boolean) -> Unit,
    onDeleteStep: (StepEntity) -> Unit,
    onFulfill: (dateUtcMillis: Long) -> Unit,
    onReturnToDreams: () -> Unit,
) {
    var editingStepId by rememberSaveable { mutableStateOf<Long?>(null) }
    var newStepText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "header") { DreamHeader(dream, onEditDream) }

            item(key = "steps_header") { StepsHeader(steps, showProgress = dream.fulfilledAt == null) }

            items(steps, key = { it.id }) { step ->
                StepRow(
                    step = step,
                    editing = editingStepId == step.id,
                    onStartEdit = { editingStepId = step.id },
                    onRename = { text ->
                        if (text.isNotBlank()) onRenameStep(step, text)
                        editingStepId = null
                    },
                    onDoneChange = { onStepDoneChange(step, it) },
                    onDelete = { onDeleteStep(step) },
                )
            }

            item(key = "new_step") {
                StepTextField(
                    value = newStepText,
                    onValueChange = { newStepText = it },
                    placeholder = stringResource(R.string.new_step_hint),
                    actionIcon = R.drawable.ic_add,
                    actionDescription = stringResource(R.string.add_step),
                    onSubmit = {
                        if (newStepText.text.isNotBlank()) {
                            onAddStep(newStepText.text)
                            newStepText = TextFieldValue()
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            item(key = "status") {
                if (dream.fulfilledAt == null) {
                    FulfillSection(onFulfill)
                } else {
                    OutlinedButton(
                        onClick = onReturnToDreams,
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .height(56.dp),
                    ) {
                        Icon(painterResource(R.drawable.ic_home), contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.return_to_dreams))
                    }
                }
            }
        }
    }
}

@Composable
private fun DreamHeader(dream: DreamEntity, onEdit: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val haptics = LocalHapticFeedback.current
    val editOnLongPress = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onEdit()
            },
        )
    }
    if (dream.imagePath != null) {
        AsyncImage(
            model = File(dream.imagePath),
            contentDescription = dream.title,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .then(editOnLongPress),
        )
    } else {
        val (start, end) = NoPhotoGradients[(dream.id % NoPhotoGradients.size).toInt()]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .clip(shape)
                .background(Brush.linearGradient(listOf(start, end)))
                .then(editOnLongPress),
        )
    }
    Text(
        text = dream.title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .padding(top = 20.dp, bottom = 8.dp)
            .then(editOnLongPress),
    )
    if (dream.fulfilledAt != null) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_star),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.fulfilled_on, formatLongDate(dream.fulfilledAt)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FulfillSection(onFulfill: (Long) -> Unit) {
    val today = remember { todayUtcMillis() }
    var date by rememberSaveable { mutableLongStateOf(today) }
    var pickingDate by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 24.dp),
    ) {
        OutlinedButton(
            onClick = { pickingDate = true },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(painterResource(R.drawable.ic_calendar), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.fulfill_date, formatLongDate(date)))
        }
        Button(
            onClick = { onFulfill(date) },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(painterResource(R.drawable.ic_star), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.fulfill), style = MaterialTheme.typography.titleMedium)
        }
    }

    if (pickingDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= today
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { date = it }
                        pickingDate = false
                    },
                ) { Text(stringResource(R.string.done)) }
            },
            dismissButton = {
                TextButton(onClick = { pickingDate = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun StepsHeader(steps: List<StepEntity>, showProgress: Boolean) {
    val done = steps.count { it.done }
    Text(
        text = stringResource(R.string.steps_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp),
    )
    if (steps.isEmpty()) {
        Text(
            text = stringResource(R.string.steps_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    } else if (showProgress) {
        Text(
            text = stringResource(R.string.steps_progress, done, steps.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        LinearProgressIndicator(
            progress = { done.toFloat() / steps.size },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp)),
        )
    }
}

@Composable
private fun StepRow(
    step: StepEntity,
    editing: Boolean,
    onStartEdit: () -> Unit,
    onRename: (String) -> Unit,
    onDoneChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    if (editing) {
        var text by rememberSaveable(step.id, stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(step.text, TextRange(step.text.length)))
        }
        StepTextField(
            value = text,
            onValueChange = { text = it },
            autoFocus = true,
            placeholder = stringResource(R.string.new_step_hint),
            actionIcon = R.drawable.ic_check,
            actionDescription = stringResource(R.string.save_step),
            onSubmit = { onRename(text.text) },
        )
        return
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = step.done,
                onCheckedChange = onDoneChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Text(
                text = step.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (step.done) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (step.done) TextDecoration.LineThrough else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onStartEdit)
                    .padding(vertical = 14.dp),
            )
            IconButton(onClick = onStartEdit) {
                Icon(
                    painterResource(R.drawable.ic_edit),
                    contentDescription = stringResource(R.string.edit_step),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.delete_step),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StepTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    actionIcon: Int,
    actionDescription: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        trailingIcon = {
            IconButton(onClick = onSubmit, enabled = value.text.isNotBlank()) {
                Icon(painterResource(actionIcon), contentDescription = actionDescription)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
    )
}
