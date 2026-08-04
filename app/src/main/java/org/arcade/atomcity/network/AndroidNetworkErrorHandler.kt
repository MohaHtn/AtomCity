package org.arcade.atomcity.network

import org.arcade.atomcity.ui.core.GlobalUIState

class AndroidNetworkErrorHandler : NetworkErrorHandler {
    override fun onError(message: String) {
        GlobalUIState.globalError.value = message
    }
}
