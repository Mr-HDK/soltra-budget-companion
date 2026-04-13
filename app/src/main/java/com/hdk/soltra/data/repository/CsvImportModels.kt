package com.hdk.soltra.data.repository

data class CsvPreview(
    val headers: List<String>,
    val sampleRows: List<List<String>>,
)

data class CsvMapping(
    val dateColumn: String,
    val amountColumn: String,
    val categoryColumn: String,
    val paymentColumn: String?,
    val merchantColumn: String?,
    val noteColumn: String?,
)

data class CsvImportResult(
    val addedCount: Int,
    val skippedCount: Int,
    val errors: List<String>,
)
