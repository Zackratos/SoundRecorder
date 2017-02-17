package com.danielkim.soundrecorder;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return StartFragment.newInstance();
    }
}
