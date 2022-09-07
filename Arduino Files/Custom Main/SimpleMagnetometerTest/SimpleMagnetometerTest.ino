#include "LSM6DS3.h"
#include "Wire.h"

//Create a instance of class LSM6DS3
LSM6DS3 IMU(I2C_MODE, 0x6A);    //I2C device address 0x6A

void setup() {

  uint8_t gModification;
  IMU.readRegister(&gModification, LSM6DS3_ACC_GYRO_CTRL1_XL);
  gModification = (gModification & 0xF3) | LSM6DS3_ACC_GYRO_FS_XL_16g;
  IMU.writeRegister(LSM6DS3_ACC_GYRO_CTRL1_XL, gModification);

  Serial.begin(9600);
  while (!Serial);
  Serial.println("Started");

  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }
  Serial.println("Magnetic Field in uT");
  Serial.println("X\tY\tZ");
}

void calibrateMagno(double & mag_origin_x, double & mag_origin_y, double & mag_origin_z) {
  double sumX = 0;
  double sumY = 0;
  double sumZ = 0;
/*
  Serial.println("CALIBRATING MAGNO:");
  Serial.print("Magnetic field sample rate = ");
  Serial.print(IMU.magneticFieldSampleRate());
  Serial.println(" uT");
  Serial.println();
  //disregard the first 100 points, highly inaccurate!
  for(int i = 0; i < 100; i++) {
    while(!IMU.magneticFieldAvailable()) {}
    float xd, yd, zd;
    IMU.readMagneticField(xd, yd, zd);
  }
  for(int i = 0; i < 100; i++) {
    while(!IMU.magneticFieldAvailable()) {}
    float xc, yc, zc;
    IMU.readMagneticField(xc, yc, zc);
    
    sumX += xc;
    sumY += yc;
    sumZ += zc;   
  }
*/
  mag_origin_x = sumX/100.0;
  mag_origin_y = sumY/100.0;
  mag_origin_z = sumZ/100.0;
}

void loop() {
  float x, y, z;
/*
  if (IMU.magneticFieldAvailable()) {
    IMU.readMagneticField(x, y, z);

    Serial.print(x);
    Serial.print('\t');
    Serial.print(y);
    Serial.print('\t');
    Serial.println(z);
  }
*/
}
