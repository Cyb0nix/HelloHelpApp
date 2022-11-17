package com.cybonix.hellohelp;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;



public class Startup extends Application {

    final String TAG = "Startup";
    @Override
    public void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Log.e(TAG, "database");
    }
}