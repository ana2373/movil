package com.example.kaffacafeteria.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.promoPopupDataStore by preferencesDataStore(name = "promo_popup_prefs")

class PromoPopupDismissStore(private val context: Context) {
    private val dismissedPromoIdsKey = stringSetPreferencesKey("dismissed_promo_ids")
    private val dismissedEventoIdsKey = stringSetPreferencesKey("dismissed_evento_ids")

    suspend fun getDismissedPromoIds(): Set<String> {
        return try {
            val prefs = context.promoPopupDataStore.data.first()
            prefs[dismissedPromoIdsKey] ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun dismissPromo(id: Int) {
        try {
            context.promoPopupDataStore.edit { prefs ->
                val current = prefs[dismissedPromoIdsKey] ?: emptySet()
                prefs[dismissedPromoIdsKey] = current + id.toString()
            }
        } catch (e: Exception) {
        }
    }

    suspend fun getDismissedEventoIds(): Set<String> {
        return try {
            val prefs = context.promoPopupDataStore.data.first()
            prefs[dismissedEventoIdsKey] ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun dismissEvento(id: Int) {
        try {
            context.promoPopupDataStore.edit { prefs ->
                val current = prefs[dismissedEventoIdsKey] ?: emptySet()
                prefs[dismissedEventoIdsKey] = current + id.toString()
            }
        } catch (e: Exception) {
        }
    }
}
