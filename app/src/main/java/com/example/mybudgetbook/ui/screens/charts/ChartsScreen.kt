package com.example.mybudgetbook.ui.screens.charts

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.Buchung
import com.example.mybudgetbook.data.entity.Kategorie
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(db: AppDatabase, onBack: () -> Unit) {
    val transactions by db.buchungDao().observeAllRaw().collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by db.kategorieDao().getAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val reportRows = transactions.withoutSplitParents()
    val expenseTotals = reportRows.filter { it.betragvz < 0.0 }
        .groupBy { it.idBuchungKategorie }
        .mapValues { (_, rows) -> rows.sumOf { abs(it.betragvz) } }
    val categoryLabels = categories.associate { it.id to it.name }
    val pieEntries = expenseTotals.mapNotNull { (id, amount) ->
        if (amount <= 0.0) null else PieEntry(amount.toFloat(), categoryLabels[id] ?: "أخرى")
    }
    val months = remember { lastSixMonths() }
    val monthIncome = months.map { month -> reportRows.filter { it.betragvz > 0.0 && it.buchungsdatum.isSameMonth(month.first) }.sumOf { it.betragvz }.toFloat() }
    val monthExpense = months.map { month -> reportRows.filter { it.betragvz < 0.0 && it.buchungsdatum.isSameMonth(month.first) }.sumOf { abs(it.betragvz) }.toFloat() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("التقارير البيانية") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") }
        }) },
        containerColor = Color(0xFF121212),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("المصروفات حسب الفئة", color = Color.White)
                        if (pieEntries.isEmpty()) Text("لا توجد مصروفات كافية للرسم", color = Color.LightGray)
                        else AndroidView(
                            factory = { context -> PieChart(context) },
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                            update = { chart ->
                                val dataSet = PieDataSet(pieEntries, "المصروفات").apply {
                                    colors = com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS.toList()
                                    valueTextColor = AndroidColor.WHITE
                                    valueTextSize = 12f
                                }
                                chart.description.isEnabled = false
                                chart.setEntryLabelColor(AndroidColor.WHITE)
                                chart.legend.textColor = AndroidColor.WHITE
                                chart.data = PieData(dataSet)
                                chart.invalidate()
                            },
                        )
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("الدخل والمصروفات شهريًا", color = Color.White)
                        AndroidView(
                            factory = { context -> BarChart(context) },
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            update = { chart ->
                                val income = BarDataSet(monthIncome.mapIndexed { i, value -> BarEntry(i.toFloat(), value) }, "الدخل").apply {
                                    color = AndroidColor.rgb(67, 160, 71); valueTextColor = AndroidColor.WHITE
                                }
                                val expenses = BarDataSet(monthExpense.mapIndexed { i, value -> BarEntry(i.toFloat(), value) }, "المصروفات").apply {
                                    color = AndroidColor.rgb(229, 57, 53); valueTextColor = AndroidColor.WHITE
                                }
                                chart.description.isEnabled = false
                                chart.legend.textColor = AndroidColor.WHITE
                                chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                                chart.xAxis.valueFormatter = IndexAxisValueFormatter(months.map { it.second })
                                chart.xAxis.granularity = 1f
                                chart.xAxis.textColor = AndroidColor.WHITE
                                chart.axisLeft.textColor = AndroidColor.WHITE
                                chart.axisRight.isEnabled = false
                                chart.data = BarData(income, expenses).apply { barWidth = 0.35f }
                                chart.groupBars(0f, 0.08f, 0.02f)
                                chart.invalidate()
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun List<Buchung>.withoutSplitParents(): List<Buchung> {
    val parentsWithChildren = filter { parent -> any { it.idBuchungSplittid == parent.id } }.map { it.id }.toSet()
    return filterNot { it.id in parentsWithChildren }
}

private fun lastSixMonths(): List<Pair<Long, String>> {
    val formatter = SimpleDateFormat("MMM", Locale("ar"))
    return (5 downTo 0).map { offset ->
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, -offset)
        }
        calendar.timeInMillis to formatter.format(calendar.time)
    }
}

private fun Long.isSameMonth(other: Long): Boolean {
    val left = Calendar.getInstance().apply { timeInMillis = this@isSameMonth }
    val right = Calendar.getInstance().apply { timeInMillis = other }
    return left.get(Calendar.YEAR) == right.get(Calendar.YEAR) && left.get(Calendar.MONTH) == right.get(Calendar.MONTH)
}
