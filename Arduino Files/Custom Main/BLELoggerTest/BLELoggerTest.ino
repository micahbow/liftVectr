#include "LSM6DS3.h"
#include "Wire.h"
#include <ArduinoBLE.h>

//Create a instance of class LSM6DS3
LSM6DS3 IMU(I2C_MODE, 0x6A);    //I2C device address 0x6A

//Global BLE vars
BLEService IMUService("181C"); //BLE SERVICE "0x181C" = User Data
BLEStringCharacteristic IMUDataArr("2ADA",BLERead | BLENotify, 100); //BLE CHARACTERISTIC "0x2ADA" = Fitness Machine Status

//Global Data vars
float ax = 0.0,ay = 0.0,az = 0.0,gx = 0.0,gy = 0.0,gz = 0.0,mx = 0.0,my = 0.0,mz = 0.0,xOff = 0.0,yOff = 0.0,zOff = 0.0;
unsigned long t;
float initialData[10] = {ax, ay, az, gx, gy, gz, mx, my, mz, (float)t}; 
String initialDataString = "";

//function prototypes
void calibrateGyro(double & offsetx, double & offsety, double & offsetz);
void calibrateSerial();
void calibrateBLE();
void calibrateIMU();
void readIMU();
void updateIMUDataArr();

void setup() {
  calibrateSerial();
  
  calibrateIMU();

  calibrateBLE();
}

void loop() {

  // wait for a Bluetooth® Low Energy central
  BLEDevice central = BLE.central();

  // if a central is connected to the peripheral:
  if (central) {
    Serial.print("Connected to central: ");
    // print the central's BT address:
    Serial.println(central.address());
    // turn on the LED to indicate the connection:
    digitalWrite(LED_BUILTIN, HIGH);

    while (central.connected()) {
        
        readIMU(); //read IMU data and time
        updateIMUDataArr();
      
    }
    // when the central disconnects, turn off the LED:
    digitalWrite(LED_BUILTIN, LOW);
    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }
/*
  //print data to serial
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
 */
}

//PROTOTYPED FUNCTIONS BELOW

void updateIMUDataArr() {
  //Read and calculate data from IMU
  float newData[10] = {ax, ay, az, gx, gy, gz, mx, my, mz, (float)t}; 
  String newDataString = "";
  for(int i = 0; i < 9; i++){
    newDataString += String(newData[i],5) += ",";
  }
  newDataString += String(newData[9],5);
  IMUDataArr.writeValue(newDataString);  // and update the IMU characteristic
}

void readIMU() {
  //wait for all IMU readings 
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
  gx=gx-xOff;
  gy=gy-yOff;
  gz=gz-zOff;
}

void calibrateIMU() {
  //SETUP IMU
  if (IMU.begin() != 0) {
    Serial.println("Failed to initialize IMU!");
    while (1); //stall indefinitely if fail
  }

  uint8_t gModification;
  IMU.readRegister(&gModification, LSM6DS3_ACC_GYRO_CTRL1_XL);
  gModification = (gModification & 0xF3) | LSM6DS3_ACC_GYRO_FS_XL_16g;
  IMU.writeRegister(LSM6DS3_ACC_GYRO_CTRL1_XL, gModification);

  //Any module calibration here
  calibrateGyro(xOff,yOff,zOff);
    //--may need to add for accel/magno, flush out vals
  //print vars for serial
  Serial.println("t,aX,aY,aZ,gX,gY,gZ,mX,mY,mZ");  
}

void calibrateBLE() {
  //SETUP BLE COMM
  pinMode(LED_BUILTIN, OUTPUT); // initialize the built-in LED pin to indicate when a central is connected
  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");
    while (1); //stall indefinitely if fail
  }
    /* Set a local name for the Bluetooth® Low Energy device */
    BLE.setLocalName("IMUData");
    BLE.setAdvertisedService(IMUService); // add the service UUID
    IMUService.addCharacteristic(IMUDataArr); // add the characteristic
    BLE.addService(IMUService); // Add the service  

    //encode string w/ default values
    for(int i = 0; i < 9; i++){
    initialDataString += String(initialData[i],5) += ",";
    }
    initialDataString += String(initialData[9],5); //out of loop to prevent extra space
    IMUDataArr.writeValue(initialDataString); // set initial value for this characteristic
  
    // start advertising
    BLE.advertise();
  
    Serial.println("Bluetooth® device active, waiting for connections...");
}

void calibrateSerial() {
  //SETUP USB SERIAL COMM
  Serial.begin(9600);
  while (!Serial);
  Serial.println("Started");  
}

void calibrateGyro(float & offsetx, float & offsety, float & offsetz) {
  double sumX = 0;
  double sumY = 0;
  double sumZ = 0;
  //disregard the first 100 points, highly inaccurate!
  for(int i = 0; i < 100; i++) {
    float xd, yd, zd;
    xd = IMU.readFloatGyroX();
    yd = IMU.readFloatGyroY();
    zd = IMU.readFloatGyroZ();
  }
  for(int i = 0; i < 100; i++) {
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
