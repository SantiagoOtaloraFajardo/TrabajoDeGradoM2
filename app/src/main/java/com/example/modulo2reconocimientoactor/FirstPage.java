package com.example.modulo2reconocimientoactor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import androidx.appcompat.app.AppCompatActivity;

public class FirstPage extends AppCompatActivity {
    EditText numRobots;
    Button cargarImagen;
    BaseLoaderCallback baseLoaderCallback;
    @Override
    protected void onCreate(Bundle savedInstenceState)
    {
        super.onCreate(savedInstenceState);
        setContentView(R.layout.first_page);
        numRobots=(EditText) findViewById(R.id.numRobots);
        cargarImagen=(Button) findViewById(R.id.cargarImagen);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch (status) {

                    case BaseLoaderCallback.SUCCESS:
                        Log.i("OpenCV","OpenCV loaded successfully");
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }


            }
        };
    }
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    public void siguientePagina(View view){
        if(Integer.parseInt(numRobots.getText().toString())!=0)
        {
            Intent Intent = new Intent(view.getContext(), SecondPage.class);

            Log.d("numActores","numero actores primera pagina "+numRobots.getText());
            Intent.putExtra("numActores",Integer.parseInt(numRobots.getText().toString()));
            startActivity(Intent);
            finish();
        }else{
            Toast.makeText(this,"Tiene que haber como minimo un actor", Toast.LENGTH_SHORT).show();
        }

    }

}
