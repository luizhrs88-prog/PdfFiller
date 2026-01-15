package com.example.pdffiller;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FileMonitoringService extends Service {
    private static final String TAG = "FileMonitoringService";
    private MyFileObserver fileObserver;
    private String pathToMonitor = "/sdcard/Music/dirteste"; // Example path

    private Uri uriform;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        //Extra

        //getIntent().getStringExtra("select");
        fileObserver = new MyFileObserver(pathToMonitor, getApplicationContext());
        fileObserver.startWatching(); // Start monitoring
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY; // Or START_NOT_STICKY, depending on your needs
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        if (fileObserver != null) {
            fileObserver.stopWatching(); // Stop monitoring when the service is destroyed
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // This is a started service, not a bound service
    }
}