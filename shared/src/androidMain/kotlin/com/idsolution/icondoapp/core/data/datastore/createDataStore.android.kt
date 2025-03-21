package com.idsolution.icondoapp.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.context.GlobalContext.get

actual fun createDataStore(): DataStore<Preferences> {
    val context: Context = get().get()
    return PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath.toPath()
    }
}