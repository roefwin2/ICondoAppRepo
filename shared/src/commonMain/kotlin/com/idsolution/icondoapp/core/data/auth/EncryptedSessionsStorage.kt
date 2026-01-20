package com.idsolution.icondoapp.core.data.auth

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
                it[stringPreferencesKey(KEY_ACCESS_TOKEN)]
            }.first()
            val refreshToken = dataStore.data.map {
                it[stringPreferencesKey(KEY_REFRESH_TOKEN)]
            }.first()
            val username = dataStore.data.map {
                it[stringPreferencesKey(USERNAME_AUTH_INFO)]
            }.first()
            val password = dataStore.data.map {
                it[stringPreferencesKey(PASSWORD_AUTH_INFO)]
            }.first()

            if (accessToken == null) {
                null
            } else {
                AuthInfo(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    username = username ?: "",
                    password = password ?: ""
                )
            }
        }
    }

    override suspend fun set(info: AuthInfo?) {
        withContext(Dispatchers.IO) {
            if (info == null) {
                dataStore.edit { prefs ->
                    prefs.remove(stringPreferencesKey(KEY_ACCESS_TOKEN))
                    prefs.remove(stringPreferencesKey(KEY_REFRESH_TOKEN))
                    prefs.remove(stringPreferencesKey(USERNAME_AUTH_INFO))
                    prefs.remove(stringPreferencesKey(PASSWORD_AUTH_INFO))
                }
                return@withContext
            } else {
                println("📦 EncryptedSessionsStorage set:")
                println("   - Access token: ${info.accessToken.take(20)}...")
                println("   - Has refresh token: ${info.refreshToken != null}")
                dataStore.edit { prefs ->
                    prefs[stringPreferencesKey(KEY_ACCESS_TOKEN)] = info.accessToken
                    info.refreshToken?.let {
                        prefs[stringPreferencesKey(KEY_REFRESH_TOKEN)] = it
                    }
                    prefs[stringPreferencesKey(USERNAME_AUTH_INFO)] = info.username
                    prefs[stringPreferencesKey(PASSWORD_AUTH_INFO)] = info.password
                }
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(KEY_ACCESS_TOKEN))
            prefs.remove(stringPreferencesKey(KEY_REFRESH_TOKEN))
            prefs.remove(stringPreferencesKey(USERNAME_AUTH_INFO))
            prefs.remove(stringPreferencesKey(PASSWORD_AUTH_INFO))
        }
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
        private const val KEY_REFRESH_TOKEN = "KEY_REFRESH_TOKEN"
        private const val USERNAME_AUTH_INFO = "USERNAME_AUTH_INFO"
        private const val PASSWORD_AUTH_INFO = "PASSWORD_AUTH_INFO"
    }
}