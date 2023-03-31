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
#define GPIO_NUM GPIO_NUM_25		// Which GPIO PIN we are using to wake up
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
const int sensorPin = GPIO_NUM_25;
int sensorValue = -1;

/* 
 * - Set detectTime upon sensor wakeup
 * - Check deep sleep
 * - Test sensor data from env
*/

//-------------------------------------BLE Functions-------------------------------------------------
// Helper function for finding characteristic type
characteristic getCharacteristic(BLECharacteristic *blechar){
	BLEUUID uuid = blechar->getUUID();
	if (uuid.equals( pCharacteristic_real->getUUID() ) ) {
		Serial.println("Found real in getChar!");
		return c_real;
	} else if ( uuid.equals( pCharacteristic_debug->getUUID() ) ) {
		Serial.println("Found debug in getChar!");
		return c_debug;
	} else {
		return unknown;
	}
}

// Send value through characteristic
void setSendValue(BLECharacteristic *pCharacteristic, char *buffer){
	// get Characteristic
	characteristic curChar = getCharacteristic(pCharacteristic);
	
	// Describe data to send
	Serial.print("Sending data: ");
	Serial.print(buffer);
	
	// Print relevant data based on enum
	if (curChar == c_real){
		Serial.println(" to Real characteristic!");
	}	else if (curChar == c_debug){
		Serial.println(" to debug characteristic!");
	}	
	
	// Do actual operation
	pCharacteristic->setValue(buffer);
	pCharacteristic->notify();
}

// ConnectionCallbacks from BLE Server
class ConnectionCallbacks: public BLEServerCallbacks {
	// Someone connected
	void onConnect (BLEServer *pServer){
		connected = true;
		pAdvertising->stop();
		Serial.println("Device connected!");
	}

	// Someone disconnected
	void onDisconnect (BLEServer *pServer){
		connected = false;
		pAdvertising->start();
		Serial.println("Device disconnected!");
	}
};

// CharacteristicsCallbacks from individual characteristics
class MyCallbacks: public BLECharacteristicCallbacks {

