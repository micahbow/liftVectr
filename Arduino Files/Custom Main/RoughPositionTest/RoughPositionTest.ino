/*
  Arduino LSM9DS1 - Position Estimator
  
  This code will utilize angular acceleration and linear
  acceleration values to calculate the relative XYZ (in world-space orientation)
  position of the unit. Ignoring bias, the general process is as follows:
  1. Calibrate sensor on a flat surface (0,0,0 angular position)
  2. Calculate angular displacement via single integration of angular velocity (gyro readings)
  3. Apply rotational transform on local-framed acceleration data to get world-space acceleration
  4. Perform double integration of world-space accleration to calculate XYZ displacement
  5. Repeat

  The circuit:
  - Arduino Nano 33 BLE Sense

  created 03 March 2022
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

  Serial.print("Gyroscope sample rate = ");
  Serial.print(IMU.gyroscopeSampleRate());
  Serial.println(" Hz");

  Serial.println("World Space Displacement in Meters:");
  Serial.println("X\tY\tZ");

  accelOldTime = millis();
  gyroOldTime = millis();

}

//1. Calibrate sensor on a flat surface (0,0,0 angular position)
float xyzPosition[] = {0,0,0}; //considering starting position as origin. UNITS: METERS
float xyzOrientation[] = {0,0,0}; //this is essential for calirbration. UNITS: DEGREES
unsigned long accelOldTime = 0; //UNITS: MILLISECONDS
unsigned long gyroOldTime = 0; //UNITS: MILLISECONDS

void loop() {

  if (IMU.gyroscopeAvailable()) {
    float x_gyro,y_gyro,z_gyro; //UNITS: DEGREES/SEC
    unsigned long gyroCurrTime = millis();
    IMU.readGyroscope(x_gyro, y_gyro, z_gyro);
    //2. Calculate angular displacement via single integration of angular velocity (gyro readings)
    unsigned long gyroDeltaTime = 1000*(gyroCurrTime - gyroOldTime); //gyro readings in dps. milli*1000 = sec.
    xyzOrientation[0] += x_gyro* gyroDeltaTime;
    xyzOrientation[1] += y_gyro* gyroDeltaTime;
    xyzOrientation[2] += z_gyro* gyroDeltaTime;
    gyroOldTime = millis(); //may need to find tune the positioning of these milli calls. Should I reuse CurrTime instead? 
  }
    
  if (IMU.accelerationAvailable()) {
    float x_local_accel, y_local_accel, z_local_accel; //UNITS: G/SEC
    unsigned long accelCurrTime = millis();
    IMU.readAcceleration(x, y, z);
    //3. Apply rotational transform on local-framed acceleration data to get world-space acceleration
      //need to derive implementation first. Consider the fact that 1G vector acts in world space down on the board. We have to decompose it into relative XYZ
      //acceleration components and then subtract it from the local acceleration values to get rid of gravity drift.

      //we then need to convert local acceleraton to world acceleration via a series of trigonometric multiplications. To be derived.

      
    //4. Perform double integration of world-space accleration to calculate XYZ displacement
    unsigned long accelDeltaTime = 1000*(accelCurrTime - accelOldTime); //accelerometer readings in g, g * 9.8 = m/s^2. Must multiply by seconds squared.
    xyzPosition[0] += x_world_accel*accelDeltaTime*accelDeltaTime*9.8; 
    xyzPosition[1] += y_world_accel*accelDeltaTime*accelDeltaTime*9.8;
    xyzPosition[2] += z_world_accel*accelDeltaTime*accelDeltaTime*9.8;
    gyroOldTime = millis(); //may need to find tune the positioning of these milli calls. Should I reuse CurrTime instead? 
  }

  //print XYZ world space displacement
  Serial.print(xyzPosition[0]);
  Serial.print('\t');
  Serial.print((xyzPosition[1]);
  Serial.print('\t');
  Serial.println((xyzPosition[2]);
   
}
