package com.ankurwasnik358.corona;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;

public class SplashScreen extends AppCompatActivity {
    ImageView splashscreen ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        splashscreen = findViewById(R.id.ivSplashScreen);

        Snackbar.make(splashscreen,"Loading..." , Snackbar.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {


            @Override

            public void run() {

                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                SplashScreen.this.finish();

            }

        }, 2*1000); // wait for 5 seconds
    }
}