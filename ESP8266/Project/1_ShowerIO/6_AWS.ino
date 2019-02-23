//generate random mqtt clientID
char* generateClientID () {
  char* cID = new char[23]();
  for (int i = 0; i < 22; i += 1)
    cID[i] = (char)random(1, 256);
  return cID;
}

////callback to handle mqtt messages
//void messageArrived(MQTT::MessageData& md)
//{
//  MQTT::Message &message = md.message;
//
//  Serial.print("Message ");
//  Serial.print(++arrivedcount);
//  Serial.print(" arrived: qos ");
//  Serial.print(message.qos);
//  Serial.print(", retained ");
//  Serial.print(message.retained);
//  Serial.print(", dup ");
//  Serial.print(message.dup);
//  Serial.print(", packetid ");
//  Serial.println(message.id);
//  Serial.print("Payload ");
//  char* msg = new char[message.payloadlen+1]();
//  memcpy (msg,message.payload,message.payloadlen);
//  Serial.println(msg);
//  delete msg;
//}

//connects to websocket layer and mqtt layer
//bool connect () {
//
//
//
//    if (client.isConnected ()) {    
//        client.disconnect ();
//    }  
//    //delay is not necessary... it just help us to get a "trustful" heap space value
//    delay (1000);
//    Serial.print (millis ());
//    Serial.print (" - conn: ");
//    Serial.print (++connection);
//    Serial.print (" - (");
//    Serial.print (ESP.getFreeHeap ());
//    Serial.println (")");
//
//
//
//
//   int rc = ipstack.connect(aws_endpoint, port);
//    if (rc != 1)
//    {
//      Serial.println("error connection to the websocket server");
//      return false;
//    } else {
//      Serial.println("websocket layer connected");
//    }
//
//
//    Serial.println("MQTT connecting");
//    MQTTPacket_connectData data = MQTTPacket_connectData_initializer;
//    data.MQTTVersion = 4;
//    char* clientID = generateClientID ();
//    data.clientID.cstring = clientID;
//    rc = client.connect(data);
//    delete[] clientID;
//    if (rc != 0)
//    {
//      Serial.print("error connection to MQTT server");
//      Serial.println(rc);
//      return false;
//    }
//    Serial.println("MQTT connected");
//    return true;
//}

//subscribe to a mqtt topic
//void subscribe () {
//   //subscript to a topic
//    int rc = client.subscribe(aws_topic_times, MQTT::QOS0, messageArrived);
//    if (rc != 0) {
//      Serial.print("rc from MQTT subscribe is ");
//      Serial.println(rc);
//      return;
//    }
//    Serial.println("MQTT subscribed");
//}
//
//
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
  if (topicString.equals(aws_topic_times)) {
    messageLength = message.length();
    message.toCharArray(charList, messageLength);
    for ( int j = 0; j < messageLength + 1; j++) {
      if (substringTimes == 2) {
        waitTime = message.substring(lastPoint, j).toInt();
      }
      if (charList[j] == *"-") {
        if (lastPoint == 0) {
          bathTime =  message.substring(lastPoint, j).toInt();
          substringTimes = 1;
        } else if (substringTimes == 1) {
          stoppedTime =  message.substring(lastPoint, j).toInt();
          substringTimes = 2;
        }
        lastPoint = j + 1;
      }
    }
    setBathTime(bathTime);
    setWaitTime(waitTime);
    setStoppedTime(stoppedTime);
  }
  if (topicString.equals("configuration")) {
    WiFi.disconnect();
  }
  DBG_OUTPUT_PORT.println();
}

//connects to websocket layer and mqtt layer
bool connect () {



//  if (client.connected()) {
//    client.disconnect ();
//  }
  
  DBG_OUTPUT_PORT.print (millis ());
  DBG_OUTPUT_PORT.print (" - conn: ");
  DBG_OUTPUT_PORT.print (++connection);
  DBG_OUTPUT_PORT.print (" - (");
  DBG_OUTPUT_PORT.print (ESP.getFreeHeap ());
  DBG_OUTPUT_PORT.println (")");


  //creating random client id
  String clientIDString = (String)ESP.getChipId();
  char* clientID = strdup(clientIDString.c_str());

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
  client.subscribe(aws_statistics_topic);
  //subscript to a topic
DBG_OUTPUT_PORT.println(aws_topic_times);
DBG_OUTPUT_PORT.println(aws_topic_conf);
  
  DBG_OUTPUT_PORT.println("MQTT subscribed");
}


