/*
  Arduino LSM9DS1 - Simple Gyroscope

  This example reads the gyroscope values from the LSM9DS1
  sensor and continuously prints them to the Serial Monitor
  or Serial Plotter.

  The circuit:
  - Arduino Nano 33 BLE Sense

  created 10 Jul 2019
  by Riccardo Rizzo

  This example code is in the public domain.
*/

#include <Arduino_LSM9DS1_Modified.h>

#define GYRO_THRESH 1.7

float gyropos[3];
bool firstRun = true;
unsigned long oldmicros;

void setup() {
  Serial.begin(9600);
  while (!Serial);
  Serial.println("Started");

  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }
  Serial.print("Gyroscope sample rate = ");
  Serial.print(IMU.gyroscopeSampleRate());
  Serial.println(" Hz");
  Serial.println();
  Serial.println("Gyroscope in degrees/second");
  Serial.println("X\tY\tZ");

  gyropos[0] = 0;
  gyropos[1] = 0;
  gyropos[2] = 0;
}

void loop() {
  if(firstRun) {
    oldmicros = micros();
  }
  float x, y, z;
  
  if (IMU.gyroscopeAvailable()) {
    unsigned long newmicros = micros();
    IMU.readGyroscope(x, y, z);
    float deltaUseconds = (newmicros-oldmicros);
    oldmicros = newmicros;

    if(abs(x) > GYRO_THRESH) { gyropos[0] += (x*deltaUseconds)/1000000.0; }
    if(abs(y) > GYRO_THRESH) { gyropos[1] += (y*deltaUseconds)/1000000.0; }
    if(abs(z) > GYRO_THRESH) { gyropos[2] += (z*deltaUseconds)/1000000.0; }

    Serial.print(gyropos[0]);
    Serial.print('\t');
    Serial.print(gyropos[1]);
    Serial.print('\t');
    Serial.println(gyropos[2]);
  }
}
