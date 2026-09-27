package com.petmorph.ai.subscription

import com.petmorph.ai.settings.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Freemium gate. Currently backed by local settings so the app is fully usable.
 * To go live: implement [BillingDelegate] with Google Play Billing and call
 * [syncWithPlayStore] on launch to restore purchases.
 */
interface BillingDelegate {
    suspend fun hasProEntitlement(): Boolean
    suspend fun launchPurchaseFlow(activity: android.app.Activity)
}

class SubscriptionService(private val settings: SettingsManager) {
    val isPro: Flow<Boolean> = settings.isProFlow

    @Volatile var billingDelegate: BillingDelegate? = null

    suspend fun canCreateCompanion(currentCount: Int): Boolean =
        isPro.first() || currentCount < FREE_COMPANION_LIMIT

    suspend fun enableProForDebug() = settings.setPro(true)

    suspend fun syncWithPlayStore() {
        billingDelegate?.hasProEntitlement()?.let { settings.setPro(it) }
    }

    companion object {
        const val FREE_COMPANION_LIMIT = 1
    }
}
