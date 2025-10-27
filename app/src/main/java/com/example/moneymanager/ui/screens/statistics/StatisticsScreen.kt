package com.example.moneymanager.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.ui.viewmodel.CategoryViewModel
import com.example.moneymanager.ui.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    transactionViewModel: TransactionViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val transactionsState by transactionViewModel.transactionsState.collectAsState()
    val categoriesState by categoryViewModel.categoriesState.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Month") } // Day, Week, Month
    var selectedChartType by remember { mutableStateOf("Expenses") } // Expenses, Income, Both

    // Load all transactions and categories
    LaunchedEffect(Unit) {
        transactionViewModel.loadAllTransactions()
        categoryViewModel.loadAllCategories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            transactionsState is TransactionViewModel.TransactionsState.Loading ||
                    categoriesState is CategoryViewModel.CategoriesState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            transactionsState is TransactionViewModel.TransactionsState.Success &&
                    categoriesState is CategoryViewModel.CategoriesState.Success -> {
                val transactions = (transactionsState as TransactionViewModel.TransactionsState.Success).transactions
                val categories = (categoriesState as CategoryViewModel.CategoriesState.Success).categories

                if (transactions.isEmpty()) {
                    EmptyStatisticsState(modifier = Modifier.padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Summary Cards
                        item {
                            Text(
                                text = "Financial Summary",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            SummaryCards(transactions = transactions)
                        }

                        // Period Selector
                        item {
                            Text(
                                text = "Trends",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        item {
                            PeriodSelector(
                                selectedPeriod = selectedPeriod,
                                onPeriodSelected = { selectedPeriod = it }
                            )
                        }

                        // Bar Chart
                        item {
                            BarChartCard(
                                transactions = transactions,
                                period = selectedPeriod
                            )
                        }

                        // Chart Type Selector
                        item {
                            Text(
                                text = "Category Breakdown",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        item {
                            ChartTypeSelector(
                                selectedType = selectedChartType,
                                onTypeSelected = { selectedChartType = it }
                            )
                        }

                        // Pie Chart
                        item {
                            PieChartCard(
                                transactions = transactions,
                                categories = categories,
                                type = selectedChartType
                            )
                        }

                        // Category List
                        item {
                            CategoryBreakdownList(
                                transactions = transactions,
                                categories = categories,
                                type = selectedChartType
                            )
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error loading statistics",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCards(transactions: List<Transaction>) {
    val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpenses

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Income",
            amount = totalIncome,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Expenses",
            amount = totalExpenses,
            color = Color(0xFFEF4444),
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (netBalance >= 0)
                Color(0xFF10B981).copy(alpha = 0.1f)
            else
                Color(0xFFEF4444).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Net Balance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${String.format("%.2f", netBalance)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Day", "Week", "Month").forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(period) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ChartTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Expenses", "Income", "Both").forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BarChartCard(
    transactions: List<Transaction>,
    period: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Trends by $period",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val chartData = prepareBarChartData(transactions, period)

            if (chartData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                SimpleBarChart(data = chartData)
            }
        }
    }
}

@Composable
fun SimpleBarChart(data: List<Pair<String, Double>>) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1.0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(60.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((value / maxValue).toFloat())
                            .background(
                                color = Color(0xFF3B82F6),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }

                Text(
                    text = "$${String.format("%.0f", value)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun PieChartCard(
    transactions: List<Transaction>,
    categories: List<Category>,
    type: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = when (type) {
                    "Expenses" -> "Expenses by Category"
                    "Income" -> "Income by Category"
                    else -> "All Transactions by Category"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val pieData = preparePieChartData(transactions, categories, type)

            if (pieData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                SimplePieChart(data = pieData)
            }
        }
    }
}

@Composable
fun SimplePieChart(data: List<Pair<String, Double>>) {
    val total = data.sumOf { it.second }
    val colors = listOf(
        Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFF10B981),
        Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899),
        Color(0xFF06B6D4), Color(0xFF84CC16)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Simple progress bars as pie chart alternative
        data.forEachIndexed { index, (label, value) ->
            val percentage = (value / total * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = colors[index % colors.size],
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LinearProgressIndicator(
                        progress = { (value / total).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = colors[index % colors.size],
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdownList(
    transactions: List<Transaction>,
    categories: List<Category>,
    type: String
) {
    val categoryMap = categories.associateBy { it.name }
    val filteredTransactions = when (type) {
        "Expenses" -> transactions.filter { it.type == "expense" }
        "Income" -> transactions.filter { it.type == "income" }
        else -> transactions
    }

    val categoryTotals = filteredTransactions
        .groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .toList()
        .sortedByDescending { it.second }

    if (categoryTotals.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Detailed Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                categoryTotals.forEach { (categoryId, amount) ->
                    val category = categoryMap[categoryId]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category?.name ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$${String.format("%.2f", amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (categoryId != categoryTotals.last().first) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStatisticsState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Data Yet",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add some transactions to see your statistics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper functions
fun prepareBarChartData(transactions: List<Transaction>, period: String): List<Pair<String, Double>> {
    val now = Calendar.getInstance()

    return when (period) {
        "Day" -> {
            // Last 7 days
            (6 downTo 0).map { daysAgo ->
                val date = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -daysAgo)
                }
                val dayTransactions = transactions.filter { transaction ->
                    val txDate = Calendar.getInstance().apply {
                        time = transaction.date.toDate()
                    }
                    txDate.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                            txDate.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                }
                val label = SimpleDateFormat("EEE", Locale.getDefault()).format(date.time)
                label to dayTransactions.filter { it.type == "expense" }.sumOf { it.amount }
            }
        }
        "Week" -> {
            // Last 4 weeks
            (3 downTo 0).map { weeksAgo ->
                val startOfWeek = Calendar.getInstance().apply {
                    add(Calendar.WEEK_OF_YEAR, -weeksAgo)
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
                val endOfWeek = Calendar.getInstance().apply {
                    time = startOfWeek.time
                    add(Calendar.DAY_OF_YEAR, 6)
                }
                val weekTransactions = transactions.filter { transaction ->
                    val txDate = transaction.date.toDate()
                    txDate.after(startOfWeek.time) && txDate.before(endOfWeek.time) ||
                            txDate == startOfWeek.time || txDate == endOfWeek.time
                }
                val label = "Week ${4 - weeksAgo}"
                label to weekTransactions.filter { it.type == "expense" }.sumOf { it.amount }
            }
        }
        else -> { // Month
            // Last 6 months
            (5 downTo 0).map { monthsAgo ->
                val date = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -monthsAgo)
                }
                val monthTransactions = transactions.filter { transaction ->
                    transaction.month == date.get(Calendar.MONTH) + 1 &&
                            transaction.year == date.get(Calendar.YEAR)
                }
                val label = SimpleDateFormat("MMM", Locale.getDefault()).format(date.time)
                label to monthTransactions.filter { it.type == "expense" }.sumOf { it.amount }
            }
        }
    }
}

fun preparePieChartData(
    transactions: List<Transaction>,
    categories: List<Category>,
    type: String
): List<Pair<String, Double>> {
    val categoryMap = categories.associateBy { it.name }
    val filteredTransactions = when (type) {
        "Expenses" -> transactions.filter { it.type == "expense" }
        "Income" -> transactions.filter { it.type == "income" }
        else -> transactions
    }

    return filteredTransactions
        .groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .map { (categoryId, amount) ->
            val categoryName = categoryMap[categoryId]?.name ?: "Unknown"
            categoryName to amount
        }
        .sortedByDescending { it.second }
        .take(8) // Show top 8 categories
}



























