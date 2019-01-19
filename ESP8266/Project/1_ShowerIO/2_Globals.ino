//DEFINES AND FUNCTION PROTOTYPES

#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <FS.h>
#include <DNSServer.h>
#include <EEPROM.h>
#include <Ticker.h>
#include <WiFiManager.h>
#include <ESP8266mDNS.h>
#include <ArduinoJson.h>
#include <WiFiUdp.h>
#include <Stream.h>
//AWS
#include "sha256.h"
#include "Utils.h"


//WEBSockets
#include <Hash.h>
#include <WebSocketsClient.h>

//MQTT PUBSUBCLIENT LIB
#include <PubSubClient.h>

//AWS MQTT Websocket
#include "Client.h"
#include "AWSWebSocketClient.h"
#include "CircularByteBuffer.h"

extern "C" {
#include "user_interface.h"
}

#define rele D1      // the number of the LED pin
#define Led_Aviso D0
#define FLOW_SENSOR_PIN 5 // Sensor Input

char aws_endpoint[]    = "agq6mvwjsctpy-ats.iot.us-east-2.amazonaws.com";
char aws_region[]      = "us-east-2";
const char* aws_topic_times  = "times";
const char* aws_topic_conf  = "configuration";
int port = 443;

//MQTT config
const int maxMQTTpackageSize = 512;
const int maxMQTTMessageHandlers = 1;

AWSWebSocketClient awsWSclient(1000);

PubSubClient client(awsWSclient);

//# of connections
long connection = 0;

//count messages arrived
int arrivedcount = 0;

bool shouldSaveConfig = false;

unsigned long lastDebounceTime = 0;  // the last time the output pin was toggled
unsigned long debounceDelay = 25;    // the debounce time; increase if the output flickers

//positions to save variables on the EEPROM
int address_tempo = 0;
int address_espera = 1;
int address_pausa = 2;
int address_password = 3;
int address_email = 4;

byte armazenado;
byte minutos = EEPROM.read(address_tempo); //tempo de banho
byte minutos_espera = EEPROM.read(address_espera); //tempo de espera atÃ© o banho ser habilitado novamente
byte minutos_pausa = EEPROM.read(address_pausa); // tempo que o banho pode ficar pausado
byte password = EEPROM.read(address_password);
byte email = EEPROM.read(address_email);

int tempo = (int)minutos * 60;
int tempo_espera = (int)minutos_espera * 60;
int tempo_de_pausa = (int)minutos_pausa * 60;

#define DBG_OUTPUT_PORT Serial

ESP8266WebServer server(80);

// TODO - Verify if all these functions will be used
//API REST Mapping Functions
bool handleFileRead(String path);
void selectDurationTime();
void setActualShowerTimePlus();
void setActualShowerTimeLess();
void selectOffTime();
void setActualOffTimePlus();
void setActualOffTimeLess();
void selectPausedTime();
void setActualPausedTimePlus();
void setActualPausedTimeLess();

// Shower logic variables

volatile int flow_frequency; // Measures flow sensor pulses
unsigned int l_hour; // Calculated litres/hour
unsigned long currentTime;
unsigned long cloopTime;
boolean bathRunning;
boolean showerIsOn;
boolean waiting;
unsigned long bathTime;
unsigned long stoppedTime;
unsigned long waitingTime;
