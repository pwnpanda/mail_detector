//#include <ArduinoTrace.h>

const int buttonPin = 25;
#define GPIO_LEVEL 0  // If wakeup is on high or low edge (0 means wakeup will trigger when value is ~0)
RTC_DATA_ATTR int runNr = 0;

void print_wakeup_reason(){
  esp_sleep_wakeup_cause_t wakeup_reason;

  wakeup_reason = esp_sleep_get_wakeup_cause();

  switch(wakeup_reason)
  {
    case ESP_SLEEP_WAKEUP_EXT0 : Serial.println("Wakeup caused by external signal using RTC_IO"); break;
    case ESP_SLEEP_WAKEUP_EXT1 : Serial.println("Wakeup caused by external signal using RTC_CNTL"); break;
    case ESP_SLEEP_WAKEUP_TIMER : Serial.println("Wakeup caused by timer"); break;
    case ESP_SLEEP_WAKEUP_TOUCHPAD : Serial.println("Wakeup caused by touchpad"); break;
    case ESP_SLEEP_WAKEUP_ULP : Serial.println("Wakeup caused by ULP program"); break;
    default : Serial.printf("Wakeup was not caused by deep sleep: %d\n",wakeup_reason); break;
  }
}

int val = 0;
int anval = 0;
void setup() {
	Serial.begin(115200);
	Serial.println(F("Starting work!"));
	// put your setup code here, to run once:
	pinMode(buttonPin, INPUT);

    // Set sleep wakeup condition - WakeUp if GPIO Goes to level set in GPIO_LEVEL!
  esp_sleep_enable_ext0_wakeup(GPIO_NUM_25, GPIO_LEVEL);
  

  //Print the wakeup reason for ESP32
  print_wakeup_reason();

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
  //esp_deep_sleep_start();
  
}

void loop() {
	// put your main code here, to run repeatedly:
	anval = analogRead(buttonPin);
  pinMode(buttonPin, INPUT);
  delay(10);
  val = digitalRead(buttonPin);
	//DUMP(val);
	Serial.printf("Value read: %d\n",val);
  Serial.printf("Analog value read: %d\n",anval);
	delay(1000);
}
