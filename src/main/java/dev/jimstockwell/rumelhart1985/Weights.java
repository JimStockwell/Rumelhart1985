package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.function.IntFunction;


class Weights
{
    // TODO: do we need our own copy of weights?

    //[layer above input][node]
    private OneNodesFanningInWeights[][] weightsNew;

    Weights(double[][][] weights)
    {
        this(mapBoxed3Deep(weights));
    }

    Weights(Double[][][] weights)
    {
        if(!consistent(weights)) throw new IllegalArgumentException();

        this.weightsNew = new OneNodesFanningInWeights[weights.length][];
        for(int layer=0; layer<weightsNew.length; layer++)
        {
            weightsNew[layer] =
                new OneNodesFanningInWeights[weights[layer].length];
            for(int toNode=0; toNode<weights[layer].length; toNode++)
            {
                weightsNew[layer][toNode] =
                    new OneNodesFanningInWeights(
                        java.util.List.of(weights[layer][toNode]));
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
        if( weightsNew.length==0 ) throw new IllegalStateException();

        int[] struct = new int[weightsNew.length+1]; // +1 for input layer
        struct[0] = weightsNew[0][0].fanningIn.size();
        for(int i=0; i<weightsNew.length; i++)
        {
            struct[i+1] = weightsNew[i].length;
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

    private static Double[][][] mapBoxed3Deep(double[][][] xxx)
    {
        return Arrays.stream(xxx).map(Weights::mapBoxed2Deep)
                                 .toArray(Double[][][]::new);
    }

    private static Double[][] mapBoxed2Deep(double[][] xx)
    {
        return Arrays.stream(xx).map(Weights::mapBoxed1Deep)
                                .toArray(Double[][]::new);
    }

    private static Double[] mapBoxed1Deep(double[] x)
    {
        return Arrays.stream(x).boxed().toArray(Double[]::new);
    }

    double getWeight(int layer, int outputNode, int inputNode)
    {
        java.util.Objects.checkIndex(layer, weightsNew.length);
        java.util.Objects.checkIndex(outputNode, weightsNew[layer].length);

        return weightsNew[layer][outputNode].get(inputNode);
    }

    OneNodesFanningInWeights getFanningInWeights(int layer, int outputNode)
    {
        return weightsNew[layer][outputNode];
    }

    double[][][] toPrimitives()
    {
        return Arrays.stream(weightsNew)
                     .map(Weights::layerAsPrimitives)
                     .toArray(double[][][]::new);
    }

    static double[][] layerAsPrimitives(OneNodesFanningInWeights[] arr)
    {
        return Arrays.stream(arr)
                     .map(OneNodesFanningInWeights::toPrimitiveArray)
                     .toArray(double[][]::new);
    }

    // TODO: In Network, construct this in one pass.
    class OneNodesFanningInWeights
    {
        List<Double> fanningIn;
        /**
         * The class may keep only a reference to the provided weights,
         * so weights should be immutable or effectively immutable.
         */
        OneNodesFanningInWeights( List<Double> weights )
        {
            fanningIn = weights;
        }

        public Double get(int index)
        {
            return fanningIn.get(index);
        }

        OneNodesFanningInWeights withAdjustment(IntFunction<Double> adjuster)
        {
            List<Double> inProgress = new ArrayList<>();
            for(int i=0; i<fanningIn.size(); i++)
            {
                inProgress.add( fanningIn.get(i) + adjuster.apply(i) );
            }
            return new OneNodesFanningInWeights(inProgress);
        }

        double[] toPrimitiveArray()
        {
            return fanningIn.stream()
                            .mapToDouble(Number::doubleValue)
                            .toArray();
        }
    }
}

