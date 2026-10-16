package com.chitsmart.di

import com.chitsmart.data.api.AuthApiService
import com.chitsmart.data.local.TokenStorage
import com.chitsmart.data.local.TokenStore
import com.chitsmart.data.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module — binds [TokenStore] to [TokenStorage] and provides [AuthRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    /** Binds the concrete [TokenStore] to the [TokenStorage] interface. */
    @Binds
    @Singleton
    abstract fun bindTokenStorage(tokenStore: TokenStore): TokenStorage

    companion object {
        @Provides
        @Singleton
        fun provideAuthRepository(
            api: AuthApiService,
            tokenStorage: TokenStorage,
        ): AuthRepository = AuthRepository(api, tokenStorage)
    }
}
