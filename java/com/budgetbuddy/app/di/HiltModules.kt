package com.budgetbuddy.app.di

import android.content.Context
import com.budgetbuddy.app.data.auth.AuthenticationManager
import com.budgetbuddy.app.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val database = FirebaseDatabase.getInstance()
        // Enable offline persistence
        database.setPersistenceEnabled(true)
        return FirebaseDatabase.getInstance("https://budget-buddy-bbbe7-default-rtdb.asia-southeast1.firebasedatabase.app/")
    }

    @Provides
    @Singleton
    fun provideAuthenticationManager(
        auth: FirebaseAuth
    ): AuthenticationManager {
        return AuthenticationManager(auth)
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): FirebaseRepository {
        return FirebaseRepository(auth, database)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context = context
}