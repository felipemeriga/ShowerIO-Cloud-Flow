//REST FUNCTIONS .INO FILE
#include <ArduinoJson.h>


void check() {
  
  DBG_OUTPUT_PORT.println("Accessed the checkpoint");
  DBG_OUTPUT_PORT.println("The ESP8266 server was discovered by an app");

  String root;
  String localNetworkIp = WiFi.localIP().toString();
  String localNetworkSubnet = WiFi.localIP().toString();
  String microprocessorId = (String)ESP.getChipId();
  String deviceStatus = "ONLINE";
  String iotCoreEndPoint = "agq6mvwjsctpy-ats.iot.us-east-2.amazonaws.com";
  String iotCoreARN = "AKIAJLVWZBXXZ5NVCRWA";

  root = "{\"userId\": \"\"," ;
  root = root + "\"name\": \"UNAMED\"," ;
  root = root + "\"iotCoreARN\": \"" + iotCoreARN + "\"," ;
  root = root + "\"iotCoreEndPoint\": \"" + iotCoreEndPoint + "\"," ;
  root = root + "\"localNetworkIp\": \"" + localNetworkIp + "\"," ;
  root = root + "\"localNetworkSubnet\": \"" + localNetworkSubnet + "\"," ;
  root = root + "\"microprocessorId\": \"" + microprocessorId + "\"," ;
  root = root + "\"status\": \"" + deviceStatus + "\"}" ;
  server.send(200, "application/json", root );

}

void setBathTime(int bathTime) {

  DBG_OUTPUT_PORT.println("Set Shower Selected Duration Timer to: " + bathTime);

  EEPROM.write(address_tempo, bathTime);
  EEPROM.commit();
}

void setWaitTime(int waitTime) {
  DBG_OUTPUT_PORT.println();
  DBG_OUTPUT_PORT.println("Set Selected Actual Off Bath time:" + waitTime);
  EEPROM.write(address_espera, waitTime);
  EEPROM.commit();
}

void setStoppedTime(int stoppedTime) {
  DBG_OUTPUT_PORT.println("Set Selected Actual Paused Bath time: " + stoppedTime);
  EEPROM.write(address_pausa, stoppedTime);
  EEPROM.commit();
}


