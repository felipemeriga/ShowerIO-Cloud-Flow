//generate random mqtt clientID
char* generateClientID () {
  char* cID = new char[23]();
  for (int i = 0; i < 22; i += 1)
    cID[i] = (char)random(1, 256);
  return cID;
}


//callback to handle mqtt messages
void callback(char* topic, byte* payload, unsigned int length) {
  int lastPoint = 0;
  int substringTimes = 0;
  int bathTime;
  int waitTime;
  int stoppedTime;
  String message;
  char charList[20];
  String topicString = (String) topic;
  int messageLength;
  DBG_OUTPUT_PORT.print("Message arrived [");
  DBG_OUTPUT_PORT.print(topic);
  DBG_OUTPUT_PORT.print("] ");
  for (int i = 0; i < length; i++) {
    DBG_OUTPUT_PORT.print((char)payload[i]);
    message = message + (char)payload[i];
  }
  DBG_OUTPUT_PORT.println();
  if (topicString.equals("times")) {
    messageLength = message.length();
    message.toCharArray(charList, messageLength);
    for ( int j = 0; j < messageLength + 1; j++) {
      if (substringTimes == 2) {
        stoppedTime = message.substring(lastPoint, j).toInt();
      }
      if (charList[j] == *"-") {
        if (lastPoint == 0) {
          bathTime =  message.substring(lastPoint, j).toInt();
          substringTimes = 1;
        } else if (substringTimes == 1) {
          waitTime =  message.substring(lastPoint, j).toInt();
          substringTimes = 2;
        }
        lastPoint = j + 1;
      }
    }
    setBathTime(bathTime);
    setWaitTime(waitTime);
    setStoppedTime(stoppedTime);
    DBG_OUTPUT_PORT.println();
    DBG_OUTPUT_PORT.print(bathTime);
    DBG_OUTPUT_PORT.println();
    DBG_OUTPUT_PORT.print(waitTime);
    DBG_OUTPUT_PORT.println();
    DBG_OUTPUT_PORT.print(stoppedTime);
  }
  if (topicString.equals("configuration")) {
    WiFi.disconnect();
  }
  DBG_OUTPUT_PORT.println();
}

//connects to websocket layer and mqtt layer
bool connect () {



  if (client.connected()) {
    client.disconnect ();
  }
  //delay is not necessary... it just help us to get a "trustful" heap space value
  delay (1000);
  DBG_OUTPUT_PORT.print (millis ());
  DBG_OUTPUT_PORT.print (" - conn: ");
  DBG_OUTPUT_PORT.print (++connection);
  DBG_OUTPUT_PORT.print (" - (");
  DBG_OUTPUT_PORT.print (ESP.getFreeHeap ());
  DBG_OUTPUT_PORT.println (")");


  //creating random client id
  char* clientID = generateClientID ();

  client.setServer(aws_endpoint, port);
  if (client.connect(clientID)) {
    DBG_OUTPUT_PORT.println("connected");
    return true;
  } else {
    DBG_OUTPUT_PORT.print("failed, rc=");
    DBG_OUTPUT_PORT.print(client.state());
    return false;
  }

}


//subscribe to a mqtt topic
void subscribe () {
  client.setCallback(callback);
  client.subscribe(aws_topic_times);
  client.subscribe(aws_topic_conf);
  //subscript to a topic
  DBG_OUTPUT_PORT.println("MQTT subscribed");
}


