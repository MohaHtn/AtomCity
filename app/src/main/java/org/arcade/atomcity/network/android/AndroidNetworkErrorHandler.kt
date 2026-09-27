/**
 * Android Network Error Handler
 *
 * Implements [NetworkErrorHandler] for Android platform to propagate network and API errors to the global UI state.
 */
package org.arcade.atomcity.network.android

import org.arcade.atomcity.data.remote.NetworkErrorHandler
import org.arcade.atomcity.ui.core.GlobalUIState

class AndroidNetworkErrorHandler : NetworkErrorHandler {
    override fun onError(message: String) {
        GlobalUIState.globalError.value = message
    }

}
