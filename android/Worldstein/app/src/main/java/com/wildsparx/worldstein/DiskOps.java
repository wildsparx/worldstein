/* Copyright (C) 2017 Asher Blum */

package com.wildsparx.worldstein;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;



class DiskOps {
  private static OutputStream XOS;
  private static BufferedOutputStream BOS;
  private static String PATH;
  public static boolean AUTOFLUSH = true;

  /* Get full path of last file written */

  public static String getLastPath() {
    return PATH;
  }

  /* Write bytes to file in one shot; file will be in our directory */

  public static void saveBytesToFile(String filename, byte[] data) {
    Context ctx = MainActivity.INSTANCE.getApplicationContext();
    File file = new File(ctx.getExternalFilesDir(null), filename);
    try {
      OutputStream os = new FileOutputStream(file);
      os.write(data);
      os.close();
      PATH = file.toString();
    }
    catch(IOException e) {
      Log.e("asher", "saveBytesToFile: IOException");
    }
  }

  private static String getCurrentDate() {
    long now = System.currentTimeMillis();
    Date myDate = new Date(now);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    return simpleDateFormat.format(myDate);
  }

  /* Return just a filename, not path, e.g. "2017-01-01-44" */

  private static String getUnusedDateFileName(String currentDate) {
    Context ctx = MainActivity.INSTANCE.getApplicationContext();
    for(int i=0; i<1000; i++) {
      String filename = String.format("%s-%d", currentDate, i);
      File file = new File(ctx.getExternalFilesDir(null), filename);
      if(!file.exists()) {
        return filename;
      }
    }
    return null;
  }

  /* Open an outfile using today's date + integer suffix; don't step on existing files */

  public static void openDatedOutFile() {
    String filename = getUnusedDateFileName(getCurrentDate());
    openOutFile(filename);
  }

  /* Open an outfile in our directory with the given filename */

  public static void openOutFile(String filename) {
    Context ctx = MainActivity.INSTANCE.getApplicationContext();
    File file = new File(ctx.getExternalFilesDir(null), filename);
    try {
      XOS = new FileOutputStream(file);
      BOS = new BufferedOutputStream(XOS);
      PATH = file.toString();
    }
    catch(IOException e) {
      Log.e("asher", "saveBytesToFile: IOException");
    }
  }

  /* Write a line to the existing outfile */

  public static void writeLine(String line) {
    try {
      BOS.write(line.getBytes());
      BOS.write(10);
      if(AUTOFLUSH) {
        BOS.flush();
      } 
    } catch(IOException e) {
      Log.e("asher", "writeLine: IOException");
    }
  }

  public static void closeOutFile() {
    try {
      BOS.flush();
      XOS.close();
    } catch(IOException e) {
      Log.e("asher", "closeOutFile: IOException");
    }
    BOS = null;
    XOS = null;
  }
}
