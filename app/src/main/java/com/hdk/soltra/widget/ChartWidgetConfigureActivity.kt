package com.hdk.soltra.widget

import android.app.Activity
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.hdk.soltra.R
import com.hdk.soltra.domain.GraphConfigModel
import com.hdk.soltra.domain.GraphGrouping
import com.hdk.soltra.domain.GraphPeriod
import com.hdk.soltra.domain.GraphType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ChartWidgetConfigureActivity : Activity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var titleInput: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var periodSpinner: Spinner
    private lateinit var groupingSpinner: Spinner
    private lateinit var customRangeContainer: View
    private lateinit var fromValue: TextView
    private lateinit var toValue: TextView

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    private lateinit var typeOptions: List<Option<GraphType>>
    private lateinit var periodOptions: List<Option<GraphPeriod>>
    private lateinit var groupingOptions: List<Option<GraphGrouping>>

    private var customFromEpochMillis: Long? = null
    private var customToEpochMillis: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_chart_widget_config)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        bindViews()
        setupOptions()

        val existingConfig = ChartWidgetProvider.readConfig(this, appWidgetId)
        bindConfig(existingConfig)

        findViewById<Button>(R.id.chart_widget_pick_from_button).setOnClickListener {
            openDatePicker(initialEpochMillis = customFromEpochMillis ?: System.currentTimeMillis()) { selected ->
                customFromEpochMillis = selected
                fromValue.text = formatDate(selected)
            }
        }

        findViewById<Button>(R.id.chart_widget_pick_to_button).setOnClickListener {
            openDatePicker(initialEpochMillis = customToEpochMillis ?: System.currentTimeMillis()) { selected ->
                customToEpochMillis = selected
                toValue.text = formatDate(selected)
            }
        }

        periodSpinner.setOnItemSelectedListener(SimpleItemSelectedListener { position ->
            val selectedPeriod = periodOptions.getOrNull(position)?.value ?: GraphPeriod.CURRENT_MONTH
            customRangeContainer.visibility = if (selectedPeriod == GraphPeriod.CUSTOM) View.VISIBLE else View.GONE
        })

        findViewById<Button>(R.id.chart_widget_save_button).setOnClickListener {
            saveAndFinish()
        }
    }

    private fun bindViews() {
        titleInput = findViewById(R.id.chart_widget_title_input)
        typeSpinner = findViewById(R.id.chart_widget_type_spinner)
        periodSpinner = findViewById(R.id.chart_widget_period_spinner)
        groupingSpinner = findViewById(R.id.chart_widget_grouping_spinner)
        customRangeContainer = findViewById(R.id.chart_widget_custom_range)
        fromValue = findViewById(R.id.chart_widget_from_value)
        toValue = findViewById(R.id.chart_widget_to_value)
    }

    private fun setupOptions() {
        typeOptions = listOf(
            Option(GraphType.PIE, getString(R.string.widget_chart_type_pie)),
            Option(GraphType.DONUT, getString(R.string.widget_chart_type_donut)),
            Option(GraphType.BAR, getString(R.string.widget_chart_type_bar)),
        )
        periodOptions = listOf(
            Option(GraphPeriod.CURRENT_MONTH, getString(R.string.widget_chart_period_current_month)),
            Option(GraphPeriod.PREVIOUS_MONTH, getString(R.string.widget_chart_period_previous_month)),
            Option(GraphPeriod.CURRENT_YEAR, getString(R.string.widget_chart_period_current_year)),
            Option(GraphPeriod.PREVIOUS_YEAR, getString(R.string.widget_chart_period_previous_year)),
            Option(GraphPeriod.CUSTOM, getString(R.string.widget_chart_period_custom)),
        )
        groupingOptions = listOf(
            Option(GraphGrouping.CATEGORY, getString(R.string.widget_chart_grouping_category)),
            Option(GraphGrouping.PAYMENT_METHOD, getString(R.string.widget_chart_grouping_payment_method)),
            Option(GraphGrouping.MONTH, getString(R.string.widget_chart_grouping_month)),
        )

        typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeOptions.map { it.label })
        periodSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, periodOptions.map { it.label })
        groupingSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, groupingOptions.map { it.label })
    }

    private fun bindConfig(config: GraphConfigModel) {
        titleInput.setText(config.title)
        typeSpinner.setSelection(typeOptions.indexOfFirst { it.value == config.type }.coerceAtLeast(0))
        periodSpinner.setSelection(periodOptions.indexOfFirst { it.value == config.period }.coerceAtLeast(0))
        groupingSpinner.setSelection(groupingOptions.indexOfFirst { it.value == config.grouping }.coerceAtLeast(0))

        customFromEpochMillis = config.customFromEpochMillis
        customToEpochMillis = config.customToEpochMillis
        fromValue.text = customFromEpochMillis?.let(::formatDate) ?: getString(R.string.widget_chart_not_set)
        toValue.text = customToEpochMillis?.let(::formatDate) ?: getString(R.string.widget_chart_not_set)

        customRangeContainer.visibility = if (config.period == GraphPeriod.CUSTOM) View.VISIBLE else View.GONE
    }

    private fun saveAndFinish() {
        val selectedType = typeOptions.getOrNull(typeSpinner.selectedItemPosition)?.value ?: GraphType.PIE
        val selectedPeriod = periodOptions.getOrNull(periodSpinner.selectedItemPosition)?.value ?: GraphPeriod.CURRENT_MONTH
        val selectedGrouping = groupingOptions.getOrNull(groupingSpinner.selectedItemPosition)?.value ?: GraphGrouping.CATEGORY

        if (selectedPeriod == GraphPeriod.CUSTOM && (customFromEpochMillis == null || customToEpochMillis == null)) {
            Toast.makeText(this, R.string.widget_chart_custom_required, Toast.LENGTH_SHORT).show()
            return
        }

        val config = GraphConfigModel(
            title = titleInput.text?.toString().orEmpty(),
            type = selectedType,
            period = selectedPeriod,
            grouping = selectedGrouping,
            customFromEpochMillis = customFromEpochMillis,
            customToEpochMillis = customToEpochMillis,
        )

        ChartWidgetProvider.saveConfig(this, appWidgetId, config)
        ChartWidgetProvider.updateSingle(this, appWidgetId)

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    private fun openDatePicker(initialEpochMillis: Long, onPicked: (Long) -> Unit) {
        val initialDate = Instant.ofEpochMilli(initialEpochMillis).atZone(zoneId).toLocalDate()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = LocalDate.of(year, month + 1, day)
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()
                onPicked(selected)
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth,
        ).show()
    }

    private fun formatDate(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate().format(dateFormatter)
    }
}

private data class Option<T>(
    val value: T,
    val label: String,
)
