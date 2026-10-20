package com.example.mybudgetbook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbook.ui.navigation.Routes

@Composable
fun MainDrawerContent(onItemClick: (String) -> Unit) {
    ModalDrawerSheet(drawerContainerColor = Color(0xFF1E1E1E), modifier = Modifier.width(300.dp)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("إدارة", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
            DrawerItem("إدارة الحساب", Icons.Default.AccountBalance) { onItemClick(Routes.ACCOUNTS) }
            DrawerItem("إدارة الفئات", Icons.Default.Category) { onItemClick(Routes.CATEGORIES) }
            DrawerItem("المعاملات المتكررة", Icons.Default.Loop) { onItemClick(Routes.RECURRING) }
            DrawerItem("القوالب", Icons.Default.Assignment) { onItemClick(Routes.TEMPLATES) }
            DrawerItem("إدارة الميزانية", Icons.Default.BarChart) { onItemClick(Routes.BUDGETS) }
            DrawerItem("التقارير البيانية", Icons.Default.ShowChart) { onItemClick(Routes.CHARTS) }
            DrawerItem("أهداف الادخار", Icons.Default.AccountBalanceWallet) { onItemClick(Routes.SAVINGS) }
            DrawerItem("القواعد التلقائية", Icons.Default.Build) { onItemClick(Routes.RULES) }
            DrawerItem("الإعدادات", Icons.Default.Settings) { onItemClick(Routes.SETTINGS) }

            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
            Text("وظائف مفيدة", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))

            DrawerItem("التقويم", Icons.Default.CalendarMonth) { onItemClick(Routes.CALENDAR) }
            DrawerItem("آخر الإدخالات", Icons.Default.History) { onItemClick("recent") }
            DrawerItem("بحث مفصل", Icons.Default.Search) { onItemClick("search") }
            DrawerItem("غير مصنف", Icons.Default.Warning) { onItemClick("uncategorized") }

            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))

            DrawerItem("إنشاء نسخة احتياطية", Icons.Default.Save) { onItemClick("backup") }
            DrawerItem("استعادة نسخة احتياطية", Icons.Default.FolderOpen) { onItemClick("restore") }
            DrawerItem("تصدير البيانات", Icons.Default.Share) { onItemClick("export") }
            DrawerItem("استيراد CSV", Icons.Default.Download) { onItemClick("import_csv") }
            DrawerItem("آخر واردات CSV", Icons.Default.Description) { onItemClick("recent_csv") }
            DrawerItem("إدارة البيانات", Icons.Default.Storage) { onItemClick(Routes.DATA_MANAGEMENT) }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DrawerItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.LightGray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = Color.LightGray, fontSize = 16.sp)
    }
}
