package com.example.moneymanager.ui.screens.category

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.ui.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Filter state
    var selectedTypeFilter by remember { mutableStateOf("all") } // all, income, expense
    
    // Add category dialog state
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    
    // Delete confirmation dialog state
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current


    // Load categories on initial composition
    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPrefs.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            categoryViewModel.createDefaultCategories()
            sharedPrefs.edit().putBoolean("is_first_launch", false).apply()
        }

        categoryViewModel.loadAllCategories()
    }
    
    // Apply filter when it changes
    LaunchedEffect(selectedTypeFilter) {
        when (selectedTypeFilter) {
            "all" -> categoryViewModel.loadAllCategories()
            else -> categoryViewModel.loadCategoriesByType(selectedTypeFilter)
        }
    }
    
    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAddCategory = { name, type ->
                categoryViewModel.addCategory(Category(name = name, type = type))
                showAddCategoryDialog = false
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                categoryToDelete = null
            },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete '${categoryToDelete?.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryToDelete?.id?.let { categoryViewModel.deleteCategory(it) }
                        showDeleteConfirmDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type filter chips
                FilterChip(
                    selected = selectedTypeFilter == "all",
                    onClick = { selectedTypeFilter = "all" },
                    label = { Text("All") }
                )
                
                FilterChip(
                    selected = selectedTypeFilter == "income",
                    onClick = { selectedTypeFilter = "income" },
                    label = { Text("Income") }
                )
                
                FilterChip(
                    selected = selectedTypeFilter == "expense",
                    onClick = { selectedTypeFilter = "expense" },
                    label = { Text("Expense") }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Categories list
            when (categoriesState) {
                is CategoryViewModel.CategoriesState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is CategoryViewModel.CategoriesState.Error -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = (categoriesState as CategoryViewModel.CategoriesState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is CategoryViewModel.CategoriesState.Success -> {
                    val categories = (categoriesState as CategoryViewModel.CategoriesState.Success).categories
                    
                    if (categories.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "No categories found. Add your first category!",
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                CategoryItem(category) {
                                    categoryToDelete = category
                                    showDeleteConfirmDialog = true
                                }
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category type indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (category.type == "income") Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else Color(0xFFF44336).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name.take(1).uppercase(),
                    color = if (category.type == "income") Color(0xFF4CAF50) else Color(0xFFF44336),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = category.type.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = if (category.type == "income") Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAddCategory: (name: String, type: String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var categoryType by remember { mutableStateOf("expense") } // Default to expense
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add New Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Category Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = categoryType == "expense",
                        onClick = { categoryType = "expense" }
                    )
                    Text("Expense")
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    RadioButton(
                        selected = categoryType == "income",
                        onClick = { categoryType = "income" }
                    )
                    Text("Income")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                onAddCategory(categoryName, categoryType)
                            }
                        },
                        enabled = categoryName.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}