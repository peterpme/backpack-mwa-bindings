import { StatusBar } from "expo-status-bar";
import React, { useEffect, useState } from "react";
import {
  StyleSheet,
  Text,
  View,
  NativeModules,
  Button,
  Linking,
  NativeEventEmitter,
  Platform,
  BackHandler,
} from "react-native";

const publicKey = "11111111111111111111111111111111";

export default function App() {
  const [event, setEvent] = React.useState(null);
  // fires if app is open
  // solana-wallet://xyz-whatever-comes
  React.useEffect(() => {
    Linking.addEventListener("url", (event) => {
      const { url } = event;
      if (url && url.includes("solana-wallet")) {
        initiateWalletScenario(url);
      }
    });

    return () => {
      Linking.removeAllListeners("url");
    };
  }, []);

  // fires if app is closed
  React.useEffect(() => {
    async function f() {
      const url = await Linking.getInitialURL();
      if (url && url.includes("solana-wallet")) {
        initiateWalletScenario(url);
      }
    }

    f();
  }, []);

  React.useEffect(() => {
    const eventEmitter = new NativeEventEmitter(
      NativeModules.MwaWalletLibModule
    );

    eventEmitter.addListener("MWA_EVENT", handleNativeEvent);

    return () => {
      // eventEmitter.removeListeners();
    };
  }, []);

  function initiateWalletScenario(intent /* string */) {
    NativeModules.MwaWalletLibModule.createScenario(
      "Backpack", // wallet name
      intent,
      (event, errorMsg) => {
        switch (event) {
          case "ERROR":
            console.error("ERROR", errorMsg);
          default:
            console.log("SUCCESS");
        }
      }
    );
  }

  function handleNativeEvent(event) {
    setEvent(event.type);
    switch (event.type) {
      case "ON_SCENARIO_READY":
        console.log("SCENARIO_READY");
        break;
      case "SCENARIO_COMPLETE":
        console.log("SCENARIO_COMPLETE");
        break;
      case "SCENARIO_ERROR":
        console.log("SCENARIO_ERROR");
        break;
      case "SCENARIO_TEARDOWN_COMPLETE":
        console.log("SCENARIO_TEARDOWN_COMPLETE");
        break;
      case "AUTHORIZE_REQUEST":
        console.log("AUTHORIZE_REQUEST");
        break;
      case "RE_AUTHORIZE_REQUEST":
        console.log("RE_AUTHORIZE_REQUEST");
        break;
      case "SIGN_TRANSACTION_REQUEST":
        console.log("SIGN_TRANSACTION_REQUEST");
        break;
      case "SIGN_MESSAGE_REQUEST":
        console.log("SIGN_MESSAGE_REQUEST");
        break;
      case "SIGN_AND_SEND_TRANSACTION_REQUEST":
        console.log("SIGN_AND_SEND_TRANSACTION_REQUEST");
        break;
      case "DE_AUTHORIZE_EVENT":
        console.log("DE_AUTHORIZE_EVENT");
        break;
      case "SCENARIO_SERVING_CLIENTS":
        console.log("SCENARIO_SERVING_CLIENTS");
        break;
      default:
        console.log("UNKNOWN_EVENT", event);
    }
  }

  function ReAuthorizeView() {
    // Shouldn't need to show any buttons here or anything, but doing it for the sake of visually confirming differences
    try {
      NativeModules.MwaWalletLibModule.reauthorizeDapp();
    } catch (err) {
      console.error("authorized:err", err);
    }

    return (
      <View style={styles.modal}>
        <Button title="Re-Authorize" onPress={() => handlePress(true)} />
      </View>
    );
  }

  function AuthorizeView() {
    function handlePress(authorized) {
      try {
        NativeModules.MwaWalletLibModule.authorizeDapp(publicKey, authorized);
      } catch (err) {
        console.error("authorized:err", err);
      }
    }

    return (
      <View style={styles.modal}>
        <Button title="Authorize" onPress={() => handlePress(true)} />
        <Button title="Deauthorize" onPress={() => handlePress(false)} />
      </View>
    );
  }

  function CloseApp() {
    React.useEffect(() => {
      if (Platform.OS === "android") {
        BackHandler.exitApp(); // closes the view and returns to the app
      }
    }, []);
  }

  function SignTransactionView() {
    function handlePress(authorized) {
      try {
        NativeModules.MwaWalletLibModule.authorizeDapp(publicKey, authorized);
      } catch (err) {
        console.error("authorized:err", err);
      }
    }

    return (
      <View style={styles.modal}>
        <Button title="Authorize" onPress={() => handlePress(true)} />
        <Button title="Deauthorize" onPress={() => handlePress(false)} />
      </View>
    );
  }

  function SignMessageView() {
    function handlePress(authorized) {
      try {
        NativeModules.MwaWalletLibModule.authorizeDapp(publicKey, authorized);
      } catch (err) {
        console.error("authorized:err", err);
      }
    }

    return (
      <View style={[styles.modal, { backgroundColor: "green" }]}>
        <Button title="Authorize" onPress={() => handlePress(true)} />
        <Button title="Deauthorize" onPress={() => handlePress(false)} />
      </View>
    );
  }

  function renderViewForEvent(event) {
    switch (event) {
      case "AUTHORIZE_REQUEST":
        return <AuthorizeView />;
      case "RE_AUTHORIZE_REQUEST":
        return <ReAuthorizeView />;
      case "SIGN_TRANSACTION_REQUEST":
        return <SignTransactionView />;
      case "SIGN_MESSAGE_REQUEST":
        return <SignMessageView />;
      case "SCENARIO_TEARDOWN_COMPLETE":
        return <CloseApp />;
      default:
        if (event == null) {
          return (
            <View
              style={{
                flex: 1,
                height: 400,
                justifyContent: "center",
                backgroundColor: "yellow",
              }}
            >
              <Text>Hi</Text>
            </View>
          );
        } else {
          return (
            <View
              style={{
                flex: 1,
                justifyContent: "center",
                backgroundColor: "red",
              }}
            />
          );
        }
    }
  }

  return (
    <View style={styles.container}>
      <StatusBar style="auto" />
      {renderViewForEvent(event)}
    </View>
  );
}

const styles = StyleSheet.create({
  modal: {
    height: 100,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  container: {
    height: 100,
    justifyContent: "center",
  },
});
