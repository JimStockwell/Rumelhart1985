package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;

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

    private static double[][] twoDCopyOf(double[][] source)
    {
        final double[][] tmp = new double[source.length][];
        for(int i=0; i<source.length; i++)
        {
            tmp[i] = Arrays.copyOf(source[i],source[i].length);
        }
        return tmp;
    }

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

    int[] answer(int[] inputPattern)
    {
        return new int[]{1};
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
