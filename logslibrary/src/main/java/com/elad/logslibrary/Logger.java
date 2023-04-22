package com.elad.logslibrary;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Logger {

    private static final String TAG = "LogsLibrary";
    private static final String LOG_FILE_NAME = "logs.txt";
    private static final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private static ContentResolver resolver = null;
    private static Uri sLogFileUri;

    public static void v(String message) {
        Log.v(TAG, message);
        saveLogToFile("VERBOSE", message);
    }

    public static void d(String message) {
        Log.d(TAG, message);
        saveLogToFile("DEBUG", message);
    }

    public static void i(String message) {
        Log.i(TAG, message);
        saveLogToFile("INFO", message);
    }

    public static void w(String message) {
        Log.w(TAG, message);
        saveLogToFile("WARN", message);
    }

    public static void e(String message) {
        Log.e(TAG, message);
        saveLogToFile("ERROR", message);
    }

    public static void setLogFile(Context context) {
        if (sLogFileUri == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // On Android 10 and above, we need to use MediaStore API to access the logs file
                resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, LOG_FILE_NAME);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Logs/");
                Uri externalUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                sLogFileUri = resolver.insert(externalUri, values);
            } else {
                // On Android 9 and below, we can use direct file access
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File logDir = new File(context.getExternalFilesDir(null), "Logs");
                    if (!logDir.exists()) {
                        if (!logDir.mkdirs()) {
                            Log.e(TAG, "Failed to create log directory");
                            return;
                        }
                    }
                    File logFile = new File(logDir, LOG_FILE_NAME);
                    sLogFileUri = Uri.fromFile(logFile);
                } else {
                    Log.e(TAG, "External storage not available");
                }
            }
        }
    }

    private static void saveLogToFile(String level, String message) {
        if (sLogFileUri != null) {
            Calendar calendar = Calendar.getInstance();
            String logTime = LOG_TIME_FORMAT.format(calendar.getTime());
            String logLine = String.format(Locale.getDefault(), "[%s] %s: %s%n", logTime, level, message);
            try {
                if (resolver != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // On Android 10 and above, we need to use an OutputStream to write to the log file
                        OutputStream outputStream = resolver.openOutputStream(sLogFileUri, "wa");
                        outputStream.write(logLine.getBytes());
                        outputStream.close();
                    } else {
                        // On Android 9 and below, we can use a FileWriter to write to the log file
                        FileWriter fileWriter = new FileWriter(sLogFileUri.getPath(), true);
                        fileWriter.write(logLine);
                        fileWriter.flush();
                        fileWriter.close();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to write log to file", e);
            }
        }
    }



    /**
     * Reads the logs from the log file and returns them as an ArrayList of strings.
     *
     * @param context The context of the app.
     * @return ArrayList of strings containing the logs from the log file, or null if there was an error reading the file.
     */
    public static ArrayList<String> readLogsFromFile(Context context) {
        if (sLogFileUri == null) {
            return null; // log file URI is not set
        }

        ArrayList<String> logs = new ArrayList<>();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(sLogFileUri);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logs.add(line);
            }

            bufferedReader.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            return null; // error reading the file
        }

        return logs;
    }

    /**
     * Returns the directory where the log file should be stored.
     *
     * @param context The context of the app.
     * @return The directory where the log file should be stored.
     */
    private static File getLogFileDirectory(Context context) {
        File logDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // External storage is available
            logDir = new File(context.getExternalFilesDir(null), "Logs");
        } else {
            // External storage is not available, use internal storage
            logDir = new File(context.getFilesDir(), "Logs");
        }

        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        return logDir;
    }
}

