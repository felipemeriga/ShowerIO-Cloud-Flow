//Main Function .INO FILE

void configureServer() {

  server.on ( "/check", check);
  server.begin();
  DBG_OUTPUT_PORT.println("HTTP server started");
}

void configureGPIO(void) {

  pinMode(rele, OUTPUT);
  pinMode(Led_Aviso, OUTPUT);

  digitalWrite(rele, LOW);
  digitalWrite(Led_Aviso, LOW);

  //EEPROM Start Function
  EEPROM.begin(512);

  armazenado = EEPROM.read(address_tempo);
  if (armazenado > 45 || armazenado < 1 ) {
    minutos = 7;
    EEPROM.write(address_tempo, minutos);
    EEPROM.commit();
  }
  else {
    minutos = armazenado;
  }

  armazenado = EEPROM.read(address_espera);
  if (armazenado > 45 || armazenado < 1 ) {
    minutos_espera = 5;
    EEPROM.write(address_espera, minutos_espera);
    EEPROM.commit();
  }
  else {
    minutos_espera = armazenado;
  }

  armazenado = EEPROM.read(address_pausa);
  if (armazenado > 45 || armazenado < 1 ) {
    minutos_pausa = 3;
    EEPROM.write(address_pausa, minutos_pausa);
    EEPROM.commit();
  }
  else {
    minutos_pausa = armazenado;
  }
}

void setup(void) {

  //WiFi.disconnect();

  // deplay for 2 sec for smartConfig
  Serial.println("2 sec before clear SmartConfig");
  delay(2000);

  DBG_OUTPUT_PORT.begin(115200);
  DBG_OUTPUT_PORT.print("\n");
  DBG_OUTPUT_PORT.setDebugOutput(true);
  configureGPIO();
  configureServer();
  initBathConfiguration();
  int cnt = 0;

  // set for STA mode
  bool timeout = true;
  WiFi.mode(WIFI_STA);
  if (WiFi.SSID() != NULL) {
    DBG_OUTPUT_PORT.println("Getting last saved Wifi");
    WiFi.begin(WiFi.SSID().c_str(), WiFi.psk().c_str());
    unsigned long start = millis();
    while (millis() - start < 30000) {
      if (WiFi.status() == WL_CONNECTED) {
        timeout = false;
        break;
      }
      delay(500);
    }
  }
  if (timeout) {
    timeout = true;
    DBG_OUTPUT_PORT.println("Begin smart config");
    WiFi.beginSmartConfig();
    unsigned long start = millis();
    unsigned long waitTime;
    if (WiFi.SSID() != NULL) {
      waitTime = 30000;
    } else {
      waitTime = 300000;
    }

    while (millis() - start < 30000) {
      if (WiFi.status() == WL_CONNECTED) {
        timeout = false;
        break;
      }
      delay(500);
    }

  }
  if (timeout) {
    DBG_OUTPUT_PORT.println("Restarting ESP");
    while (1)ESP.restart();
    delay(500);
  }
  // if wifi cannot connect start smartconfig
  //  while (WiFi.status() != WL_CONNECTED) {
  //    delay(500);
  //    DBG_OUTPUT_PORT.print(".");
  //    if (cnt++ >= 15) {
  //      WiFi.beginSmartConfig();
  //      while (1) {
  //        delay(500);
  //        if (WiFi.smartConfigDone()) {
  //          DBG_OUTPUT_PORT.println("SmartConfig Success");
  //          break;
  //        }
  //      }
  //    }
  //  }

  DBG_OUTPUT_PORT.println("");
  DBG_OUTPUT_PORT.println("");

  WiFi.printDiag(DBG_OUTPUT_PORT);

  // Print the IP address
  DBG_OUTPUT_PORT.println(WiFi.localIP());

  //fill AWS parameters
  awsWSclient.setAWSRegion(aws_region);
  awsWSclient.setAWSDomain(aws_endpoint);
  awsWSclient.setAWSKeyID(aws_key);
  awsWSclient.setAWSSecretKey(aws_secret);
  awsWSclient.setUseSSL(true);
  checkConnectionTime = millis();

  if (connect ()) {
    subscribe ();
  }
}

void verifyConnection() {
  if (checkConnectionTime >=  300000) {
    if (WiFi.status() != WL_CONNECTED) {
      DBG_OUTPUT_PORT.println("Disconnected, restarting ESP!");
      while (1)ESP.restart();
      delay(500);
    }
    checkConnectionTime = millis();
  }

}

void loop(void) {
  server.handleClient();
  bathProcess();
  verifyConnection();

  //keep the mqtt up and running
  if (awsWSclient.connected ()) {
    client.loop ();
  } else {
    //handle reconnection
    if (connect ()) {
      subscribe ();
    }
  }

}





