package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Collector;
import java.util.function.IntFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;


/**
 * Value class representing the weights in a network.
 * Does not include node thetas.
 */
class Weights
{
    final private OneNodesFanningInWeights[][] weights;

    Weights(double[][][] weights)
    {
        this(map3Deep(weights, Double::valueOf));
    }

    Weights(Double[][][] w)
    {
        if(!consistent(w)) throw new IllegalArgumentException();

        // a safe copy.
        // all three layers are copies.
        this.weights = new OneNodesFanningInWeights[w.length][];
        for(int layer=0; layer<weights.length; layer++)
        {
            weights[layer] =
                new OneNodesFanningInWeights[w[layer].length];
            for(int toNode=0; toNode<w[layer].length; toNode++)
            {
                weights[layer][toNode] =
                    new OneNodesFanningInWeights(
                        java.util.List.of(w[layer][toNode]));
            }
        }
    }

    /**
     * Network structure as implied by the weights.
     * Includes the input layer.
     *
     * @throws IllegalStateException if invoked when there are no weights.
     */
    int[] structure()
    {
        if( weights.length==0 ) throw new IllegalStateException();

        int[] struct = new int[weights.length+1]; // +1 for input layer
        struct[0] = weights[0][0].fanningIn.size();
        for(int i=0; i<weights.length; i++)
        {
            struct[i+1] = weights[i].length;
        }
        return struct;
    }

    /**
     * Validates that the given weights structure
     * is self consistent.
     *
     * @param weights the weights indexed by layer, output node, input node
     */
    private boolean consistent(Double[][][] weights)
    {
        // If there isn't even a layer 0,
        // it's kind of a boring set of weights,
        // but it is self consistent!

        if(weights.length == 0) return true;

        //
        // Layer 0 doesn't need to match anything in weights
        // because there is no layer -1, that is, no input layer of nodes.
        // So instead, we check that each node at least has a consistent
        // number of inputs.
        //
        long variety =
            Arrays.stream(weights[0]).mapToInt(outNode->outNode.length)
                                     .distinct()
                                     .count();
        if( variety != 1 ) return false;

        //
        // For these layers > 0,
        // check that each node has the number of inputs
        // as their are nodes in the lower numbered layer.
        //
        for(int layer=1; layer<weights.length; layer++)
        {
            for(int node=0; node<weights[layer].length; node++)
            {
                if( weights[layer][node].length != weights[layer-1].length)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static Double[][][] map3Deep(
        double[][][] xxx,
        DoubleFunction<Double> mapper)
    {
        // 3D array is xxx
        // 2D array is xx
        // 1D array is x
        Function<double[],Double[]> x =
            (double[] arr) ->
                DoubleStream.of(arr).mapToObj(mapper).toArray(Double[]::new);
        Function<double[][], Double[][]> xx =
            (double[][] arr) ->
                Stream.of(arr).map(x).toArray(Double[][]::new);

        return Stream.of(xxx).map(xx).toArray(Double[][][]::new);
    }

    Weights nextWeights(Weights.ThreeIntFunction<Double> adjuster)
    {
        var array = IntStream.range(1,weights.length+1)
                             .mapToObj(layer->newLayerOfWeights(layer,adjuster))
                             .toArray(double[][][]::new);
        return new Weights(array);
    }

    private double[][] newLayerOfWeights(
        int layer,
        ThreeIntFunction<Double> adjuster)
    {
        IntFunction<double[]> getInputWeights =
                 outNode -> newNodeInputWeights( layer-1, outNode, adjuster);

        return IntStream.range(0,weights[layer-1].length)
                        .mapToObj(getInputWeights)
                        .toArray(double[][]::new);
    }

    /**
     * Returns a new updated instance of input weights
     * for the specified node.
     */
    private double[] newNodeInputWeights(
        int wLayer,
        int outNode,
        ThreeIntFunction<Double> adjuster)
    {
        IntFunction<Double> adj =
            inNode -> adjuster.getAdjustment(wLayer,outNode,inNode);

        return getFanningInWeights(wLayer, outNode).withAdjustment(adj)
                                                   .toPrimitiveArray();
    }

    @FunctionalInterface
    interface ThreeIntFunction<T> {
        abstract T getAdjustment(int layer, int outNode, int inNode);
    }

    double getWeight(int layer, int outputNode, int inputNode)
    {
        java.util.Objects.checkIndex(layer, weights.length);
        java.util.Objects.checkIndex(outputNode, weights[layer].length);

        return weights[layer][outputNode].get(inputNode);
    }

    OneNodesFanningInWeights getFanningInWeights(int layer, int outputNode)
    {
        // don't need to make a copy; OneNodesFanningInWeights is immutable
        return weights[layer][outputNode];
    }

    double[][][] toPrimitives()
    {
        return Arrays.stream(weights)
                     .map(Weights::layerAsPrimitives)
                     .toArray(double[][][]::new);
    }

    static double[][] layerAsPrimitives(OneNodesFanningInWeights[] arr)
    {
        return Arrays.stream(arr)
                     .map(OneNodesFanningInWeights::toPrimitiveArray)
                     .toArray(double[][]::new);
    }

    /**
     * A value class representing
     * a collection of weights
     * that are the input to a single node.
     */
    class OneNodesFanningInWeights
    {
        private List<Double> fanningIn;
        /**
         * The class may keep only a reference to the provided weights,
         * so weights should be immutable or effectively immutable.
         */
        OneNodesFanningInWeights( List<Double> weights )
        {
            fanningIn = new ArrayList(weights);
        }

        /**
         * Creates a new instance from the provided
         * effectively immutable list of weights.
         *
         * This constructor is private because we don't trust
         * the public to provide an effectively immutable object.
         */
        private OneNodesFanningInWeights( ArrayList<Double> weights )
        {
            fanningIn = weights;
        }

        public Double get(int index)
        {
            return fanningIn.get(index);
        }

        OneNodesFanningInWeights withAdjustment(IntFunction<Double> adjuster)
        {
            ArrayList<Double> asList =
                IntStream.range(0,fanningIn.size())
                         .mapToObj(i -> fanningIn.get(i) + adjuster.apply(i))
                         .collect(
                            ArrayList::new,
                            ArrayList::add,
                            ArrayList::addAll);

            return new OneNodesFanningInWeights(asList);
        }

        double[] toPrimitiveArray()
        {
            return fanningIn.stream()
                            .mapToDouble(Number::doubleValue)
                            .toArray();
        }
    }
}

