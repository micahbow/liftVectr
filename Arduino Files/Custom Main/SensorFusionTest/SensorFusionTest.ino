#include <Arduino_LSM9DS1_Modified.h>
#include "SensorFusion.h" //SF
SF fusion;

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
  Serial.print(IMU.gyroscopeSampleRate());
  Serial.println(" Hz");
  Serial.println();
  //disregard the first 100 points, highly inaccurate!
  for(int i = 0; i < 100; i++) {
    while(!IMU.gyroscopeAvailable()) {}
    float xd, yd, zd;
    IMU.readGyroscope(xd, yd, zd);
  }
  for(int i = 0; i < 100; i++) {
    while(!IMU.gyroscopeAvailable()) {}
    float xc, yc, zc;
    IMU.readGyroscope(xc, yc, zc);
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
  
  //disregard the first 100 points, highly inaccurate!
  for(int i = 0; i < 100; i++) {
    while(!IMU.accelerationAvailable()) {}
    float xd, yd, zd;
    IMU.readAcceleration(xd, yd, zd);
  }

  float sumaX = 0;
  float sumaY = 0;
  float sumaZ = 0;
  
  for(int i = 0; i < 100; i++) {
    while(!IMU.accelerationAvailable()) {}
    float xd, yd, zd;
    IMU.readAcceleration(xd, yd, zd);
    sumaX += xd;
    sumaY += yd;
    sumaZ += zd;
  }
  fusion.initQuat(sumaX/100.0,sumaY/100.0,sumaZ/100.0,summX/100.0,summY/100.0,summZ/100.0);

  //wait till steady state
  for(int i = 0; i < 1000; i++) {
    while(!IMU.accelerationAvailable()) {}
    IMU.readAcceleration(ax, ay, az);
    while(!IMU.gyroscopeAvailable()) {}
    IMU.readGyroscope(gx, gy, gz);
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
  while(!IMU.accelerationAvailable()) {}
  IMU.readAcceleration(ax, ay, az);
  while(!IMU.gyroscopeAvailable()) {}
  IMU.readGyroscope(gx, gy, gz);

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
