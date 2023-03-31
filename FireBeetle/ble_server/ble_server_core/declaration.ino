/*
	Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleServer.cpp
	Ported to Arduino ESP32 by Evandro Copercini
	updates by chegewara
	
	See the following for generating UUIDs:
	https://www.uuidgenerator.net/

*/

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <stdio.h>
#include <esp_wifi.h>
#include <esp_bt_main.h>

// #include <ArduinoTrace.h>

// Copy-paste from limits.h
/* Minimum and maximum values a `signed long int' can hold.
   (Same as `int').  */
#undef LONG_MAX
#define LONG_MAX __LONG_MAX__
/* Maximum value an `unsigned long int' can hold.  (Minimum is 0).  */
#undef ULONG_MAX
#define ULONG_MAX (LONG_MAX * 2UL + 1UL)

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_REAL_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CHARACTERISTIC_DEBUG_UUID "0f06709f-e038-4f4f-8795-31c514ec22dd"
// ----------------------------------------------------------------------------------
#define uS_TO_S_FACTOR 1000000  // Conversion factor for micro seconds to seconds
#define TIME_TO_REST_FOR_SEND_SECONDS 30 // Time to rest before retransmitting
// ----------------------------------------------------------------------------------
#define GPIO_NUM GPIO_NUM_1		// Which GPIO PIN we are using to wake up
#define GPIO_LEVEL 0	// If wakeup is on high or low edge (0 means wakeup will trigger when value is ~0)
// ----------------------------------------------------------------------------------
BLEServer *pServer;
BLEService *pService;
BLEAdvertising *pAdvertising;
BLECharacteristic *pCharacteristic_real;
BLECharacteristic *pCharacteristic_debug;
// ----------------------------------------------------------------------------------
bool connected = false;
int sleep_length_ms = 5000;
int sleep_counter = 0;
char *curTime = new char[11];
// Store through sleep - if 0 it means unset!
RTC_DATA_ATTR unsigned long detectTime = 0;
RTC_DATA_ATTR bool debug = false;

enum characteristic {
	c_real, c_debug, unknown
};
typedef enum characteristic characteristic;
// ----------------------------------------------------------------------------------
//const int sensorPin = GPIO_NUM_25;
const int sensorPin = 25;
int sensorValue = -1;