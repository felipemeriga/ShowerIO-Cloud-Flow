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

#define buttonPin D2  // the number of the pushbutton pin
#define rele D1      // the number of the LED pin
#define Led_Aviso D0

char aws_endpoint[]    = "agq6mvwjsctpy-ats.iot.us-east-2.amazonaws.com";
char aws_key[]         = "AKIAILQOI2X6CSXLOZKA";
char aws_secret[]      = "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";
char aws_region[]      = "us-east-2";
const char* aws_topic  = "times";
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

Ticker botao;
Ticker tyme;

bool shouldSaveConfig = false;

// Variables will change:
int releState = LOW;         // the current state of the output pin
int buttonState;             // the current reading from the input pin
int lastButtonState = LOW;   // the previous reading from the input pin
int led_S = LOW;

unsigned long lastDebounceTime = 0;  // the last time the output pin was toggled
unsigned long debounceDelay = 25;    // the debounce time; increase if the output flickers

enum Estado_Botao {
  desligado,
  iniciar_banho,
  pausar_banho,
  continuar_banho
};


enum Estado_Banho {
  habilitado,
  desabilitado
};

Estado_Botao Estado_Bot = desligado;
Estado_Banho Banho = habilitado;

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

//Shower Logic Functions
void logica_botao();
void logica_tempo();

