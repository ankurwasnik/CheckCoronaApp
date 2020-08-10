package com.ankurwasnik358.corona;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class ResultActivity extends AppCompatActivity {

    ConstraintLayout constraintLayout;
    ImageView ivMessage ;
    ImageButton imgbtnWHO , imgbtnMYGOV ,imgbtnCall ;
    TextView helpnumbers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultactivity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        constraintLayout = findViewById(R.id.constraintLayout);

        ivMessage = findViewById(R.id.imageView);
        imgbtnMYGOV=findViewById(R.id.imageButtonmygov);
        imgbtnWHO =findViewById(R.id.imageButtonwho);
        imgbtnCall = findViewById(R.id.imageViewcallButton);
        imgbtnCall.setVisibility(View.GONE);
        helpnumbers=findViewById(R.id.tvHelpNumbers);
        helpnumbers.setVisibility(View.GONE);



        Intent intent = getIntent();
        int op = intent.getIntExtra("Output", -1);
        float prediction = intent.getFloatExtra("Prediction", -1);
        String output = "";
        if (op == 0) {
            output = "Healthy";
           constraintLayout.setBackgroundResource(R.drawable.thumbsup_logo);
            ivMessage.setImageResource(R.drawable.health_card);



        } else if (op == 1) {
            output = "Unhealthy";
            imgbtnCall.setVisibility(View.VISIBLE);
            helpnumbers.setVisibility(View.VISIBLE);
            constraintLayout.setBackgroundResource(R.drawable.result_unhealthy);
            ivMessage.setVisibility(View.GONE);




        } else {
            output = "Something went wrong. Please try again !";


        }

        imgbtnWHO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("https://covid19.who.int/");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }


            }
        });

        imgbtnMYGOV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri webpage = Uri.parse("https://www.mygov.in/covid-19");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }

            }
        });

        imgbtnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + "1123978046"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                else{
                    Snackbar.make(imgbtnCall,"Something Went Wrong !" , Snackbar.LENGTH_SHORT).show();
                }
            }
        });




    }
}