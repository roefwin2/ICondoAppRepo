package com.idsolution.icondoapp.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

expect fun createDataStore() : DataStore<Preferences>

internal const val DATA_STORE_FILE_NAME  = "prefs.preferences_pb"