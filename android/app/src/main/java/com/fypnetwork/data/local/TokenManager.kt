package com.fypnetwork.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_tokens")

/**
 * Persists the JWT access/refresh token pair between app launches.
 *
 * DataStore (not raw SharedPreferences) is the current Android-recommended
 * approach for small key-value data; it's async, type-safe, and avoids
 * the disk I/O-on-main-thread pitfalls of the old SharedPreferences API.
 */
@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val accessTokenFlow: Flow<String?> =
        context.dataStore.data.map { it[Keys.ACCESS_TOKEN] }

    val isLoggedInFlow: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ACCESS_TOKEN] != null }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun getAccessToken(): String? = context.dataStore.data.first()[Keys.ACCESS_TOKEN]

    suspend fun getRefreshToken(): String? = context.dataStore.data.first()[Keys.REFRESH_TOKEN]

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