 	// When someone writes to characteristic
	void onWrite(BLECharacteristic *pCharacteristic) {
		Serial.println("Inside onWrite");
		characteristic curChar = getCharacteristic(pCharacteristic);
		
		// if characteristic is REAL, do this
		if ( curChar == c_real ) {
			Serial.println("Write received for real service UUID match!");
			std::string value = pCharacteristic->getValue();

	  		// Make sure we have data
			if (value.length() > 0) {
				Serial.println("Characteristic was written! ");
				// Print data
				int res = (int) value[0];
				if (res == 1){
					Serial.println("Ack from device received, going to sleep!");
					// If we are currently debugging, do not go to sleep!

					// We have received an ack from a phone, meaning alert is sent successfully
					if (!debug){
						/*
						* Reset detection!!
						* 1. Set detectTime to 0
						* 2. Sleep until sensor again detects post
						*/
						
						detectTime = 0;
						esp_wifi_stop();
						esp_bt_controller_disable();
						esp_bluedroid_disable();
						esp_deep_sleep_start();	
					}
					return;
				}

				Serial.print("Value other than 1 received, something is wrong! INVESTIGATE!\nVal:");
				Serial.println(res);

				return;

			} else {
				Serial.println("Something went wrong when reading data from characteristic. Length is 0!");
			}

			Serial.println("****************************************");
			return;
		}

		// If characteristic is debug
		if ( curChar == c_debug ) {
			Serial.println("Write received for debug service UUID match!");
	  		// Send data for 30 seconds upon receipt, then stop. If already active, ignore.
			std::string value = pCharacteristic->getValue();

	  		// Make sure we have data
			if (value.length() > 0) {
				Serial.println("Characteristic was written!");
				// Print all data
				int res = (int) value[0];
				if (res == 1){
					Serial.println("Value = 1 received, Client is requesting debug data!");
		  			// If Debug is not already enabled
					if (!debug) {
		  				// Debug set to true leads to sending data
						Serial.println("Setting debug to true!");
						debug = true;
					}
					return;
				}

				Serial.println("Other value received, something is wrong! INVESTIGATE!");
				return;
			}

			Serial.printf("Unknown UUID: %s ! Check how this happened!\n", pCharacteristic->getUUID());
			return;
		}
	}
};
// Start listening and running bluetooth connections
void start_bluetooth() {

	esp_bt_controller_enable(ESP_BT_MODE_BLE);
	esp_bluedroid_enable();

	BLEDevice::init("FireBeetle ESP32-E Robin");
	// Initiate callback class
	BLECharacteristicCallbacks * myCallbacks = new MyCallbacks();

	pServer = BLEDevice::createServer();
	pServer->setCallbacks(new ConnectionCallbacks());
	pService = pServer->createService(SERVICE_UUID);
	
	// Handling real data
	pCharacteristic_real = pService->createCharacteristic(
		CHARACTERISTIC_REAL_UUID,
		BLECharacteristic::PROPERTY_READ |
		BLECharacteristic::PROPERTY_WRITE |
		BLECharacteristic::PROPERTY_NOTIFY
		);
	delay(150);

	// Handle debug data
	pCharacteristic_debug = pService->createCharacteristic(
		CHARACTERISTIC_DEBUG_UUID,
		BLECharacteristic::PROPERTY_READ |
		BLECharacteristic::PROPERTY_WRITE |
		BLECharacteristic::PROPERTY_NOTIFY
		);
	
	// Set callbacks
	pCharacteristic_real->setCallbacks(myCallbacks);
	pCharacteristic_debug->setCallbacks(myCallbacks);
	
	// Set values
	pCharacteristic_debug->setValue("0");
	pCharacteristic_real->setValue( getTimeAsString(curTime, getCurrent() ) );
	delay(150);
	pService->addCharacteristic(pCharacteristic_debug);
	delay(150);
	pService->addCharacteristic(pCharacteristic_real);
	delay(150);
	
	// Start Service
	pService->start();
	pAdvertising = BLEDevice::getAdvertising();
	pAdvertising->addServiceUUID(SERVICE_UUID);
	pAdvertising->setScanResponse(true);
	pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
	pAdvertising->setMinPreferred(0x12);
	
	// Start advertising
	pAdvertising->start();
	// BLEDevice::startAdvertising();
	Serial.println("Characteristic defined! Now you can read it in your phone!");
}
//-------------------------------------Sensor and time Functions-------------------------------------------------
unsigned long mailDetected(){
	detectTime = getCurrent();
	Serial.printf("Detected post at: %d\n", detectTime);
	return detectTime;
}

// Calculate how long it was since last mail arrived
unsigned long getTimeSincePostArrived(){
	unsigned long now = getCurrent();
	
	//  Calculate offset if overflow occurs
	if (now < detectTime){
		Serial.printf("Overflow! Time since detection: %d seconds!", (ULONG_MAX - detectTime) + now);
		return (ULONG_MAX - detectTime) + now;
	}

	// If no overflow, handle normally
	Serial.printf("Time since detection: %d seconds!", now - detectTime);
	return now - detectTime;
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
	int interval = TIME_TO_REST_FOR_SEND_SECONDS * uS_TO_S_FACTOR;

	int sleep_length_uS = (sleep_length_ms * 1000);

	int round = 0;
	int limit = interval / sleep_length_uS;
	while(round * sleep_length_uS < limit) {
		// send new value every 5 seconds
		getTimeAsString(curTime, getTimeSincePostArrived() );
		setSendValue(pCharacteristic_real, curTime);
		round++;
		// Sleep short time until next small transmission window (short sleep)
		delay(sleep_length_ms);
	}

	// sleep for big transmission break (long sleep)
	esp_sleep_enable_timer_wakeup(interval);
}

void setup() {
	Serial.begin(115200);

  pinMode(sensorPin, INPUT);
  
  Serial.println("Started!");
    
	// Set sleep wakeup condition - WakeUp if GPIO Goes to level set in GPIO_LEVEL!
	// Wakeup if beam is broken
	esp_sleep_enable_ext0_wakeup(GPIO_NUM, GPIO_LEVEL);

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

int getSensorData(){
  // real LDR sensor data
  // analogRead returns value between 0 - 4095
  sensorValue = analogRead(sensorPin);
  pinMode(sensorPin, INPUT);
  sensorValueDigital = digitalRead(sensorPin);
  
  if (sensorValue <= 0) Serial.println("No value read from pin");
  else Serial.printf("Value from light sensor - read: analog %d | digital %d\n", sensorValue, sensorValueDigital);

  return sensorValue;
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
