package com.elad.logslibraryapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.TextView;

import com.elad.logslibrary.Logger;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView main_LBL_logs;
    private final ActivityResultLauncher<String[]> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (checkIfAllPermissionsGranted(result)) {
                    runTest();
                } else {
                    requestPermissionsWithRationaleCheck();
                }
            }
    );
    private final ActivityResultLauncher<Intent> requestManageExternalStoragePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        runTest();
                    } else {
                        requestPermissionsWithRationaleCheck();
                    }
                }
    });

    private boolean checkIfAllPermissionsGranted(Map<String, Boolean> result) {
        for (Boolean value : result.values()) {
            if (!value)
                return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        requestStoragePermission();
    }

    private void findViews() {
        main_LBL_logs = findViewById(R.id.main_LBL_logs);
    }

    private void runTest() {
        Logger.setLogFile(this);

        Logger.v("Test V");
        Logger.d("Test D");
        Logger.i("Test I");
        Logger.w("Test W");
        Logger.e("Test E");

        ArrayList<String> readRes = Logger.readLogsFromFile(this);
        if (readRes != null) {
            for (String readRe : readRes) {
                String text = main_LBL_logs.getText().toString() + readRe + "\n";
                main_LBL_logs.setText(text);
            }
        }
    }

    private void requestStoragePermission() {
        // Check if the device is running on Android 11 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Request MANAGE_EXTERNAL_STORAGE permission
            if (!Environment.isExternalStorageManager()) {
                requestManageExternalStoragePermission();
            } else {
                runTest();
            }
        } else {
            // Request READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                resultLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE});
            } else {
                runTest();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestManageExternalStoragePermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        requestManageExternalStoragePermissionLauncher.launch(intent);
    }

    private void showDialog(String title, String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel, cancelListener);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void requestPermissionsWithRationaleCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            boolean shouldShowPermissionRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            if (shouldShowPermissionRationale) {
                requestManageExternalStoragePermission();
            } else {
                showDialog(
                        "Permission Required",
                        "Manage External Storage permission required for logs library usage",
                        (dialog, which) -> { // ok
                            resultLauncher.launch(new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE});
                        },
                        (dialog, which) -> { // cancel
                            openAppPermissionsSettings();
                        });
            }
        } else {
            boolean shouldShowPermissionsRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (shouldShowPermissionsRationale) {
                resultLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE});
            } else {
                showDialog(
                        "Permissions Required",
                        "Write & Read External Storage permissions required for logs library usage",
                        (dialog, which) -> { // ok
                            resultLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE});
                        },
                        (dialog, which) -> { // cancel
                            openAppPermissionsSettings();
                        });
            }
        }
    }

    private void openAppPermissionsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}