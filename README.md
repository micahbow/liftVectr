# liftVectr
"A wristwatch made for lifters, by lifters."

liftVectr is a wrist device and mobile app pairing that provides motion measurement, analysis, and tracking tools for "one-rep max" powerlifting style lifts.

## Installation

Create a folder on your local computer for the project. Navigate to that directory within a terminal and clone the repository.

```bash
git clone https://github.com/micahbow/liftVectr.git
```
Install [Android Studio](https://developer.android.com/studio) and the [Arduino IDE](https://docs.arduino.cc/software/ide-v1).

## Usage

### Android Application

1. Open liftVectr's "application" directory within Android Studio.
2. Wait ~5 minutes for Gradle dependencies to load. If prompted, allow Android Studio to resolve any missing dependencies. 
3. At this point in time, a green Android icon labeled "app" should be visible in the toolbar. Press the green hammer icon to build the project. The application should build without any errors. *If experiencing a Gradle JDK error, navigate to (File->Settings->Build, Execution, Deployment->Build Tools->Gradle) and make sure the Gradle JDK is 11.*
4. To run the app on an emulator, first download an Android Virtual Device by selecting the device dropdown to the right of the green "app" button -> AVD Manager -> Create Virtual Device, and then following the prompts. *We recommend choosing older devices such as the Nexus 4 API 30, as they generally have lower RAM/storage requirements.* To run the app on a physical Android device (recommended to use bluetooth functionality), connect the desired device via USB to the computer. Ensure that the device has "Developer Mode" enabled, a process which differs by device. A dialog box requesting permissions for "remote debugging" is displayed - allow this for Android Studio to recognize the device. The device should now appear within the list of emulators and devices alongside the green play button.
5. Once the AVD is installed, select it in the devices dropdown, and press the play button. The app should start itself up and run properly within the emulator. *If the emulator starts up and freezes, or crashes on boot, it is likely that there is not enough currently available RAM on your computer for the emulation process.*

### Arduino Project

1. Open BLELoggerTest.ino within the Arduino IDE.
2. Install the ArduinoBLE library in Tools->Library Manager->Search:"ArduinoBLE" and click install for the latest version.
3. Install the Arduino Mbed OS Nano Boards package in Tools->Board->Boards Manager->Search: "Arduino Mbed OS Nano" and click install.
4. Press the Upload (arrow) button to compile and upload to the board, ensuring it is plugged in and recognized under the correct COM port in Tools->Port
5. Restart the board by pressing the on-board reset button once.
6. Start the main program under debug mode by opening the Serial Terminal in Tools->Serial Monitor. The device is now advertising to pair.

## Project Specification

The liftVectr project includes a hardware portion, consisting of the IMU and Bluetooth Low Energy (BLE) functionalities within a microcontroller chip, and a software portion which consists of an Android app.

### Hardware Overview

The microcontroller used for the project originally was the Arduino Nano 33 BLE, but it is in the process of being replaced with the Seeed Studio XIAO nRF52840 Sense, which has a much smaller chip size than the Arduino. Regardless of hardware chip, the hardware functions involve idling until paired to a BLE receiver, and continually reading the IMU and publishing the measurements (X,Y,Z dimensions of gyroscope in degrees/second and accelerometer in gs/second) while connected. Case housing for the chip and other hardware features such as rechargeable battery will be implemented for the Beta build when the parts arrive and a means for 3D printing is secured.

### Software Overview

The Android app provides a visually flowing and aesthetic multi-page UI (EXTERNAL INTERFACE) for recording a new exercise session, listing previously recorded exercise sessions, and displaying selected exercise sessions with charted data. A simple two-option NavBar allows users intuitive ease of access with the application's features. The backend functions involve handling and recording communications with the hardware over BLE (EXTERNAL INTERFACE), reading and writing exercise data with a local Room database (PERSISTENCE), and performing processing and chart display of exercise data (INTERNAL SYSTEMS).

### Test Plans

A test plan document in the repository outlines key features present for the liftVectr Alpha build, and includes plans and provisions in place to test for acceptable functionality with respect to user interactions and visuals, data recording consistency and reliability, graceful error handling for exceptional user use cases, and accurate measurement and polling from the hardware to the software devices - and IMU + Bluetooth Low Energy (BLE) equipped microcontroller chip, and an Android app.

