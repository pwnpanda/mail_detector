#include <ArduinoTrace.h>

const int buttonPin = 25;
int val = 0;
void setup() {
	Serial.begin(115200);
	Serial.println(F("Starting work!"));
	// put your setup code here, to run once:
	//pinMode(buttonPin, INPUT);
}

void loop() {
	// put your main code here, to run repeatedly:
	val = analogRead(buttonPin);
	//DUMP(val);
	Serial.printf("Value read: %d\n",val);
	delay(2000);
}