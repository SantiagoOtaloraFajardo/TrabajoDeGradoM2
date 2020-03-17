package com.example.modulo2reconocimientoactor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    Mat srcDetectarColor, dstDetectarColor;
    Scalar verdeOscuro, rojoOscuro, verdeClaro, rojoClaro;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        //Oscuro = componente bajo y Claro = componente alto
        verdeOscuro= new Scalar(25,51,25);
        verdeClaro= new Scalar(229,255,229);
        rojoOscuro= new Scalar(51,25,25);
        rojoClaro= new Scalar(255,229,229);
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch (status) {

                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }


            }
        };

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame;
        frame=inputFrame.rgba();


        return frame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        srcDetectarColor = new Mat(width,height, CvType.CV_16UC4);
        dstDetectarColor = new Mat(width,height, CvType.CV_16UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }
    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There's a problem, yo!", Toast.LENGTH_SHORT).show();
        }

        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }



    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){

            cameraBridgeViewBase.disableView();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }



    //----------------------------UI methods:>
    //wrapper to invoke cam app
    Mat baseMat;
    static final int REQ_NOOD=1, REQ_GALL = 2;
    public void invokeCamera(View view){
        Intent sendNoodsInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(sendNoodsInt.resolveActivity(getPackageManager()) != null){
            startActivityForResult( sendNoodsInt , REQ_NOOD );
        }
    }
    //doing thumbnail stuff for now, too sleppy
    @Override
    protected void onActivityResult( int REQ_COD, int RES_COD, Intent data ){
        if ( REQ_COD == REQ_NOOD &&  RES_COD == RESULT_OK  ) {
            Bundle extras = data.getExtras();
            Bitmap picBM = (Bitmap) extras.get("data");
            baseMat = new Mat(picBM.getHeight() , picBM.getWidth() , CvType.CV_32SC3);
            Utils.bitmapToMat(picBM,baseMat);
            cameraBridgeViewBase.enableView();
            ImageView IV = findViewById(R.id.picZone);
            IV.setImageBitmap(picBM);



        }
        if ( REQ_COD == REQ_GALL &&  RES_COD == RESULT_OK  ) {
            Uri selectedImage = data.getData();
            try {
                //ImageDecoder.Source imgSrc = ImageDecoder.createSource(  getContentResolver() , selectedImage );
                Bitmap gallBM = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                //Bitmap gallBM = ImageDecoder.decodeBitmap(imgSrc );
                ImageView IV = findViewById(R.id.picZone);

                baseMat = new Mat();
                Utils.bitmapToMat(gallBM ,baseMat );
                evokeMat(baseMat);
            } catch (IOException e) {
                Log.i("TAG", "Some exception " + e);
            }
        }
    }

    //wrapper to fetch from gallery

    public void invokeGallery(View view){
        Intent ImgPickInt = new Intent( Intent.ACTION_PICK );
        ImgPickInt.setType("image/*");
        startActivityForResult(ImgPickInt, REQ_GALL );


    }
    public void evokeMat(Mat mat){
        ImageView IV = findViewById(R.id.picZone);
        Bitmap temp = Bitmap.createBitmap(mat.width(),mat.height(), Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap( mat , temp);
        IV.setImageBitmap(temp);

    }

    public void toOutViewLog (String logOut){ //to on screen log output
        TextView logOutV =  findViewById(R.id.dbgOut);
        logOutV.setText( logOut );

    }

    public String getSceneName(){
        TextView nameView = findViewById( R.id.nameInp );
        return nameView.getText().toString();
    }

}
