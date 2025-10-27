package com.example.moneymanager.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.ui.viewmodel.AuthViewModel
import com.example.moneymanager.ui.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onTransactionClick: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val transactionsState by transactionViewModel.transactionsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        transactionViewModel.loadAllTransactions()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Money Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Smart Finance Assistant",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1)
                ),
                actions = {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            ) {
                                Text("3", fontSize = 10.sp)
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToTransactions) {
                            Icon(Icons.Default.List, contentDescription = "All Transactions", tint = Color.White)
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6366F1),
                            Color(0xFFF8FAFC)
                        ),
                        startY = 0f,
                        endY = 400f
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // User Welcome Section
                    WelcomeSection(currentUser)
                }
                
                item {
                    // Balance Card
                    BalanceCard(transactionsState)
                }
                
                item {
                    // Quick Actions Grid
                    QuickActions(
                        onNavigateToAddTransaction = onNavigateToAddTransaction,
                        onNavigateToTransactions = onNavigateToTransactions,
                        onNavigateToCategories = onNavigateToCategories,
                        onNavigateToStatistics = onNavigateToStatistics
                    )
                }
                
                item {
                    // Income vs Expense Cards
                    IncomeExpenseCards(transactionsState)
                }
                
                item {
                    // Recent Transactions Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6366F1),
                            modifier = Modifier.clickable { onNavigateToTransactions() }
                        )
                    }
                }
                
                when (transactionsState) {
                    is TransactionViewModel.TransactionsState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 16.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = Color(0xFF6366F1)
                                )
                            }
                        }
                    }
                    is TransactionViewModel.TransactionsState.Error -> {
                        item {
                            ErrorCard(
                                (transactionsState as TransactionViewModel.TransactionsState.Error).message
                            )
                        }
                    }
                    is TransactionViewModel.TransactionsState.Success -> {
                        val transactions = (transactionsState as TransactionViewModel.TransactionsState.Success).transactions
                        val recentTransactions = transactions.take(5)
                        if (recentTransactions.isEmpty()) {
                            item {
                                EmptyState(onNavigateToAddTransaction)
                            }
                        } else {
                            items(recentTransactions) { transaction ->
                                TransactionItem(transaction, onTransactionClick)
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun WelcomeSection(currentUser: com.example.moneymanager.data.model.User?) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape)
                    .background(Brush.linearGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00)))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser?.displayName?.firstOrNull()?.toString()?.uppercase() ?: "U",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Good Evening!", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF64748B))
                Text(
                    text = currentUser?.displayName ?: currentUser?.email?.split('@')?.get(0) ?: "User",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)
                )
                Text("Ready to track expenses?", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6366F1))
            }
        }
    }
}

@Composable
fun BalanceCard(transactionsState: TransactionViewModel.TransactionsState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                Brush.linearGradient(colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))),
                shape = RoundedCornerShape(24.dp)
            ).padding(24.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Total Balance", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                        when (transactionsState) {
                            is TransactionViewModel.TransactionsState.Success -> {
                                val balance = transactionsState.transactions.filter { it.type == "income" }.sumOf { it.amount } - 
                                             transactionsState.transactions.filter { it.type == "expense" }.sumOf { it.amount }
                                Text(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(balance),
                                    style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            else -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        }
                    }
                    Icon(Icons.Default.Warning, contentDescription = "Wallet", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Monthly Summary", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun QuickActions(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    data class QuickAction(val title: String, val icon: ImageVector, val color: Color, val onClick: () -> Unit)
    
    val actions = listOf(
        QuickAction("Add Transaction", Icons.Default.Add, Color(0xFFEF4444), onNavigateToAddTransaction),
        QuickAction("View Bills", Icons.Default.List, Color(0xFF3B82F6), onNavigateToTransactions),
        QuickAction("Categories", Icons.Default.Star, Color(0xFF10B981), onNavigateToCategories),
        QuickAction("Statistics", Icons.Default.Info, Color(0xFFF59E0B), onNavigateToStatistics)
    )
    LazyRow(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(actions) { action ->
            Card(
                modifier = Modifier.width(100.dp).clickable { action.onClick() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(action.color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = action.icon, contentDescription = action.title, tint = action.color, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = action.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, 
                         color = Color(0xFF374151), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseCards(transactionsState: TransactionViewModel.TransactionsState) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Income Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF34D399)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Income", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Income", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                }
                Spacer(modifier = Modifier.height(12.dp))
                when (transactionsState) {
                    is TransactionViewModel.TransactionsState.Success -> {
                        val totalIncome = transactionsState.transactions.filter { it.type == "income" }.sumOf { it.amount }
                        Text(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(totalIncome),
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    else -> CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF10B981))
                }
                Text("This Month Income", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
            }
        }
        
        // Expense Card
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(Brush.linearGradient(colors = listOf(Color(0xFFEF4444), Color(0xFFF87171)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expense", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Expense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                }
                Spacer(modifier = Modifier.height(12.dp))
                when (transactionsState) {
                    is TransactionViewModel.TransactionsState.Success -> {
                        val totalExpense = transactionsState.transactions.filter { it.type == "expense" }.sumOf { it.amount }
                        Text(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(totalExpense),
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                    else -> CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFEF4444))
                }
                Text("This Month Expense", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onTransactionClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clickable { transaction.id?.let { onTransactionClick(it) } },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(
                    if (transaction.type == "income") 
                        Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF34D399)))
                    else Brush.linearGradient(colors = listOf(Color(0xFFEF4444), Color(0xFFF87171)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == "income") Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = transaction.type, tint = Color.White, modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.category ?: "Uncategorized", style = MaterialTheme.typography.titleMedium, 
                     fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                if (!transaction.description.isNullOrEmpty()) {
                    Text(transaction.description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
                }
                transaction.date?.let { date ->
                    Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date.toDate()),
                         style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
                Text(
                    text = if (transaction.type == "income") "+${formatter.format(transaction.amount)}" 
                           else "-${formatter.format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = if (transaction.type == "income") Color(0xFF10B981) else Color(0xFFEF4444)
                )
                Text(if (transaction.type == "income") "Income" else "Expense", 
                     style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF))
            }
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Loading Error", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF7F1D1D), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun EmptyState(onNavigateToAddTransaction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(
                    Brush.linearGradient(colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("No Transactions Yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Start tracking your first income or expense!", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.clickable { onNavigateToAddTransaction() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Recording", modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                     style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}