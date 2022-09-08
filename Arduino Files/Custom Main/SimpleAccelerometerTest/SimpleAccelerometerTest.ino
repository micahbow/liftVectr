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

#include "LSM6DS3.h"
#include "Wire.h"

//Create a instance of class LSM6DS3
LSM6DS3 IMU(I2C_MODE, 0x6A);    //I2C device address 0x6A

void setup() {
  Serial.begin(9600);
  while (!Serial);
  Serial.println("Started");

  if (IMU.begin() != 0) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }

  uint8_t gModification;
  IMU.readRegister(&gModification, LSM6DS3_ACC_GYRO_CTRL1_XL);
  gModification = (gModification & 0xF3) | LSM6DS3_ACC_GYRO_FS_XL_16g;
  IMU.writeRegister(LSM6DS3_ACC_GYRO_CTRL1_XL, gModification);

  Serial.print("Accelerometer sample rate = ");
  Serial.print(104.0F);
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

  uint8_t accelerometerAvailable;
  IMU.readRegister(&accelerometerAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
  if ((accelerometerAvailable & 0x01) != 0x00) {
    x = IMU.readFloatAccelX();
    y = IMU.readFloatAccelY();
    z = IMU.readFloatAccelZ();
    //WE CAN FIX THE X AXIS
    Serial.print(0);
    Serial.print('\t');
    Serial.print(y);
    Serial.print('\t');
    Serial.println(z);
  }
}
