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
#include <neotimer.h>

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
#define TRANSMISSION_TIME_MS 15000 // Time to do transmission before sleeping
// ----------------------------------------------------------------------------------
#define GPIO_NUM GPIO_NUM_25   // Which GPIO PIN we are using to wake up
#define GPIO_LEVEL 0  // If wakeup is on high or low edge (0 means wakeup will trigger when value is ~0)
// ----------------------------------------------------------------------------------
BLEServer *pServer;
BLEService *pService;
BLEAdvertising *pAdvertising;
BLECharacteristic *pCharacteristic_real;
BLECharacteristic *pCharacteristic_debug;
BLECharacteristicCallbacks * myCallbacks;
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

enum behaviour_enum {
  transmit, debug_on, sleep_on, undefined, initial
};
typedef enum behaviour_enum behaviour_enum;
behaviour_enum behaviour = undefined;

enum bluetooth_status {
  started, working, off
};
typedef bluetooth_status bluetooth_status;
bluetooth_status bluetooth = off;
// ----------------------------------------------------------------------------------
//const int sensorPin = GPIO_NUM_25;
const int sensorPin = 25;
int sensorValue = -1;
int sensorValueDigital = -1;
int repeat = 0;

int sleep_interval = TIME_TO_REST_FOR_SEND_SECONDS * uS_TO_S_FACTOR;

const unsigned long wait = 30000; // 30 sec wait timer
unsigned long lastTrigger = 0; // Holds last reset counter
behaviour_enum prev_case = initial;
bool print_new = true;

Neotimer delay1 = Neotimer(TRANSMISSION_TIME_MS); // Timer running for 15 seconds
Neotimer delay2 = Neotimer(150); // Timer running very short delay for BT
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
		Serial.printf("Overflow! Time since detection: %d milliseconds - %d seconds!\n", (ULONG_MAX - detectTime) + cur_time, ((ULONG_MAX - detectTime) + cur_time) / 1000);
		return (ULONG_MAX - detectTime) + cur_time;
	}

	// If no overflow, handle normally
	Serial.printf("Time since detection: %d milliseconds - %d seconds! Detected: %d, Time now: %d\n", (cur_time - detectTime), (cur_time - detectTime) / 1000, detectTime, cur_time);
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

