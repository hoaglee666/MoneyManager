package pose.moneymanager.di

import android.content.Context
import pose.moneymanager.data.repository.AuthRepository
import pose.moneymanager.data.repository.BudgetRepository
import pose.moneymanager.data.repository.CategoryRepository
import pose.moneymanager.data.repository.FirebaseAuthRepository
import pose.moneymanager.data.repository.FirebaseBudgetRepository
import pose.moneymanager.data.repository.FirebaseCategoryRepository
import pose.moneymanager.data.repository.FirebaseTransactionRepository
import pose.moneymanager.data.repository.TransactionRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {

        val webClientId = context.getString(pose.moneymanager.R.string.default_web_client_id)

        require(webClientId.isNotBlank()) {
            "default_web_client_id is missing! Check your google-services.json."
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return FirebaseAuthRepository(auth)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): TransactionRepository {
        return FirebaseTransactionRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CategoryRepository {
        return FirebaseCategoryRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): BudgetRepository {
        return FirebaseBudgetRepository(firestore, auth)
    }
}
