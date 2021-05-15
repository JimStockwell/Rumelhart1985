package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Collector;
import java.util.function.IntFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Value class representing the weights in a network.
 * Does not include node thetas.
 */
class SimpleWeights implements Weights
{
    final private OneNodesFanningInWeights[][] weights;

    SimpleWeights(double[][][] weights) throws IllegalArgumentException
    {
        this(map3Deep(weights, Double::valueOf));
    }

    SimpleWeights(Double[][][] w) throws IllegalArgumentException
    {
        if(!consistent(w)) throw new IllegalArgumentException();

        weights = IntStream.range(0,w.length)
                           .mapToObj(layer -> weightsForLayer(w[layer]))
                           .toArray(OneNodesFanningInWeights[][]::new);
    }

    private OneNodesFanningInWeights[] weightsForLayer(Double[][] thoseWeights)
    {
            IntFunction<OneNodesFanningInWeights> weightsForToNode =
                toNode -> new OneNodesFanningInWeights(
                                            List.of(thoseWeights[toNode]));

            return IntStream.range(0,thoseWeights.length)
                            .mapToObj(weightsForToNode)
                            .toArray(OneNodesFanningInWeights[]::new);
    }

    @Override
    public boolean consistentWith(int[] structure)
    {
        if(weights.length==0 && structure.length<=1) return true;
        if(structure.length != weights.length+1) return false;
        if(weights.length>0 && weights[0].length>0) 
            if(weights[0][0].size() != structure[0]) return false;
        
        for(int layer=0; layer<weights.length; layer++)
        {
            if(weights[layer].length != structure[layer+1]) return false;
        }
        return true;
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
        if( variety > 1 ) return false;

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

        Double[][][] retval = Stream.of(xxx).map(xx).toArray(Double[][][]::new);

        assert xxx.length == retval.length;

        return retval;
    }

    /**
     * @param adjuster amount to add to the weight
     */
    @Override
    public Weights nextWeights(Weights.ThreeIntFunction<Double> adjuster)
    {
        var array = IntStream.range(1,weights.length+1)
                             .mapToObj(layer->newLayerOfWeights(layer,adjuster))
                             .toArray(double[][][]::new);
        return new SimpleWeights(array);
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

    @Override
    public double getWeight(int layer, int outputNode, int inputNode)
    {
        java.util.Objects.checkIndex(layer, weights.length);
        java.util.Objects.checkIndex(outputNode, weights[layer].length);

        return weights[layer][outputNode].get(inputNode);
    }

    private OneNodesFanningInWeights getFanningInWeights(
        int layer,
        int outputNode)
    {
        // don't need to make a copy; OneNodesFanningInWeights is immutable
        return weights[layer][outputNode];
    }

    @Override
    public double[][][] toPrimitives()
    {
        return Arrays.stream(weights)
                     .map(SimpleWeights::layerAsPrimitives)
                     .toArray(double[][][]::new);
    }

    private static double[][] layerAsPrimitives(OneNodesFanningInWeights[] arr)
    {
        return Arrays.stream(arr)
                     .map(OneNodesFanningInWeights::toPrimitiveArray)
                     .toArray(double[][]::new);
    }

    @Override public int hashCode()
    {
        return Arrays.deepHashCode(weights);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;
        SimpleWeights that = (SimpleWeights) o;
        return java.util.Objects.deepEquals(this.weights,that.weights);
    }

    /**
     * A value class representing
     * a collection of weights
     * that are the input to a single node.
     */
    private class OneNodesFanningInWeights
    {
        private List<Double> fanningIn;
        /**
         * The class may keep only a reference to the provided weights,
         * so weights should be immutable or effectively immutable.
         */
        OneNodesFanningInWeights( List<Double> weights )
        {
            fanningIn = new ArrayList<Double>(weights);
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

        public int size()
        {
            return fanningIn.size();
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

        @Override public int hashCode()
        {
            return fanningIn.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if(this == o) return true;
            if(o == null) return false;
            if(getClass() != o.getClass()) return false;
            OneNodesFanningInWeights that = (OneNodesFanningInWeights) o;
            return java.util.Objects.equals(this.fanningIn,that.fanningIn);
        }

    }
}


