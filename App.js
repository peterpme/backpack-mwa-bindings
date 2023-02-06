import { StatusBar } from "expo-status-bar";
import React, { useEffect, useState } from "react";
import {
  StyleSheet,
  Text,
  View,
  NativeModules,
  Button,
  Linking,
} from "react-native";

console.log("authorize?", NativeModules.MwaWalletLibModule.authorizeDapp);

export default function App() {
  React.useEffect(() => {
    // fires if app is open
    Linking.addEventListener("url", (event) => {
      console.log("1addEventListener:event", event);
      console.log("1addEventListener:url", event.url);
      if (url && url.includes("solana-wallet")) {
        initiateWalletScenario(url);
      }
    });

    return () => {
      Linking.removeAllListeners("url");
    };
  }, []);

  React.useEffect(() => {
    async function f() {
      // fires if app is closed
      const url = await Linking.getInitialURL();
      console.log("2getInitialURL:url", url);
      if (url && url.includes("solana-wallet")) {
        initiateWalletScenario(url);
      }
    }

    f();
  }, []);

  function initiateWalletScenario(intent /* string */) {
    NativeModules.MwaWalletLibModule.createScenario(
      "Backpack", // this'll always be Backpack
      intent,
      (event, data) => {
        switch (event) {
          case "SCENARIO_READY":
            console.log("SCENARIO_READY");
            break;
          case "SCENARIO_COMPLETE":
            console.log("SCENARIO_COMPLETE", data);
            break;
          case "SCENARIO_ERROR":
            console.log("SCENARIO_ERROR", data);
            break;
          case "SCENARIO_TEARDOWN_COMPLETE":
            console.log("SCENARIO_TEARDOWN_COMPLETE", data);
            break;
          case "AUTHORIZE_REQUEST":
            // show the bottom sheet modal
            console.log("AUTHORIZE_REQUEST");
            break;
          case "RE_AUTHORIZE_REQUEST":
            console.log("RE_AUTHORIZE_REQUEST", data);
            break;
          case "SIGN_TRANSACTION_REQUEST":
            console.log("SIGN_TRANSACTION_REQUEST", data);
            break;
          case "SIGN_MESSAGE_REQUEST":
            console.log("SIGN_MESSAGE_REQUEST", data);
            break;
          case "SIGN_AND_SEND_TRANSACTION_REQUEST":
            console.log("SIGN_AND_SEND_TRANSACTION_REQUEST", data);
            break;
          case "DE_AUTHORIZE_EVENT":
            console.log("DE_AUTHORIZE_EVENT", data);
            break;
          default:
            console.log("error");
        }
      }
    );
  }

  // console.log("authorizeRequest", authorizeRequest);

  function authorize(authorized) {
    console.log("authorized", authorized);
    try {
      NativeModules.MwaWalletLibModule.authorizeDapp("xyz", authorized);
    } catch (err) {
      console.error("authorized:err", err);
    }
  }

  return (
    <View style={styles.container}>
      <Text>Open up App.js to start working on your app!</Text>
      <StatusBar style="auto" />
      <Button
        title="Test"
        onPress={() => NativeModules.MwaWalletLibModule.tryTest2("Friend")}
      />
      <Button title="Authorize Request" onPress={() => authorize(true)} />
      <Button title="Decline Request" onPress={() => authorize(false)} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
  },
});
