package com.ankurwasnik358.corona;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    Interpreter tflite;
    public Bitmap bitmap=null;
    Uri imageuri;
    private  int imageSizeX;
    private  int imageSizeY;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f; // 1.0 -> 255.0
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f; //255.0 -> 1.0
    private TensorImage inputImageBuffer;
    private  TensorBuffer outputProbabilityBuffer;
    private  TensorProcessor probabilityProcessor;


    TextView textView ;
    public ImageView imageView;
    Button btnTest ,btnDev , btnHelp , btnUpload , btnMap;
    private Handler handler = new Handler();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnDev=findViewById(R.id.button3);
        btnTest=findViewById(R.id.btnTest);
        btnHelp=findViewById(R.id.btnContact);
        imageView =findViewById(R.id.ivUploadXrays);
        btnUpload=findViewById(R.id.button);
        btnMap = findViewById(R.id.btnMap);



        //setup imageview
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap==null) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 12);
                }
                else {
                    Snackbar.make(btnUpload,"Bitmap already loaded.",Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        //setup dev btn
        btnDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.instagram.com/wasnik.ankur.358/?hl=en");
                Intent intent = new Intent(Intent.ACTION_VIEW , uri);
                intent.setPackage("com.instagram.android");

                try {
                    startActivity(intent);
                }
                catch (ActivityNotFoundException e){
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.instagram.com/wasnik.ankur.358/?hl=en")));
                }
            }
        });

        //setup help button
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + "1123978046"));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Something went wrong ! Please try again later .", Toast.LENGTH_SHORT).show();
                    }

            }
        });

        // setup Test button
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (bitmap==null){
                        Snackbar.make(btnUpload,"Please Upload Chest X ray." , Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    //initialize the model
                    try {
                        tflite = new Interpreter(loadModelFile(MainActivity.this));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //ready inputs to model
                    int imageTensorIndex = 0;
                    int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
                    imageSizeY = imageShape[1];
                    imageSizeX = imageShape[2];
                    DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

                    //ready output to model
                    int probabilityTensorIndex = 0;
                    int[] probabilityShape =tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
                    DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

                    inputImageBuffer = new TensorImage(imageDataType);
                    outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
                    probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();
                    //load the image
                    inputImageBuffer = loadImage(bitmap);
                    //run the model and store pred in outputProbabilityBuffer
                    tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());

                    tflite.close();
                    tflite= null ;
                    showresult();
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri geoLocation =Uri.parse("geo:0,0?q=nearby hospital");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.google.android.apps.maps");
                intent.setData(geoLocation);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                else {
                    Snackbar.make(btnMap,"Install Google Maps",Snackbar.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void showresult(){

         Snackbar.make( btnTest , "Getting Results... ",Snackbar.LENGTH_SHORT).show();

        //getting prediction from outputProbabilityBuffer
         final float[] pred = outputProbabilityBuffer.getFloatArray() ;
         float output = pred[0];
         final int op;
         if (output < 0.4) {
             op = 0 ;
          }
         else {
            op=1;
          }
        new Handler().postDelayed(new Runnable() {

            @Override

            public void run() {

                Intent intent = new Intent(MainActivity.this , ResultActivity.class);
                intent.putExtra("Output", op);
                intent.putExtra("Prediction",pred[0]);
                startActivity(intent);

            }

        }, 2000); // wait for 1 seconds

        //after usage , get ready for next run
        new Handler().postDelayed(new Runnable() {

            @Override

            public void run() {
                bitmap=null;
                imageView.setImageResource(R.drawable.card_bg);

            }

        }, 3000); // wait for 1 seconds


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==12 && resultCode==RESULT_OK && data!=null) {
            imageuri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK && data!=null){
            Bitmap thumbnail = data.getParcelableExtra("data");
            bitmap = thumbnail;
        }
    }
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }
    private TensorOperator getPostprocessNormalizeOp(){
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }
    private TensorImage loadImage( Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(getPreprocessNormalizeOp())
                        .build();

        return imageProcessor.process(inputImageBuffer);
    }

}
