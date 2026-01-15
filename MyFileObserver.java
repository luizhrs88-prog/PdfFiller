package com.example.pdffiller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.File;
import java.io.FileOutputStream;

import static android.support.v4.app.ActivityCompat.requestPermissions;

public class MyFileObserver extends FileObserver {
//    private static final String pathdir = "/sdcard/Music/dirteste/";
    private static final String pathdir = "";

    private static final String pdfform = "content://com.android.providers.media.documents/document/document%3A1000051128";
    private static final String outputpdf = "filled.pdf";

    private static final String TAG = "MyFileObserver";

    private Context ac;

    public MyFileObserver(String path, Context nc) {
        super(path, ALL_EVENTS); // Monitor all events
        ac = nc;
    }

    @Override
    public void onEvent(int event, String path) {
        switch (event) {
            case FileObserver.ACCESS:
                Log.d(TAG, "File accessed: " + path);
                break;

            case FileObserver.MODIFY:
                Log.d(TAG, "File modified: " + path);


            case FileObserver.CLOSE_WRITE:
                Log.d(TAG, "File terminou de escrever: " + path);



                break;
            case FileObserver.CREATE:
                Log.d(TAG, "File created: " + path);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (path.endsWith(".xls")){

                    Intent intent = new Intent("com.example.ACTION_SERVICE_MESSAGE");
                    intent.putExtra("message", "Data from Service!");
                    intent.putExtra("path", path);
                    Log.i("DEBUG", "File chamando broadcastmanager: " + path);

                    //  LocalBroadcastManager( sendBroadcast(intent);
                    LocalBroadcastManager.getInstance(ac).sendBroadcast(intent);
                    //  LocalBroadcastManager.getInstance()
                    //   LocalBroadcastManager.getInstance( ).sendBroadcast(intent);
                    //sendBroadcast
                    //Intent ni = sendBroadcast(new Intent(RefreshTask.REFRESH_DATA_INTENT));
                }

                break;
            case FileObserver.DELETE:
                Log.d(TAG, "File deleted: " + path);
                break;
            // Add more cases for other events as needed

            case FileObserver.CLOSE_WRITE | FileObserver.CREATE:
                Log.d(TAG, "File created and close write " + path);


            default:
                Log.d(TAG, "Unknown event " + event + " for file: " + path);
                break;
        }
    }
}