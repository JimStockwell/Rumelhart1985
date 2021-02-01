package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import static java.lang.Math.exp;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.twoDCopyOf;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.threeDCopyOf;

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

    // first index = layer in network
    // second index = unit within layer
    // third index = weight into that unit
    private final double[][][] w;   // layer 0 is from input to first units
    private final double[][] theta; // layer 0 is first units
    private final int[] structure;  // layer 0 is inputs

    private boolean wLayerIsValid(int inputCount, int outputCount, double[][] layerOfW)
    {
            if(outputCount != layerOfW.length) return false;

            for(int node=0; node<outputCount; node++)
            {
                if(inputCount != layerOfW[node].length) return false;
            }

            return true;
    }

    private boolean validate(int[] structure, double[][][] w, double[][] theta)
    {
        if(w != null) {
            if(structure.length-1 != w.length) return false;

            for(int layer=0; layer<structure.length-1; layer++)
            {
                if(!wLayerIsValid(structure[layer], structure[layer+1], w[layer])) return false;
            }
        }
        
        if(structure.length-1 != theta.length) return false;
        for(int layer=0; layer<structure.length-1; layer++)
        {
            if(structure[layer+1] != theta[layer].length) return false;
        }

        return true;
    }

    Network(int[] structure, double[][][] w, double[][] theta)
    {
        if(!validate(structure,w,theta)) throw new IllegalArgumentException();

        this.structure = structure==null ? null : Arrays.copyOf(structure,structure.length);

        if(w != null)
        {
            this.w = threeDCopyOf(w);
        } else {
            this.w = null;
        }

        this.theta = twoDCopyOf(theta);
    }

    public double[][][] w()
    {
        return threeDCopyOf(this.w);
    }

    public double[][] theta()
    {
        return twoDCopyOf(this.theta);
    }

    Network learn(Patterns p)
    {
        return new Network(new int[] {}, new double[][][]{{{1}}}, new double[][]{{1}});
    }

    /**
     * Computes the output for a unit with the specified inputs and weights
     * @param inputs the inputs for this unit
     * @param weights the weights for this set of inputs
     * @return the output for this unit
     */
    private double unitOutput(double[] inputs, double[] weights, double theta)
    {
        double netpj = 0; // accumulate netpj

        for( int i=0; i<inputs.length; i++)
        {
            netpj += inputs[i] * weights[i];
        }
        
        return 1/(1+exp(-(netpj+theta)));
    }

    double[] answerOneLayer(double[] input, double[][] w, double[] theta)
    {
        double retval[] = new double[theta.length];
        for(int node=0; node<theta.length; node++)
        {
            retval[node] = unitOutput(input, w[node], theta[node]);
        }
        return retval;
    }

    double[] answer(double[] inputPattern)
    {
        double[] tmp = answerOneLayer(inputPattern, w[0], theta[0]);
        for(int layer=1; layer<structure.length-1; layer++)
        {
            tmp = answerOneLayer(tmp, w[layer], theta[layer]);
        }
        return tmp;
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
