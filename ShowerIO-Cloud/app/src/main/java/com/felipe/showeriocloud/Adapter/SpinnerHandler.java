package com.felipe.showeriocloud.Adapter;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class SpinnerHandler implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

    boolean userSelect = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        userSelect = true;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (userSelect) {
            // Your selection handling code here
            ((Spinner) parent).getPrompt();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
