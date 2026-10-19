package com.archi.dreaming.ui.dreams

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.archi.dreaming.R
import com.archi.dreaming.data.DreamEntity
import com.archi.dreaming.data.StepProgress
import com.archi.dreaming.ui.theme.Cocoa900
import com.archi.dreaming.ui.theme.DreamingTheme
import com.archi.dreaming.ui.theme.NoPhotoGradients
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState
import java.io.File

private enum class NavTab(@StringRes val title: Int, @DrawableRes val icon: Int) {
    Dreams(R.string.nav_dreams, R.drawable.ic_home),
    Favorites(R.string.nav_favorites, R.drawable.ic_favorite),
    Goals(R.string.nav_goals, R.drawable.ic_star),
    Profile(R.string.nav_profile, R.drawable.ic_person),
}

// Лёгкий наклон карточек создаёт «хаотичную» доску мечт
private val Tilts = listOf(-2.5f, 1.5f, 2f, -1.5f, -3f, 2.5f, 1f, -2f, 3f, -1f)

@Composable
fun DreamsScreen(
    dreams: List<DreamEntity>,
    progress: Map<Long, StepProgress>,
    onAddClick: () -> Unit,
    onDreamClick: (DreamEntity) -> Unit,
    onEditClick: (DreamEntity) -> Unit,
    onDeleteClick: (DreamEntity) -> Unit,
    onReorder: (List<DreamEntity>) -> Unit,
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val gridState = rememberLazyStaggeredGridState()

    // Локальная копия списка: во время перетаскивания порядок меняется здесь,
    // а в базу сохраняется, когда палец отпущен
    var items by remember { mutableStateOf(dreams) }
    var dragging by remember { mutableStateOf(false) }
    var moved by remember { mutableStateOf(false) }
    LaunchedEffect(dreams) {
        if (!dragging) items = dreams
    }
    val reorderState = rememberReorderableLazyStaggeredGridState(gridState) { from, to ->
        items = items.toMutableList().apply { add(to.index, removeAt(from.index)) }
        moved = true
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    var selectedTab by rememberSaveable { mutableIntStateOf(NavTab.Dreams.ordinal) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                NavTab.entries.forEach { tab ->
                    val title = stringResource(tab.title)
                    NavigationBarItem(
                        selected = selectedTab == tab.ordinal,
                        onClick = {
                            selectedTab = tab.ordinal
                            // Остальные разделы пока заглушки
                            if (tab != NavTab.Dreams) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.stub_section, title),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = stringResource(R.string.my_dreams),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
            )

            if (items.isEmpty()) {
                EmptyDreams(Modifier.weight(1f))
            } else LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                state = gridState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 14.dp,
            ) {
                items(items, key = { it.id }) { dream ->
                    ReorderableItem(reorderState, key = dream.id) { isDragging ->
                        var menuExpanded by remember { mutableStateOf(false) }
                        DreamCard(
                            dream = dream,
                            progress = progress[dream.id],
                            isDragging = isDragging,
                            menuExpanded = menuExpanded,
                            onMenuDismiss = { menuExpanded = false },
                            onClick = { onDreamClick(dream) },
                            onEditClick = { onEditClick(dream) },
                            onDeleteClick = { onDeleteClick(dream) },
                            // Долгое нажатие поднимает карточку: её можно перетащить,
                            // а если отпустить на месте — откроется меню действий
                            dragModifier = Modifier.longPressDraggableHandle(
                                onDragStarted = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    dragging = true
                                    moved = false
                                },
                                onDragStopped = {
                                    dragging = false
                                    if (moved) onReorder(items) else menuExpanded = true
                                },
                            ),
                        )
                    }
                }
            }

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .height(56.dp),
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_dream), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun EmptyDreams(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.empty_dreams_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_dreams_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DreamCard(
    dream: DreamEntity,
    progress: StepProgress?,
    isDragging: Boolean,
    menuExpanded: Boolean,
    onMenuDismiss: () -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    dragModifier: Modifier,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val lift by animateFloatAsState(if (isDragging) 1f else 0f, label = "lift")
    val tilt = Tilts[(dream.id % Tilts.size).toInt()]

    Box {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp + 9.dp * lift),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            // Поднятая карточка чуть увеличивается и выпрямляется
            modifier = Modifier.graphicsLayer {
                rotationZ = tilt * (1f - lift)
                scaleX = 1f + 0.05f * lift
                scaleY = 1f + 0.05f * lift
            },
        ) {
            // Короткое нажатие открывает мечту; долгое обрабатывает dragModifier
            Column(
                modifier = Modifier
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = {},
                        hapticFeedbackEnabled = false,
                    )
                    .then(dragModifier),
            ) {
                if (dream.imagePath != null) {
                    Box {
                        // Высота карточки следует пропорциям обрезанного фото
                        AsyncImage(
                            model = File(dream.imagePath),
                            contentDescription = dream.title,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        progress?.let { CardProgress(it) }
                    }
                    Text(
                        text = dream.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp),
                    )
                } else {
                    Box {
                        NoPhotoContent(dream)
                        progress?.let { CardProgress(it) }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = onMenuDismiss,
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit_dream)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_edit), contentDescription = null) },
                onClick = {
                    onMenuDismiss()
                    onEditClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete_dream)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_delete), contentDescription = null) },
                onClick = {
                    onMenuDismiss()
                    confirmDelete = true
                },
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_dream_question, dream.title)) },
            text = { Text(stringResource(R.string.delete_dream_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDeleteClick()
                    },
                ) { Text(stringResource(R.string.delete_dream)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

/** Круг прогресса шагов в правом верхнем углу карточки. */
@Composable
private fun BoxScope.CardProgress(progress: StepProgress) {
    ProgressPie(
        progress = progress,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(10.dp),
    )
}

/** Мечта без фото: мягкий градиент с крупным названием, высота немного разная для «хаоса». */
@Composable
private fun NoPhotoContent(dream: DreamEntity) {
    val (start, end) = NoPhotoGradients[(dream.id % NoPhotoGradients.size).toInt()]
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = (110 + (dream.id % 4).toInt() * 20).dp)
            .background(Brush.linearGradient(listOf(start, end))),
    ) {
        Text(
            text = dream.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Cocoa900,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DreamsScreenEmptyPreview() {
    DreamingTheme { DreamsScreen(dreams = emptyList(), progress = emptyMap(), onAddClick = {}, onDreamClick = {}, onEditClick = {}, onDeleteClick = {}, onReorder = {}) }
}
