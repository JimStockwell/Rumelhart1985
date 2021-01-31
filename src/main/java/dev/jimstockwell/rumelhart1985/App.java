package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import static java.lang.Math.exp;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.twoDCopyOf;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}

class Network
{
    //
    // The input units have no transfer function
    // the hidden units and output units do have a transfer function
    //
    // Weighted connections go from the input units,
    // to the hidden units, between the layers of hidden units,
    // and between the last layer of hidden units and the output units.
    // There are no weights from the output units.
    //

    private final double[][] w;
    private final double[][] theta;

    Network(int[] structure, double[][] w, double[][] theta)
    {
        this.w = twoDCopyOf(w);
        this.theta = twoDCopyOf(theta);
    }

    public double[][] w()
    {
        return twoDCopyOf(this.w);
    }

    public double[][] theta()
    {
        return twoDCopyOf(this.theta);
    }

    Network learn(Patterns p)
    {
        return new Network(new int[] {}, new double[][]{{1}}, new double[][]{{1}});
    }

    double[] answer(double[] inputPattern)
    {
        double netpj = 0; // accumulate netpj

        for( int i=0; i<inputPattern.length; i++)
        {
            netpj += inputPattern[i] * w[0][i];
        }
        
        return new double[]{1/(1+exp(-(netpj+theta[0][0])))};
    }
}

class Patterns
{
    int[][][] patterns;

    Patterns(int[][][] p)
    {
        patterns = p;
    }
}
