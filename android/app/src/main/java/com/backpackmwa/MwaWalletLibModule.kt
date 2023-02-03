package com.backpackmwa

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import com.facebook.react.bridge.*
import com.solana.mobilewalletadapter.walletlib.association.AssociationUri
import com.solana.mobilewalletadapter.walletlib.association.LocalAssociationUri
import com.solana.mobilewalletadapter.walletlib.authorization.AuthIssuerConfig
import com.solana.mobilewalletadapter.walletlib.protocol.MobileWalletAdapterConfig
import com.solana.mobilewalletadapter.walletlib.scenario.*

// by typing `val` we're holding onto a reference to reactContext
class MwaWalletLibModule(val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "MwaWalletLibModule"
    private var scenario: Scenario? = null

    companion object {
        private val TAG = MwaWalletLibModule::class.simpleName
    }

    @ReactMethod
    fun tryTest(name: String) {
        Log.d(TAG, "Testing: $name")
    }

    @ReactMethod
    fun tryTest2(name: String) {
        Log.d(TAG, "Testing2: $name")
    }

    @ReactMethod
    fun createScenario(
        walletName: String, // our wallet's name (Backpack)
        uriStr: String,
        callback: Callback
//        promise: Promise
    ) {
        // Not production code! Testing purposes only START
        scenario?.let {
            it.close()
        }

        scenario = null
        // Not production code! Testing purposes only END

        val uri = Uri.parse(uriStr)

        val associationUri = AssociationUri.parse(uri)
        if (associationUri == null) {
//            promise.reject("Unsupported association URI") // update to new version
            return
        } else if (associationUri !is LocalAssociationUri) {
//            promise.reject("Current implementation of fakewallet does not support remote clients")
            return
        }

        val callbacks = createMobileWalletAdapterScenarioCallbacks(callback)

        // created a scenario, told it to start (kicks off some threads in the background)
        // we've kept a reference to it in the global state of this module (scenario)
        // this won't be garbage collected and will just run, sit & wait for an incoming connection
        scenario = associationUri.createScenario(
            reactContext,
            MobileWalletAdapterConfig(
                true,
                10,
                10,
                arrayOf(MobileWalletAdapterConfig.LEGACY_TRANSACTION_VERSION, 0)
            ),
            AuthIssuerConfig(walletName),
            callbacks
        ).also { it.start() }

//        promise.resolve(true)
    }

    private fun createMobileWalletAdapterScenarioCallbacks(
        callback: Callback
    ): Scenario.Callbacks {
        return object : Scenario.Callbacks {
            // done
            override fun onScenarioReady() {
                Log.d(TAG, "onScenarioReady")
                callback.invoke("SCENARIO_READY")
            }

            // done
            override fun onScenarioServingClients() {
                callback.invoke("SCENARIO_SERVING_CLIENTS")
//               TODO("Not yet implemented")
            }

            // done (TBD on scenario?.close)
            override fun onScenarioServingComplete() {
                scenario?.close(); // returns (effectively) immediately, it doesn't take very long to run
                callback.invoke("SERVING_COMPLETE")
            }

            // done
            override fun onScenarioComplete() {
                callback.invoke("SCENARIO_COMPLETE")
            }

            // done
            override fun onScenarioError() {
                callback.invoke("SCENARIO_ERROR")
            }

            // done (slightly different on native side but that's bc of viewModelScope)
            override fun onScenarioTeardownComplete() {
                callback.invoke("SCENARIO_TEARDOWN_COMPLETE")
            }

            override fun onAuthorizeRequest(request: AuthorizeRequest) {
                callback.invoke("AUTHORIZE_REQUEST", request)
            }

            override fun onReauthorizeRequest(request: ReauthorizeRequest) {
                callback.invoke("RE_AUTHORIZE_REQUEST", request)
            }

            override fun onSignTransactionsRequest(request: SignTransactionsRequest) {
                callback.invoke("SIGN_TRANSACTION_REQUEST", request)
            }

            override fun onSignMessagesRequest(request: SignMessagesRequest) {
                callback.invoke("SIGN_MESSAGE_REQUEST", request)
            }

            override fun onSignAndSendTransactionsRequest(request: SignAndSendTransactionsRequest) {
                callback.invoke("SIGN_AND_SEND_TRANSACTION_REQUEST", request)
            }

            override fun onDeauthorizedEvent(event: DeauthorizedEvent) {
                callback.invoke("DE_AUTHORIZE_EVENT", event)
            }

        }
    }
}