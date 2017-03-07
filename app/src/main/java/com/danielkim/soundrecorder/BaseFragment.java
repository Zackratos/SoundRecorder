package com.danielkim.soundrecorder;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IVY on 2017/3/7.
 */

public class BaseFragment extends Fragment {


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    protected void requestRunTimePermission(String[] requestPermissions) {
        List<String> permissions = new ArrayList<>();
        for (String permission : requestPermissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permission);
            }
        }
        if (permissions.isEmpty()) {
            grantedPermission();
        } else {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                List<String> deniedPermissions = new ArrayList<>();
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i]);
                    }
                }
                if (deniedPermissions.isEmpty()) {
                    grantedPermission();
                } else {
                    deniedPermission(deniedPermissions);
                }
            }
        }
    }

    protected void grantedPermission() {

    }

    protected void deniedPermission(List<String> deniedPermissions) {

    }
}
