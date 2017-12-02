/* Copyright (C) 2017 Asher Blum */

package com.wildsparx.worldstein;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

/* for wifi */
import android.os.Handler;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.net.wifi.WifiManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import java.util.List;
import android.net.wifi.ScanResult;
import android.content.IntentFilter;

import android.util.Log;
import android.content.Context;

import org.json.*;


class BLESensor {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;
    private static int BLE_SCAN_INTERVAL_MS = 5 * 1000;

    private BluetoothAdapter mBluetoothAdapter;
    private static boolean mRunning;

    public BLESensor() {
      final BluetoothManager bluetoothManager =
        (BluetoothManager)MainActivity.INSTANCE.getSystemService(Context.BLUETOOTH_SERVICE);
      mBluetoothAdapter = bluetoothManager.getAdapter();
      mRunning = false;
    }

    public void start() {
      //test1();
      if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
        Log.e("asher", "bluetooth not enabled");
        return;
      }
      mRunning = true;
      mBluetoothAdapter.startLeScan(mLeScanCallback);
      Log.d("asher", "bluetooth started");
    }

    private void test1() {
      byte[] tv = { 0x23, 0x45, 0x67, (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef };
      String ss = binToHex(tv);
      Log.d("asherx", ss);
    }

    public void stop() {
      //test1();
      if(mBluetoothAdapter != null) {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
      }
      mRunning = false;
    }

    /* ***** END public API ***** */

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
      @Override
      public void onLeScan(final BluetoothDevice device, int rssi,
            byte[] scanRecord) {
        Log.d("asher", String.format("ble callback; rssi=%d", rssi));
        try {
          JSONObject jo = new JSONObject();
          jo.put("type", "ble");
          jo.put("rssi", rssi);
          jo.put("bin", rtrim(binToHex(scanRecord), '0'));
          jo.put("mac", device.getAddress());
          MainActivity.INSTANCE.writeSensorData(jo);
        } catch(JSONException e) {
          Log.e("asher", "BLESensor exception building JSONObject");
        }
      }
    };

    /* 0-15 -> '0'-'f' */
    private static char hexDigit(char i) {
      if(i < 10) {
        return (char)('0' + i);
      }
      return (char)((char)(i-10) + 'a');
    }

    private static String binToHex(byte[] bin) {
      StringBuilder sb = new StringBuilder();
      for(int i=0; i<bin.length; i++) {
        char hi = (char)((bin[i] >> 4) & 0xf);
        char lo = (char)(bin[i] & 0xf);
        sb.append(hexDigit(hi));
        sb.append(hexDigit(lo));
      }
      return sb.toString();
    }

    private static String rtrim(String s, char trimThis) {
      int i = s.length()-1;
      while (i > 0 && s.charAt(i) == trimThis) {
        i--;
      }
      return s.substring(0,i+1);
    }
}

