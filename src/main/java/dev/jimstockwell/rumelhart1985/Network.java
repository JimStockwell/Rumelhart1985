package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.twoDCopyOf;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.threeDCopyOf;

class Network implements Cloneable
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
    private int[] structure;  // layer 0 is inputs;

    private double η = 1.0;

    private Weights weights;   
    private double[][] theta; // layer 0 is first hidden units
                              // no activation function.

    private Outputs outputs; // cached results of forward sweeps
                                // layer 0 is a copy of the input

    private ActivationFunction activationFunction =
                new LogisticActivationFunction();

    //
    // A shallow copy of everything,
    // and that's okay because we don't change parts of anything.
    // Either replace or leave alone.
    //
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

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

    void wIsValidOrThrow(double[][][] w) throws IllegalArgumentException
    {
        if(w == null) throw new IllegalArgumentException();
        if(structure.length-1 != w.length) throw new IllegalArgumentException();
        for(int layer=0; layer<structure.length-1; layer++)
        {
            if(!wLayerIsValid(
                structure[layer],
                structure[layer+1],
                w[layer]))
            {
                throw new IllegalArgumentException();
            }
        }
        
    }

    void thetaIsValidOrThrow(double[][] theta) throws IllegalArgumentException
    {
        if(theta == null) throw new IllegalArgumentException();
        if(structure.length-1 != theta.length)
            throw new IllegalArgumentException();
        for(int layer=0; layer<structure.length-1; layer++)
        {
            if(structure[layer+1] != theta[layer].length)
                throw new IllegalArgumentException();
        }
    }

    Network withTheta(double[][] theta)
    {
        thetaIsValidOrThrow(theta);
        try
        {
            Network newOne = (Network)clone();
            newOne.theta = twoDCopyOf(theta);
            return newOne;
        }
        catch(CloneNotSupportedException e)
        {
            throw new RuntimeException("Internal Error",e);
        }
    }

    Network withW(double[][][] w)
    {
        wIsValidOrThrow(w);
        try
        {
            Network newOne = (Network)clone();
            newOne.weights = new Weights(w);
            return newOne;
        }
        catch(CloneNotSupportedException e)
        {
            throw new RuntimeException("Internal Error",e);
        }
    }

    Network withEta(double η)
    {
        try
        {
            Network newOne = (Network)clone();
            newOne.η = η;
            return newOne;
        }
        catch(CloneNotSupportedException e)
        {
            throw new RuntimeException("Internal Error",e);
        }
    }

    Network withStructure(int[] structure)
    {
        // TODO: shouldn't be able to override existing structure
        // should have to start fresh
        if(structure==null) throw new IllegalArgumentException();
        try
        {
            Network newOne = (Network)clone();
            newOne.structure = Arrays.copyOf(structure,structure.length);
            return newOne
               .withW(defaultW(structure))
               .withTheta(defaultTheta(structure));
        }
        catch(CloneNotSupportedException e)
        {
            throw new RuntimeException("Internal Error",e);
        }
    }

    Network(int[] structure, double[][][] w, double[][] theta)
    {
        if(!validate(structure,w,theta)) throw new IllegalArgumentException();

        this.structure =
            structure==null ? null : Arrays.copyOf(structure,structure.length);

        if(w != null)
        {
            weights = new Weights(w);
        } else {
            weights = null;
        }

        this.theta = twoDCopyOf(theta);
    }

    static double[][] defaultTheta(int[] structure)
    {
        final Random rnd = new Random();
        double[][] retval = new double[structure.length-1][];
        final int FIRST_LAYER_AFTER_INPUT = 1;
        for(int layer=FIRST_LAYER_AFTER_INPUT ; layer<structure.length; layer++)
        {
            retval[layer-1] = new double[structure[layer]];
            for(int node=0; node<structure[layer]; node++)
            {
                retval[layer-1][node] = rnd.nextDouble()/1000.0;
            }
        }
        return retval;
    }

    static double[][][] defaultW(int[] structure)
    {
        final Random rnd = new Random();
        double[][][] retval = new double[structure.length-1][][];
        final int FIRST_LAYER_AFTER_INPUT = 1;
        for(int layer=FIRST_LAYER_AFTER_INPUT ; layer<structure.length; layer++)
        {
            retval[layer-1] = new double[structure[layer]][];
            for(int node=0; node<structure[layer]; node++)
            {
                retval[layer-1][node] = new double[structure[layer-1]];
                for(int weight=0; weight<structure[layer-1]; weight++)
                {
                    retval[layer-1][node][weight] = rnd.nextDouble()/1000.0;
                }
            }
        }
        return retval;
    }

    // TODO: Since w and theta may be added,
    // it would be nice to populate them lazily.
    Network(int[] structure)
    {
        final Random rnd = new Random();

        this.structure = Arrays.copyOf(structure, structure.length);
        this.weights = new Weights(defaultW(structure));
        this.theta = defaultTheta(structure);
    }

    // TODO: We need a proper response if the Network
    // actually gets used with such a minimal initialization.
    Network()
    {
    }

    // TODO: We need a proper response if the Network
    // is used WITHOUT setting w
    public double[][][] w()
    {
        return this.weights.toPrimitives();
    }

    // TODO: We need a proper response if the Network
    // is used WITHOUT setting theta
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
                sweepBack(pats.patterns[patIdx][1]);
            }
            log.add(this);
        }
        
        return log;
    }

    /**
     * Do the back propigation pass,
     * updating this.weights and this.theta
     */
    void sweepBack(double[] targetArray)
    {
        Target target = new Target(targetArray);

        Deltas deltas = new ArrayDeltas( target, outputs, weights);

        weights = newWeights(deltas);
        theta = newTheta(deltas);
    }

    Weights newWeights(Deltas deltas)
    {
        var array = IntStream.range(1,structure.length)
                             .mapToObj(layer->newLayerOfWeights(layer,deltas))
                             .toArray(double[][][]::new);
        return new Weights(array);
    }

    double[][] newTheta(Deltas deltas)
    {
        return IntStream.range(1,structure.length)
                        .mapToObj(layer->newLayerOfThetas(layer,deltas))
                        .toArray(double[][]::new);
    }

    private double[] newLayerOfThetas(int layer, Deltas deltas)
    {
        IntToDoubleFunction getTheta = 
            node -> deltas.getDelta(layer-1,node)*η + theta[layer-1][node];

        return IntStream.range(0,structure[layer])
                        .mapToDouble(getTheta)
                        .toArray();
    }

    private double[][] newLayerOfWeights(int layer, Deltas deltas)
    {
        IntFunction<double[]> getInputWeights =
                 outNode -> newNodeInputWeights( deltas, layer-1, outNode);

        return IntStream.range(0,structure[layer])
                        .mapToObj(getInputWeights)
                        .toArray(double[][]::new);
    }

    /**
     * Returns a new updated instance of input weights
     * for the specified node.
     */
    private double[] newNodeInputWeights(Deltas deltas, int wLayer, int outNode)
    {
        IntFunction<Double> adj =
            inNode -> η *
                      deltas.getDelta(wLayer,outNode) *
                      outputs.get(wLayer,inNode);

        return weights.getFanningInWeights(wLayer, outNode)
                      .withAdjustment(adj)
                      .toPrimitiveArray();
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

    private void sweepForward(double[] inputPattern)
    {
        double[][] outs = new double[structure.length][];
        outs[0] = inputPattern;

        outs[1] = answerOneLayer(inputPattern, weights.toPrimitives()[0], theta[0]);
        for(int layer=2; layer<structure.length; layer++)
        {
            outs[layer] =
                answerOneLayer(outs[layer-1], weights.toPrimitives()[layer-1], theta[layer-1]);
        }
        outputs = new Outputs(outs);
    }

    double[] answer(double[] inputPattern)
    {
        sweepForward(inputPattern);
        return outputs.getLastLayer()
                         .stream()
                         .mapToDouble(Number::doubleValue)
                         .toArray();
    }

    Outputs outputs()
    {
        return outputs;
    }
}


