package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.twoDCopyOf;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.threeDCopyOf;

class Network
{
    //
    // Some comments on the structure this network imposes...
    //
    // The input units have no transfer function
    // the hidden units and output units do have a transfer function
    //
    // Weighted connections go from the input units,
    // to the hidden units, between the layers of hidden units,
    // and between the last layer of hidden units and the output units.
    // There are no weights from the output units.
    //

    //
    // Not all the following members are 3 indexes deep,
    // but for as many indexes as one does have,
    // the following structure is used:
    //
    // first index = layer in network
    // second index = unit within layer
    // third index = weight into that unit
    //
    
    private final int[] structure;  // layer 0 is inputs;
    private final double η;         // gradient descent step size
    private final Weights weights;   
    private final double[][] theta; // layer 0 is first hidden units

    //
    // TODO: respect modularization of ActivationFunction.
    // This looks all broken out and seperate and well modularized,
    // but actually, we hard code its derivite all around, so be careful,
    // until this is improved!
    //
    // If we make this NOT final,
    // be sure to update equals and hashCode
    //
    private final ActivationFunction activationFunction;

    Network()
    {
        this( null, 0, null, null, new LogisticActivationFunction());
    }

    private Network(
        int[] structure,
        double eta,
        Weights w,
        double[][] theta,
        ActivationFunction af)
    {
        this.structure = structure;
        this.η = eta;
        this.weights = w;
        this.theta = theta;
        this.activationFunction = af;
    }

    Network withTheta(double[][] theta)
    {
        thetaIsValidOrThrow(theta);

        return new Network(
            this.structure,
            this.η,
            this.weights,
            twoDCopyOf(theta), // our own safe copy
            activationFunction);
    }

    Network withW(double[][][] w)
    {
        // TODO: better approach to validating w...
        // Build
        wIsValidOrThrow(w);

        return new Network(
            this.structure,
            this.η,
            new Weights(w),
            this.theta,
            activationFunction);
    }

    Network withW(Weights w)
    {
        if(w == null) throw new NullPointerException();
        if(! w.consistentWith(structure)) throw new IllegalArgumentException();

        return new Network(
            this.structure,
            this.η,
            w,
            this.theta,
            activationFunction);
    }

    private void wIsValidOrThrow(double[][][] w) throws IllegalArgumentException
    {
        if(w == null) throw new IllegalArgumentException();
        if(structure.length-1 != w.length) throw new IllegalArgumentException();

        if(structure.length==1) return; // can't call Weights::structure

        Weights weights = new Weights(w); // checks internal consistency

        if(!weights.consistentWith(structure))
                throw new IllegalArgumentException();
    }

    private void
    thetaIsValidOrThrow(double[][] theta) throws IllegalArgumentException
    {
        if(theta == null) throw new IllegalArgumentException();
        if(structure.length-1 != theta.length)
            throw new IllegalArgumentException();
        if(!structureLayersConsistentWithTheta(theta))
            throw new IllegalArgumentException();
    }
    
    private boolean structureLayersConsistentWithTheta(double[][] theta)
    {
        IntPredicate badLayer = i -> structure[i+1] != theta[i].length;

        return IntStream.range(0,structure.length-1)
                        .filter(badLayer)
                        .count() == 0;
    }

    Network withEta(double η)
    {
        return new Network(
            this.structure,
            η,
            this.weights,
            this.theta,
            activationFunction);
    }

    /**
     * This factory method creates a Network similar to this one, but
     * replaces the current structure with the provided structure.
     * Each entry in the array is the number of nodes on a layer.
     * The first entry is for the input layer,
     * and the last entry is for the output layer.
     *
     * A zero length structure has no input or output nodes.
     * A one length structure uses the same nodes for input and for output.
     * A two length structure has no hidden layers.
     * A three length structure is the first with a hidden layer.
     *
     * Having no nodes on a layer is not useful, but it is allowed.
     *
     * Theta and W are set to random settings.
     */
    Network withStructure(int[] structure)
    {
        if(structure==null) throw new NullPointerException();

        if(!structureIsValid(structure)) throw new IllegalArgumentException();

        return new Network(
            Arrays.copyOf(structure, structure.length),
            this.η,
            Weights.defaultW(structure,.01),
            defaultTheta(structure),
            activationFunction);
    }

    private boolean structureIsValid(int[] s)
    {
        IntPredicate badLayer = count -> count < 0;
        return IntStream.of(s)
                        .filter(badLayer)
                        .count() == 0;
    }

