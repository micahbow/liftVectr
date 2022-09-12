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

#include "LSM6DS3.h"
#include "Wire.h"

#define GYRO_THRESH 1.7

//Create a instance of class LSM6DS3
LSM6DS3 IMU(I2C_MODE, 0x6A);    //I2C device address 0x6A

double gyropos[3];
bool firstRun = true;
unsigned long oldmicros;

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

  Serial.print("Gyroscope sample rate = ");
  Serial.print(104.0F);
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

  uint8_t gyroscopeAvailable;
  IMU.readRegister(&gyroscopeAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
  if ((gyroscopeAvailable & 0x02) != 0x00) {
    unsigned long newmicros = micros();
    x = IMU.readFloatGyroX();
    y = IMU.readFloatGyroY();
    z = IMU.readFloatGyroZ();
    unsigned long deltaUseconds = (newmicros-oldmicros);
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
