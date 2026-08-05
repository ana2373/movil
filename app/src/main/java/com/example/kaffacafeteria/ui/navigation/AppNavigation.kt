package com.example.kaffacafeteria.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kaffacafeteria.ui.admin.*
import com.example.kaffacafeteria.ui.auth.LoginScreen
import com.example.kaffacafeteria.ui.auth.RegisterScreen
import com.example.kaffacafeteria.ui.barista.CashRegisterScreen
import com.example.kaffacafeteria.ui.gestion.ComprasScreen
import com.example.kaffacafeteria.ui.gestion.MermasScreen
import com.example.kaffacafeteria.ui.components.KaffaBottomBar
import com.example.kaffacafeteria.ui.components.NavItem
import com.example.kaffacafeteria.ui.home.HomeScreen
import com.example.kaffacafeteria.ui.orders.OrderDetailScreen
import com.example.kaffacafeteria.ui.orders.OrderListScreen
import com.example.kaffacafeteria.ui.pos.POSScreen
import com.example.kaffacafeteria.ui.productos.ProductListScreen
import com.example.kaffacafeteria.ui.profile.ProfileScreen
import com.example.kaffacafeteria.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object POS : Screen("pos")
    data object Orders : Screen("orders")
    data object OrderDetail : Screen("orders/{pedidoId}") {
        fun createRoute(pedidoId: Int) = "orders/$pedidoId"
    }
    data object Productos : Screen("productos")
    data object AdminPanel : Screen("admin_panel")
    data object Users : Screen("admin/users")
    data object Categories : Screen("admin/categories")
    data object ManageProducts : Screen("admin/products")
    data object PaymentMethods : Screen("admin/payment_methods")
    data object Reports : Screen("admin/reports")
    data object Caja : Screen("caja")
    data object Compras : Screen("compras")
    data object Mermas : Screen("mermas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isLoggedIn by remember { mutableStateOf(false) }

    val bottomNavItems = remember {
        listOf(
            NavItem("Inicio", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
            NavItem("Pedidos", Screen.Orders.route, Icons.Filled.List, Icons.Outlined.List),
            NavItem("POS", Screen.POS.route, Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
            NavItem("Productos", Screen.Productos.route, Icons.Filled.Search, Icons.Outlined.Search),
            NavItem("Perfil", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
        )
    }

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    fun requireAuth(action: () -> Unit) {
        if (isLoggedIn) action() else navController.navigate(Screen.Login.route)
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                KaffaBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        if (route != currentRoute) {
                            if (route in setOf(Screen.Orders.route, Screen.POS.route, Screen.Profile.route)) {
                                requireAuth {
                                    navController.navigate(route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            } else {
                                navController.navigate(route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onContinue = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                    onSessionState = { isLoggedIn = it }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        isLoggedIn = true
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onRegisterSuccess = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } } }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToOrders = { requireAuth { navController.navigate(Screen.Orders.route) } },
                    onNavigateToPOS = { requireAuth { navController.navigate(Screen.POS.route) } },
                    onNavigateToProfile = { requireAuth { navController.navigate(Screen.Profile.route) } },
                    onNavigateToAdminPanel = { requireAuth { navController.navigate(Screen.AdminPanel.route) } },
                    onNavigateToCaja = { requireAuth { navController.navigate(Screen.Caja.route) } },
                    onNavigateToCompras = { requireAuth { navController.navigate(Screen.Compras.route) } },
                    onNavigateToProductos = { navController.navigate(Screen.Productos.route) },
                    onNavigateToGastos = { requireAuth { navController.navigate(Screen.Compras.route) } },
                    onNavigateToMermas = { requireAuth { navController.navigate(Screen.Mermas.route) } },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onLogout = {
                        isLoggedIn = false
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        isLoggedIn = false
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    darkTheme = darkTheme,
                    onToggleTheme = onToggleTheme
                )
            }

            composable(Screen.POS.route) { POSScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Orders.route) {
                OrderListScreen(
                    onBack = { navController.popBackStack() },
                    onOrderClick = { navController.navigate(Screen.OrderDetail.createRoute(it)) }
                )
            }

            composable(Screen.OrderDetail.route, arguments = listOf(navArgument("pedidoId") { type = NavType.IntType })) { backStackEntry ->
                OrderDetailScreen(
                    pedidoId = backStackEntry.arguments?.getInt("pedidoId") ?: 0,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Productos.route) {
                ProductListScreen(
                    onBack = { navController.popBackStack() },
                    onBuy = {
                        requireAuth { navController.navigate(Screen.POS.route) }
                    }
                )
            }
            composable(Screen.AdminPanel.route) {
                AdminPanelScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToUsers = { navController.navigate(Screen.Users.route) },
                    onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                    onNavigateToProducts = { navController.navigate(Screen.ManageProducts.route) },
                    onNavigateToPaymentMethods = { navController.navigate(Screen.PaymentMethods.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) }
                )
            }
            composable(Screen.Users.route) { UsersScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Categories.route) { CategoriesScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.ManageProducts.route) { ManageProductsScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.PaymentMethods.route) { PaymentMethodsScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Reports.route) { ReportsScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Caja.route) { CashRegisterScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Compras.route) { ComprasScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.Mermas.route) { MermasScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
