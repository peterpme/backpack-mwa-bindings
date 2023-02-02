package com.backpackmwa
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.util.Log

class MwaWalletLibModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "MwaWalletLibModule"

    @ReactMethod fun test(name: String) {
        Log.d("MwaWalletLibModule", "Testing: $name");
    }
}