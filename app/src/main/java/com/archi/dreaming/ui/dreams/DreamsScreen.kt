package com.archi.dreaming.ui.dreams

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.archi.dreaming.R
import com.archi.dreaming.data.DreamEntity
import com.archi.dreaming.ui.theme.DreamingTheme
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
    onAddClick: () -> Unit,
) {
    val context = LocalContext.current
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

            if (dreams.isEmpty()) {
                EmptyDreams(Modifier.weight(1f))
            } else LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 14.dp,
            ) {
                items(dreams, key = { it.id }) { dream -> DreamCard(dream) }
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
private fun DreamCard(dream: DreamEntity) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.rotate(Tilts[(dream.id % Tilts.size).toInt()]),
    ) {
        // Высота карточки следует пропорциям обрезанного фото
        AsyncImage(
            model = File(dream.imagePath),
            contentDescription = dream.title,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = dream.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DreamsScreenEmptyPreview() {
    DreamingTheme { DreamsScreen(dreams = emptyList(), onAddClick = {}) }
}
