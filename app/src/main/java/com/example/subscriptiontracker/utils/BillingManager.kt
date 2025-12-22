package com.example.subscriptiontracker.utils

import android.app.Activity
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    private val context: Context,
    private val onPurchaseSuccess: (String) -> Unit
) : DefaultLifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {
    
    private var billingClient: BillingClient? = null
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    // Product IDs
    companion object {
        const val PRODUCT_MONTHLY = "premium_monthly"
        const val PRODUCT_3MONTHS = "premium_3months"
        const val PRODUCT_YEARLY = "premium_yearly"
        const val PRODUCT_LIFETIME = "premium_lifetime"
        
        val SUBSCRIPTION_PRODUCTS = listOf(
            PRODUCT_MONTHLY,
            PRODUCT_3MONTHS,
            PRODUCT_YEARLY
        )
        
        val INAPP_PRODUCTS = listOf(
            PRODUCT_LIFETIME
        )
    }
    
    init {
        initializeBillingClient()
    }
    
    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(this)
    }
    
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _isReady.value = true
        }
    }
    
    override fun onBillingServiceDisconnected() {
        _isReady.value = false
    }
    
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }
    
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            onPurchaseSuccess(purchase.products.firstOrNull() ?: "")
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            // Purchase acknowledged
        }
    }
    
    fun launchBillingFlow(activity: Activity, productId: String, productType: String) {
        if (!_isReady.value) return
        
        if (productType == "SUBS") {
            // Subscription
            billingClient?.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(productId)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                        )
                    )
                    .build()
            ) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && 
                    productDetailsList.isNotEmpty()) {
                    val productDetails = productDetailsList.first()
                    val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
                    
                    if (offerDetails != null) {
                        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerDetails.offerToken)
                            .build()
                        
                        val billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(listOf(productDetailsParams))
                            .build()
                        
                        billingClient?.launchBillingFlow(activity, billingFlowParams)
                    }
                }
            }
        } else if (productType == "INAPP") {
            // One-time purchase
            billingClient?.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(productId)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                        )
                    )
                    .build()
            ) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && 
                    productDetailsList.isNotEmpty()) {
                    val productDetails = productDetailsList.first()
                    
                    val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                    
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(listOf(productDetailsParams))
                        .build()
                    
                    billingClient?.launchBillingFlow(activity, billingFlowParams)
                }
            }
        }
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        billingClient?.endConnection()
    }
}

