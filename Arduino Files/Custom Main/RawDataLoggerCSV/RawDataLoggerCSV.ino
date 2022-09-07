#include "LSM6DS3.h"
#include "Wire.h"

//Create a instance of class LSM6DS3
LSM6DS3 IMU(I2C_MODE, 0x6A);    //I2C device address 0x6A

float ax,ay,az,gx,gy,gz,mx,my,mz,xOff,yOff,zOff;
unsigned long t;
void calibrateGyro(double & offsetx, double & offsety, double & offsetz);

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
  calibrateGyro(xOff,yOff,zOff);
  Serial.println("t,aX,aY,aZ,gX,gY,gZ,mX,mY,mZ");
}

void loop() {
  uint8_t gyroscopeAvailable;
  uint8_t accelerometerAvailable;
  IMU.readRegister(&gyroscopeAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
  IMU.readRegister(&accelerometerAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
  while((gyroscopeAvailable & 0x02) == 0x00 || (accelerometerAvailable & 0x01) == 0x00) {}
  t=micros();
  gx = IMU.readFloatGyroX();
  gy = IMU.readFloatGyroY();
  gz = IMU.readFloatGyroZ();
  ax = IMU.readFloatAccelX();
  ay = IMU.readFloatAccelY();
  az = IMU.readFloatAccelZ();
  //IMU.readMagneticField(mx,my,mz);
  gx=gx-xOff;
  gy=gy-yOff;
  gz=gz-zOff;
  Serial.print(t);
  Serial.print(',');
  Serial.print(ax);
  Serial.print(',');
  Serial.print(ay);
  Serial.print(',');
  Serial.print(az);
  Serial.print(',');
  Serial.print(gx);
  Serial.print(',');
  Serial.print(gy);
  Serial.print(',');
  Serial.print(gz);
  /*
  Serial.print(',');
  Serial.print(mx);
  Serial.print(',');
  Serial.print(my);
  Serial.print(',');
  Serial.println(mz);
  */
}

void calibrateGyro(float & offsetx, float & offsety, float & offsetz) {
  double sumX = 0;
  double sumY = 0;
  double sumZ = 0;
  //disregard the first 100 points, highly inaccurate!
  for(int i = 0; i < 100; i++) {
    uint8_t gyroscopeAvailable;
    IMU.readRegister(&gyroscopeAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
    while((gyroscopeAvailable & 0x02) == 0x00) {}
    float xd, yd, zd;
    xd = IMU.readFloatGyroX();
    yd = IMU.readFloatGyroY();
    zd = IMU.readFloatGyroZ();
  }
  for(int i = 0; i < 100; i++) {
    uint8_t gyroscopeAvailable;
    IMU.readRegister(&gyroscopeAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
    while((gyroscopeAvailable & 0x02) == 0x00) {}
    float xc, yc, zc;
    xc = IMU.readFloatGyroX();
    yc = IMU.readFloatGyroY();
    zc = IMU.readFloatGyroZ();
    sumX += xc;
    sumY += yc;
    sumZ += zc;   
  }
  //average values
  offsetx = (float)(sumX/100.0);
  offsety = (float)(sumY/100.0);
  offsetz = (float)(sumZ/100.0);
}
