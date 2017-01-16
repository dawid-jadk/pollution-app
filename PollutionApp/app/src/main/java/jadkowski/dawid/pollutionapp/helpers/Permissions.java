package jadkowski.dawid.pollutionapp.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by dawid on 06/01/2017.
 */

public class Permissions implements ActivityCompat.OnRequestPermissionsResultCallback{
    private Context context;
    private Activity activity;
    private static final int PERMISSION_REQUEST_CODE = 1;
    public Permissions(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(activity, new String[] {
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permissions", "Permission Granted.");
                } else {
                    Log.e("Permissions", "Permission Denied.");
                }
                break;
        }
    }
}
