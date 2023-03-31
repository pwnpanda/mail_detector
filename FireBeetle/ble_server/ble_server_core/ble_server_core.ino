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
	int sleep_interval = TIME_TO_REST_FOR_SEND_SECONDS * uS_TO_S_FACTOR;

	int sleep_length_uS = (sleep_length_ms * 1000);

	int round = 0;
	int send_interval = sleep_interval / sleep_length_uS;
	while(round * sleep_length_uS <= send_interval) {
		// send new value every 5 seconds
		getTimeAsString(curTime, getTimeSincePostArrived() );
		setSendValue(pCharacteristic_real, curTime);
		round++;
		// Sleep short time until next small transmission window (short sleep - 5 seconds)
		delay(sleep_length_ms);
	}

	// sleep for big transmission break (long sleep)
	esp_sleep_enable_timer_wakeup(sleep_interval);
}

void setup() {
	Serial.begin(115200);

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

// Only runs if debug mode!
void loop() {

	if (connected) {

		// When post is detected, get time. Then when sending notification to phone, get current time and deduct previous time	

		getTimeAsString(curTime, getTimeSincePostArrived());
		setSendValue(pCharacteristic_real, curTime);

		if (debug) {
			sleep_counter++;
			sleep_length_ms = 1000;
			
			// real LDR sensor data
			// analogRead returns value between 0 - 4095
			sensorValue = analogRead(sensorPin);
			
			if (sensorValue <= 0)	Serial.println("No value read from pin");
			else Serial.printf("Value from light sensor - read: %d\n", sensorValue);

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
