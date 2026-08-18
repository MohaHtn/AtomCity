package org.arcade.atomcity.network.android

import org.arcade.atomcity.data.remote.NetworkErrorHandler
import org.arcade.atomcity.ui.core.GlobalUIState

class AndroidNetworkErrorHandler : NetworkErrorHandler {
    override fun onError(message: String) {
        GlobalUIState.globalError.value = message
    }

