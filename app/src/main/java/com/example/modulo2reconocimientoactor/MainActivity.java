package com.example.modulo2reconocimientoactor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
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
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.io.IOException;
import java.util.ArrayList;

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

        View pz = findViewById(R.id.picZone);
        pz.setOnTouchListener(new View.OnTouchListener() {
            @Override //MUST EXTEND IMAGE VIEW ON FUTURE, to allow draw overlay, and easier management of stuff;
            public boolean onTouch(View view0, MotionEvent motionEvent) {
                ImageView view = (ImageView) view0;
                if ( motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    double x = motionEvent.getX() , y = motionEvent.getY() ;
                    touchCoords.add( new Point( x , y ) );

                    //ftsot and integrity logging/checking ahead; end funct will just fill static(?as of now local) var touchCoords
                    double vx = view.getX() , vy = view.getY(),
                        vtx = view.getTranslationX() , vty = view.getTranslationY(),
                        vw = view.getWidth(), vh = view.getHeight(), viewTop= view.getTop() , viewLeft =view.getLeft() ;

                    //preprocess points
                    Size displaySize  = new Size( view.getWidth(), view.getHeight() ) ,
                            picSize = new Size( view.getDrawable().getIntrinsicWidth() , view.getDrawable().getIntrinsicHeight() );
                    bmpCoords =  touch2BmpCoords(touchCoords, displaySize , picSize  );



                    i++;
                    toOutViewLog("point "+ i + "\nx: "+ x + " \ny:"+y );
                    evokeMat(baseMat);
                    Log.d("POINTCLC", "point "+ i + "data (x , y , viewWidth , viewHeigh):        "+ x + " :: " + y + " :: " + vw + " :: " + vh);
                    //Log.d("POINTADD", "point "+ i + "-- x: "+motionEvent.getX() + "  y:"+motionEvent.getY());
                }
                return true;//if ret false-> only enters once
            }
        });

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
    //doing thumbnail stuff for now, too sleppy //oops. ..  will this be needed?
    @Override
    protected void onActivityResult( int REQ_COD, int RES_COD, Intent data ){
        if ( REQ_COD == REQ_NOOD &&  RES_COD == RESULT_OK  ) {

            resetPoints();

            Bundle extras = data.getExtras();
            Bitmap picBM = (Bitmap) extras.get("data");
            baseMat = new Mat(picBM.getHeight() , picBM.getWidth() , CvType.CV_32SC3);
            Utils.bitmapToMat(picBM,baseMat);
            ImageView IV = findViewById(R.id.picZone);
            IV.setImageBitmap(picBM);




        }
        if ( REQ_COD == REQ_GALL &&  RES_COD == RESULT_OK  ) {
            Uri selectedImage = data.getData();
            try {

                resetPoints();

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

        //overlaying points (can also do extending imageView but this seems easier as of now)
        Mat mat2 = drawOnMat( mat , bmpCoords );

        Utils.matToBitmap( mat2 , temp);

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


    //temporarily defining aux class point, this is ALSO defined in ocv and android??
    /*public class Point{

        private double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }*/
    //temp vars and useful
    ArrayList<Point > touchCoords = new ArrayList<>();
    ArrayList<Point > bmpCoords = new ArrayList<>();
    static int i = 0;


    //prob aux method will not be needed?
    public void updatePoints(View view , Size asindisplaySize ){ //will most likely not even use

    }
    public void resetPoints(){
        touchCoords = new ArrayList<>();
        bmpCoords = new ArrayList<>();
        i = 0;
        if(baseMat != null)
            evokeMat(baseMat);
    }

    public void resetPoints( View view ){
        resetPoints();
    }

    //expects touchCoords relative to the displays size, NOTE: 0,0 > left, top
    public ArrayList<Point> touch2BmpCoords(ArrayList<Point> touchCords , Size displaySize  , Size bmpSize){
        ArrayList<Point> bmpCoords = new ArrayList<>();
        for  (Point p :touchCords){
            //note x * matDim / dispDim
            bmpCoords.add(new Point( p.x  * bmpSize.getWidth() / displaySize.getWidth() , p.y * bmpSize.getHeight() /displaySize.getHeight() ) ) ;
        }
        return bmpCoords;
    }

    public Mat drawOnMat(Mat mat, ArrayList<Point> pointList){
        //no try catch use at your own risk lol #YOLO
        Mat retMat = mat.clone();
        double val[]  = {0,0,0,1};
        Log.d("point", "sizeofpnt " + pointList.size());

        for (Point p : pointList ){
            for (int a = -5; a<5 ; a++ ){
                for (int b = -5 ; b<5;b++ ) {
                    try {
                        retMat.put((int) p.y + a, (int) p.x + b, val);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("putError", e.getMessage());
                    }
                }
            }
        }
        return retMat;
    }

}
