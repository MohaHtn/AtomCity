package org.arcade.atomcity.domain.repository

import kotlinx.coroutines.flow.Flow

interface BaseRepository<T> {
    suspend fun getAll(): Flow<List<T>>
    suspend fun getById(id: String): Flow<T>
    suspend fun create(item: T): Flow<T>
    suspend fun update(item: T): Flow<T>
    suspend fun delete(id: String): Flow<Boolean>
} 