package com.chitsmart.di

import com.chitsmart.data.api.AuthApiService
import com.chitsmart.data.local.TokenStore
import com.chitsmart.data.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module — provides [AuthRepository] as a singleton.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApiService,
        tokenStore: TokenStore,
    ): AuthRepository = AuthRepository(api, tokenStore)
}
