package com.chitsmart.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chitsmart.ui.assistant.AIChatScreen
import com.chitsmart.ui.assistant.AssistantHomeScreen
import com.chitsmart.ui.assistant.HumanSupportScreen
import com.chitsmart.ui.auth.*
import com.chitsmart.ui.chits.*
import com.chitsmart.ui.home.HomeScreen
import com.chitsmart.ui.notifications.NotificationsScreen
import com.chitsmart.ui.onboarding.OnboardingScreen
import com.chitsmart.ui.payment.*
import com.chitsmart.ui.profile.*
import com.chitsmart.ui.splash.SplashScreen

// ── Route constants ────────────────────────────────────────────────────────────

object AppRoutes {
    // Auth flow (no bottom nav)
    const val SPLASH         = "splash"
    const val ONBOARDING     = "onboarding"
    const val LOGIN          = "login"
    const val OTP            = "otp/{mobileNumber}"
    const val REGISTRATION   = "registration/{mobileNumber}"

    // Home tab
    const val HOME           = "home"
    const val NOTIFICATIONS  = "notifications"

    // My Chits tab
    const val BROWSE_GROUPS  = "browse_groups"
    const val GROUP_DETAILS  = "group_details/{groupId}/{groupName}"
    const val ENROLLMENT     = "enrollment/{groupId}/{groupName}"
    const val MY_CHITS       = "my_chits"
    const val GROUP_MEMBER   = "group_member/{membershipId}/{groupName}"

    // Payments tab
    const val PAYMENTS_HOME  = "payments_home"
    const val PAYMENT_SUMMARY   = "payment/summary/{installmentId}"
    const val PAYMENT_HISTORY   = "payment_history"
    const val PAYOUT_HISTORY    = "payout_history"
    const val RECEIPT           = "receipt/{paymentId}"

    // Assistant tab
    const val ASSISTANT      = "assistant"
    const val AI_CHAT        = "ai_chat"

    // Profile tab
    const val PROFILE        = "profile"
    const val PERSONAL_INFO  = "personal_info"
    const val BANK_ACCOUNTS  = "bank_accounts"
    const val SECURITY       = "security_settings"
    const val HELP_CENTER    = "help_centre"
    const val LANGUAGE_PREFS = "preferences"
    const val NOMINEES       = "nominees"

    // Human support
    const val HUMAN_SUPPORT  = "human_support"

    // Helper builders
    fun otp(mobileNumber: String) = "otp/$mobileNumber"
    fun registration(mobileNumber: String) = "registration/$mobileNumber"
    fun groupDetails(groupId: String, groupName: String) = "group_details/$groupId/$groupName"
    fun enrollment(groupId: String, groupName: String) = "enrollment/$groupId/$groupName"
    fun groupMember(membershipId: String, groupName: String) = "group_member/$membershipId/$groupName"
    fun paymentSummary(installmentId: String) = "payment/summary/$installmentId"
    fun receipt(paymentId: String) = "receipt/$paymentId"
}

// ── Bottom nav destinations ───────────────────────────────────────────────────

sealed class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    // Current release: Home | My Chits | Payments | Assistant | Profile
    // Auctions tab is DEFERRED — do not add here (see docs/scope-decisions.md)
    object Home : BottomNavDestination(AppRoutes.HOME, "Home", Icons.Outlined.Home)
    object MyChits : BottomNavDestination(AppRoutes.MY_CHITS, "My Chits", Icons.Outlined.AccountBalance)
    object Payments : BottomNavDestination(AppRoutes.PAYMENTS_HOME, "Payments", Icons.Outlined.Payments)
    object Assistant : BottomNavDestination(AppRoutes.ASSISTANT, "Assistant", Icons.Outlined.SmartToy)
    object Profile : BottomNavDestination(AppRoutes.PROFILE, "Profile", Icons.Outlined.Person)
}

val bottomNavItems = listOf(
    BottomNavDestination.Home,
    BottomNavDestination.MyChits,
    BottomNavDestination.Payments,
    BottomNavDestination.Assistant,
    BottomNavDestination.Profile,
)

// ── Root nav graph ────────────────────────────────────────────────────────────

