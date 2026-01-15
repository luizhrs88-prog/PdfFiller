package com.example.pdffiller;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class MyForegroundWatchService extends Service {

    private WatchService watchService;
    private Thread watchThread;
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "hello";


    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize WatchService here if needed
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Build and display the persistent notification

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("File Watcher Service")
                .setContentText("Monitoring file changes...")
                .setSmallIcon(R.drawable.ic_notification)
                .build();



        // Start as foreground service
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC); // Android 10+
/*
        // Start WatchService in a separate thread
        watchThread = new Thread(() -> {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                Path dir = Paths.get("/sdcard/Download"); // Example directory
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Log.d("WatchService", "Event kind: " + event.kind() + ". File affected: " + event.context());
                        // Process the event (e.g., update UI, perform data sync)
                    }
                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                Log.e("WatchService", "Error in WatchService thread", e);
            }
        });
        watchThread.start();
*/
        return START_STICKY; // Service restarts if killed by system
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                Log.e("WatchService", "Error closing WatchService", e);
            }
        }
        if (watchThread != null && watchThread.isAlive()) {
            watchThread.interrupt();
        }
        stopForeground(true); // Remove foreground status and notification
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}