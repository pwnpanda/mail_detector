
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
						Serial.println("Resetting and deep sleeping");
						detectTime = 0;
						esp_wifi_stop();
						esp_bt_controller_disable();
						esp_bluedroid_disable();
            // Cancel all wakeup sources
            esp_sleep_disable_wakeup_source(ESP_SLEEP_WAKEUP_ALL);
            // Activate IO wakeup source
            setDeepSleepWakeup();
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
