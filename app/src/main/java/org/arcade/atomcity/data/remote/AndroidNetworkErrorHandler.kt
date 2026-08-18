package org.arcade.atomcity.data.remote

import org.arcade.atomcity.ui.core.GlobalUIState

class AndroidNetworkErrorHandler : NetworkErrorHandler {
    override fun onError(message: String) {
        GlobalUIState.globalError.value = message
    }
}
