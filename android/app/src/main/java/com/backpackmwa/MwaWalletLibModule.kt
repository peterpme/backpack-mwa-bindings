package com.backpackmwa

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import com.facebook.react.bridge.*
import com.solana.mobilewalletadapter.common.util.NotifyingCompletableFuture
import com.solana.mobilewalletadapter.walletlib.association.AssociationUri
import com.solana.mobilewalletadapter.walletlib.association.LocalAssociationUri
import com.solana.mobilewalletadapter.walletlib.authorization.AuthIssuerConfig
import com.solana.mobilewalletadapter.walletlib.protocol.MobileWalletAdapterConfig
import com.solana.mobilewalletadapter.walletlib.protocol.MobileWalletAdapterServer.SignedPayloadsResult
import com.solana.mobilewalletadapter.walletlib.scenario.*
import kotlinx.coroutines.Dispatchers

// by typing `val` we're holding onto a reference to reactContext
class MwaWalletLibModule(val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "MwaWalletLibModule"
    private var scenario: Scenario? = null
    var authRequest: AuthorizeRequest? = null
    var reAuthRequest: ReauthorizeRequest? = null
    var stRequest: SignTransactionsRequest? = null
    var spRequest: SignPayloadsRequest? = null
    var signAndSendTransactionsRequest: SignAndSendTransactionsRequest? = null

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
    fun authorizeDapp(publicKey: String, authorized: Boolean) {
        Log.d(TAG, "authorizeDapp: $publicKey:$authorized")

        if (authorized) {
            authRequest?.completeWithAuthorize(
                publicKey.toByteArray(),
                "Backpack",
                null,
                null
//                authRequest?.sourceVerificationState.authorizationScope.encodeToByteArray()
            )
        } else {
            authRequest?.completeWithDecline()
        }
    }

    // TODO check to see if the cluster string (passed into a AuthorizeRequest) is valid
    @ReactMethod
    fun authorizeDappSimulateClusterNotSupported() {
        authRequest?.completeWithClusterNotSupported()
    }

    // TODO see if this is needed and how it fires
    @ReactMethod
    fun authorizeDappSimulateInternalError() {
        authRequest?.completeWithInternalError(RuntimeException("Internal error during authorize: -1234"))
    }

    // TODO implement the whole thing
    @ReactMethod
    fun signPayloadsSimulateSign(publicKey: String) {
        check(publicKey != null) { "Unknown public key for signing request" }
    }

    @ReactMethod
    fun signPayloadsDeclined() {
        authRequest?.completeWithDecline()
    }

    @ReactMethod
    fun signPayloadsSimulateAuthTokenInvalid() {
        spRequest?.completeWithAuthorizationNotValid()
    }

    @ReactMethod
    fun signPayloadsSimulateInvalidPayloads() {
        val payloads = spRequest?.payloads ?: return
        val valid = BooleanArray(payloads.size) { i -> i != 0 }
        spRequest?.completeWithInvalidPayloads(valid)
    }

    @ReactMethod
    fun signPayloadsSimulateInternalError() {
        spRequest?.completeWithInternalError(RuntimeException("Internal error during signing: -1234"))
    }

    // TODO handle signing transaction in javascript
    @ReactMethod
    fun signAndSendTransactionsSimulateSign() {
        // TODO lines 209 in MobileWalletAdapterViewModel
    }

    @ReactMethod
    fun signAndSendTransactionsDeclined() {
        signAndSendTransactionsRequest?.completeWithDecline()
    }

    @ReactMethod
    fun signAndSendTransactionsSimulateAuthTokenInvalid() {
        signAndSendTransactionsRequest?.completeWithAuthorizationNotValid()
    }

    @ReactMethod
    fun signAndSendTransactionsSimulateInvalidPayloads() {
        val payloads = signAndSendTransactionsRequest?.payloads ?: return
        val valid = BooleanArray(payloads.size) { i -> i != 0};
        signAndSendTransactionsRequest?.completeWithInvalidSignatures(valid)
    }

    @ReactMethod
    fun signAndSendTransactionsSubmitted() {
        Log.d(TAG, "Simulating transactions submitted on cluster") // TODO request.cluster
//        signAndSendTransactionsRequest?.completeWithSignatures(signAndSendTransactionsRequest?.signatures)
    }


    @ReactMethod
    fun signAndSendTransactionsNotSubmitted() {
        // TODO MobileWalletAdapterViewModel.signAndSendTransactionsNotSubmitted
    }

    @ReactMethod
    fun signAndSendTransactionsSend() {
        // TODO MobileWalletAdapterViewModel.signAndSendTransactionsSend
    }

    @ReactMethod
    fun signAndSendTransactionsSimulateTooManyPayloads() {
        signAndSendTransactionsRequest?.completeWithTooManyPayloads()
    }

    @ReactMethod
    fun signAndSendTransactionsSimulateInternalError() {
        signAndSendTransactionsRequest?.completeWithInternalError(RuntimeException("Internal error during sign_and_send_transactions: -1234"))
    }

    @ReactMethod
    fun reauthorizeDapp() {
        Log.d(TAG, "reauthorizeDap");
        reAuthRequest?.completeWithReauthorize()
    }

    @ReactMethod
    fun createScenario(
        walletName: String, // our wallet's name (Backpack)
        uriStr: String,
        callback: Callback
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
//                callback.invoke("SCENARIO_READY")
            }

            // done
            override fun onScenarioServingClients() {
                Log.d(TAG, "onScenarioServingClients")
//                callback.invoke("SCENARIO_SERVING_CLIENTS")
            }

            // done (TBD on scenario?.close)
            override fun onScenarioServingComplete() {
                Log.d(TAG, "onScenarioServingComplete")
                scenario?.close(); // returns (effectively) immediately, it doesn't take very long to run
//                callback.invoke("SERVING_COMPLETE")
            }

            // done
            override fun onScenarioComplete() {
                Log.d(TAG, "onScenarioComplete")
//                callback.invoke("SCENARIO_COMPLETE")
            }

            // done
            override fun onScenarioError() {
                Log.e(TAG, "onScenarioError")
//                callback.invoke("SCENARIO_ERROR")
            }

            // done (slightly different on native side but that's bc of viewModelScope)
            override fun onScenarioTeardownComplete() {
                Log.d(TAG, "onScenarioTeardownComplete")

//                callback.invoke("SCENARIO_TEARDOWN_COMPLETE")
            }


            override fun onAuthorizeRequest(request: AuthorizeRequest) {
                Log.d(TAG, "onAuthorizeRequest")
                authRequest = request
                callback.invoke("AUTHORIZE_REQUEST")
            }

            override fun onReauthorizeRequest(request: ReauthorizeRequest) {
                Log.d(TAG, "onReauthorizeRequest")
//                callback.invoke("RE_AUTHORIZE_REQUEST", request)
            }

            override fun onSignTransactionsRequest(request: SignTransactionsRequest) {
                Log.d(TAG, "onSignTransactionsRequest")
//                callback.invoke("SIGN_TRANSACTION_REQUEST", request)
            }

            override fun onSignMessagesRequest(request: SignMessagesRequest) {
                Log.d(TAG, "onSignMessagesRequest")
//                callback.invoke("SIGN_MESSAGE_REQUEST", request)
            }

            override fun onSignAndSendTransactionsRequest(request: SignAndSendTransactionsRequest) {
                Log.d(TAG, "onSignAndSendTransactionsRequest")
//                callback.invoke("SIGN_AND_SEND_TRANSACTION_REQUEST", request)
            }

            override fun onDeauthorizedEvent(event: DeauthorizedEvent) {
                Log.d(TAG, "onDeauthorizedEvent")
//                callback.invoke("DE_AUTHORIZE_EVENT", event)
            }
        }
    }
}