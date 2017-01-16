package jadkowski.dawid.pollutionapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import jadkowski.dawid.pollutionapp.R;
import jadkowski.dawid.pollutionapp.helpers.Permissions;

/**
 * Created by dawid on 22/01/2016.
 */
public class SplashActivity extends AppCompatActivity {
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        if (Build.VERSION.SDK_INT >= 23) {
            if (new Permissions(getApplicationContext(), this).checkPermission()) {
                proceed();
            } else {
                new Permissions(getApplicationContext(), this).requestPermission();
            }
        } else {
            proceed();
        }
    }

    private void proceed() {
        final ImageView logoViewer = (ImageView) findViewById(R.id.logo);
        final Animation fadeinAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fade_in);
        final Animation fadeoutAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fade_out);

        logoViewer.startAnimation(fadeinAnim); //start animation of a text in the logo
        fadeinAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            //once the animation finished start the main activity
            @Override
            public void onAnimationEnd(Animation animation) {
                logoViewer.startAnimation(fadeoutAnim);
                finish();
                Intent startTheMainActivity = new Intent(getBaseContext(), MainActivity.class);
                startActivity(startTheMainActivity);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    proceed();
                } else {
                    if (new Permissions(getApplicationContext(), this).checkPermission()) {
                        proceed();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                        builder.setTitle(getString(R.string.loc_perm_title));
                        builder.setMessage(getString(R.string.loc_perm_msg));
                        builder.setPositiveButton(getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                new Permissions(SplashActivity.this.getApplicationContext(), SplashActivity.this).requestPermission();
                            }
                        });
                        builder.setNegativeButton(getString(R.string.exit_btn), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                        dialog = builder.create();
                        dialog.show();
                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);

                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onDestroy();
    }
}
