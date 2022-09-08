#include "LSM6DS3.h"
#include "Wire.h"
#define G_THRESH 2
#define M_THRESH 1

//Create a instance of class LSM6DS3
LSM6DS3 IMU(I2C_MODE, 0x6A);    //I2C device address 0x6A

double xOff,yOff,zOff;
double m0x,m0y,m0z;
bool firstRun = true;

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
  uint8_t gyroscopeAvailable;
  IMU.readRegister(&gyroscopeAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
  while((gyroscopeAvailable & 0x02) == 0x00) {}
  float gxt,gyt,gzt;
  gxt = IMU.readFloatGyroX();
  gyt = IMU.readFloatGyroY();
  gzt = IMU.readFloatGyroZ();
  gx=gxt-xOff;
  gy=gyt-yOff;
  gz=gzt-zOff;

  if(abs(gx) < G_THRESH) {gx = 0;}
  if(abs(gy) < G_THRESH) {gy = 0;}
  if(abs(gz) < G_THRESH) {gz = 0;}
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

  calibrateGyro(xOff,yOff,zOff); 
  calibrateMagno(m0x,m0y,m0z); 
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

  //check with origin condition w/ mag.
  /*
  while(!IMU.magneticFieldAvailable()) {}
  float xmag, ymag, zmag;
  IMU.readMagneticField(xmag, ymag, zmag);
  if((abs(xmag-m0x)<M_THRESH) && (abs(ymag-m0y)<M_THRESH) && (abs(zmag-m0z)<M_THRESH)) {
    xyzAngle[0] = 0;
    xyzAngle[1] = 0;
    xyzAngle[2] = 0;
  }
  */
  //normalize within +/- 360 degree
  while(xyzAngle[0] > 360) {xyzAngle[0]-=360;}
  while(xyzAngle[1] > 360) {xyzAngle[1]-=360;}
  while(xyzAngle[2] > 360) {xyzAngle[2]-=360;}

  while(xyzAngle[0] < -360) {xyzAngle[0]+=360;}
  while(xyzAngle[1] < -360) {xyzAngle[1]+=360;}
  while(xyzAngle[2] < -360) {xyzAngle[2]+=360;}

  //print
  Serial.print(xyzAngle[0]);
  Serial.print('\t');
  Serial.print(xyzAngle[1]);
  Serial.print('\t');
  Serial.println(xyzAngle[2]);

}
