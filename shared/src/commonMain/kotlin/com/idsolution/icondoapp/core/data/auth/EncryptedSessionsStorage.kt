package com.example.testkmpapp.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.idsolution.icondoapp.core.domain.AuthInfo
import com.idsolution.icondoapp.core.domain.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EncryptedSessionsStorage(
    private val dataStore: DataStore<Preferences>
) : SessionStorage {
    override suspend fun get(): AuthInfo? {
        return withContext(Dispatchers.IO) {
            val accessToken = dataStore.data.map {
                it[stringPreferencesKey(KEY_AUTH_INFO)]
            }.first()
            val username = dataStore.data.map {
                it[stringPreferencesKey(USERNAME_AUTH_INFO)]
            }.first()
            if (accessToken == null) {
                null
            } else {
                AuthInfo(accessToken = accessToken, username = username ?: "")
            }
        }
    }

    override suspend fun set(info: AuthInfo?) {
        withContext(Dispatchers.IO) { // Blocking
            if (info == null) {
                dataStore.edit { dataStore ->
                    dataStore.remove(stringPreferencesKey(KEY_AUTH_INFO))
                    dataStore.remove(stringPreferencesKey(USERNAME_AUTH_INFO))
                }
                return@withContext
            } else {
                println("EncryptedSessionsStorage set: $info")
                dataStore.edit { dataStore ->
                    dataStore[stringPreferencesKey(KEY_AUTH_INFO)] = info.accessToken
                    dataStore[stringPreferencesKey(USERNAME_AUTH_INFO)] = info.username
                }
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { dataStore ->
            dataStore.remove(stringPreferencesKey(KEY_AUTH_INFO))
            dataStore.remove(stringPreferencesKey(USERNAME_AUTH_INFO))
        }
    }

    companion object {
        private const val KEY_AUTH_INFO = "KEY_AUTH_INFO"
        private const val USERNAME_AUTH_INFO = "USERNAME_AUTH_INFO"
    }
}
