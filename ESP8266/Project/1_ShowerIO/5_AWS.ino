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
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    message = message + (char)payload[i];
  }
  Serial.println();
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
    Serial.println();
    Serial.print(bathTime);
    Serial.println();
    Serial.print(waitTime);
    Serial.println();
    Serial.print(stoppedTime);
  }
  Serial.println();
}

//connects to websocket layer and mqtt layer
bool connect () {



  if (client.connected()) {
    client.disconnect ();
  }
  //delay is not necessary... it just help us to get a "trustful" heap space value
  delay (1000);
  Serial.print (millis ());
  Serial.print (" - conn: ");
  Serial.print (++connection);
  Serial.print (" - (");
  Serial.print (ESP.getFreeHeap ());
  Serial.println (")");


  //creating random client id
  char* clientID = generateClientID ();

  client.setServer(aws_endpoint, port);
  if (client.connect(clientID)) {
    Serial.println("connected");
    return true;
  } else {
    Serial.print("failed, rc=");
    Serial.print(client.state());
    return false;
  }

}


//subscribe to a mqtt topic
void subscribe () {
  client.setCallback(callback);
  client.subscribe(aws_topic_times);
  client.subscribe(aws_topic_conf);
  //subscript to a topic
  Serial.println("MQTT subscribed");
}


