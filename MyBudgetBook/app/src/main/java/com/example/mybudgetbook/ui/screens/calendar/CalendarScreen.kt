package com.example.mybudgetbook.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mybudgetbook.viewmodel.CalendarViewModel
import com.example.mybudgetbook.viewmodel.DailyStat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel, onBack: () -> Unit) {
    val stats by viewModel.dailyStats.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()

    val monthFormat = SimpleDateFormat("MMMM", Locale("ar"))
    val yearFormat = SimpleDateFormat("yyyy", Locale.US)
    val monthName = monthFormat.format(currentMonth.time)
    val yearName = yearFormat.format(currentMonth.time)
    val firstDayOfMonth = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK)
    val emptyDays = firstDayOfMonth - 1
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("التقويم", color = Color.White) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                    actions = { IconButton(onClick = {}) { Icon(Icons.Default.FilterAlt, null, tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF121212)).padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) { Icon(Icons.Default.ChevronLeft, null, tint = Color.White) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(monthName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(yearName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    IconButton(onClick = { viewModel.nextMonth() }) { Icon(Icons.Default.ChevronRight, null, tint = Color.White) }
                }
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C)).padding(vertical = 8.dp)) {
                    val daysOfWeek = listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
                    daysOfWeek.forEach { day -> Text(text = day, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp) }
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(4.dp)) {
            items(emptyDays) { Spacer(modifier = Modifier.height(80.dp)) }
            items(daysInMonth) { index ->
                val day = index + 1
                DayCell(day, stats[day])
            }
        }
    }
}

@Composable
fun DayCell(day: Int, stat: DailyStat?) {
    Column(
        modifier = Modifier.padding(2.dp).height(80.dp).background(Color(0xFF1E1E1E), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(day.toString(), color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.weight(1f))
        if (stat != null) {
            if (stat.income > 0) Text(text = "+${stat.income.toInt()}", color = Color(0xFF43A047), fontSize = 9.sp, maxLines = 1)
            if (stat.expense < 0) Text(text = "${stat.expense.toInt()}", color = Color(0xFFE53935), fontSize = 9.sp, maxLines = 1)
            if (stat.balance != 0.0) Text(text = "${stat.balance.toInt()}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
