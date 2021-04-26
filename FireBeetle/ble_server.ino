/*
    Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleServer.cpp
    Ported to Arduino ESP32 by Evandro Copercini
    updates by chegewara
*/

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <stdio.h>

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
BLEServer *pServer;
BLEService *pService;
BLEAdvertising *pAdvertising;
BLECharacteristic *pCharacteristic;
bool connected = false;
char *buf = new char[11];

void setSendValue(){
  Serial.print(F("Setting current value: "));
  Serial.println(buf);
  pCharacteristic->setValue(buf);
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

char *getCurrentAsString(){
  ltoa(getCurrent(), buf, 10);
  Serial.print(F("Buffer is: "));
  Serial.println(buf);
  return buf;
  //return std::to_string(getCurrent());
}
class MyCallbacks: public BLECharacteristicCallbacks {
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

  // When someone writes to characteristic
  void onWrite(BLECharacteristic *pCharacteristic) {
    std::string value = pCharacteristic->getValue();
    // Make sure we have data
    if (value.length() > 0) {
      Serial.println("Characteristic was written! New value: ");
      // Print all data
      int res = (int) value[0]
      if (res == 1) Serial.println("Value = 1 received, going to sleep!");
      // for (int i = 0; i < value.length(); i++)  Serial.printf("\nStr: %s, Char: %c, D - int: %d, X - hex: %x A - double as hex: %a, E - double: %e", value[i], value[i], value[i], value[i], value[i], value[i]);
    } else {
      Serial.println("Something went wrong when reading data from characteristic. Length is 0!");
    }
    Serial.println("****************************************");
    delay(15000);
    // goto sleep!
  }
};

// todo needs error-handling to flush connections in case of disconnect!

void setup() {
  Serial.begin(115200);
  Serial.println(F("Starting BLE work!"));

  BLEDevice::init("FireBeetle ESP32-E Robin");
  pServer = BLEDevice::createServer();
  pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE |
                                         BLECharacteristic::PROPERTY_NOTIFY /*|
                                         BLECharacteristic::PROPERTY_INDICATE*/
                                       );
  pCharacteristic->setCallbacks(new MyCallbacks());
  pCharacteristic->setValue(getCurrentAsString());
  pService->start();
  pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  pAdvertising->start();
  // BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

void loop() {
  getCurrentAsString();
  setSendValue();
  // put your main code here, to run repeatedly:
  delay(5000);
}
