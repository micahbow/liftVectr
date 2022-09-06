package com.example.liftvectr.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsHandler {

    // Array of all permissions needed by the app
    private static final String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    public static void askForPermissions(Activity activity) {
        List<String> permissionsToAsk = new ArrayList<>();
        int requestResult = 0;

        // Check and record all non granted permissions
        Log.i("PERMISSIONSHANDLER","CHECKING FOR PERMISSIONS");
        for(String permission : PermissionsHandler.permissions) {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Record ungranted permission
                permissionsToAsk.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        // Ask for permissions
        Log.i("PERMISSIONSHANDLER","REQUESTING PERMISSIONS");
        if (permissionsToAsk.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionsToAsk.toArray(new String[permissionsToAsk.size()]), requestResult);
        }
    }
}
