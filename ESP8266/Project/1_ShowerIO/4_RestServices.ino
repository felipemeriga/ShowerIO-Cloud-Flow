//REST FUNCTIONS .INO FILE


void check() { 
  
  DBG_OUTPUT_PORT.println("Accessed the checkpoint");
  DBG_OUTPUT_PORT.println("The ESP8266 server was discovered by an app");

  String root;
  String localNetworkIp = WiFi.localIP().toString();
  String localNetworkSubnet = WiFi.localIP().toString();
  String microprocessorId = (String)ESP.getChipId();
  String deviceStatus = "ONLINE";
  String iotCoreEndPoint = "agq6mvwjsctpy-ats.iot.us-east-1.amazonaws.com";
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

  DBG_OUTPUT_PORT.println("Set Shower Selected Duration Timer to: " + (String)bathTime);

  EEPROM.write(address_time, bathTime);
  EEPROM.commit();
  bathTime = EEPROM.read(address_time);
}

void setWaitTime(int waitTime) {
  DBG_OUTPUT_PORT.println();
  DBG_OUTPUT_PORT.println("Set Selected Actual Off Bath time:" + (String)waitTime);
  EEPROM.write(address_wait, waitTime);
  EEPROM.commit();
  bathWaitTime = EEPROM.read(address_wait);
}

void setStoppedTime(int stoppedTime) {
  DBG_OUTPUT_PORT.println("Set Selected Actual Paused Bath time: " + (String)stoppedTime);
  EEPROM.write(address_stopped, stoppedTime);
  EEPROM.commit();
  bathStoppedTime = EEPROM.read(address_stopped);
}
