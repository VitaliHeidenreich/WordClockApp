//This example code is in the Public Domain (or CC0 licensed, at your option.)
//By Evandro Copercini - 2018
//
//This example creates a bridge between Serial and Classical Bluetooth (SPP)
//and also demonstrate that SerialBT have the same functionalities of a normal Serial

#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;
static String comBuilder = "";

void setup() {
  Serial.begin(115200);
  SerialBT.begin("Wordclock_123"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");
}



void loop() {
  if (Serial.available()) {
    SerialBT.write(Serial.read());
  }
  if (SerialBT.available()) {
      char tempIn = SerialBT.read();
      //Serial.write(tempIn);
      comBuilder = comBuilder + tempIn;
      if( tempIn == '$' ){
            Serial.print( "Found: " + comBuilder );
            SerialBT.println("Antwort vom ESP32: " + comBuilder);
            //do something
            comBuilder = "";
      }
  }
  delay(20);
}