// Send info then go to long, timed sleep
void sendData() {
  if (bluetooth != working) return;
  
  Serial.println("Updating timing information for sending!");
  getTimeAsString(curTime, getTimeSincePostArrived() );
  setSendValue(pCharacteristic_real, curTime);
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

void printWakeUpReason(esp_sleep_wakeup_cause_t wakeup_reason){
    switch(wakeup_reason)
    {
      case ESP_SLEEP_WAKEUP_UNDEFINED:    Serial.println("In case of deep sleep: reset was not caused by exit from deep sleep"); break;
      case ESP_SLEEP_WAKEUP_ALL:          Serial.println("Not a wakeup cause: used to disable all wakeup sources with esp_sleep_disable_wakeup_source"); break;
      case ESP_SLEEP_WAKEUP_EXT0:         Serial.println("Wakeup caused by external signal using RTC_IO"); break;
      case ESP_SLEEP_WAKEUP_EXT1:         Serial.println("Wakeup caused by external signal using RTC_CNTL"); break;
      case ESP_SLEEP_WAKEUP_TIMER:        Serial.println("Wakeup caused by timer"); break;
      case ESP_SLEEP_WAKEUP_TOUCHPAD:     Serial.println("Wakeup caused by touchpad"); break;
      case ESP_SLEEP_WAKEUP_ULP:          Serial.println("Wakeup caused by ULP program"); break;
      case ESP_SLEEP_WAKEUP_GPIO:         Serial.println("Wakeup caused by GPIO (light sleep only)"); break;
      case ESP_SLEEP_WAKEUP_UART:         Serial.println("Wakeup caused by UART (light sleep only)"); break;
      default : Serial.printf("Wakeup was not caused by deep sleep: %d\n",wakeup_reason); break;
    }
}

 void goToSleep(){
  esp_wifi_stop();
  bluetooth = off;
  esp_bt_controller_disable();
  esp_bluedroid_disable();
  // Cancel all wakeup sources
  esp_sleep_disable_wakeup_source(ESP_SLEEP_WAKEUP_ALL);
  // Activate IO wakeup source
  setDeepSleepWakeup();
  esp_deep_sleep_start();
}

void setup() {
	Serial.begin(115200);

  pinMode(sensorPin, INPUT);

	Serial.println("Started!");

  delay1.set(TRANSMISSION_TIME_MS);
  delay2.set(150);
  
  getSensorData();
   
  setDeepSleepWakeup();

	// Detect reason for wakeup
	esp_sleep_wakeup_cause_t wakeup_reason;
	wakeup_reason = esp_sleep_get_wakeup_cause();
  
  printWakeUpReason(wakeup_reason);

  // Mail already detected, but it registered a new delivery. Retry transmission
  if(detectTime != 0 && !debug){
    // Set enum for checking in loop to indicate need to start BT services and sending
    Serial.println("Mail already detected, but new delivery is registered! Retrying transmission!");
    behaviour = transmit;
  }

  
	// New mail detected
	if (wakeup_reason == ESP_SLEEP_WAKEUP_EXT0 && detectTime == 0 && !debug){
		// Set detection timestamp
		mailDetected();
    // Set enum for checking in loop to indicate need to start BT services and sending
    behaviour = transmit;
    Serial.println("New mail detected! Turn on BT and transmit!");
	}

	if (wakeup_reason == ESP_SLEEP_WAKEUP_TIMER && detectTime!= 0 && !debug){
		// Set enum for checking in loop to indicate need to start sending
    behaviour = transmit;
    Serial.println("Retransmitting data after sleep interval (30 seconds)");
	}

	// Sleep if nothing to report and we are not debugging
	if (detectTime == 0 && !debug){
    // Set enum for going to deep sleep
    behaviour = sleep_on;
	}

	// If debug is on, never go to sleep - use loop!
	if (debug){
    behaviour = debug_on;
	}
}

// Wait 30 sec on startup before sleeping, to enable debug testing
void loop() {
  
  // Only print info first time and when on change
  if (prev_case == behaviour) print_new = false;
  prev_case = behaviour;

  if(bluetooth == off){
    bluetooth = started;
    Serial.println("Starting BlueTooth!");
    start_bluetooth();
    repeat = 0;
    if (delay2.repeat(5, 150L)){
      Serial.printf("Run: %d\n", repeat);
      startup_bt_with_delays(repeat);
      repeat++;
    }
  }
  
  // bt_on_and_transmit, transmit, debug, sleep, unknown
  switch(behaviour){
    
    case transmit:
      if (print_new)  Serial.println("Transmitting data to BT!");
      // sleep interval for long break (30 sec - updates GATT Characteristic every 15 seconds)
      if (bluetooth == working){
        // need to change to this? https://github.com/ben-jacobson/non_blocking_timers/blob/main/examples/non_blocking_timers/non_blocking_timers.ino
        if (delay1.repeat(2)){
          Serial.println("Executing");
          // send data for 30 seconds
          sendData();
        }
      }
      // sleep for big transmission break (long sleep - 30 sec)
      esp_sleep_enable_timer_wakeup(sleep_interval);
      break;

    case debug_on:
      if (print_new)  Serial.println("In debug!");
      break;

    case sleep_on:
      if (millis() > 30000 ) {  
        Serial.println("Nothing to do - going to deep sleep");
        goToSleep();
      } else {  if (print_new) Serial.println("Waiting 30 sec before going to deep sleep after startup"); }
      break;
      
    case undefined:
      if (print_new)  Serial.println("No state defined. Continuing without doing anything");
      break;

    default:
      if (print_new)  Serial.println("Unknown state and defined behaviour! Ignoring!");
      break;
  }
  
  unsigned long currentTime = millis();
  if (currentTime - lastTrigger >= wait){
    
  	if (connected && debug) {
			sleep_counter++;
			sleep_length_ms = 1000;
			
			sensorValue = getSensorData();

			char *LDRSensorData = new char [25];
			snprintf(LDRSensorData, 25, "%f", sensorValue );
			setSendValue(pCharacteristic_debug, LDRSensorData);
  	}
  
  	// If we have printed debug info for 30sec, stop it and reset counter
  	if (sleep_counter >= 30){
  		debug = false;
  		sleep_counter = 0;
  		sleep_length_ms = 5000;
  		Serial.println("Reported debug signal for 30 sec. Setting Debug off and stopping!");
      lastTrigger = currentTime;
  	}
   
  }
  //update_timers();
}