    // TODO: move this out to its own class,
    //       then simplify
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
                retval[layer-1][node] = rnd.nextDouble()*.01;
            }
        }
        return retval;
    }

    /**
     * @throws IllegalStateException if Network is not initialized.
     */
    public double[][][] w()
    {
        return this.weights.toPrimitives();
    }

    /**
     * @throws IllegalStateException if Network is not initialized.
     */
    public double[][] theta()
    {
        return twoDCopyOf(this.theta);
    }

    // TODO: cause this to return a NEW network instance,
    // leaving 'this' unchanged by learning.
    Network learn(Patterns pats, int iterations)
    {
        return Stream.iterate(this, net -> oneLearningPass(net, pats))
                     .skip(iterations)
                     .findFirst()
                     .get();
    }

    Network learn(Patterns pats)
    {
        return Stream.iterate(this, net -> oneLearningPass(net, pats))
                     .dropWhile(net -> !net.closeEnough(pats))
                     .findFirst()
                     .get();
    }

    private static Network oneLearningPass(Network netIn, Patterns pats)
    {
        Network net = netIn;
        for(int patIdx=0; patIdx<pats.size(); patIdx++)
        {
            net = sweepBackAndForward(net, pats, patIdx);
        }
        return net;
    }

    private static Network sweepBackAndForward(Network in, Patterns pats, int i)
    {
            Outputs outputs = sweepForward(in, pats.getInputPattern(i));
            return sweepBack(pats.getOutputPattern(i), outputs, in);
    }

    double loss(Patterns pats)
    {
        return IntStream.range(0, pats.size())
                        .mapToDouble(idx -> loss(pats,idx))
                        .sum();
    }

    private double loss(Patterns pats, int idx)
    {
        return lossForOnePattern(
            answer(pats.getInputPattern(idx)),
            pats.getOutputPattern(idx));
    }

    static double lossForOnePattern(double[] output, double[] target)
    {
        if(output.length != target.length) throw new IllegalArgumentException();

        IntToDoubleFunction lossAtIndex =
            i -> 0.5*(target[i]-output[i])*(target[i]-output[i]);

        return IntStream.range(0,output.length)
                        .mapToDouble(lossAtIndex)
                        .sum();
    }

    boolean closeEnough(Patterns pats)
    {
        IntPredicate closeEnoughByIndex =
            patIdx -> closeEnoughForOnePattern(
                    answer(pats.getInputPattern(patIdx)),
                    pats.getOutputPattern(patIdx));

        return IntStream.range(0,pats.size())
                        .allMatch(closeEnoughByIndex);
    }

    private static
    boolean closeEnoughForOnePattern(double[] output, double[] target)
    {
        if(output.length != target.length) throw new IllegalArgumentException();

        IntFunction<Boolean> okayAtIndex =
            i -> Math.abs(target[i]-output[i]) < .1;

        return IntStream.range(0,output.length)
                        .mapToObj(okayAtIndex)
                        .reduce(true,(a,b)->a && b);
    }

    /**
     * Do the back propigation pass,
     */
    private static Network sweepBack(
        double[] targetArray,
        Outputs outputs,
        Network net)
    {
        Target target = new Target(targetArray);

        Deltas deltas = new ArrayDeltas( target, outputs, net.weights);

        Weights weights = newWeights(net, outputs, deltas);
        double[][] theta = newTheta(net, deltas);
        return net.withW(weights).withTheta(theta);
    }

    private static Weights newWeights(
        Network net,
        Outputs outputs,
        Deltas deltas)
    {
        Weights.ThreeIntFunction<Double> weightChange = (layer,out,in) ->
             net.η * deltas.getDelta(layer,out) * outputs.get(layer,in);

        return net.weights.nextWeights(weightChange);
    }

    private static double[][] newTheta(Network net, Deltas deltas)
    {
        return IntStream.range(1,net.structure.length)
                        .mapToObj(layer->newLayerOfThetas(net, layer,deltas))
                        .toArray(double[][]::new);
    }

    private static double[] newLayerOfThetas(
        Network net,
        int layer,
        Deltas deltas)
    {
        IntToDoubleFunction getTheta = 
            node -> deltas.getDelta(layer-1,node)*net.η +
                    net.theta[layer-1][node];

        return IntStream.range(0,net.structure[layer])
                        .mapToDouble(getTheta)
                        .toArray();
    }


    private static Outputs sweepForward(Network start, double[] inputPattern)
    {
        double[][] outs = new double[start.structure.length][];
        outs[0] = inputPattern;

        for(int layer=1; layer<start.structure.length; layer++)
        {
            outs[layer] =
                answerOneLayer(
                    start.activationFunction,
                    outs[layer-1],
                    start.weights.toPrimitives()[layer-1],
                    start.theta[layer-1]);
        }

        return new Outputs(outs);
    }

    private static double[] answerOneLayer(
        ActivationFunction activationFunction,
        double[] input,
        double[][] w,
        double[] theta)
    {
        IntToDoubleFunction nodeToOutput =
            node -> unitOutput(activationFunction, input, w[node], theta[node]);

        return IntStream.range(0, theta.length)
                        .mapToDouble(nodeToOutput)
                        .toArray();
    }

    /**
     * Computes the output for a unit with the specified inputs and weights
     * @param inputs the inputs for this unit
     * @param weights the weights for this set of inputs
     * @return the output for this unit
     */
    private static double unitOutput(
        ActivationFunction activationFunction,
        double[] inputs,
        double[] weights,
        double theta)
    {
        double netpj = IntStream.range(0, inputs.length)
                                .mapToDouble(i -> inputs[i] * weights[i])
                                .sum();
        
        return activationFunction.f(netpj, theta);
    }

    double[] answer(double[] inputPattern)
    {
        List<Double> results = sweepForward(this, inputPattern).getLastLayer();
        return toDoubleArray(results);
    }
    
    private static double[] toDoubleArray(List<Double> list)
    {
        return list.stream()
                   .mapToDouble(Number::doubleValue)
                   .toArray();
    }

    Outputs outputs(double[] inputPattern)
    {
        return sweepForward(this, inputPattern);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;
        Network that = (Network) o;
        return
            Arrays.equals(that.structure, this.structure) &&
            that.η == this.η &&
            Objects.equals(that.weights, this.weights) &&
            Arrays.deepEquals(that.theta, this.theta);
    }

    @Override
    public int hashCode()
    {
        //
        // just simply Objects::hash will not work
        // because of arrays
        //
        return Arrays.hashCode(structure) * 31 +
               Objects.hash(η) * 31 +
               Objects.hashCode(weights) * 31 + // might be null
               Arrays.deepHashCode(theta);
    }
}


