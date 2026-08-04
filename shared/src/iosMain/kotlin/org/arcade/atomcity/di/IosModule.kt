package org.arcade.atomcity.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import org.koin.dsl.module
import okio.Path.Companion.toPath

@OptIn(ExperimentalForeignApi::class)
val iosModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            storage = androidx.datastore.core.okio.OkioStorage(
                fileSystem = okio.FileSystem.SYSTEM,
                producePath = {
                    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                        directory = NSDocumentDirectory,
                        inDomain = NSUserDomainMask,
                        appropriateForURL = null,
                        create = false,
                        error = null,
                    )
                    val path = requireNotNull(documentDirectory).path + "/api_keys.preferences_pb"
                    path.toPath()
                },
                serializer = androidx.datastore.preferences.core.PreferencesSerializer
            )
        )
    }
}
