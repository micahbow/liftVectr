#include <Arduino_LSM9DS1_Modified.h>
#define G_THRESH 2

//NOTE: THIS CODE DOES NOT ACCOUNT FOR GIMBAL LOCK.

double xOff,yOff,zOff;
bool firstRun = true;

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
    Serial.print(xc);
    Serial.print('\t');
    Serial.print(yc);
    Serial.print('\t');
    Serial.println(zc);
    
    sumX += xc;
    sumY += yc;
    sumZ += zc;   
  }
  Serial.println("GYRO OFFSETS:");
  //average values
  offsetx = sumX/100.0;
  offsety = sumY/100.0;
  offsetz = sumZ/100.0;
  Serial.print(offsetx);
  Serial.print('\t');
  Serial.print(offsety);
  Serial.print('\t');
  Serial.println(offsetz);
}

void getUnbiasedGyro(double & gx, double & gy, double & gz) {
  while(!IMU.gyroscopeAvailable()) {}
  float gxt,gyt,gzt;
  IMU.readGyroscope(gxt, gyt, gzt);
  gx=gxt-xOff;
  gy=gyt-yOff;
  gz=gzt-zOff;

  gy = 0; //TEST

  if(abs(gx) < G_THRESH) {gx = 0;}
  if(abs(gy) < G_THRESH) {gy = 0;}
  if(abs(gz) < G_THRESH) {gz = 0;}
}

void setup() {
  Serial.begin(9600);
  while (!Serial);
  Serial.println("Started");

  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }
  calibrateGyro(xOff,yOff,zOff);

  Serial.println("Gyroscope in degrees/second");
  Serial.println("X\tY\tZ");
  
}

unsigned long oldMuTime;

double xyzAngle[3];

void loop() {
  if(firstRun) {
    firstRun = false;
    oldMuTime = micros();
    xyzAngle[0] = 0;
    xyzAngle[1] = 0;
    xyzAngle[2] = 0;
  }
  double x, y, z;

  unsigned long newMuTime = micros();
  getUnbiasedGyro(x,y,z);

  unsigned long deltaMuTime = newMuTime-oldMuTime;
  oldMuTime = newMuTime;

  //calculate angular displacement.
  xyzAngle[0]+=(x*deltaMuTime)/1000000.0;
  xyzAngle[1]+=(y*deltaMuTime)/1000000.0;
  xyzAngle[2]+=(z*deltaMuTime)/1000000.0;
  
  Serial.print(xyzAngle[0]);
  Serial.print('\t');
  Serial.print(xyzAngle[1]);
  Serial.print('\t');
  Serial.println(xyzAngle[2]);

}
