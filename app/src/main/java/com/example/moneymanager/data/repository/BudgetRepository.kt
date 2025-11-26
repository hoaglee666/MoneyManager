package pose.moneymanager.data.repository

import pose.moneymanager.data.model.Budget
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Interface for managing budget data.
 */
interface BudgetRepository {

    // Retrieves a flow of budgets for a given date.
    fun getBudgets(date: Date): Flow<List<Budget>>

    //save a new budget
    suspend fun saveBudget(budget: Budget): Result<Unit>

    //update existing budget
    suspend fun updateBudget(budget: Budget): Result<Unit>

    //delete budget
    suspend fun deleteBudget(budgetId: String): Result<Unit>
}
