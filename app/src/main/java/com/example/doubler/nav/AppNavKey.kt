package com.example.doubler.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface AppNavKey: NavKey

@Serializable
object Register: AppNavKey
@Serializable
object Login: AppNavKey
@Serializable
object CreatePersona: AppNavKey
@Serializable
object Home: AppNavKey
@Serializable
object EmailHome: AppNavKey
@Serializable
object EmailInbox: AppNavKey
@Serializable
object EmailOutbox: AppNavKey
@Serializable
object EmailDrafts: AppNavKey
@Serializable
object EmailStarred: AppNavKey
@Serializable
object ComposeEmail: AppNavKey
@Serializable
data class EmailDetail(val emailId: String): AppNavKey
