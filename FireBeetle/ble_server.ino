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
BLECharacteristic *pCharacteristic;
char *buf = new char[11];

void setSendValue(){
  Serial.print(F("Setting current value: "));
  Serial.println(buf);
  pCharacteristic->setValue(buf);
  pCharacteristic->indicate();
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

void setup() {
  Serial.begin(115200);
  Serial.println(F("Starting BLE work!"));

  BLEDevice::init("FireBeetle ESP32-E Robin");
  pServer = BLEDevice::createServer();
  pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ | BLECharacteristic::NOTIFY |
                                         BLECharacteristic::PROPERTY_INDICATION
                                       );

  pCharacteristic->setValue(getCurrentAsString());
  pService->start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

void loop() {
  getCurrentAsString();
  setSendValue();
  // put your main code here, to run repeatedly:
  delay(2000);
}
