package com.example.doubler.feature.auth.data.local

import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    val ID = stringPreferencesKey("ID")
    val USER_NAME = stringPreferencesKey("USER_NAME")
    val EMAIL = stringPreferencesKey("EMAIL")
    val IMAGE_URL = stringPreferencesKey("IMAGE_URL")
    val TOKEN = stringPreferencesKey("TOKEN")
}