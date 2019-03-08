//send a message to a mqtt topic
//void sendmessage (String status) {
//  String message;
//  String microprocessorId = (String)ESP.getChipId();
//  //send a message
//  char buf[100];
//
//  message = "{\"microprocessorId\": \"" + microprocessorId + "\", " ;
//  message = message + "\"status\": \"" + status + "\"}" ;
//
//
//  strcpy(buf, message.c_str());
//  int rc = client.publish(aws_status_topic, buf);
//}


boolean updateBathStatistics(int bathDuration, int liters) {
  String message;
  String microprocessorId = (String)ESP.getChipId();
  char buf[100];
  DBG_OUTPUT_PORT.println("Updating statistics after the bath is concluded!");

  message = "{\"microprocessorId\": \"" + microprocessorId + "\", " ;
  message = message + "\"liters\": \"" + liters + "\", " ;
  message = message + "\"bathDuration\": \"" + bathDuration + "\"}" ;
  strcpy(buf, message.c_str());
  int rc = client.publish(aws_statistics_topic, buf);

}



//boolean updateDeviceStatus(String status) {
//
//  StaticJsonBuffer<300> JSONbuffer;
//  JsonObject& JSONencoder = JSONbuffer.createObject();
//
//  String requestBody;
//  String microprocessorId = (String)ESP.getChipId();
//  JSONencoder["microprocessorId"] = microprocessorId;
//  JSONencoder["status"] = status;
//
//  char JSONmessageBuffer[300];
//  JSONencoder.prettyPrintTo(JSONmessageBuffer, sizeof(JSONmessageBuffer));
//  Serial.println(JSONmessageBuffer);
//
//  http.begin("https://ocq98geoph.execute-api.us-east-1.amazonaws.com/tst");
//  http.addHeader("Content-Type", "application/json");
//
//  int httpResponseCode = http.PUT(JSONmessageBuffer);
//
//  if (httpResponseCode > 0) {
//
//    String response = http.getString();
//
//    Serial.println(httpResponseCode);
//    http.end();
//    return true;
//
//  } else {
//
//    Serial.print("Error on sending PUT Request: ");
//    Serial.println(httpResponseCode);
//    http.end();
//    return false;
//  }
//
//
//}
//
void verifyConnection(MillisTimer &mt) {
 /* boolean updateStatusResult;
  String status;

  if (WiFi.status() != WL_CONNECTED) {
    DBG_OUTPUT_PORT.println("Disconnected, restarting ESP!");
    status = "OFFLINE";
  } else {
    DBG_OUTPUT_PORT.println("ESP8266 is still connected");
    status = "ONLINE";
  }

  sendmessage("AOOO");
  if (updateStatusResult) {
    DBG_OUTPUT_PORT.println("Status updated!");
  } else {
    DBG_OUTPUT_PORT.println("Error updating the device status!");
  }

  if (status.equals("OFFLINE")) {
    while (1)ESP.restart();
    delay(500);
  }*/

}



