#include "LSM6DS3.h"
#include "Wire.h"
#include "SensorFusion.h" //SF
SF fusion;

//Create a instance of class LSM6DS3
LSM6DS3 IMU(I2C_MODE, 0x6A);    //I2C device address 0x6A

float gx, gy, gz, ax, ay, az, mx, my, mz;
float pitch, roll, yaw;
float deltat;
double offx,offy,offz;

void calibrateGyro(double & offsetx, double & offsety, double & offsetz) {
  double sumX = 0;
  double sumY = 0;
  double sumZ = 0;

  Serial.println("CALIBRATING GYRO:");  
  Serial.print("Gyroscope sample rate = ");
  Serial.print(104.0F);
  Serial.println(" Hz");
  Serial.println();
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
  offsetx = sumX/100.0;
  offsety = sumY/100.0;
  offsetz = sumZ/100.0;
}

void setup() {

  uint8_t gModification;
  IMU.readRegister(&gModification, LSM6DS3_ACC_GYRO_CTRL1_XL);
  gModification = (gModification & 0xF3) | LSM6DS3_ACC_GYRO_FS_XL_16g;
  IMU.writeRegister(LSM6DS3_ACC_GYRO_CTRL1_XL, gModification);

  Serial.begin(9600); //serial to display data
  while (!Serial);
  Serial.println("Started");
  // your IMU begin code goes here
  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }

  calibrateGyro(offx,offy,offz);

  float summX = 0;
  float summY = 0;
  float summZ = 0;
  
  //disregard the first 100 points, highly inaccurate!
  /*
  for(int i = 0; i < 100; i++) {
    while(!IMU.magneticFieldAvailable()) {}
    float xd, yd, zd;
    IMU.readMagneticField(xd, yd, zd);
  }

  for(int i = 0; i < 100; i++) {
    while(!IMU.magneticFieldAvailable()) {}
    float xd, yd, zd;
    IMU.readMagneticField(xd, yd, zd);
    summX += xd;
    summY += yd;
    summZ += zd;
  }
  */
  //disregard the first 100 points, highly inaccurate!
  for(int i = 0; i < 100; i++) {
    uint8_t accelerometerAvailable;
    IMU.readRegister(&accelerometerAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
    while((accelerometerAvailable & 0x01) == 0x00) {}
    float xd, yd, zd;
    xd = IMU.readFloatAccelX();
    yd = IMU.readFloatAccelY();
    zd = IMU.readFloatAccelZ();
  }

  float sumaX = 0;
  float sumaY = 0;
  float sumaZ = 0;
  
  for(int i = 0; i < 100; i++) {
    uint8_t accelerometerAvailable;
    IMU.readRegister(&accelerometerAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
    while((accelerometerAvailable & 0x01) == 0x00) {}
    float xd, yd, zd;
    xd = IMU.readFloatAccelX();
    yd = IMU.readFloatAccelY();
    zd = IMU.readFloatAccelZ();
    sumaX += xd;
    sumaY += yd;
    sumaZ += zd;
  }
  fusion.initQuat(sumaX/100.0,sumaY/100.0,sumaZ/100.0,summX/100.0,summY/100.0,summZ/100.0);

  //wait till steady state
  for(int i = 0; i < 1000; i++) {
    uint8_t accelerometerAvailable;
    IMU.readRegister(&accelerometerAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
    while((accelerometerAvailable & 0x01) == 0x00) {}
    ax = IMU.readFloatAccelX();
    ay = IMU.readFloatAccelY();
    az = IMU.readFloatAccelZ();
    uint8_t gyroscopeAvailable;
    IMU.readRegister(&gyroscopeAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
    while((gyroscopeAvailable & 0x02) == 0x00) {}
    gx = IMU.readFloatGyroX();
    gy = IMU.readFloatGyroY();
    gz = IMU.readFloatGyroZ();
    gx=(gx-(float)offx)*DEG_TO_RAD;
    gy=(gy-(float)offy)*DEG_TO_RAD;
    gz=(gz-(float)offz)*DEG_TO_RAD;
    deltat = fusion.deltatUpdate(); //this have to be done before calling the fusion update
    fusion.MahonyUpdate(gx, gy, gz, ax, ay, az, deltat);  //mahony is suggested if there isn't the mag and the mcu is slow
  }

  Serial.println();
  Serial.println("Gyroscope in degrees/second");
  Serial.println("X\tY\tZ"); 
}

void loop() {
  // now you should read the gyroscope, accelerometer (and magnetometer if you have it also)
  // NOTE: the gyroscope data have to be in radians
  // if you have them in degree convert them with: DEG_TO_RAD example: gx * DEG_TO_RAD
    uint8_t accelerometerAvailable;
    IMU.readRegister(&accelerometerAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
    while((accelerometerAvailable & 0x01) == 0x00) {}
    ax = IMU.readFloatAccelX();
    ay = IMU.readFloatAccelY();
    az = IMU.readFloatAccelZ();
    uint8_t gyroscopeAvailable;
    IMU.readRegister(&gyroscopeAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
    while((gyroscopeAvailable & 0x02) == 0x00) {}
    gx = IMU.readFloatGyroX();
    gy = IMU.readFloatGyroY();
    gz = IMU.readFloatGyroZ();

  //if not using mahony
  //while(!IMU.magneticFieldAvailable()) {}
  //IMU.readMagneticField(mx, my, mz);

  gx=(gx-(float)offx)*DEG_TO_RAD;
  gy=(gy-(float)offy)*DEG_TO_RAD;
  gz=(gz-(float)offz)*DEG_TO_RAD;

  deltat = fusion.deltatUpdate(); //this have to be done before calling the fusion update
  //choose only one of these two:
  fusion.MahonyUpdate(gx, gy, gz, ax, ay, az, deltat);  //mahony is suggested if there isn't the mag and the mcu is slow
  //fusion.MadgwickUpdate(gx, gy, gz, ax, ay, az, mx, my, mz, deltat);  //else use the magwick, it is slower but more accurate
  //WE ARE fixing X on accelerometer and Y on gyroscope.

  
  pitch = fusion.getPitch();
  roll = fusion.getRoll();    //you could also use getRollRadians() ecc
  //yaw = fusion.getYaw();

    Serial.print(pitch);
    Serial.print('\t');
    Serial.print(roll);
    Serial.print('\t');
    Serial.println(0); //we don't care about yaw
}
