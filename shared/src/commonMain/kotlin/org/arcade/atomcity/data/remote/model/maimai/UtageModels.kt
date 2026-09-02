package org.arcade.atomcity.data.remote.model.maimai

import kotlinx.serialization.Serializable

@Serializable
data class UtageData(
    val source: String? = null,
    val chart_attributes: ChartAttributes? = null,
    val utage_chart_list: List<UtageChart> = emptyList()
)

@Serializable
data class ChartAttributes(
    val pre_dx: List<UtageAttribute> = emptyList(),
    val post_dx: List<UtageAttribute> = emptyList(),
    val off_attributes: List<UtageAttribute> = emptyList(),
    val unused_attributes: List<UtageAttribute> = emptyList()
)

@Serializable
data class UtageAttribute(
    val img: String? = null,
    val attribute: String? = null,
    val details_fr: String? = null,
    val comments_fr: String? = null,
    val description: String? = null,
    val description_fr: String? = null
)

@Serializable
data class UtageChart(
    val song: String,
    val attribute: String? = null,
    val comment: String? = null,
    val forced_options: String? = null,
    val details_fr: String? = null,
    val variants: List<UtageVariant>? = null
)

@Serializable
data class UtageVariant(
    val comment: String? = null,
    val attribute: String? = null,
    val forced_options: String? = null,
    val details_fr: String? = null
)
