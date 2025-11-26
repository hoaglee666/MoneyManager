package pose.moneymanager.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pose.moneymanager.data.model.Transaction
import pose.moneymanager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val _transactionsState = MutableStateFlow<TransactionsState>(TransactionsState.Loading)
    val transactionsState: StateFlow<TransactionsState> = _transactionsState.asStateFlow()

    private val _currentTransaction = MutableStateFlow<Transaction?>(null)
    val currentTransaction: StateFlow<Transaction?> = _currentTransaction.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()
    private val _selectedTransactionIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTransactionIds: StateFlow<Set<String>> = _selectedTransactionIds.asStateFlow()

    // --- Search Logic ---
    private var _cachedTransactions: List<Transaction> = emptyList() // Stores raw data from repo
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applySearchFilter()
    }

    private fun applySearchFilter() {
        val query = _searchQuery.value
        if (query.isBlank()) {
            _transactionsState.value = TransactionsState.Success(_cachedTransactions)
        } else {
            val filtered = _cachedTransactions.filter { transaction ->
                transaction.category.contains(query, ignoreCase = true) ||
                        transaction.description.contains(query, ignoreCase = true) ||
                        transaction.amount.toString().contains(query)
            }
            _transactionsState.value = TransactionsState.Success(filtered)
        }
    }

    // --- Selection Mode ---

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedTransactionIds.value = emptySet()
        }
    }

    fun toggleTransactionSelection(transactionId: String) {
        _selectedTransactionIds.value = if (_selectedTransactionIds.value.contains(transactionId)) {
            _selectedTransactionIds.value - transactionId
        } else {
            _selectedTransactionIds.value + transactionId
        }
    }

    fun selectAllTransaction(transactions: List<Transaction>) {
        _selectedTransactionIds.value = transactions.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedTransactionIds.value = emptySet()
    }

    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val idsToDelete = _selectedTransactionIds.value.toList()
            var successCount = 0
            var failCount = 0

            idsToDelete.forEach { id ->
                transactionRepository.deleteTransaction(id).fold(
                    onSuccess = { successCount++ },
                    onFailure = { failCount++ }
                )
            }
            _isSelectionMode.value = false
            _selectedTransactionIds.value = emptySet()
            if (failCount > 0 ){
                Log.e("TransactionViewModel", "Failed to delete $failCount transactions")
            }
        }
    }

    init {
        loadAllTransactions()
    }

    // --- Data Loading (Updated to use cache) ---

    fun loadAllTransactions() {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.getAllTransactions()
                .catch { e ->
                    _transactionsState.value = TransactionsState.Error(e.message ?: "Failed to load transactions")
                }
                .collectLatest { transactions ->
                    _cachedTransactions = transactions
                    applySearchFilter()
                }
        }
    }

    fun loadTransactionsByType(type: String) {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.getTransactionsByType(type)
                .catch { e ->
                    Log.e("TransactionViewModel", "Error loading by type: ${e.message}", e)
                    _transactionsState.value = TransactionsState.Error(e.message ?: "Failed to load transactions by type")
                }
                .collectLatest { transactions ->
                    _cachedTransactions = transactions
                    applySearchFilter()
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTransactionsByMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.getTransactionsByMonth(yearMonth.monthValue, yearMonth.year)
                .catch { e ->
                    Log.e("TransactionViewModel", "Error loading by month: ${e.message}", e)
                    _transactionsState.value = TransactionsState.Error(e.message ?: "Failed to load transactions by month")
                }
                .collectLatest { transactions ->
                    _cachedTransactions = transactions
                    applySearchFilter()
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTransactionsByTypeAndMonth(type: String, yearMonth: YearMonth) {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.getTransactionsByTypeAndMonth(type, yearMonth)
                .catch { e ->
                    Log.e("TransactionViewModel", "Error loading by type and month: ${e.message}", e)
                    _transactionsState.value = TransactionsState.Error(e.message ?: "Failed to load filtered transactions")
                }
                .collectLatest { transactions ->
                    _cachedTransactions = transactions
                    applySearchFilter()
                }
        }
    }

    fun loadRecentTransactions(limit: Int = 5) {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.getRecentTransactions(limit)
                .catch { e ->
                    _transactionsState.value = TransactionsState.Error(e.message ?: "Failed to load transactions")
                }
                .collectLatest { transactions ->
                    _cachedTransactions = transactions
                    applySearchFilter()
                }
        }
    }

    // --- CRUD Operations ---

    fun getTransactionById(id: String) {
        viewModelScope.launch {
            transactionRepository.getTransactionById(id).fold(
                onSuccess = { transaction -> _currentTransaction.value = transaction },
                onFailure = { /* Handle error */ }
            )
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.addTransaction(transaction)
                .fold(
                    onSuccess = { loadAllTransactions() },
                    onFailure = { error ->
                        _transactionsState.value = TransactionsState.Error(error.message ?: "Failed to add transaction")
                    }
                )
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
                .fold(
                    onSuccess = { loadAllTransactions() },
                    onFailure = { /* Handle error */ }
                )
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transactionId)
                .fold(
                    onSuccess = { loadAllTransactions() },
                    onFailure = { /* Handle error */ }
                )
        }
    }

    sealed class TransactionsState {
        object Loading : TransactionsState()
        data class Success(val transactions: List<Transaction>) : TransactionsState()
        data class Error(val message: String) : TransactionsState()
    }
}