package org.arcade.atomcity.data.remote

interface NetworkErrorHandler {
    fun onError(message: String)
}
