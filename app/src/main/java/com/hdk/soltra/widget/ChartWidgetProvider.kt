package com.hdk.soltra.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import com.hdk.soltra.MainActivity
import com.hdk.soltra.R
import com.hdk.soltra.data.local.AppDatabase
import com.hdk.soltra.domain.CategoryModel
import com.hdk.soltra.domain.ExpenseRecord
import com.hdk.soltra.domain.GraphConfigModel
import com.hdk.soltra.domain.GraphType
import com.hdk.soltra.domain.PaymentMethod
import com.hdk.soltra.ui.GraphComposerLogic
import com.hdk.soltra.ui.GraphPreviewUiState
import com.hdk.soltra.util.minorToMoneyString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.Locale
import kotlin.math.max

class ChartWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            updateAllWidgets(context)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            removeConfig(context, appWidgetId)
        }
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, ChartWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(provider)
        if (appWidgetIds.isEmpty()) return
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.hdk.soltra.widget.ACTION_CHART_REFRESH"
        private const val PREFS_NAME = "chart_widget_preferences"
        private const val KEY_CONFIG_PREFIX = "chart_config_"
        private const val LEGACY_KEY_TYPE_PREFIX = "chart_type_"
        private const val CHART_BITMAP_WIDTH = 720
        private const val CHART_BITMAP_HEIGHT = 420
        private val json = Json { ignoreUnknownKeys = true }

        fun refresh(context: Context) {
            val intent = Intent(context, ChartWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }

        fun updateSingle(context: Context, appWidgetId: Int) {
            updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
        }

        internal fun saveConfig(context: Context, appWidgetId: Int, config: GraphConfigModel) {
            val normalized = GraphComposerLogic.sanitizeGraphConfig(config)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString("$KEY_CONFIG_PREFIX$appWidgetId", json.encodeToString(GraphConfigModel.serializer(), normalized))
                .remove("$LEGACY_KEY_TYPE_PREFIX$appWidgetId")
                .apply()
        }

        internal fun readConfig(context: Context, appWidgetId: Int): GraphConfigModel {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val stored = prefs.getString("$KEY_CONFIG_PREFIX$appWidgetId", null)
            if (!stored.isNullOrBlank()) {
                return runCatching {
                    json.decodeFromString(GraphConfigModel.serializer(), stored)
                }.map { GraphComposerLogic.sanitizeGraphConfig(it) }
                    .getOrElse { defaultConfig(context) }
            }

            // Backward compatibility for old widget instances that only stored chart type.
            val legacyType = prefs.getString("$LEGACY_KEY_TYPE_PREFIX$appWidgetId", null)
            if (!legacyType.isNullOrBlank()) {
                val parsedType = runCatching { GraphType.valueOf(legacyType) }.getOrElse { GraphType.PIE }
                return GraphConfigModel(
                    title = context.getString(R.string.widget_chart_default_title),
                    type = parsedType,
                )
            }

            return defaultConfig(context)
        }

        internal fun removeConfig(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove("$KEY_CONFIG_PREFIX$appWidgetId")
                .remove("$LEGACY_KEY_TYPE_PREFIX$appWidgetId")
                .apply()
        }

        private fun defaultConfig(context: Context): GraphConfigModel {
            return GraphConfigModel(title = context.getString(R.string.widget_chart_default_title))
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            val config = readConfig(context, appWidgetId)
            val snapshot = runBlocking(Dispatchers.IO) {
                loadSnapshot(context, config)
            }

            val chartBitmap = buildChartBitmap(
                chartType = snapshot.preview.chartType,
                preview = snapshot.preview,
                widthPx = CHART_BITMAP_WIDTH,
                heightPx = CHART_BITMAP_HEIGHT,
                backgroundColor = Color.parseColor("#F6F2E9"),
            )

            val launchIntent = Intent(context, MainActivity::class.java)
            val launchPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 100 + 11,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val views = RemoteViews(context.packageName, R.layout.widget_chart).apply {
                setTextViewText(R.id.widget_chart_title, snapshot.preview.title)
                setTextViewText(
                    R.id.widget_chart_subtitle,
                    context.getString(
                        R.string.widget_chart_subtitle_format,
                        snapshot.preview.periodLabel,
                        snapshot.preview.groupingLabel,
                    ),
                )
                setTextViewText(R.id.widget_chart_total, snapshot.totalLabel)
                setImageViewBitmap(R.id.widget_chart_image, chartBitmap)
                setOnClickPendingIntent(R.id.widget_chart_root, launchPendingIntent)
                if (snapshot.preview.hasData) {
                    setViewVisibility(R.id.widget_chart_empty, android.view.View.GONE)
                } else {
                    setViewVisibility(R.id.widget_chart_empty, android.view.View.VISIBLE)
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private suspend fun loadSnapshot(context: Context, config: GraphConfigModel): ChartWidgetSnapshot {
            val database = AppDatabase.getInstance(context)
            val budget = database.budgetConfigDao().getOrNull()
            val monthStartDay = budget?.monthStartDay ?: 1
            val currencyCode = budget?.currencyCode ?: "EUR"

            val categories = database.categoryDao().getAll()
            val categoryById = categories.associateBy { it.id }
            val expenseRecords = database.expenseDao().getAll().map { expense ->
                val category = categoryById[expense.categoryId]
                ExpenseRecord(
                    id = expense.id,
                    amountMinor = expense.amountMinor,
                    occurredAtEpochMillis = expense.occurredAtEpochMillis,
                    categoryId = expense.categoryId,
                    categoryName = category?.name ?: context.getString(R.string.widget_chart_category_fallback),
                    categoryColorHex = category?.colorHex ?: "#0E6B68",
                    paymentMethod = parsePaymentMethod(expense.paymentMethod),
                    merchantOrLabel = expense.merchantOrLabel,
                    note = expense.note,
                )
            }

            val categoryModels = categories.map { category ->
                CategoryModel(
                    id = category.id,
                    name = category.name,
                    colorHex = category.colorHex,
                    iconName = category.iconName,
                    sortOrder = category.sortOrder,
                    isActive = category.isActive,
                    monthlyBudgetMinor = category.monthlyBudgetMinor,
                )
            }

            val preview = GraphComposerLogic.buildGraphPreview(
                config = config,
                monthStartDay = monthStartDay,
                allExpenses = expenseRecords,
                categories = categoryModels,
                zoneId = java.time.ZoneId.systemDefault(),
                locale = Locale.getDefault(),
            )

            return ChartWidgetSnapshot(
                preview = preview,
                totalLabel = context.getString(
                    R.string.widget_chart_total_format,
                    preview.totalMinor.minorToMoneyString(currencyCode),
                ),
            )
        }

        private fun parsePaymentMethod(raw: String): PaymentMethod {
            return runCatching { PaymentMethod.valueOf(raw) }.getOrElse { PaymentMethod.CARTE_TPE }
        }

        private fun buildChartBitmap(
            chartType: GraphType,
            preview: GraphPreviewUiState,
            widthPx: Int,
            heightPx: Int,
            backgroundColor: Int,
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(backgroundColor)

            if (!preview.hasData) return bitmap

            return when (chartType) {
                GraphType.PIE -> {
                    drawPieOrDonut(canvas, preview, widthPx, heightPx, donut = false, backgroundColor = backgroundColor)
                    bitmap
                }
                GraphType.DONUT -> {
                    drawPieOrDonut(canvas, preview, widthPx, heightPx, donut = true, backgroundColor = backgroundColor)
                    bitmap
                }
                GraphType.BAR -> {
                    drawBars(canvas, preview, widthPx, heightPx)
                    bitmap
                }
            }
        }

        private fun drawPieOrDonut(
            canvas: Canvas,
            preview: GraphPreviewUiState,
            widthPx: Int,
            heightPx: Int,
            donut: Boolean,
            backgroundColor: Int,
        ) {
            val baseSlices = preview.points
                .asSequence()
                .filter { it.valueMinor > 0L }
                .sortedByDescending { it.valueMinor }
                .toList()
            if (baseSlices.isEmpty()) return

            val slices = if (baseSlices.size <= 5) {
                baseSlices
            } else {
                val top = baseSlices.take(4)
                val other = baseSlices.drop(4).sumOf { it.valueMinor }
                top + listOf(top.first().copy(key = "other", label = "Other", valueMinor = other, colorHex = "#DCE9E7"))
            }

            val total = slices.sumOf { it.valueMinor }.toDouble().coerceAtLeast(1.0)
            val size = minOf(widthPx, heightPx) * 0.9f
            val left = (widthPx - size) / 2f
            val top = (heightPx - size) / 2f
            val oval = RectF(left, top, left + size, top + size)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

            var startAngle = -90f
            slices.forEach { slice ->
                val sweepAngle = ((slice.valueMinor / total) * 360.0).toFloat()
                paint.color = parseColorOr(slice.colorHex, Color.parseColor("#0E6B68"))
                canvas.drawArc(oval, startAngle, sweepAngle, true, paint)
                startAngle += sweepAngle
            }

            if (donut) {
                val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = backgroundColor
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(widthPx / 2f, heightPx / 2f, size * 0.24f, holePaint)
            }
        }

        private fun drawBars(
            canvas: Canvas,
            preview: GraphPreviewUiState,
            widthPx: Int,
            heightPx: Int,
        ) {
            val dataPoints = preview.points
                .takeLast(8)
                .ifEmpty { preview.points }
            if (dataPoints.isEmpty()) return

            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
            val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                color = Color.parseColor("#425066")
            }

            val marginLeft = widthPx * 0.08f
            val marginRight = widthPx * 0.06f
            val marginTop = heightPx * 0.1f
            val marginBottom = heightPx * 0.14f
            val chartWidth = max(1f, widthPx - marginLeft - marginRight)
            val chartHeight = max(1f, heightPx - marginTop - marginBottom)
            val maxValue = dataPoints.maxOfOrNull { it.valueMinor }?.toFloat()?.coerceAtLeast(1f) ?: 1f
            val slotWidth = chartWidth / dataPoints.size
            val barWidth = slotWidth * 0.62f

            canvas.drawLine(
                marginLeft,
                marginTop + chartHeight,
                marginLeft + chartWidth,
                marginTop + chartHeight,
                axisPaint,
            )

            dataPoints.forEachIndexed { index, point ->
                val ratio = (point.valueMinor.toFloat() / maxValue).coerceIn(0f, 1f)
                val barHeight = chartHeight * ratio
                val left = marginLeft + (slotWidth * index) + ((slotWidth - barWidth) / 2f)
                val top = marginTop + chartHeight - barHeight
                val right = left + barWidth
                val bottom = marginTop + chartHeight
                barPaint.color = parseColorOr(point.colorHex, Color.parseColor("#0E6B68"))
                canvas.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, barPaint)
            }
        }

        private fun parseColorOr(hex: String, fallback: Int): Int {
            return runCatching { Color.parseColor(hex) }.getOrElse { fallback }
        }
    }
}

private data class ChartWidgetSnapshot(
    val preview: GraphPreviewUiState,
    val totalLabel: String,
)
