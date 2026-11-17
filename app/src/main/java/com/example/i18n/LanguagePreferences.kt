package com.example.i18n

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppLanguage(val code: String, val label: String) {
  EN("EN", "English"),
  HI("HI", "हिन्दी")
}

private val Context.dataStore by preferencesDataStore(name = "srjp_school_prefs")

class LanguagePreferences(private val context: Context) {
  private val languageKey = stringPreferencesKey("app_language")

  val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
    when (prefs[languageKey]) {
      AppLanguage.HI.code -> AppLanguage.HI
      else -> AppLanguage.EN
    }
  }

  suspend fun setLanguage(language: AppLanguage) {
    context.dataStore.edit { prefs ->
      prefs[languageKey] = language.code
    }
  }
}
