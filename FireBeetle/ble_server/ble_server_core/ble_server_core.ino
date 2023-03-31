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
#include <esp_sleep.h>

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
#define GPIO_NUM GPIO_NUM_25   // Which GPIO PIN we are using to wake up
#define GPIO_LEVEL 0  // If wakeup is on high or low edge (0 means wakeup will trigger when value is ~0)
// ----------------------------------------------------------------------------------
BLEServer *pServer;
BLEService *pService;
BLEAdvertising *pAdvertising;
BLECharacteristic *pCharacteristic_real;
BLECharacteristic *pCharacteristic_debug;
// ----------------------------------------------------------------------------------
bool connected = false;
int sleep_length_ms = 5000 * 2;
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
int sensorValueDigital = -1;
//------------------------ DECLARATIONS DONE ----------------------------------------
/* 
 * - Set detectTime upon sensor wakeup
 * - Check deep sleep
 * - Test sensor data from env
*/

//-------------------------------------Sensor and time Functions-------------------------------------------------
unsigned long mailDetected(){
	detectTime = getCurrent();
	Serial.printf("Detected post at: %d\n", detectTime);
	return detectTime;
}

// Calculate how long it was since last mail arrived
unsigned long getTimeSincePostArrived(){
	unsigned long cur_time = getCurrent();
	
	//  Calculate offset if overflow occurs
	if (cur_time < detectTime){
		Serial.printf("Overflow! Time since detection: %d milliseconds - %d seconds!", (ULONG_MAX - detectTime) + cur_time, (ULONG_MAX - detectTime) + cur_time / 1000);
		return (ULONG_MAX - detectTime) + cur_time;
	}

	// If no overflow, handle normally
	Serial.printf("Time since detection: %d milliseconds - %d seconds! Detected: %d, Time now: %d", (cur_time - detectTime), (cur_time - detectTime) / 1000, detectTime, cur_time);
	return (cur_time - detectTime);
}

// Get current time since start in milliseconds
unsigned long getCurrent(){
	return millis();
}

// Convert time to string
char *getTimeAsString(char *buf, unsigned long time){
	ltoa(time, buf, 10);
	Serial.print(F("Time is: "));
	Serial.println(buf);
	return buf;
}

//---------------------------------------Main Functions-------------------------------------------------

// Send info continously for a short interval, then go to long sleep
// Short interval is small transmission window, long interval is long transmission break
void sendAndSleep() {
	// sleep interval and send interval
	int sleep_interval = TIME_TO_REST_FOR_SEND_SECONDS * uS_TO_S_FACTOR;

	int sleep_length_uS = (sleep_length_ms * 1000);

	int sending_round = 0;
	int send_interval = sleep_interval / sleep_length_uS;
	while(sending_round * sleep_length_uS <= send_interval) {
		// send new value every 5 seconds
		getTimeAsString(curTime, getTimeSincePostArrived() );
		setSendValue(pCharacteristic_real, curTime);
		sending_round++;
		// Sleep short time until next small transmission window (short sleep - 5 seconds)
		delay(sleep_length_ms);
	}

	// sleep for big transmission break (long sleep)
	esp_sleep_enable_timer_wakeup(sleep_interval);
}


int getSensorData(){
  // real LDR sensor data
  // analogRead returns value between 0 - 4095
  sensorValue = analogRead(sensorPin);
  pinMode(sensorPin, INPUT);
  sensorValueDigital = digitalRead(sensorPin);
  
  if (sensorValue < 0) Serial.println("No value read from pin");
  else Serial.printf("Value from light sensor - read: analog %d | digital %d\n", sensorValue, sensorValueDigital);

  return sensorValue;
}

void setDeepSleepWakeup(){
  // Set sleep wakeup condition - WakeUp if GPIO Goes to level set in GPIO_LEVEL!
  // Wakeup if beam is broken
  esp_sleep_enable_ext0_wakeup(GPIO_NUM, GPIO_LEVEL);  
}

void setup() {
	Serial.begin(115200);

  pinMode(sensorPin, INPUT);

	Serial.println("Started!");

  getSensorData();
   
  setDeepSleepWakeup();

	// Detect reason for wakeup
	esp_sleep_wakeup_cause_t wakeup_reason;
	wakeup_reason = esp_sleep_get_wakeup_cause();
	
	// New mail detected
	if (wakeup_reason == ESP_SLEEP_WAKEUP_EXT0 && detectTime == 0 && !debug){
		// Set detection timestamp
		mailDetected();
		Serial.println(F("Starting BLE work!"));
		start_bluetooth();
		// send data for x seconds, then sleep for x seconds
		sendAndSleep();
	}

	if (wakeup_reason == ESP_SLEEP_WAKEUP_TIMER && detectTime!= 0 && !debug){
		sendAndSleep();
	}

	// Mail already detected, retry transmission
	if(detectTime != 0 && !debug){
		Serial.println(F("Starting BLE work!"));
		start_bluetooth();
		// send data for x seconds, then sleep for x seconds
		sendAndSleep();
	}

	// Sleep if nothing to report and we are not debugging
	if (detectTime == 0 && !debug){
		esp_deep_sleep_start();
	}

	// If debug is on, never go to sleep - use loop!
	if (debug){
		Serial.println(F("Starting BLE work!"));
		start_bluetooth();
	}
}

// Only runs if debug mode!
void loop() {

	if (connected) {

		// When post is detected, get time. Then when sending notification to phone, get current time and deduct previous time	

		getTimeAsString(curTime, getTimeSincePostArrived());
		setSendValue(pCharacteristic_real, curTime);

		if (debug) {
			sleep_counter++;
			sleep_length_ms = 1000;
			
			sensorValue = getSensorData();

			char *LDRSensorData = new char [25];
			snprintf(LDRSensorData, 25, "%f", sensorValue );
			setSendValue(pCharacteristic_debug, LDRSensorData);
		}
	}

	// If we have printed debug info for 30sec, stop it and reset counter
	if (sleep_counter >= 30){
		debug = false;
		sleep_counter = 0;
		sleep_length_ms = 5000;
		Serial.println("Reported debug signal for 30 sec. Setting Debug off!");
	}

	// Sleep for set period
	delay(sleep_length_ms);
}
