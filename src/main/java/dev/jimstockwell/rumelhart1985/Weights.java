package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.function.IntFunction;


class Weights
{
    // TODO: do we need our own copy of weights?
    private OneNodesFanningInWeights[][] weightsNew;

    Weights(Double[][][] weights)
    {
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

    Weights(double[][][] weights)
    {
        this(mapBoxed3Deep(weights));
    }

    Weights(OneNodesFanningInWeights[][] weights)
    {
        weightsNew = weights;
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

