# Tip: Do python development in a separate IDE that has an interpreter
# No need to configure a python interpreter in Android Studio for the script to work
import json
import numpy as np

# Fake function that fills IMUData acceleration with random values
# Demos json input/output conversions and use of an external module (numpy)
def imu_data_to_position(imuDataJson):
    imu_data = json.loads(imuDataJson)
    for point in imu_data:
        point["x_lin_acc"] = np.random.randint(10)
        point["y_lin_acc"] = np.random.randint(10)
        point["z_lin_acc"] = np.random.randint(10)
    return json.dumps(imu_data)

# Demos that we can call multiple functions from the same python file
def hello():
    print("Hello from Python!!");