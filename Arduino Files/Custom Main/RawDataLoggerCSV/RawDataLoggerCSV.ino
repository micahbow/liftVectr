#include <Arduino_LSM9DS1_Modified.h>
float ax,ay,az,gx,gy,gz,mx,my,mz,xOff,yOff,zOff;
unsigned long t;
void calibrateGyro(double & offsetx, double & offsety, double & offsetz);

void setup() {
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
  while(!IMU.gyroscopeAvailable() || !IMU.accelerationAvailable() || !IMU.magneticFieldAvailable()) {}
  t=micros();
  IMU.readGyroscope(gx, gy, gz);
  IMU.readAcceleration(ax, ay, az);
  IMU.readMagneticField(mx,my,mz);
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
  Serial.print(',');
  Serial.print(mx);
  Serial.print(',');
  Serial.print(my);
  Serial.print(',');
  Serial.println(mz);
}

void calibrateGyro(float & offsetx, float & offsety, float & offsetz) {
  double sumX = 0;
  double sumY = 0;
  double sumZ = 0;
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
  offsetx = (float)(sumX/100.0);
  offsety = (float)(sumY/100.0);
  offsetz = (float)(sumZ/100.0);
}
