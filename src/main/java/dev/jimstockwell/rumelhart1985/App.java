package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
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
    private final int[] structure;  // layer 0 is inputs;

    protected double initialStepSize () { return 1.0; }
    private final double η = initialStepSize();

    private double[][][] w;   // layer 0 is from input units
    private double[][] theta; // layer 0 is first hidden units
                              // no activation function.
    private double[][] outputs; // cached results of forward sweeps
                                // layer 0 is a copy of the input

    private ActivationFunction activationFunction =
                new LogisticActivationFunction();

    private boolean wLayerIsValid(
        int inputCount,
        int outputCount,
        double[][] layerOfW)
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
                if(!wLayerIsValid(
                    structure[layer],
                    structure[layer+1],
                    w[layer]))
                {
                    return false;
                }
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

        this.structure =
            structure==null ? null : Arrays.copyOf(structure,structure.length);

        if(w != null)
        {
            this.w = threeDCopyOf(w);
        } else {
            this.w = null;
        }

        this.theta = twoDCopyOf(theta);
    }

    Network(int[] structure)
    {
        Random rnd = new Random();

        this.structure = Arrays.copyOf(structure, structure.length);
        this.w = new double[structure.length-1][][];
        this.theta = new double[structure.length-1][];

        final int FIRST_LAYER_AFTER_INPUT = 1;
        for(int layer=FIRST_LAYER_AFTER_INPUT ; layer<structure.length; layer++)
        {
            this.w[layer-1] = new double[structure[layer]][];
            for(int node=0; node<structure[layer]; node++)
            {
                this.w[layer-1][node] = new double[structure[layer-1]];
                for(int weight=0; weight<structure[layer-1]; weight++)
                {
                    this.w[layer-1][node][weight] = rnd.nextDouble()/1000.0;
                }
            }
            this.theta[layer-1] = new double[structure[layer]];
            for(int node=0; node<structure[layer]; node++)
            {
                this.theta[layer-1][node] = rnd.nextDouble()/1000.0;
            }
        }

    }

    public double[][][] w()
    {
        return threeDCopyOf(this.w);
    }

    public double[][] theta()
    {
        return twoDCopyOf(this.theta);
    }

    
    int learningLoops;
    boolean learningDone()
    {
        return learningLoops > 0;
    }

    LearningLog learn(Patterns pats)
    {
        var log = new LearningLog();
        for(learningLoops = 0; !learningDone(); learningLoops++)
        {
            for(int patIdx=0; patIdx<pats.patterns.length; patIdx++)
            {
                assert patIdx==0 : "only designed for one pattern so far";

                sweepForward(pats.patterns[patIdx][0]);
                double[] target = pats.patterns[patIdx][1];
                // right here is a good place to find E_p if we want

                for(int layer=structure.length-1; layer>=1; layer--)
                {
                    assert layer==1 : "only designed for no hidden layers";

                    for(int outNode=0; outNode<structure[layer]; outNode++)
                    {
                        // variable names directly from Rumelhart
                        double o_pj = outputs[layer][outNode];
                        double t_pj = pats.patterns[patIdx][1][outNode];
                        double δ_pj = (t_pj - o_pj) * o_pj * (1-o_pj);

                        for(int inNode=0; inNode<structure[layer-1]; inNode++)
                        {
                            double i_pi = outputs[layer-1][inNode];
                            double ΔpWji = η * δ_pj * i_pi;
                            w[layer-1][outNode][inNode] += ΔpWji;
                        }
                        // Theta is like w, except always 1 for input,
                        // per Rumelhart.
                        double ΔpWji = η * δ_pj * 1;
                        theta[layer-1][outNode] += ΔpWji;
                    }
                }
            }
            log.add(this);
        }
        
        return log;
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
        
        return activationFunction.f(netpj, theta);
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

    private double[] sweepForward(double[] inputPattern)
    {
        outputs = new double[structure.length][];
        outputs[0] = inputPattern;

        outputs[1] = answerOneLayer(inputPattern, w[0], theta[0]);
        for(int layer=2; layer<structure.length; layer++)
        {
            outputs[layer] =
                answerOneLayer(outputs[layer-1], w[layer-1], theta[layer-1]);
        }
        return outputs[structure.length-1];
    }

    double[] answer(double[] inputPattern)
    {
        sweepForward(inputPattern);
        return outputs[outputs.length-1];
    }
}

class Patterns
{
    double[][][] patterns;

    Patterns(double [][][] p)
    {
        patterns = p;
    }

    int size()
    {
        return patterns.length;
    }

    static Patterns xor()
    {
        return new Patterns(new double[][][]
        {
            {{0,0},{0}},
            {{0,1},{1}},
            {{1,0},{1}},
            {{1,1},{0}}
        });
    }

    static Patterns flip()
    {
        return new Patterns(new double[][][]
        {
            {{0},{1}},
            {{1},{0}}
        });
    }
}

class LearningLog
{
    private ArrayList<Network> netList = new ArrayList<>();

    void add(Network network) {
        netList.add(network);
    }

    Network net(int step)
    {
        return netList.get(step);
    }
}

