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
// #include <ArduinoTrace.h>

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_REAL_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CHARACTERISTIC_DEBUG_UUID "0f06709f-e038-4f4f-8795-31c514ec22dd"
// ----------------------------------------------------------------------------------
#define uS_TO_S_FACTOR 1000000  // Conversion factor for micro seconds to seconds
#define S_TO_HR_FACTOR 3600			// Conversion factor for seconds to hours
#define TIME_TO_SLEEP_HOURS  40		// Time ESP32 will go to sleep (in hours)
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
bool debug = false;
int sleep_counter = 0;
char *curTime = new char[11];
RTC_DATA_ATTR char *detectTime = new char[11];
enum characteristic {
	c_real, c_debug, unknown
};
typedef enum characteristic characteristic;
// ----------------------------------------------------------------------------------

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

void setSendValue(BLECharacteristic *pCharacteristic, char *buffer){
  Serial.print("Sending data: ");
  Serial.print(buffer);
  // get Characteristic
  characteristic curChar = getCharacteristic(pCharacteristic);
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

unsigned long getCurrent(){
  return millis();
  /*  How to calculate offset if overflow occurs
   * if (nowTime < before){
   *   sum = (MAX_UNSIGNED_LONG - before) + nowTime
   * }
   */
}

char *getTimeAsString(char *buf, unsigned long time){
  ltoa(time, buf, 10);
  Serial.print(F("Time is: "));
  Serial.println(buf);
  return buf;
}

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
			// goto sleep!
			// esp_wifi_stop();
			//esp_bt_controller_disable();
			//esp_bluedroid_disable();
			// esp_deep_sleep_start();
		}else{
		  Serial.print("Value other than 1 received, something is wrong! INVESTIGATE!\nVal:");
		  Serial.println(res);
		}
	  
	  } else {
		Serial.println("Something went wrong when reading data from characteristic. Length is 0!");
	  }
	  Serial.println("****************************************");
	  
	  

	// If characteristic is debug
	} else if ( curChar == c_debug ) {
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
		  // Debug is not already enabled
		  if (!debug) {
		  	// Debug set to true leads to sending data
		  	Serial.println("Setting debug to true!");
		  	debug = true;
		  }
		} else {
			Serial.println("Other value received, something is wrong! INVESTIGATE!");
		}
	  } else {
		Serial.printf("Unknown UUID: %s ! Check how this happened!\n", pCharacteristic->getUUID());
	  }
	}
  }
};

// todo needs error-handling to flush connections in case of disconnect!

void setup() {
  Serial.begin(115200);
  Serial.println(F("Starting BLE work!"));
  
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
  pService->start();
  pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  pAdvertising->start();
  // BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
  
  // Set sleep wakeup condition
  esp_sleep_enable_ext0_wakeup(GPIO_NUM, GPIO_LEVEL);
}

void loop() {
  int sleep = 5000;

  if (connected) {
	getTimeAsString(curTime, getCurrent());
	setSendValue(pCharacteristic_real, curTime);
	
	if (debug) {
		sleep_counter++;
		sleep = 1000;
		/* char *LDRSensorData = new char [25]
		snprintf(LDRSensorData, 25, "%f", getSensorData() );
		pCharacteristic_debug->setValue( LDRSensorData );
		pCharacteristic_debug->notify();
		Serial.print("Sending data: ");
		Serial.println(LDRSensorData);
		*/
		char * LDRSignal = new char[25];
		snprintf(LDRSignal, 25, "%f", ((double) rand() / (RAND_MAX)) );
		setSendValue(pCharacteristic_debug, LDRSignal);
		Serial.print("Sending data: ");
		Serial.println(LDRSignal);
	}
  }

  // If we have printed debug info for 30sec, stop it and reset counter
  if (sleep_counter >= 30)	debug = false; sleep_counter = 0; sleep = 5000;
 
  // Sleep for 1 sec
  delay(sleep);
}