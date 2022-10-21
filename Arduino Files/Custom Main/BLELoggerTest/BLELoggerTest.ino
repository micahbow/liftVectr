#include "LSM6DS3.h"
#include "Wire.h"
#include <ArduinoBLE.h>

//Create a instance of class LSM6DS3
LSM6DS3 IMU(I2C_MODE, 0x6A);    //I2C device address 0x6A

//Global BLE vars
BLEService IMUService("181C"); //BLE SERVICE "0x181C" = User Data
BLEStringCharacteristic IMUDataArr("2ADA",BLERead | BLENotify | BLEWrite, 100); //BLE CHARACTERISTIC "0x2ADA" = Fitness Machine Status

//Global Data vars
float ax = 0.0,ay = 0.0,az = 0.0,gx = 0.0,gy = 0.0,gz = 0.0,xOff = 0.0,yOff = 0.0,zOff = 0.0;
unsigned long t;
float initialData[7] = {ax, ay, az, gx, gy, gz, (float)t};
String initialDataString = "";
unsigned long offset;
unsigned long lastMillis = 0;
unsigned long lastBuzz = 0;
bool flashRed = false;
bool needToBuzz = false;
int buzzDelay = 5000;

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

  buzz(3, 100);

  setRed();
}

void loop() {

  // wait for a Bluetooth® Low Energy central
  BLEDevice central = BLE.central();

  // if a central is connected to the peripheral:
  if (central) {
    setGreen();
    Serial.print("Connected to central: ");
    // print the central's BT address:
    Serial.println(central.address());
    offset = millis();

    buzz(2, 100);

    while (central.connected()) {
      if(IMUDataArr.written()){
        buzzDelay = IMUDataArr.value().toInt() * 1000;
        lastBuzz = millis();
        needToBuzz = true;
      }
      if(needToBuzz){
        unsigned long currentTime = millis();
        if(currentTime - lastBuzz >= buzzDelay){
          needToBuzz = false;
          buzz(3,250);
        }
      }
      readIMU(); //read IMU data and time
      updateIMUDataArr();
      
    }
    Serial.print("Disconnected from central: ");
    Serial.println(central.address());

    buzz(2, 100);
  }
  else{
    flashBlueRed();
  }
}

//PROTOTYPED FUNCTIONS BELOW

void updateIMUDataArr() {
  //Read and calculate data from IMU
  float newData[7] = {ax, ay, az, gx, gy, gz, (float)(t-offset)};
  String newDataString[4] = {String(newData[0],5) + "," + String(newData[1],5),
                             String(newData[2],5) + "," + String(newData[3],3),
                             String(newData[4],3) + "," + String(newData[5],3),
                             String(newData[6], 1)};
  for(int i = 0; i < 4; i++){
    IMUDataArr.writeValue(newDataString[i]);
    Serial.println(IMUDataArr.value());
    delay(25);
  }
}

void readIMU() {
  //wait for all IMU readings 
  uint8_t gyroscopeAvailable;
  uint8_t accelerometerAvailable;
  IMU.readRegister(&gyroscopeAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
  IMU.readRegister(&accelerometerAvailable, LSM6DS3_ACC_GYRO_STATUS_REG);
  while((gyroscopeAvailable & 0x02) == 0x00 || (accelerometerAvailable & 0x01) == 0x00) {}
  t=millis();
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
  Serial.println("t,aX,aY,aZ,gX,gY,gZ");
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
    initialDataString = String(initialData[0],5) + "," + String(initialData[1],5);
    IMUDataArr.writeValue(initialDataString); // set initial value for this characteristic
    Serial.println(IMUDataArr.value());

    // start advertising
    BLE.advertise();
  
    Serial.println("Bluetooth® device active, waiting for connections...");
}

void buzz(int times, int delayms){
  pinMode(10, OUTPUT);
  for(int i = 0; i < times; i++){
      digitalWrite(10, HIGH);
      delay(delayms);
      digitalWrite(10, LOW);
      delay(delayms);
  }
}

void flashBlueRed(){
  unsigned long currentTime = millis();
  if(currentTime - lastMillis >= 250){
    lastMillis = currentTime;
    if(flashRed){
      setRed();
      flashRed = false;
    }
    else{
      setBlue();
      flashRed = true;
    }
  }
}

void setRed(){
  digitalWrite(LEDG, HIGH);
  digitalWrite(LEDB, HIGH);
  digitalWrite(LEDR, LOW);
}

void setGreen(){
  digitalWrite(LEDR, HIGH);
  digitalWrite(LEDB, HIGH);
  digitalWrite(LEDG, LOW);
}

void setBlue(){
  digitalWrite(LEDR, HIGH);
  digitalWrite(LEDG, HIGH);
  digitalWrite(LEDB, LOW);
}


void calibrateSerial() {
  //SETUP USB SERIAL COMM
  Serial.begin(9600);
  //while (!Serial);
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
