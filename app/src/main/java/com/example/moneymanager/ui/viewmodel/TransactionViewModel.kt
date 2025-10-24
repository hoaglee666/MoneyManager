package com.example.moneymanager.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.repository.TransactionRepository
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

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedTransactionIds.value = emptySet() //clear selection when exit
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

            //exist select and clear
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

    fun loadAllTransactions() {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.getAllTransactions()
                .catch { e ->
                    _transactionsState.value = TransactionsState.Error(e.message ?: "Failed to load transactions")
                }
                .collectLatest { transactions ->
                    _transactionsState.value = TransactionsState.Success(transactions)
                }
        }
    }


    fun loadTransactionsByType(type: String) {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.getTransactionsByType(type)
                .catch { e ->
                    Log.e("TransactionViewModel", "Error loading by type: ${e.message}", e)
                    _transactionsState.value = TransactionsState.Error(
                        e.message ?: "Failed to load transactions by type"
                    )
                }
                .collectLatest { transactions ->
                    _transactionsState.value = TransactionsState.Success(transactions)
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
                    _transactionsState.value = TransactionsState.Error(
                        e.message ?: "Failed to load transactions by month"
                    )
                }
                .collectLatest { transactions ->
                    _transactionsState.value = TransactionsState.Success(transactions)
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
                    _transactionsState.value = TransactionsState.Success(transactions)
                }
        }
    }

    fun getTransactionById(id: String) {
        viewModelScope.launch {
            transactionRepository.getTransactionById(id).fold(
                onSuccess = { transaction ->
                    _currentTransaction.value = transaction
                },
                onFailure = { error ->
                    // Handle error
                }
            )
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.addTransaction(transaction)
                .fold(
                    onSuccess = { 
                        loadAllTransactions()
                    },
                    onFailure = { error ->
                        _transactionsState.value = TransactionsState.Error(
                            error.message ?: "Failed to add transaction"
                        )
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTransactionsByTypeAndMonth(type: String, yearMonth: YearMonth) {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            transactionRepository.getTransactionsByTypeAndMonth(type, yearMonth)
                .catch { e ->
                    Log.e("TransactionViewModel", "Error loading by type and month: ${e.message}", e)
                    _transactionsState.value = TransactionsState.Error(
                        e.message ?: "Failed to load filtered transactions"
                    )
                }
                .collectLatest { transactions ->
                    _transactionsState.value = TransactionsState.Success(transactions)
                }
        }
    }

    sealed class TransactionsState {
        object Loading : TransactionsState()
        data class Success(val transactions: List<Transaction>) : TransactionsState()
        data class Error(val message: String) : TransactionsState()
    }
}