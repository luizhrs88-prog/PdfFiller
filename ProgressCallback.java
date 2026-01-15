package com.example.pdffiller;

public interface ProgressCallback {
    void onProgressUpdate(int progress);
    void onComplete(String result);
    void onStartthread(int minv, int maxv);
   // void onError(Exception e);
}
