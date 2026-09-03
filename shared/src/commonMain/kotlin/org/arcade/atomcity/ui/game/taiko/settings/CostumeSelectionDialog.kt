package org.arcade.atomcity.ui.game.taiko.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.TaikoServerCostume
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel

@Composable
fun SelectionDialog(
    title: String,
    items: List<String>,
    type: String,
    costumes: List<TaikoServerCostume>? = null,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    taikoViewModel: TaikoViewModel,
    settings: TaikoServerUserSettingsResponse?
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredItems = remember(items, searchQuery, costumes, type) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { item ->
                val id = extractId(item)
                val idStr = id.toString()
                val costume = costumes?.find { it.costumeId == id && it.costumeType == type }
                val name = costume?.costumeName ?: ""
                val nameEn = costume?.costumeNameEN ?: ""
                
                idStr.contains(searchQuery, ignoreCase = true) ||
                name.contains(searchQuery, ignoreCase = true) ||
                nameEn.contains(searchQuery, ignoreCase = true) ||
                getItemDisplayName(type, item).contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (type != "speed" && type != "random") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        placeholder = { Text("Rechercher...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(
                        when (type) {
                            "speed" -> 2
                            "title" -> 2
                            else -> 3
                        }
                    ),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredItems) { item ->
                        val id = extractId(item)
                        
                        Surface(
                            onClick = { onSelect(item) },
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.aspectRatio(
                                when (type) {
                                    "speed" -> 1.1f
                                    "title" -> 1.8f
                                    else -> 0.85f
                                }
                            ),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center, 
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                ) {
                                    if (item == "Normal") {
                                        Surface(
                                            modifier = Modifier.size(42.dp),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    "OFF",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    } else {
                                        val isPlain = id == 0
                                        
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            if (isPlain && settings != null) {
                                                if (type == "body") {
                                                    AsyncImage(
                                                        model = taikoViewModel.getMaskImageUrl("body", "body", id),
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Fit,
                                                        colorFilter = ColorFilter.tint(taikoViewModel.getDonColor(settings.bodyColor))
                                                    )
                                                } else if (type == "face") {
                                                    AsyncImage(
                                                        model = taikoViewModel.getMaskImageUrl("body", "face", id),
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Fit,
                                                        colorFilter = ColorFilter.tint(taikoViewModel.getDonColor(settings.faceColor))
                                                    )
                                                } else if (type == "head") {
                                                    AsyncImage(
                                                        model = taikoViewModel.getMaskImageUrl("head", "head", id),
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Fit,
                                                        colorFilter = ColorFilter.tint(taikoViewModel.getDonColor(settings.bodyColor))
                                                    )
                                                }
                                            }

                                            AsyncImage(
                                                model = if (type == "title" || type == "speed" || type == "random") {
                                                    buildImageUrl(type, item)
                                                } else {
                                                    taikoViewModel.getCostumeImageUrl(type, id)
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize().then(
                                                    if (type == "puchi") Modifier.scale(2.2f).offset( y= -(10).dp, x= (12).dp) else Modifier
                                                ),
                                                contentScale = ContentScale.Fit,
                                                filterQuality = if (type == "speed" || type == "random") FilterQuality.None else FilterQuality.Low
                                            )
                                        }
                                    }
                                }
                                
                                val id = extractId(item)
                                val costume = costumes?.find { it.costumeId == id && it.costumeType == type }
                                val rawName = costume?.costumeNameEN ?: costume?.costumeName
                                val name = if (rawName == "Plain Don") "Défaut" else rawName

                                Text(
                                    text = name ?: getItemDisplayName(type, item),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 2,
                                    minLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                                )
                                
                                if (name != null) {
                                    Text(
                                        text = "ID: $id",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Fermer", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}
