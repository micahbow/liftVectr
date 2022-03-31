/*
  The circuit:
    Arduino Nano 33 BLE, or Arduino Nano 33 BLE Sense board.

  You can use a generic Bluetooth® Low Energy central app, like LightBlue (iOS and Android) or
  nRF Connect (Android), to interact with the services and characteristics
  created in this sketch.

  This example code is in the public domain.
*/

#include <ArduinoBLE.h>

 // Bluetooth® Low Energy IMU Service
BLEService IMUService("180F");

// Bluetooth® Low Energy Battery Level Characteristic
BLEStringCharacteristic IMUDataArr("2A19",  // standard 16-bit characteristic UUID
    BLERead | BLENotify, 100); // remote clients will be able to get notifications if this characteristic changes

float xPos = 0.0, yPos = 0.0, zPos = 0.0, xVel = 0.0, yVel = 0.0, zVel = 0.0, cTime = 0.0;
float initialData[7] = {xPos, yPos, zPos, xVel, yVel, zVel, cTime}; 
String initialDataString = "";

void setup() {
  Serial.begin(9600);    // initialize serial communication
  while (!Serial);

  pinMode(LED_BUILTIN, OUTPUT); // initialize the built-in LED pin to indicate when a central is connected

  // begin initialization
  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");

    while (1);
  }

  /* Set a local name for the Bluetooth® Low Energy device
     This name will appear in advertising packets
     and can be used by remote devices to identify this Bluetooth® Low Energy device
     The name can be changed but maybe be truncated based on space left in advertisement packet
  */
  BLE.setLocalName("IMUData");
  BLE.setAdvertisedService(IMUService); // add the service UUID
  IMUService.addCharacteristic(IMUDataArr); // add the battery level characteristic
  
  BLE.addService(IMUService); // Add the battery service

  for(int i = 0; i < 6; i++){
  initialDataString += String(initialData[i],1) += " ";
  }
  initialDataString += String(initialData[6],1);

  IMUDataArr.writeValue(initialDataString); // set initial value for this characteristic

  /* Start advertising Bluetooth® Low Energy.  It will start continuously transmitting Bluetooth® Low Energy
     advertising packets and will be visible to remote Bluetooth® Low Energy central devices
     until it receives a new connection */

  // start advertising
  BLE.advertise();

  Serial.println("Bluetooth® device active, waiting for connections...");
}

void updateIMUDataArr() {
  //Read and calculate data from IMU
  xPos = 0.0, yPos = 0.0, zPos = 0.0, xVel = 0.0, yVel = 0.0, zVel = 0.0, cTime = 0.0;
  float newData[7] = {xPos, yPos, zPos, xVel, yVel, zVel, cTime}; 
  String newDataString = "";
  for(int i = 0; i < 6; i++){
    newDataString += String(newData[i],1) += " ";
  }
  newDataString += String(newData[6],1);
  IMUDataArr.writeValue(newDataString);  // and update the IMU characteristic
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
        updateIMUDataArr();
      
    }
    // when the central disconnects, turn off the LED:
    digitalWrite(LED_BUILTIN, LOW);
    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }
}
