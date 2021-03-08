package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


class Weights
{
    // TODO: do we need our own copy of weights?
    Double[][][] weights;

    Weights(Double[][][] weights)
    {
        this.weights = weights;
    }

    Weights(double[][][] weights)
    {
        this.weights = mapBoxed3Deep(weights);
    }
    
    private Double[][][] mapBoxed3Deep(double[][][] xxx)
    {
        return Arrays.stream(xxx).map(this::mapBoxed2Deep)
                                 .toArray(Double[][][]::new);
    }

    private Double[][] mapBoxed2Deep(double[][] xx)
    {
        return Arrays.stream(xx).map(this::mapBoxed1Deep)
                                .toArray(Double[][]::new);
    }

    private Double[] mapBoxed1Deep(double[] x)
    {
        return Arrays.stream(x).boxed().toArray(Double[]::new);
    }

    double getWeight(int layer, int outputNode, int inputNode)
    {
        return weights[layer][outputNode][inputNode];
    }
}

