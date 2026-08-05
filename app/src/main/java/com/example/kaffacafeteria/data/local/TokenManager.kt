package com.example.kaffacafeteria.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kaffacafeteria.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREF_NAME)

class TokenManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey(Constants.TOKEN_KEY)
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val PROMO_SHOWN_DATE_KEY = stringPreferencesKey("promo_shown_date")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[TOKEN_KEY]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun deleteToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }

    suspend fun isDarkMode(): Boolean {
        return context.dataStore.data.first()[DARK_MODE_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun getPromoShownDate(): String? {
        return context.dataStore.data.first()[PROMO_SHOWN_DATE_KEY]
    }

    suspend fun setPromoShownDate(date: String) {
        context.dataStore.edit { prefs ->
            prefs[PROMO_SHOWN_DATE_KEY] = date
        }
    }
}
