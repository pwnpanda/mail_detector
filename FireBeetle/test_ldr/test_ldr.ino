//#include <ArduinoTrace.h>

const int buttonPin = 25;
#define GPIO_LEVEL 0  // If wakeup is on high or low edge (0 means wakeup will trigger when value is ~0)
RTC_DATA_ATTR int runNr = 0;

int val = 0;
void setup() {
	Serial.begin(115200);
	Serial.println(F("Starting work!"));
	// put your setup code here, to run once:
	pinMode(buttonPin, INPUT);

    // Set sleep wakeup condition - WakeUp if GPIO Goes to level set in GPIO_LEVEL!
  esp_sleep_enable_ext0_wakeup(GPIO_NUM_25, GPIO_LEVEL);
  
  int i = 0;

  while(i<5){
      val = digitalRead(buttonPin);
    //DUMP(val);
    Serial.printf("Value read: %d\n",val);
    delay(1000);
    i++;
  }
  if (runNr>0){
    Serial.println("Run #" + String(runNr));
  }
  runNr++;
  Serial.println(F("Going to sleep!"));
  esp_deep_sleep_start();
  
}

void loop() {
	// put your main code here, to run repeatedly:
	// val = analogRead(buttonPin);
  val = digitalRead(buttonPin);
	//DUMP(val);
	Serial.printf("Value read: %d\n",val);
	delay(1000);
}
