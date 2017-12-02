/* Copyright (C) 2017 Asher Blum */

package com.wildsparx.worldstein;

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


class WifiSensor {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;
    private static int WIFI_SCAN_INTERVAL_MS = 5 * 1000;
    private static Handler mWifiScanHandler;
    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;
    private static boolean mRunning;

    public WifiSensor() {
      mWifiScanHandler = new Handler();
      mRunning = false;
    }

    public void start() {
      mRunning = true;
      mWifiScanHandler.postDelayed(mWifiScanRunnable, 10);
    }

    public void stop() {
      mRunning = false;
    }

    private Runnable mWifiScanRunnable = new Runnable() {
       @Override
       public void run() {
          if(!mRunning) {
            return;
          }
          mWifiScanHandler.postDelayed(this, WIFI_SCAN_INTERVAL_MS);
          tryWifiStart();
       }
    };

    private void wifiStart() {
      // getApplicationContext().getSystemService
      Log.d("asher", "wifiStart");
      mWifiManager = (WifiManager) MainActivity.INSTANCE.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
      mWifiManager.setWifiEnabled(true);
      mWifiReceiver = new WifiReceiver();
      MainActivity.INSTANCE.registerReceiver(mWifiReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
      mWifiManager.startScan();
    }

    private void tryWifiStart() {
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
         MainActivity.INSTANCE.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        MainActivity.INSTANCE.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                 PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        return; // wait for callback
      }

      /* If we don't need permission: */
      wifiStart();
    }

    /* Callback when user grants permission for wifi */

    //@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
             int[] grantResults) {
      if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                       && grantResults.length > 0
                       && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.d("asher", "onRequestPermissionsResult OK");
        wifiStart();
      }
      else {
        Log.d("asher", String.format("onRequestPermissionsResult requestCode=%d  permissions.len=%d  grantResults.len=%d", requestCode, permissions.length, grantResults.length));
      }
    }




    class WifiReceiver extends BroadcastReceiver
    {
        public void onReceive(Context c, Intent intent)
        {
            List<ScanResult> scanResults = mWifiManager.getScanResults();
            Log.d("asher", String.format("onReceive: %d", scanResults.size()));
            JSONArray jScanResults = new JSONArray();
            try { 
              for(int i=0; i<scanResults.size(); i++) {
                Log.d("asher", "scanResult: " + scanResults.get(i).SSID);
                JSONObject jo = new JSONObject();
                /* keeping the Android names here, although some are bad */
                jo.put("ssid", scanResults.get(i).SSID);
                jo.put("bssid", scanResults.get(i).BSSID);
                jo.put("level", scanResults.get(i).level);
                jScanResults.put(jo);
              }
              JSONObject mjo = new JSONObject();
              mjo.put("type", "wifi");
              mjo.put("scan_results", jScanResults);
              MainActivity.INSTANCE.writeSensorData(mjo);
            } catch(JSONException e) {
              Log.e("asher", "WifiSensor exception building JSONObject");
            }
        }
    }
    
}
