package Utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class RobotDetectorThread implements Callable<ArrayList<Point>> {
    private Point semilla;
    private Mat imagen;
    public RobotDetectorThread(Point semilla, Mat imagen)
    {
        this.semilla=semilla;
        this.imagen=imagen;

    }

    //Comprobar a partir del promedio -> suma de las intensidades del vecindario divido entre 4
    //Retorna el punto del vecindario que mas se parece al promedio
    private Point compararVecino(Point pixelEvaluado)
    {
        double intensidadPixelEvaluado=0.0;
        ArrayList<Point> vecindario= new ArrayList<>();
        ArrayList<Double> valoresIntensidadVecindario = new ArrayList<>();
        //Izquierda de la semilla 0
        vecindario.add(new Point(pixelEvaluado.x-1,pixelEvaluado.y));
        valoresIntensidadVecindario.add(calcularIntensidad(this.imagen.get((int)vecindario.get(0).y,(int)vecindario.get(0).x)));
        //Derecha de la semilla 1
        vecindario.add(new Point(pixelEvaluado.x+1,pixelEvaluado.y));
        valoresIntensidadVecindario.add(calcularIntensidad(this.imagen.get((int)vecindario.get(1).y,(int)vecindario.get(1).x)));
        //Arriba de la semilla 2
        vecindario.add(new Point(pixelEvaluado.x,pixelEvaluado.y+1));
        valoresIntensidadVecindario.add(calcularIntensidad(this.imagen.get((int)vecindario.get(2).y,(int)vecindario.get(2).x)));
        //Abajo de la semilla 3
        vecindario.add(new Point(pixelEvaluado.x,pixelEvaluado.y-1));
        valoresIntensidadVecindario.add(calcularIntensidad(this.imagen.get((int)vecindario.get(3).y,(int)vecindario.get(3).x)));

        //Definir el "vecino" con mayor similitud al centro
        intensidadPixelEvaluado=calcularIntensidad(this.imagen.get((int)pixelEvaluado.y,(int)pixelEvaluado.x));
        double menor = Double.MAX_VALUE;
        int posMenor=0;
        for(int i =0; i<valoresIntensidadVecindario.size();i++)
        {
            double dif=0;
            dif=Math.abs(valoresIntensidadVecindario.get(i)-intensidadPixelEvaluado);
            if(dif<menor)
            {
                menor=dif;
                posMenor=i;
            }
        }
        //Nuevo centro
        return vecindario.get(posMenor);
    }

    //Suma de las intensidades
    private double calcularIntensidad(double[] valores)
    {
        double intensidad=0.0;
        for(int i=0; i< valores.length;i++)
        {
            intensidad+=valores[i];
        }
        return intensidad;
    }
    @Override
    public ArrayList<Point> call() throws Exception {
        ArrayList<Point> objeto=new ArrayList<>();
        objeto.add(this.semilla);
        Point centro=new Point();
        Point nuevoCentro=new Point();
        centro=semilla.clone();
        do {
            nuevoCentro=compararVecino(centro);
            objeto.add(centro);
            centro=nuevoCentro.clone();
        }while(calcularIntensidad(this.imagen.get((int)centro.y,(int)centro.x))!=0);
        return objeto;
    }
}
