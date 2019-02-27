package com.domain.no.wordclock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;

import java.util.UUID;

public class BluetoothConnectivity {
    private static BluetoothAdapter bluetoothAdapter = null;
    private static BluetoothSocket bluetoothSocket = null;
    private static BluetoothManager bluetoothManager = null;
    private static BluetoothLeScanner bluetoothScanner = null;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothLeScanner getBluetoothScanner() {
        return bluetoothScanner;
    }

    public void setBluetoothScanner(BluetoothLeScanner bluetoothScanner) {
        BluetoothConnectivity.bluetoothScanner = bluetoothScanner;
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        BluetoothConnectivity.bluetoothManager = bluetoothManager;
    }


    public void setBluetoothAdapter( BluetoothAdapter bt ){ bluetoothAdapter = bt; }

    public BluetoothAdapter getBluetoothAdapter(){
        return bluetoothAdapter;
    }

    public void setBluetoothSocket( BluetoothSocket ba ){
        bluetoothSocket = ba;
    }

    public BluetoothSocket getBluetoothSocket(){ return bluetoothSocket; }

    public static UUID getUUID() {
        return uuid;
    }

}
