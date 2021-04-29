/*
	Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleServer.cpp
	Ported to Arduino ESP32 by Evandro Copercini
	updates by chegewara
*/

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <stdio.h>
// #include <ArduinoTrace.h>

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_REAL_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CHARACTERISTIC_DEBUG_UUID "0f06709f-e038-4f4f-8795-31c514ec22dd"
BLEServer *pServer;
BLEService *pService;
BLEAdvertising *pAdvertising;
BLECharacteristic *pCharacteristic_real;
BLECharacteristic *pCharacteristic_debug;
bool connected = false;
char *buf = new char[11];

void setSendValue(){
  Serial.print(F("Setting current value: "));
  Serial.println(buf);
  pCharacteristic_real->setValue(buf);
  pCharacteristic_real->notify();
}

unsigned long getCurrent(){
  return millis();
  /*  How to calculate offset if overflow occurs
   * if (nowTime < before){
   *   sum = (MAX_UNSIGNED_LONG - before) + nowTime
   * }
   */
}

char *getCurrentAsString(){
  ltoa(getCurrent(), buf, 10);
  Serial.print(F("Buffer is: "));
  Serial.println(buf);
  return buf;
  //return std::to_string(getCurrent());
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
	BLEUUID curUUID = pCharacteristic->getUUID();
	// if characteristic is REAL, do this
	if ( curUUID.equals( pCharacteristic_real->getUUID() ) ) {
	  Serial.println("Write received for real service UUID match!");
	  std::string value = pCharacteristic->getValue();

	  // Make sure we have data
	  if (value.length() > 0) {
		Serial.println("Characteristic was written! New value: ");
		// Print all data
		int res = (int) value[0];
		if (res == 1) Serial.println("Value = 1 received, going to sleep!");
		else  Serial.println("Other value received, something is wrong! INVESTIGATE!");
	  
	  } else {
		Serial.println("Something went wrong when reading data from characteristic. Length is 0!");
	  }
	  Serial.println("****************************************");
	  delay(15000);
	  // goto sleep!

	} else if ( curUUID.equals( pCharacteristic_debug->getUUID() ) ) {
	  Serial.println("Write received for debug service UUID match!");
	  // Send data for 30 seconds upon receipt, then stop. If already active, ignore.
	  std::string value = pCharacteristic->getValue();

	  // Make sure we have data
	  if (value.length() > 0) {
		Serial.println("Characteristic was written! New value: ");
		// Print all data
		int res = (int) value[0];
		if (res == 1){
		  Serial.println("Value = 1 received!");
		  Serial.println("Should now begin transmission of data");
		  // TODO send data
		} else  {
			Serial.println("Other value received, something is wrong! INVESTIGATE!");
		}
	  } else {
		Serial.printf("Unknown UUID: %s ! Check how this happened!\n", curUUID);
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
  pCharacteristic_real->setValue(getCurrentAsString());
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
  pService->dump();
  /* 
   *  DEBUG is not advertising! Debug to see why it is not sent! Only one characteristic per service?
   */
}

void loop() {
  
  if (connected) {
	getCurrentAsString();
	setSendValue();
	char * buf2 = new char[25];
	snprintf(buf2, 25, "%f", ((double) rand() / (RAND_MAX)) );
	pCharacteristic_debug->setValue( buf2 );
	Serial.print("Sending data: ");
	Serial.println(buf2);
	pCharacteristic_debug->notify();
  }
  
  // put your main code here, to run repeatedly
  delay(5000);
}