package org.arcade.atomcity.ui.game.maimai.utage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.arcade.atomcity.data.remote.model.maimai.UtageAttribute
import org.arcade.atomcity.data.remote.model.maimai.UtageData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtageInfoBottomSheet(
    onDismiss: () -> Unit,
    utageData: UtageData?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categoryDescriptions = remember(utageData) {
        val map = mutableMapOf<UtageAttributeCategory, String>()
        fun checkAndExtract(list: List<UtageAttribute>, cat: UtageAttributeCategory) {
            val descObjs = list.filter { it.attribute.isNullOrBlank() }
            val texts = descObjs.mapNotNull {
                (it.description ?: it.description_fr ?: it.details_fr ?: it.comments_fr)?.trim()
            }.filter { it.isNotEmpty() }
            if (texts.isNotEmpty()) {
                map[cat] = texts.joinToString("\n\n")
            }
        }
        utageData?.chart_attributes?.let { attrs ->
            checkAndExtract(attrs.pre_dx, UtageAttributeCategory.PRE_DX)
            checkAndExtract(attrs.post_dx, UtageAttributeCategory.POST_DX)
            checkAndExtract(attrs.off_attributes, UtageAttributeCategory.OFF)
            checkAndExtract(attrs.unused_attributes, UtageAttributeCategory.UNUSED)
        }
        map
    }

    val allCategorizedAttributes = remember(utageData) {
        val list = mutableListOf<CategorizedUtageAttribute>()
        utageData?.chart_attributes?.let { attrs ->
            attrs.pre_dx.filter { !it.attribute.isNullOrBlank() }.forEach {
                list.add(CategorizedUtageAttribute(it, UtageAttributeCategory.PRE_DX, "Pre-DX"))
            }
            attrs.post_dx.filter { !it.attribute.isNullOrBlank() }.forEach {
                list.add(CategorizedUtageAttribute(it, UtageAttributeCategory.POST_DX, "Post-DX"))
            }
            attrs.off_attributes.filter { !it.attribute.isNullOrBlank() }.forEach {
                list.add(CategorizedUtageAttribute(it, UtageAttributeCategory.OFF, "Spéciaux"))
            }
            attrs.unused_attributes.filter { !it.attribute.isNullOrBlank() }.forEach {
                list.add(CategorizedUtageAttribute(it, UtageAttributeCategory.UNUSED, "Inutilisés"))
            }
        }
        list
    }

    var selectedCategory by remember { mutableStateOf(UtageAttributeCategory.PRE_DX) }

    val preDxCount = remember(allCategorizedAttributes) { allCategorizedAttributes.count { it.category == UtageAttributeCategory.PRE_DX } }
    val postDxCount = remember(allCategorizedAttributes) { allCategorizedAttributes.count { it.category == UtageAttributeCategory.POST_DX } }
    val offCount = remember(allCategorizedAttributes) { allCategorizedAttributes.count { it.category == UtageAttributeCategory.OFF } }
    val unusedCount = remember(allCategorizedAttributes) { allCategorizedAttributes.count { it.category == UtageAttributeCategory.UNUSED } }

    val filteredAttributes = remember(allCategorizedAttributes, selectedCategory) {
        allCategorizedAttributes.filter { it.category == selectedCategory }
    }

    val currentCategoryDescription = categoryDescriptions[selectedCategory]

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Attributs des charts Utage",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Guide des spécificités des charts 宴 (Utage)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (utageData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Chargement des attributs...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(UtageAttributeCategory.entries.toTypedArray()) { cat ->
                        val isSelected = selectedCategory == cat
                        val count = when (cat) {
                            UtageAttributeCategory.PRE_DX -> preDxCount
                            UtageAttributeCategory.POST_DX -> postDxCount
                            UtageAttributeCategory.OFF -> offCount
                            UtageAttributeCategory.UNUSED -> unusedCount
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (filteredAttributes.isEmpty() && currentCategoryDescription.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun attribut dans cette catégorie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!currentCategoryDescription.isNullOrBlank()) {
                            item(key = "desc_${selectedCategory.name}") {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = currentCategoryDescription,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                lineHeight = 20.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        items(
                            items = filteredAttributes,
                            key = { "attr_${it.category.name}_${it.attribute.attribute}" }
                        ) { catAttr ->
                            ExpressiveAttributeCard(item = catAttr)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveAttributeCard(
    item: CategorizedUtageAttribute,
    modifier: Modifier = Modifier
) {
    val attr = item.attribute
    val detailText = attr.details_fr ?: attr.comments_fr ?: attr.description ?: ""

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (attr.img != null) {
                    AsyncImage(
                        model = attr.img,
                        contentDescription = attr.attribute,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                } else {
                    Text(
                        text = attr.attribute?.take(1) ?: "宴",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attr.attribute ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (detailText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp,
                            letterSpacing = 0.15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
