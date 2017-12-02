/* Copyright (C) 2017 Asher Blum */

package com.wildsparx.worldstein;

import android.widget.TextView;

class CounterText {
  private TextView mTextView;
  private int mCtr;

  public CounterText(int resId) {
    mTextView = (TextView)MainActivity.INSTANCE.findViewById(resId);
    mCtr = 0;
  }

  public void zeroize() {
    mCtr = 0;
    updateTextField();
  }

  public void increment() {
    mCtr ++;
    updateTextField();
  }

  private void updateTextField() {
    String buf = String.format("%4d", mCtr);
    mTextView.setText(buf);
  }
}
