package com.danielkim.soundrecorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class PlayActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context, String name) {
        Intent intent = new Intent(context, PlayActivity.class);
        intent.putExtra("name", name);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return PlayFragment.newInstance(getIntent().getStringExtra("name"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