@Composable
fun ChitSmartNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH,
    ) {

        // ── Auth flow ─────────────────────────────────────────────────────────

        composable(AppRoutes.SPLASH) {
            SplashScreen(
                onNavigate = { isFirstLaunch ->
                    val target = if (isFirstLaunch) AppRoutes.ONBOARDING else AppRoutes.LOGIN
                    navController.navigate(target) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(AppRoutes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onOtpSent = { mobile ->
                    navController.navigate(AppRoutes.otp(mobile))
                },
                onRegister = {
                    // Direct registration without OTP (edge case — admin flow)
                    navController.navigate(AppRoutes.registration(""))
                },
            )
        }

        composable(
            route = AppRoutes.OTP,
            arguments = listOf(navArgument("mobileNumber") { type = NavType.StringType }),
        ) { backStack ->
            val mobile = backStack.arguments?.getString("mobileNumber") ?: ""
            OTPScreen(
                mobileNumber = mobile,
                onVerified = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate(AppRoutes.registration(mobile))
                    } else {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.SPLASH) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
                onEdit = { navController.popBackStack() },
            )
        }

        composable(
            route = AppRoutes.REGISTRATION,
            arguments = listOf(navArgument("mobileNumber") { type = NavType.StringType }),
        ) { backStack ->
            val mobile = backStack.arguments?.getString("mobileNumber") ?: ""
            RegistrationScreen(
                mobileNumber = mobile.removePrefix("+91").trim(),
                onRegister = { _, _, _, _ ->
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Home tab ──────────────────────────────────────────────────────────

        composable(AppRoutes.HOME) {
            HomeScreen(
                onPayNow = { navController.navigate(AppRoutes.PAYMENTS_HOME) },
                onPayInstallment = { navController.navigate(AppRoutes.PAYMENTS_HOME) },
                onMyStatement = { navController.navigate(AppRoutes.PAYMENT_HISTORY) },
                onDownloadReceipt = { navController.navigate(AppRoutes.PAYMENT_HISTORY) },
                onContactSupport = { navController.navigate(AppRoutes.AI_CHAT) },
                onAiAssistant = { navController.navigate(AppRoutes.AI_CHAT) },
                onNotifications = { navController.navigate(AppRoutes.NOTIFICATIONS) },
                onViewAllTransactions = { navController.navigate(AppRoutes.PAYMENT_HISTORY) },
            )
        }

        composable(AppRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onNotificationTap = { /* navigate based on type */ },
            )
        }

        // ── My Chits tab ──────────────────────────────────────────────────────

        composable(AppRoutes.MY_CHITS) {
            ActiveMembershipsScreen(
                onPayNow = { membership ->
                    navController.navigate(AppRoutes.paymentSummary(membership.groupName))
                },
                onViewDetails = { membership ->
                    navController.navigate(
                        AppRoutes.groupMember(membership.groupName, membership.groupName)
                    )
                },
                onDownloadCertificate = { /* launch PDF intent */ },
                onBrowseGroups = { navController.navigate(AppRoutes.BROWSE_GROUPS) },
                onBack = { navController.popBackStack() },
                onNotifications = { navController.navigate(AppRoutes.NOTIFICATIONS) },
            )
        }

        composable(AppRoutes.BROWSE_GROUPS) {
            BrowseGroupsScreen(
                onEnroll = { group ->
                    navController.navigate(AppRoutes.enrollment(group.name, group.name))
                },
                onKnowMore = { group ->
                    navController.navigate(AppRoutes.groupDetails(group.name, group.name))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = AppRoutes.GROUP_DETAILS,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType },
            ),
        ) { backStack ->
            val groupName = backStack.arguments?.getString("groupName") ?: ""
            GroupDetailsScreen(
                group = sampleGroupDetail.copy(groupName = groupName),
                onBack = { navController.popBackStack() },
                onEnroll = {
                    navController.navigate(AppRoutes.enrollment(groupName, groupName))
                },
            )
        }

        composable(
            route = AppRoutes.ENROLLMENT,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType },
            ),
        ) { backStack ->
            val groupName = backStack.arguments?.getString("groupName") ?: ""
            EnrollmentScreen(
                groupName = groupName,
                onBack = { navController.popBackStack() },
                onEnrollmentComplete = {
                    navController.navigate(AppRoutes.MY_CHITS) {
                        popUpTo(AppRoutes.HOME)
                    }
                },
            )
        }

        composable(
            route = AppRoutes.GROUP_MEMBER,
            arguments = listOf(
                navArgument("membershipId") { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType },
            ),
        ) { backStack ->
            val groupName = backStack.arguments?.getString("groupName") ?: ""
            GroupMemberViewScreen(
                membership = sampleMembershipView.copy(groupName = groupName),
                onBack = { navController.popBackStack() },
                onPayNow = { _ ->
                    navController.navigate(AppRoutes.paymentSummary(groupName))
                },
                onViewReceipt = { paymentId ->
                    navController.navigate(AppRoutes.receipt(paymentId))
                },
            )
        }

        // ── Payments tab ──────────────────────────────────────────────────────

        composable(AppRoutes.PAYMENTS_HOME) {
            PaymentsHomeScreen(
                onPayDue = { due ->
                    navController.navigate(AppRoutes.paymentSummary(due.groupName))
                },
                onViewHistory = { navController.navigate(AppRoutes.PAYMENT_HISTORY) },
                onViewPayouts = { navController.navigate(AppRoutes.PAYOUT_HISTORY) },
                onNotifications = { navController.navigate(AppRoutes.NOTIFICATIONS) },
            )
        }

        composable(
            route = AppRoutes.PAYMENT_SUMMARY,
            arguments = listOf(navArgument("installmentId") { type = NavType.StringType }),
        ) {
            PaymentFlowScreen(
                onBack = { navController.popBackStack() },
                onPaymentComplete = { success, txnId ->
                    // PaymentFlowScreen handles its own step navigation internally
                    // Done button navigates to Home
                    if (success) {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.PAYMENTS_HOME)
                        }
                    }
                },
            )
        }

        composable(AppRoutes.PAYMENT_HISTORY) {
            PaymentHistoryScreen(
                onReceiptTap = { tx ->
                    navController.navigate(AppRoutes.receipt(tx.id))
                },
                onDownloadStatement = { /* launch PDF export */ },
                onBack = { navController.popBackStack() },
                onPayNow = { navController.navigate(AppRoutes.PAYMENTS_HOME) },
            )
        }

        composable(AppRoutes.PAYOUT_HISTORY) {
            PayoutHistoryScreen(
                onBack = { navController.popBackStack() },
                onDownloadStatement = { /* launch PDF */ },
                onDownloadCertificate = { /* launch PDF */ },
            )
        }

        composable(
            route = AppRoutes.RECEIPT,
            arguments = listOf(navArgument("paymentId") { type = NavType.StringType }),
        ) { backStack ->
            val paymentId = backStack.arguments?.getString("paymentId") ?: ""
            ReceiptScreen(
                paymentId = paymentId,
                onBack = { navController.popBackStack() },
                onDownloadPDF = { /* launch PDF intent */ },
                onShareWhatsApp = { /* launch WhatsApp share intent */ },
                onShareEmail = { /* launch email share intent */ },
            )
        }

        // ── Assistant tab ─────────────────────────────────────────────────────

        composable(AppRoutes.ASSISTANT) {
            AssistantHomeScreen(
                onStartChat = { message ->
                    navController.navigate(AppRoutes.AI_CHAT)
                },
                onResumeConversation = { _ ->
                    navController.navigate(AppRoutes.AI_CHAT)
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoutes.AI_CHAT) {
            AIChatScreen(
                onBack = { navController.popBackStack() },
                onHumanSupport = { navController.navigate(AppRoutes.HUMAN_SUPPORT) },
            )
        }

        composable(AppRoutes.HUMAN_SUPPORT) {
            HumanSupportScreen(
                onBack = { navController.popBackStack() },
                onEmailSupport = { /* launch email intent */ },
            )
        }

        // ── Profile tab ───────────────────────────────────────────────────────

        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                onMenuItemTap = { item ->
                    when (item.route) {
                        AppRoutes.PERSONAL_INFO  -> navController.navigate(AppRoutes.PERSONAL_INFO)
                        AppRoutes.BANK_ACCOUNTS  -> navController.navigate(AppRoutes.BANK_ACCOUNTS)
                        AppRoutes.NOMINEES       -> navController.navigate(AppRoutes.NOMINEES)
                        AppRoutes.SECURITY       -> navController.navigate(AppRoutes.SECURITY)
                        AppRoutes.LANGUAGE_PREFS -> navController.navigate(AppRoutes.LANGUAGE_PREFS)
                        AppRoutes.HELP_CENTER    -> navController.navigate(AppRoutes.HELP_CENTER)
                        else -> { /* no-op for legal/about in this release */ }
                    }
                },
                onLogout = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoutes.PERSONAL_INFO) {
            PersonalInfoScreen(
                onBack = { navController.popBackStack() },
                onSave = { _, _, _ -> navController.popBackStack() },
            )
        }

        composable(AppRoutes.BANK_ACCOUNTS) {
            BankAccountsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoutes.SECURITY) {
            SecuritySettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoutes.HELP_CENTER) {
            HelpCentreScreen(
                onBack = { navController.popBackStack() },
                onAskAI = { navController.navigate(AppRoutes.AI_CHAT) },
                onHumanSupport = { navController.navigate(AppRoutes.HUMAN_SUPPORT) },
            )
        }

        composable(AppRoutes.LANGUAGE_PREFS) {
            PreferencesScreen(
                onBack = { navController.popBackStack() },
                onSave = { _ -> navController.popBackStack() },
            )
        }

        composable(AppRoutes.NOMINEES) {
            // Nominees screen — stub for this release (deferred implementation)
            androidx.compose.material3.Scaffold(
                topBar = {
                    androidx.compose.material3.TopAppBar(
                        title = { Text("Nominees", fontWeight = FontWeight.SemiBold) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                            }
                        },
                    )
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Nominees management coming soon.", color = Color(0xFF64748B))
                }
            }
        }
    }
}
