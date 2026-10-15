package com.chitsmart.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom navigation destinations.
 *
 * Current release: Home | My Chits | Payments | Assistant | Profile
 *
 * EXCLUDED — Auctions tab is deferred. Do not add it here.
 * See docs/scope-decisions.md Decision 3.
 */
sealed class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    object Home : BottomNavDestination(
        route = "home",
        label = "Home",
        icon = Icons.Outlined.Home,
    )
    object MyChits : BottomNavDestination(
        route = "my_chits",
        label = "My Chits",
        icon = Icons.Outlined.AccountBalance,
    )
    object Payments : BottomNavDestination(
        route = "payments",
        label = "Payments",
        icon = Icons.Outlined.Payments,
    )
    object Assistant : BottomNavDestination(
        route = "assistant",
        label = "Assistant",
        icon = Icons.Outlined.SmartToy,
    )
    object Profile : BottomNavDestination(
        route = "profile",
        label = "Profile",
        icon = Icons.Outlined.Person,
    )
}

val bottomNavItems = listOf(
    BottomNavDestination.Home,
    BottomNavDestination.MyChits,
    BottomNavDestination.Payments,
    BottomNavDestination.Assistant,
    BottomNavDestination.Profile,
)

/**
 * All app routes — including nested routes beyond the bottom nav.
 */
object AppRoutes {
    // Auth (no bottom nav)
    const val SPLASH         = "splash"
    const val ONBOARDING     = "onboarding"
    const val LOGIN          = "login"
    const val OTP            = "otp/{mobileNumber}"
    const val REGISTRATION   = "registration"
    // KYC routes deferred — do not add here until KYC release

    // Home tab
    const val HOME           = "home"
    const val NOTIFICATIONS  = "notifications"

    // My Chits tab
    const val MY_CHITS           = "my_chits"
    const val BROWSE_GROUPS      = "browse_groups"
    const val GROUP_DETAILS      = "group_details/{groupId}"
    const val ENROLLMENT         = "enrollment/{groupId}"
    const val GROUP_MEMBER_VIEW  = "group_member_view/{membershipId}"

    // Payments tab
    const val PAYMENTS           = "payments"
    const val PAYMENT_SUMMARY    = "payment/summary/{installmentId}"
    const val PAYMENT_METHOD     = "payment/method/{installmentId}"
    const val PAYMENT_PROCESSING = "payment/processing/{paymentId}"
    const val PAYMENT_SUCCESS    = "payment/success/{paymentId}"
    const val PAYMENT_FAILURE    = "payment/failure/{paymentId}"
    const val PAYMENT_HISTORY    = "payment_history"
    const val RECEIPT            = "receipt/{paymentId}"
    const val PAYOUT_HISTORY     = "payout_history"
    // Auction routes deferred — do not add here until auctions release

    // Assistant tab
    const val ASSISTANT          = "assistant"
    const val AI_CHAT            = "ai_chat/{sessionId}"
    const val POLICY_REFERENCE   = "policy_reference/{policyId}"
    const val HUMAN_SUPPORT      = "human_support"

    // Profile tab
    const val PROFILE            = "profile"
    const val PERSONAL_INFO      = "personal_info"
    const val BANK_ACCOUNTS      = "bank_accounts"
    const val NOMINEES           = "nominees"
    const val SECURITY           = "security"
    const val HELP_CENTER        = "help_center"
    const val LANGUAGE_PREFS     = "language_prefs"
}
