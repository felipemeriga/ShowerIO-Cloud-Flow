#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

void setup() {

  int cnt = 0;

  // set for STA mode
  WiFi.mode(WIFI_STA);

  // put your setup code here, to run once:
  Serial.begin(9600);

  // deplay for 2 sec for smartConfig
  Serial.println("2 sec before clear SmartConfig");
  delay(2000);

  // if wifi cannot connect start smartconfig
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    if (cnt++ >= 15) {
      WiFi.beginSmartConfig();
      while (1) {
        delay(500);
        if (WiFi.smartConfigDone()) {
          Serial.println("SmartConfig Success");
          break;
        }
      }
    }
  }

  Serial.println("");
  Serial.println("");

  WiFi.printDiag(Serial);

  // Print the IP address
  Serial.println(WiFi.localIP());
}

void loop() {


}

