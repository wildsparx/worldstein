/* Copyright (C) 2017 Asher Blum */

package com.wildsparx.worldstein;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.*;


public class MainActivity extends AppCompatActivity {
    static MainActivity INSTANCE;
    private WifiSensor mWifiSensor;
    private BLESensor mBLESensor;
    private int mNumLines;
    private boolean mScanning;
    private CounterText mWifiCounter;
    private CounterText mBLECounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setBackgroundColor();

        INSTANCE = this;
        mNumLines = 0;
        mScanning = false;
        mWifiCounter = new CounterText(R.id.wifi_ctr);
        mBLECounter = new CounterText(R.id.ble_ctr);

        DiskOps.openDatedOutFile();
        setBottomText(DiskOps.getLastPath());
        mWifiSensor = new WifiSensor();
        mBLESensor = new BLESensor();
        setStatusText("READY");

    }

    private void setStatusText(String s) {
      TextView tv = (TextView)findViewById(R.id.status_txt);
      tv.setText(s);
    }

    private void setBackgroundColor() {
      View someView = findViewById(R.id.et0);
      View root = someView.getRootView();
      //root.setBackgroundColor(Color.BLACK);
      //root.setBackgroundColor(0x0000ff);
      //root.setBackgroundColor(Color.HSVToColor(new float[]{ 60f, 0.5f, 0.25f } ));
      root.setBackgroundColor(Color.BLUE);
    }

    private void appendLine(String s) {
      long now = System.currentTimeMillis();
      String buf = String.format("%d %s", now, s);
      DiskOps.writeLine(buf);
      mNumLines ++;
    }

    private void setEt0(String val) {
      EditText et0 = (EditText)findViewById(R.id.et0);
      et0.setText(val);
    }

    private void setBottomText(String val) {
      TextView tv = (TextView)findViewById(R.id.bottomText);
      tv.setText(val);
    }

    private void sendEmail() {
      //String filename="contacts_sid.vcf"; 
      //File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), filename);
      File attachment = new File(DiskOps.getLastPath());
      Uri attachmentUri = Uri.fromFile(attachment); 
      Intent emailIntent = new Intent(Intent.ACTION_SEND);
      emailIntent.setType("vnd.android.cursor.dir/email");
      String to[] = {"asher@wildsparx.com"};
      emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
      emailIntent.putExtra(Intent.EXTRA_STREAM, attachmentUri);
      emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Worldstein data");
      startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Write a popup at bottom of screen */
    private void writeSnackbar(String s) {
      View view = findViewById(R.id.et0);
      Snackbar.make(view, s, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
    }

    /* Callback when user grants permission for wifi or ... */
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
             int[] grantResults) {
      mWifiSensor.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void incrementOneCounter(String ty) {
      if(ty.equals("wifi")) {
        mWifiCounter.increment();
      }
      if(ty.equals("ble")) {
        mBLECounter.increment();
      }
    }

    /* Called by sensor to append data to file */
    public void writeSensorData(JSONObject jo) {
      if(!mScanning) {
        return;
      }
      long now = System.currentTimeMillis();
      try {
        incrementOneCounter(jo.optString("type", ""));
        jo.put("ts", now);
      } catch(JSONException e) {
        Log.e("asher", "MainActivity: error adding ts to JSON");
      }
     
      DiskOps.writeLine(jo.toString());
    }

    public void writeMeta() {
      try {
        JSONObject jo = new JSONObject();
        jo.put("type", "meta");
        jo.put("mfg", Build.MANUFACTURER);
        jo.put("model", Build.MODEL);
        jo.put("brand", Build.BRAND);
        jo.put("product", Build.PRODUCT);
        jo.put("model", Build.MODEL);
        jo.put("sdk", Build.VERSION.SDK_INT);
        writeSensorData(jo);
      } catch(JSONException e) {
        Log.e("asher", "MainActivity: error building meta JSON");
      }
    }

    /* *********** Button Handlers ******** */

    public void onButton1(View v) {
      EditText et = (EditText)findViewById(R.id.et0);
      String s = et.getText().toString();
      try {
        JSONObject jo = new JSONObject();
        jo.put("type", "text");
        jo.put("val", s);
        writeSensorData(jo);
        et.setText("");
      } catch(JSONException e) {
        Log.e("asher", "error serializing text record");
      }
    }

    public void onButton2(View v) {
      mScanning = true;
      writeMeta();
      mWifiCounter.zeroize();
      mBLESensor.start();
      mWifiSensor.start();
      setStatusText("SCANNING");
    }

    public void onButton3(View v) {
      mWifiSensor.stop();
      mBLESensor.stop();
      mScanning = false;
      setStatusText("READY");
    }

    public void onButton4(View v) {
      sendEmail();
    }
    /* *********** /Button Handlers ******** */

}
