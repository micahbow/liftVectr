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

#define GYRO_THRESHOLD 1.7 //minimum degrees per second to consider
#define ACCEL_THRESHOLD 5 //minimum g to consider

//1. Calibrate sensor on a flat surface (0,0,0 angular position)
float xyzPosition[3]; //considering starting position as origin. UNITS: METERS
int xyzOrientation[3]; //this is essential for calirbration. UNITS: DEGREES
float xyzVelocity[3]; //this may need to be fed to the app. UNITS: METERS/SEC
unsigned long accelOldTime; //UNITS: MILLISECONDS
unsigned long gyroOldTime; //UNITS: MILLISECONDS
bool firstRun = true;

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

  xyzPosition[0] = 0;
  xyzPosition[1] = 0;
  xyzPosition[2] = 0;
  xyzOrientation[0] = 0;
  xyzOrientation[1] = 0;
  xyzOrientation[2] = 0;
  xyzVelocity[0] = 0;
  xyzVelocity[1] = 0;
  xyzVelocity[2] = 0;

}

void loop() {
  if(firstRun) {
    firstRun = false;
    accelOldTime = micros();
    gyroOldTime = micros();
  }

  //update orientation block
  /*
  if (IMU.gyroscopeAvailable()) {
    float x_gyro,y_gyro,z_gyro; //UNITS: DEGREES/SEC
    unsigned long gyroCurrTime = micros();
    IMU.readGyroscope(x_gyro, y_gyro, z_gyro);
    //2. Calculate angular displacement via single integration of angular velocity (gyro readings) ONLY IF LARGE ENOUGH
    unsigned long gyroDeltaTime = 1000*(gyroCurrTime - gyroOldTime); //gyro readings in dps. milli*1000 = sec.
    if(x_gyro > GYRO_THRESHOLD || x_gyro < -1*GYRO_THRESHOLD) { xyzOrientation[0] += (int)(x_gyro* gyroDeltaTime); }
    if(y_gyro > GYRO_THRESHOLD || y_gyro < -1*GYRO_THRESHOLD) { xyzOrientation[1] += (int)(y_gyro* gyroDeltaTime); }
    if(z_gyro > GYRO_THRESHOLD || z_gyro < -1*GYRO_THRESHOLD) { xyzOrientation[2] += (int)(z_gyro* gyroDeltaTime); }

    if(xyzOrientation[0]>= 360) { xyzOrientation[0] = xyzOrientation[0]%360; }
    if(xyzOrientation[0]<= 360) { xyzOrientation[0] = -1*(abs(xyzOrientation[0])%360); }

    if(xyzOrientation[1]>= 360) { xyzOrientation[1] = xyzOrientation[1]%360; }
    if(xyzOrientation[1]<= 360) { xyzOrientation[1] = -1*(abs(xyzOrientation[1])%360); }

    if(xyzOrientation[2]>= 360) { xyzOrientation[2] = xyzOrientation[2]%360; }
    if(xyzOrientation[2]<= 360) { xyzOrientation[2] = -1*(abs(xyzOrientation[2])%360); }
    
    gyroOldTime = micros(); //may need to find tune the positioning of these milli calls. Should I reuse CurrTime instead? 
  }
  */

  //update velocity/position blocks
  if (IMU.accelerationAvailable()) {
    float x_local_accel, y_local_accel, z_local_accel; //UNITS: G/SEC
    unsigned long accelCurrTime = micros();
    IMU.readAcceleration(x_local_accel, y_local_accel, z_local_accel);

    /* hiding for now, want to see performance without gyro
    
    //3. Apply rotational transform on local-framed acceleration data to get world-space acceleration
      //need to derive implementation first. Consider the fact that 1G vector acts in world space down on the board. 
      //We then need to convert local acceleraton to world acceleration via a series of trigonometric multiplication.
      float magnitude = (x_local_accel*x_local_accel)+(y_local_accel*y_local_accel)+(z_local_accel*z_local_accel);
      float x_world_accel, y_world_accel, z_world_accel;

      //degrees to radian conversion
      x_world_accel = magnitude*cos(xyzOrientation[0] * 71 / 4068.0);
      y_world_accel = magnitude*cos(xyzOrientation[1] * 71 / 4068.0);
      z_world_accel = magnitude*cos(xyzOrientation[2] * 71 / 4068.0) - 1; //subtract 1g from the world acceleration in z plane.

    */

    //test block here
    float x_world_accel, y_world_accel, z_world_accel;
    x_world_accel = x_local_accel; //test if no gyro assume upright
    y_world_accel = y_local_accel;
    z_world_accel = z_local_accel - 1;
    
    //4. Perform double integration of world-space accleration to calculate XYZ displacement
    unsigned long accelDeltaTime = (accelCurrTime - accelOldTime); //accelerometer readings in g, g * 9.8 = m/s^2. Must multiply by seconds squared.
    //calculate intermediate velocities
    xyzVelocity[0] = (x_world_accel*accelDeltaTime * 9.8)/1000000.0;
    xyzVelocity[1] = (y_world_accel*accelDeltaTime * 9.8)/1000000.0;
    xyzVelocity[2] = (z_world_accel*accelDeltaTime * 9.8)/1000000.0;
    //integrate velocities one more time to get displacement
    xyzPosition[0] += (xyzVelocity[0]*accelDeltaTime)/1000000.0; 
    xyzPosition[1] += (xyzVelocity[1]*accelDeltaTime)/1000000.0;
    xyzPosition[2] += (xyzVelocity[2]*accelDeltaTime)/1000000.0;
    gyroOldTime = accelCurrTime; //may need to find tune the positioning of these milli calls. Should I reuse CurrTime instead? 
  }


  //print XYZ world space displacement

  Serial.print(xyzPosition[0]);
  Serial.print('\t');
  Serial.print(xyzPosition[1]);
  Serial.print('\t');
  Serial.println(xyzPosition[2]);
  
  //diagnostic metric, comment out when done
  /*
  Serial.print(xyzOrientation[0]);
  Serial.print('\t');
  Serial.print(xyzOrientation[1]);
  Serial.print('\t');
  Serial.println(xyzOrientation[2]);
  */
 
   
}
