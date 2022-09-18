//flat position test
#include "LSM6DS3.h"
#include "Wire.h"

#define A_THRESH 0 //minimum g to consider

//Create a instance of class LSM6DS3
LSM6DS3 IMU(I2C_MODE, 0x6A);    //I2C device address 0x6A

//1. Calibrate sensor on a flat surface (0,0,0 angular position)
double xyzVelocity[3]; //considering starting position as origin. UNITS: METERS
unsigned long accelOldTime; //UNITS: MILLISECONDS
bool firstRun = true;

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

  Serial.println("World Space Displacement in Meters:");
  Serial.println("X\tY\tZ");

  xyzVelocity[0] = 0;
  xyzVelocity[1] = 0;
  xyzVelocity[2] = 0;
}

void loop() {
  if(firstRun) {
    firstRun = false;
    accelOldTime = micros();
  }

  //update velocity/position blocks
  uint8_t accelerometerAvailable;
  IMU.readRegister(&accelerometerAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
  while((accelerometerAvailable & 0x01) == 0x00) {}
  float x_local_accel, y_local_accel, z_local_accel; //UNITS: G/SEC
  unsigned long accelCurrTime = micros();
  x_local_accel = IMU.readFloatAccelX();
  y_local_accel = IMU.readFloatAccelY();
  z_local_accel = IMU.readFloatAccelZ();
  z_local_accel -=1;

  if(abs(x_local_accel) < A_THRESH) { x_local_accel = 0; }
  if(abs(y_local_accel) < A_THRESH) { y_local_accel = 0; }
  if(abs(z_local_accel) < A_THRESH) { z_local_accel = 0; }

  /*
  Serial.print(x_local_accel);
  Serial.print('\t');
  Serial.print(y_local_accel);
  Serial.print('\t');
  Serial.println(z_local_accel);
  */
  
  //4. Perform single integration of world-space accleration to calculate XYZ velocity
  unsigned long accelDeltaTime = (accelCurrTime - accelOldTime); //accelerometer readings in g, g * 9.8 = m/s^2. Must multiply by seconds squared.
  //integrate velocities one more time to get displacement
  xyzVelocity[0] = 0;//(x_local_accel*accelDeltaTime*accelDeltaTime*9.8)/1000000.0; 
  //IN CENTIMETERS TEMPORARILY
  xyzVelocity[1] += (y_local_accel*accelDeltaTime*9.8/1000000.0);
  xyzVelocity[2] += (z_local_accel*accelDeltaTime*9.8/1000000.0);
  accelOldTime = accelCurrTime;

  Serial.print(xyzVelocity[0]);
  Serial.print('\t');
  Serial.print(xyzVelocity[1]);
  Serial.print('\t');
  Serial.println(xyzVelocity[2]);
  
  //print XYZ world space displacement
  /*
  Serial.print(xyzPosition[0]);
  Serial.print('\t');
  Serial.print(xyzPosition[1]);
  Serial.print('\t');
  Serial.println(xyzPosition[2]);*/
   
}
