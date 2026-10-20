package com.example.mybudgetbook.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mybudgetbook.data.AppDatabase
import com.example.mybudgetbook.data.entity.Kategorie
import com.example.mybudgetbook.ui.components.MainDrawerContent
import com.example.mybudgetbook.ui.screens.accounts.AccountsScreen
import com.example.mybudgetbook.ui.screens.accounts.AddEditAccountScreen
import com.example.mybudgetbook.ui.screens.addbuchung.AddBuchungScreen
import com.example.mybudgetbook.ui.screens.buchungen.BuchungenScreen
import com.example.mybudgetbook.ui.screens.charts.ChartsScreen
import com.example.mybudgetbook.ui.screens.budgets.AddBudgetScreen
import com.example.mybudgetbook.ui.screens.budgets.BudgetsScreen
import com.example.mybudgetbook.ui.screens.calendar.CalendarScreen
import com.example.mybudgetbook.ui.screens.categories.CategoriesScreen
import com.example.mybudgetbook.ui.screens.data.CsvImportScreen
import com.example.mybudgetbook.ui.screens.data.CsvRecentScreen
import com.example.mybudgetbook.ui.screens.data.DataManagementScreen
import com.example.mybudgetbook.ui.screens.recurring.RecurringScreen
import com.example.mybudgetbook.ui.screens.rules.RulesScreen
import com.example.mybudgetbook.ui.screens.savings.SavingsGoalsScreen
import com.example.mybudgetbook.ui.screens.settings.SettingsScreen
import com.example.mybudgetbook.ui.screens.statistics.StatisticsScreen
import com.example.mybudgetbook.ui.screens.templates.TemplatesScreen
import com.example.mybudgetbook.viewmodel.*
import kotlinx.coroutines.launch

object Routes {
    const val BUCHUNGEN = "buchungen"
    const val ADD = "add"
    const val ACCOUNTS = "accounts"
    const val STATISTICS = "statistics"
    const val CALENDAR = "calendar"
    const val CATEGORIES = "categories"
    const val ADD_ACCOUNT = "add_account"
    const val EDIT_ACCOUNT = "edit_account/{accountId}"
    const val RECURRING = "recurring"
    const val TEMPLATES = "templates"
    const val BUDGETS = "budgets"
    const val ADD_BUDGET = "add_budget"
    const val SAVINGS = "savings"
    const val RULES = "rules"
    const val SETTINGS = "settings"
    const val DATA_MANAGEMENT = "data_management"
    const val CSV_IMPORT = "import_csv"
    const val CSV_RECENT = "recent_csv"
    const val CHARTS = "charts"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(db: AppDatabase) {
    viewModel<RecurringProcessorViewModel>(factory = vmFactory { RecurringProcessorViewModel(db) })
    val context = LocalContext.current
    val currency = remember(context) {
        context.getSharedPreferences("mybudgetbook_settings", 0).getString("currency", "SAR") ?: "SAR"
    }
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val categories = remember { mutableStateOf<List<Kategorie>>(emptyList()) }
    LaunchedEffect(Unit) {
        db.kategorieDao().getAll().collect { categories.value = it }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawerContent { route ->
                scope.launch { drawerState.close() }
                val destination = when (route) {
                    "backup", "restore", "export" -> Routes.DATA_MANAGEMENT
                    else -> route
                }
                if (destination in listOf(
                        Routes.ACCOUNTS, Routes.STATISTICS, Routes.CALENDAR, Routes.CATEGORIES,
                        Routes.RECURRING, Routes.TEMPLATES, Routes.BUDGETS, Routes.SAVINGS,
                        Routes.RULES, Routes.SETTINGS, Routes.DATA_MANAGEMENT, Routes.CSV_IMPORT,
                        Routes.CSV_RECENT, Routes.CHARTS,
                    )
                ) {
                    nav.navigate(destination)
                }
            }
        }
    ) {
        NavHost(navController = nav, startDestination = Routes.BUCHUNGEN) {
            composable(Routes.BUCHUNGEN) {
                val vm: BuchungenViewModel = viewModel(factory = vmFactory { BuchungenViewModel(db) })
                BuchungenScreen(viewModel = vm, onAdd = { nav.navigate(Routes.ADD) }, onOpenDrawer = { scope.launch { drawerState.open() } }, onOpenStats = { nav.navigate(Routes.STATISTICS) })
            }
            composable(Routes.ADD) {
                val vm: AddBuchungViewModel = viewModel(factory = vmFactory { AddBuchungViewModel(db, currency) })
                AddBuchungScreen(viewModel = vm, onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() })
            }
            composable(Routes.ACCOUNTS) {
                val vm: AccountsViewModel = viewModel(factory = vmFactory { AccountsViewModel(db) })
                AccountsScreen(viewModel = vm, onBack = { nav.popBackStack() }, onAddAccount = { nav.navigate(Routes.ADD_ACCOUNT) }, onEditAccount = { id -> nav.navigate("edit_account/$id") })
            }
            composable(Routes.ADD_ACCOUNT) { AddEditAccountScreen(db, 0L) { nav.popBackStack() } }
            composable(Routes.EDIT_ACCOUNT) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: 0L
                AddEditAccountScreen(db, id) { nav.popBackStack() }
            }
            composable(Routes.STATISTICS) {
                val vm: StatisticsViewModel = viewModel(factory = vmFactory { StatisticsViewModel(db) })
                StatisticsScreen(viewModel = vm)
            }
            composable(Routes.CALENDAR) {
                val vm: CalendarViewModel = viewModel(factory = vmFactory { CalendarViewModel(db) })
                CalendarScreen(viewModel = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.CATEGORIES) {
                CategoriesScreen(categories = categories.value, onBack = { nav.popBackStack() })
            }
            composable(Routes.RECURRING) {
                val vm: RecurringAndTemplateViewModel = viewModel(factory = vmFactory { RecurringAndTemplateViewModel(db) })
                RecurringScreen(viewModel = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.TEMPLATES) {
                val vm: RecurringAndTemplateViewModel = viewModel(factory = vmFactory { RecurringAndTemplateViewModel(db) })
                TemplatesScreen(viewModel = vm, onBack = { nav.popBackStack() }, onUseTemplate = { nav.navigate(Routes.ADD) })
            }
            composable(Routes.BUDGETS) {
                val vm: BudgetViewModel = viewModel(factory = vmFactory { BudgetViewModel(db) })
                BudgetsScreen(viewModel = vm, onBack = { nav.popBackStack() }, onAddBudget = { nav.navigate(Routes.ADD_BUDGET) })
            }
            composable(Routes.ADD_BUDGET) {
                val vm: BudgetViewModel = viewModel(factory = vmFactory { BudgetViewModel(db) })
                AddBudgetScreen(viewModel = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.SAVINGS) { SavingsGoalsScreen(db = db, onBack = { nav.popBackStack() }) }
            composable(Routes.RULES) { RulesScreen(db = db, onBack = { nav.popBackStack() }) }
            composable(Routes.SETTINGS) { SettingsScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.DATA_MANAGEMENT) { DataManagementScreen(db = db, onBack = { nav.popBackStack() }) }
            composable(Routes.CSV_IMPORT) { CsvImportScreen(db = db, onBack = { nav.popBackStack() }) }
            composable(Routes.CSV_RECENT) { CsvRecentScreen(db = db, onBack = { nav.popBackStack() }) }
            composable(Routes.CHARTS) { ChartsScreen(db = db, onBack = { nav.popBackStack() }) }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : ViewModel> vmFactory(create: () -> T) = object : ViewModelProvider.Factory {
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
}
