package com.example.mybudgetbook.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbook.data.entity.Kategorie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(categories: List<Kategorie>, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(1) }
    var expandedParents by remember { mutableStateOf(setOf<Long>()) }

    val filteredCategories = categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val parentCategories = filteredCategories.filter { it.vaterId == null || it.vaterId == 0L }
    val childCategories = filteredCategories.filter { it.vaterId != null && it.vaterId != 0L }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("الفئات", color = Color.White) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                    actions = { IconButton(onClick = {}) { Icon(Icons.Default.Add, null, tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
                TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFF2C2C2C), contentColor = Color.White) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("أ-ي") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("الفئات") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("المفضلة") })
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("بحث", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Gray, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    items(filteredCategories.sortedBy { it.name }) { cat -> CategoryItem(cat.name, isExpanded = false, hasChildren = false, onClick = {}) }
                } else {
                    items(parentCategories) { parent ->
                        val children = childCategories.filter { it.vaterId == parent.id }
                        val isExpanded = expandedParents.contains(parent.id)
                        CategoryItem(
                            name = parent.name, isExpanded = isExpanded, hasChildren = children.isNotEmpty(),
                            onClick = { if (children.isNotEmpty()) expandedParents = if (isExpanded) expandedParents - parent.id else expandedParents + parent.id }
                        )
                        if (isExpanded) {
                            children.forEach { child -> CategoryItem(child.name, isExpanded = false, hasChildren = false, isChild = true, onClick = {}) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, isExpanded: Boolean, hasChildren: Boolean, isChild: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 16.dp, horizontal = if (isChild) 32.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, color = Color.White, fontSize = 18.sp)
        if (hasChildren) {
            Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White)
        }
    }
    HorizontalDivider(color = Color(0xFF2C2C2C), thickness = 0.5.dp)
}
