# Commented out IPython magic to ensure Python compatibility.
# Import libraries
import json
import numpy as np
import pandas as pd
import math
import ahrs
from ahrs.filters import Madgwick

def euler_from_quaternion(x, y, z, w):
        """
        Convert a quaternion into euler angles (roll, pitch, yaw)
        roll is rotation around x in radians (counterclockwise)
        pitch is rotation around y in radians (counterclockwise)
        yaw is rotation around z in radians (counterclockwise)
        """
        t0 = +2.0 * (w * x + y * z)
        t1 = +1.0 - 2.0 * (x * x + y * y)
        roll_x = math.atan2(t0, t1)

        t2 = +2.0 * (w * y - z * x)
        t2 = +1.0 if t2 > +1.0 else t2
        t2 = -1.0 if t2 < -1.0 else t2
        pitch_y = math.asin(t2)

        t3 = +2.0 * (w * z + x * y)
        t4 = +1.0 - 2.0 * (y * y + z * z)
        yaw_z = math.atan2(t3, t4)

        return roll_x, pitch_y, yaw_z # in radians

def process_imu_data(imuDataJson):
    # Load IMUData as JSON
    imu_data = json.loads(imuDataJson)

    df = pd.DataFrame(columns=['t', 'aX', 'aY', 'aZ', 'gX', 'gY', 'gZ'])
    for point in imu_data:
        df.loc[len(df.index)] = [point["micros"], point["x_lin_acc"], point["y_lin_acc"], point["z_lin_acc"],
                                 point["x_ang_vel"], point["y_ang_vel"], point["z_ang_vel"]]

    df['t'] = (df['t'] - df['t'][0])/1000 #start t at 0, convert to s

    df['dt'] = 0

    #Calculate dt, get radian angles, convert g to m/s^2
    for i in range(1,df.shape[0]):
        df.loc[i,'dt'] = (df['t'][i])-(df['t'][i-1])
    for i in range(df.shape[0]):
        df.loc[i,'gXr'] = np.deg2rad(df['gX'][i])
        df.loc[i,'gYr'] = np.deg2rad(df['gY'][i])
        df.loc[i,'gZr'] = np.deg2rad(df['gZ'][i])
        df.loc[i,'aX'] = df.loc[i,'aX']*9.81
        df.loc[i,'aY'] = df.loc[i,'aY']*9.81
        df.loc[i,'aZ'] = df.loc[i,'aZ']*9.81
    avg_dt = np.average(df['dt'].to_numpy())
    print("Average dt = ", avg_dt)

    notStill = 0.0  # Flag if we want to let user know calculations may not be accurate

    # Validate first data sample is stationary for calibration of global axes
    magnitude = np.sqrt(df.loc[0]['aX']**2 + df.loc[0]['aY']**2 + df.loc[0]['aZ']**2)
    if (magnitude > 9.85 or magnitude < 9.76):
      print("Must stay still at beginning of data collection! Measured force: ", magnitude)
      notStill = 1.0
    else:
      print("Data calibration valid, gravitational force measured at: ", magnitude)

    # Find initial angles with respect to world axes. Z = up, XY horizontal plane. Assuming gravity is a positive force updards on global Z
    #initial_roll_x = np.arccos(df.loc[0]['aZ']/magnitude);
    initial_roll_x = np.arctan2(df.loc[0]['aY'],df.loc[0]['aZ']);
    #initial_pitch_y = np.arccos(df.loc[0]['aX']/magnitude);
    initial_pitch_y = np.arcsin(df.loc[0]['aZ']/magnitude);
    #initial_yaw_z = np.arccos(df.loc[0]['aY']/magnitude);
    initial_yaw_z = np.arctan2(df.loc[0]['aY'],df.loc[0]['aX']);

    # Initial Case: If the Z axis is facing up, we should read XYZ accleration as [0,0,1g]
    # This results in 0 degree roll, 90 degree pitch, 90 degree yaw.
    # 90 degree pitch up from horizontal plane to orient up towards Z
    # yaw doesn't matter here

    # Roll is rotation along X axis, Pitch along Y axis; 0 degrees is parallel to horizontal
    # Yaw is rotation along Z axis
    print("Degree RPY alignment: ",np.rad2deg([initial_roll_x,initial_pitch_y,initial_yaw_z]))

    num_samples = df.shape[0]
    madgwick = Madgwick()
    Q = np.zeros((num_samples, 4))      # Allocation of quaternions
    Q[0] = [1.0, 0.0, 0.0, 0.0]         # Initial attitude as a quaternion
    for t in range(1, num_samples):
      madgwick.Dt = df.loc[t]['dt']
      Q[t] = madgwick.updateIMU(Q[t-1], gyr=df.loc[t][['gXr','gYr','gZr']].to_numpy(), acc=df.loc[t][['aX','aY','aZ']].to_numpy())

    xAngles = []
    yAngles = []
    zAngles = []

    for i in range(df.shape[0]):
      euler = euler_from_quaternion(Q[i][1],Q[i][2],Q[i][3],Q[i][0])
      xAngles += [initial_roll_x + euler[0]]
      yAngles += [initial_pitch_y + euler[1]]
      zAngles += [initial_yaw_z + euler[2]]

    #Convert Vertical Motion (Along Y axis)
    xAccGlobal = []
    yAccGlobal = []
    zAccGlobal = []

    Local_xAccs = []
    Local_yAccs = []
    Local_zAccs = []
    for i in range(df.shape[0]):
      #TODO: VALIDATE REMOVAL OF GRAVITY
      #xAccLocal = df['aX'][i] - 9.81*np.cos(yAngles[i])*np.cos(zAngles[i]);
      xAccLocal=0;
      yAccLocal = df['aY'][i] - 9.81*np.sin(xAngles[i])
      zAccLocal = df['aZ'][i] - 9.81*np.cos(xAngles[i]);

      #Rounding
      if(np.abs(xAccLocal)<1): xAccLocal = 0
      if(np.abs(yAccLocal)<1): yAccLocal = 0
      if(np.abs(zAccLocal)<1): zAccLocal = 0

      #Store local axes values for debugging
      Local_xAccs += [xAccLocal]
      Local_yAccs += [yAccLocal]
      Local_zAccs += [zAccLocal]

    #Calculate Vertical Motion
    vertVel = [0]
    for i in range(1,df.shape[0]):
        vertVel += [vertVel[i-1] + ((Local_yAccs[i]*np.sin(xAngles[i])) + (Local_zAccs[i]*np.cos(xAngles[i]))) * df['dt'][i]]

    #Calculate Vertical Displacement
    vertPos = [0]
    for i in range(1,df.shape[0]):
        vertPos += [vertPos[i-1] + vertVel[i-1] * df['dt'][i]]

    #Calculate Horizontal Motion
    horzVel = [0]
    for i in range(1,df.shape[0]):
        horzVel += [horzVel[i-1] + ((Local_yAccs[i]*np.cos(xAngles[i])) + (Local_zAccs[i]*np.sin(xAngles[i]))) * df['dt'][i]]

    #Calculate Saggital Displacement
    horzPos = [0]
    for i in range(1,df.shape[0]):
        horzPos += [horzPos[i-1] + horzVel[i-1] * df['dt'][i]]

    #Experiment
    #Plot bulk adjustment
    bulkAccs = np.sqrt(df['aX']**2 + df['aY']**2 + df['aZ']**2)-9.81

    #Plot bulk vertical velocity
    bulkVel = [0]
    for i in range(1,df.shape[0]):
        bulkVel += [bulkVel[i-1] + (bulkAccs[i]) * df['dt'][i]]

    #Calculate Bulk Displacement
    bulkPos = [0]
    for i in range(1,df.shape[0]):
        bulkPos += [bulkPos[i-1] + bulkVel[i-1] * df['dt'][i]]

    posDeviation = np.abs(np.array(bulkPos)-np.array(vertPos))

    bigList = [xAngles, yAngles, zAngles, vertVel, horzVel, bulkVel, vertPos, horzPos, bulkPos, posDeviation.tolist(), df['t'].tolist(), [0], [0], [notStill]]

    return json.dumps(bigList)

