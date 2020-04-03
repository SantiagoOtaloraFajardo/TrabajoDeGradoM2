package Utils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RobotDetector {
    /*
             Vecindario
                4, facilitar el procesamiento
                vecidanrio se define por la semilla
                se crean hilos por el numero de robots definidos en la primera pantalla
                se pone a correr, por el momento no se define hasta cuando
                    Trabajar en generar una lona sin objetos con paint 3D para usuar como borrado de fondo
                    Se usa valor de intensidad -> Transformar imagen a BN o a HSV
         */
    private Mat imagen;
    private ArrayList<Point> semillas;
    private final ExecutorService service;
    private final ArrayList<Future<ArrayList<Point>>> tasks;
    private ArrayList<ArrayList<Point>> objetos;
    public RobotDetector(Mat imagen, ArrayList<Point> semillas, int numRobots) {
        //Convertir la imagen de color a BN -> Probar despues on HSV
        Imgproc.cvtColor(imagen,this.imagen,Imgproc.COLOR_RGB2GRAY);
        this.semillas = semillas;
        this.service = Executors.newFixedThreadPool(numRobots);
        this.tasks = new ArrayList<>();
        this.objetos=new ArrayList<>();
        for(int i=0;i<numRobots;i++)
        {
            tasks.add(service.submit(new RobotDetectorThread(semillas.get(i),imagen)));
        }
        for(int i=0;i<numRobots;i++)
        {
            try {
                objetos.add(tasks.get(i).get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //Calcular los momentos del objeto
}
