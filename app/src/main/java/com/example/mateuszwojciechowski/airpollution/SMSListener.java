package com.example.mateuszwojciechowski.airpollution;

import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import PeripheralDisplayLibrary.Display;

public class SMSListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //jeśli przyjdzie SMS - testowo jeśli wyłączy się ekran
        if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Display display = new Display(context, (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE));
            display.connect();
            display.pulse(0);
            display.disconnect();
        }
    }
}
