# liftVectr

liftVectr is a wrist device and mobile app pairing that provides measurement, analysis, and tracking tools to weightlifters.

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
4. To run the app on an emulator, first download an Android Virtual Device by selecting the device dropdown to the right of the green "app" button -> AVD Manager -> Create Virtual Device, and then following the prompts. *We recommend choosing older devices such as the Nexus 4 API 30, as they generally have lower RAM/storage requirements.*
5. Once the AVD is installed, select it in the devices dropdown, and press the play button. The app should start itself up and run properly within the emulator. *If the emulator starts up and freezes, or crashes on boot, it is likely that there is not enough currently available RAM on your computer for the emulation process.*
