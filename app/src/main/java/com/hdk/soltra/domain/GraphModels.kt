package com.hdk.soltra.domain

import kotlinx.serialization.Serializable

@Serializable
enum class GraphType(val label: String) {
    PIE("Pie"),
    DONUT("Cercle"),
    BAR("Rectangle"),
}

@Serializable
enum class GraphPeriod(val label: String) {
    CURRENT_MONTH("Mois courant"),
    PREVIOUS_MONTH("Mois precedent"),
    CURRENT_YEAR("Annee courante"),
    PREVIOUS_YEAR("Annee precedente"),
    CUSTOM("Periode personnalisable"),
}

@Serializable
enum class GraphGrouping(val label: String) {
    CATEGORY("Par categorie"),
    PAYMENT_METHOD("Par paiement"),
    MONTH("Par mois"),
}

@Serializable
data class GraphConfigModel(
    val title: String = "Depenses",
    val type: GraphType = GraphType.PIE,
    val period: GraphPeriod = GraphPeriod.CURRENT_MONTH,
    val grouping: GraphGrouping = GraphGrouping.CATEGORY,
    val customFromEpochMillis: Long? = null,
    val customToEpochMillis: Long? = null,
)

@Serializable
data class GraphWidgetConfigModel(
    val id: Long,
    val order: Int,
    val config: GraphConfigModel,
)
