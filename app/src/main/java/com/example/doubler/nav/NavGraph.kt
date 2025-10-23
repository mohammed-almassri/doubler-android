package com.example.doubler.nav


import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.doubler.feature.auth.data.local.PreferencesDataSource
import com.example.doubler.feature.auth.data.repository.AuthRepositoryImpl
import com.example.doubler.feature.auth.domain.repository.AuthRepository
import com.example.doubler.feature.auth.ui.screen.LoginScreen
import com.example.doubler.feature.auth.ui.screen.RegisterScreen
import com.example.doubler.feature.auth.ui.viewModel.RegisterViewModel
import com.example.doubler.feature.email.ui.screen.ComposeEmailScreen
import com.example.doubler.feature.email.ui.screen.EmailDetailScreen
import com.example.doubler.feature.email.ui.screen.EmailHomeScreen
import com.example.doubler.feature.email.ui.screen.EmailListScreen
import com.example.doubler.feature.email.ui.screen.EmailListType
import com.example.doubler.feature.home.ui.screen.HomeScreen
import com.example.doubler.feature.persona.ui.screen.CreatePersonaScreen

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack<AppNavKey>(Home)

    // Determine if the bottom bar should be shown
    val showBottomBar = when (backStack.lastOrNull()) {
        Register, Login, CreatePersona, EmailHome, EmailInbox, EmailOutbox, EmailDrafts, EmailStarred, ComposeEmail -> false // Screens WITHOUT a bottom bar
        is EmailDetail -> false // Email detail screen without bottom bar
        else -> true // Screens WITH a bottom bar (Home, Settings, etc.)
    }

    Scaffold(
//        bottomBar = {
//            // Only show the NavigationBar if showBottomBar is true
//            if (showBottomBar) {
//                NavigationBar {
//                    NavigationBarItem(
//                        label = { Text("Category") },
//                        selected = backStack.lastOrNull() == Category,
//                        onClick = {
//                            backStack.clear()
//                            backStack.add(Category)
//                        },
//                        icon = {
//                            Icon(Icons.Default.Home, contentDescription = "Categories")
//                        }
//                    )
//                    NavigationBarItem(
//                        label = { Text("Settings") },
//                        selected = backStack.lastOrNull() == Settings,
//                        onClick = {
//                            // This logic is a bit redundant. Clearing and adding once is enough.
//                            backStack.clear()
//                            backStack.add(Settings)
//                        },
//                        icon = {
//                            Icon(Icons.Default.Settings, contentDescription = "Settings")
//                        }
//                    )
//                }
//            }
//        }
    ) { innerPadding ->
        val context = LocalContext.current

//        val itemListViewModel: ItemViewModel = viewModel {
//            ItemViewModel(OfflineItemsRepository(ItemDatabase.getDatabase(context).itemDao()))
//        }
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {


                entry <Register>{
                    RegisterScreen(
                        onNavigateToLogin = {
                            backStack.clear()
                            backStack.add(Login)
                        },
                        onRegisterSuccess = {
                            backStack.clear()
                            backStack.add(CreatePersona)
                        }
                    )
                }
                
                entry <CreatePersona>{
                    CreatePersonaScreen(
                        onPersonaCreated = {
                            backStack.clear()
                            backStack.add(Home)
                        }
                    )
                }
                
                entry <Login>{
                    LoginScreen(
                        onNavigateToRegister = {
                            backStack.clear()
                            backStack.add(Register)
                        }
                        , onLoginSuccess = {
                            backStack.clear()
                            backStack.add(Home)
                        }
                    )
                }
                entry <Home>{
                    HomeScreen (
                        onNavigateToLogin = {
                            backStack.clear()
                            backStack.add(Login)
                        },
                        onNavigateToEmailHome = {
                            backStack.add(EmailHome)
                        }
                    )
                }
                
                entry <EmailHome>{
                    EmailHomeScreen(
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        },
                        onNavigateToInbox = {
                            backStack.add(EmailInbox)
                        },
                        onNavigateToOutbox = {
                            backStack.add(EmailOutbox)
                        },
                        onNavigateToDrafts = {
                            backStack.add(EmailDrafts)
                        },
                        onNavigateToStarred = {
                            backStack.add(EmailStarred)
                        },
                        onNavigateToCompose = {
                            backStack.add(ComposeEmail)
                        }
                    )
                }
                
                entry <EmailInbox>{
                    EmailListScreen(
                        listType = EmailListType.INBOX,
                        onNavigateToCompose = {
                            backStack.add(ComposeEmail)
                        },
                        onNavigateToDetail = { emailId ->
                            backStack.add(EmailDetail(emailId))
                        },
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry <EmailOutbox>{
                    EmailListScreen(
                        listType = EmailListType.OUTBOX,
                        onNavigateToCompose = {
                            backStack.add(ComposeEmail)
                        },
                        onNavigateToDetail = { emailId ->
                            backStack.add(EmailDetail(emailId))
                        },
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry <EmailDrafts>{
                    EmailListScreen(
                        listType = EmailListType.DRAFTS,
                        onNavigateToCompose = {
                            backStack.add(ComposeEmail)
                        },
                        onNavigateToDetail = { emailId ->
                            backStack.add(EmailDetail(emailId))
                        },
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry <EmailStarred>{
                    EmailListScreen(
                        listType = EmailListType.STARRED,
                        onNavigateToCompose = {
                            backStack.add(ComposeEmail)
                        },
                        onNavigateToDetail = { emailId ->
                            backStack.add(EmailDetail(emailId))
                        },
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry <ComposeEmail>{
                    ComposeEmailScreen(
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                
                entry <EmailDetail> { emailDetail ->
                    EmailDetailScreen(
                        emailId = emailDetail.emailId,
                        onNavigateBack = {
                            backStack.removeLastOrNull()
                        },
                        onNavigateToCompose = { replyTo, subject ->
                            backStack.add(ComposeEmail)
                        }
                    )
                }
//                entry<Items> {
//                    ItemScreen(
//                        modifier = modifier,
//                        itemListViewModel,
//                        navigateToItemDetails = {
//                            backStack.add(ItemDetails)
//                        }
//                    )
//                }
//                entry<ItemDetails> {
//                    ItemDetailsScreen(
//                        modifier = modifier,
//                        itemListViewModel
//                    )
//                }
//                entry<Category> {
//                    CategoryScreen(
//                        modifier = modifier,
//                        itemListViewModel = itemListViewModel,
//                        navigateToItemScreen = ({ cat ->
//                            // This clears the history, which might be what you want.
//                            // If you want to be able to go "back" to the category screen,
//                            // you should just use backStack.add()
//                            backStack.clear()
//                            backStack.add(Items(cat))
//                        })
//                    )
//                }
//
//                entry<Settings> {
//                    SettingsScreen(modifier)
//                }
            },
            modifier = modifier
                .padding(innerPadding)
        )
    }
}
