package org.arcade.atomcity.data.cache

class DataCache<T>(private val validityDuration: Long = DEFAULT_CACHE_VALIDITY_DURATION) {
    private var cachedData: T? = null
    private var cacheTimestamp: Long = 0

    fun get(): T? {
        val currentTime = System.currentTimeMillis()
        return if (cachedData != null && currentTime - cacheTimestamp < validityDuration) {
            cachedData
        } else {
            null
        }
    }

    fun put(data: T) {
        cachedData = data
        cacheTimestamp = System.currentTimeMillis()
    }

    fun clear() {
        cachedData = null
        cacheTimestamp = 0
    }

    fun isValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        return cachedData != null && currentTime - cacheTimestamp < validityDuration
    }

    companion object {
        const val DEFAULT_CACHE_VALIDITY_DURATION = 10 * 60 * 1000L // 10 minutes
    }
}