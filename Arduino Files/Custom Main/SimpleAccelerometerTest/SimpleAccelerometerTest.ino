/*
  Arduino LSM9DS1 - Simple Accelerometer

  This example reads the acceleration values from the LSM9DS1
  sensor and continuously prints them to the Serial Monitor
  or Serial Plotter. Modified to use 16G range.

  The circuit:
  - Arduino Nano 33 BLE Sense

  created 10 Jul 2019
  by Riccardo Rizzo

  modified 03 March 2022
  by Micah Bowonthamachakr

*/

#include <Arduino_LSM9DS1_Modified.h>

void setup() {
  Serial.begin(9600);
  while (!Serial);
  Serial.println("Started");

  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }

  Serial.print("Accelerometer sample rate = ");
  Serial.print(IMU.accelerationSampleRate());
  Serial.println(" Hz");
  Serial.println();
  Serial.println("Acceleration in G's");
  Serial.println("X\tY\tZ");

  //do this to freeze serialplotter range

  Serial.print(16.0);
  Serial.print('\t');
  Serial.print(16.0);
  Serial.print('\t');
  Serial.println(16.0);

  Serial.print(-16.0);
  Serial.print('\t');
  Serial.print(-16.0);
  Serial.print('\t');
  Serial.println(-16.0);
}

void loop() {
  float x, y, z;

  if (IMU.accelerationAvailable()) {
    IMU.readAcceleration(x, y, z);
    //WE CAN FIX THE X AXIS
    Serial.print(0);
    Serial.print('\t');
    Serial.print(y);
    Serial.print('\t');
    Serial.println(z);
  }
}
