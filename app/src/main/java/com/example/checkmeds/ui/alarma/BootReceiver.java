package com.example.checkmeds.ui.alarma;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BOOT", "Dispositivo reiniciado");

            // Reprogramar todas las alarmas necesarias


        }
    }
}

