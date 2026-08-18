package org.arcade.atomcity.data.remote

import org.arcade.atomcity.utils.PlatformUtils

class IosNetworkErrorHandler : NetworkErrorHandler {
    override fun onError(message: String) {
        PlatformUtils.log("Network Error", message, isError = true)
    }
}
