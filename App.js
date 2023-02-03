import { StatusBar } from "expo-status-bar";
import React, { useEffect } from "react";
import { StyleSheet, Text, View, NativeModules, Button } from "react-native";

export default function App() {
  return (
    <View style={styles.container}>
      <Text>Open up App.js to start working on your app!</Text>
      <StatusBar style="auto" />
      <Button
        title="Test"
        onPress={() => NativeModules.MwaWalletLibModule.tryTest2("Friend")}
      />
      <Button
        title="Create Scenario"
        onPress={() => {
          NativeModules.MwaWalletLibModule.createScenario(
            "Backpack", // this'll always be Backpack
            "solana-wallet:/v1/associate/local?association=BC0IWdfk8xzpyI2hum5sLWQeBOjdmp4sa7JBviKeYb4OF5w464L5ZHjk3OYhDTOfzkSlaPOJta9FjoGhsRgIFfc&port=49319", // this'll be handled by the IntentFilter and Deep Link and/or App Link
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
                  console.log("AUTHORIZE_REQUEST", data);
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
        }}
      />
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
