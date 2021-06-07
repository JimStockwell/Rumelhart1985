package dev.jimstockwell.rumelhart1985;

import java.util.function.ToIntFunction;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Value class representing the weights in a network.
 * Does not include node thetas.
 */
class EdgeWeights implements Weights
{
    final private Edges edges;

    EdgeWeights(double[][][] weights)
    {
        Objects.requireNonNull(weights);
        Weights.checkConsistent(weights);

        // Determine the "structure" of the specified weights
        ToIntFunction<double[][]> countOfNodes = nodes -> nodes.length;
        IntStream nonInputLayers = Arrays.stream(weights)
            .mapToInt(countOfNodes);
        IntStream inputLayer;
        if( weights.length > 0 && weights[0].length > 0)
        {
            inputLayer = IntStream.of(weights[0][0].length);
        }
        else
        {
            inputLayer = IntStream.empty();
        }
        int[] structure = IntStream.concat(inputLayer,nonInputLayers).toArray();

        // A function to populate the Edges based on the specified weights
        Edges.Populator populator =
            c -> weights[c.getLayer()][c.getOutNode()][c.getInNode()];

        this.edges = new Edges(structure,populator);
    }

    private static double[][][] toPrimitives(Double[][][] source)
    {
        ToDoubleFunction<Double> unboxer = wrapped -> wrapped.doubleValue();

        Function<Double[],double[]> x =
            (Double[] arr) -> Stream.of(arr).mapToDouble(unboxer).toArray();

        Function<Double[][], double[][]> xx =
            (Double[][] arr) -> Stream.of(arr).map(x).toArray(double[][]::new);

        double[][][] retval =
            Stream.of(source).map(xx).toArray(double[][][]::new);

        return retval;
    }

    EdgeWeights(Double[][][] weights)
    {
        this(toPrimitives(Objects.requireNonNull(weights)));
    }

    EdgeWeights(int[] structure, Weights.ThreeIntFunction<Double> f)
    {
        this(structure,
            c -> f.apply(c.getLayer(),c.getOutNode(),c.getInNode()));
    }

    EdgeWeights(int[] structure, Edges.Populator populator)
    {
        this.edges = new Edges(structure,populator);
    }

    @Override
    public Weights populate( Weights.ThreeIntFunction<Double> f)
    {
        return new EdgeWeights(edges.getStructure(), f);
    }

    @Override
    public boolean consistentWith(int[] structure)
    {
        return edges.consistentWith(structure);
    }

    @Override
    public double getWeight(int layer, int outputNode, int inputNode)
    {
        return edges.get(new Edges.Coordinates(layer,outputNode,inputNode));
    }

    @Override
    public int numberOfWeightLayers()
    {
        return edges.numberOfNodeLayers() - 1;
    }

    @Override
    public int sizeOfWeightLayer(int layer)
    {
        // Weight layer 0 == node layer 1
        return edges.sizeOfNodeLayer(layer+1);
    }

    @Override public int hashCode()
    {
        return edges.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;
        EdgeWeights that = (EdgeWeights) o;
        return this.edges.equals(that.edges);
    }

    @Override
    public String toString()
    {
        return edges.toString();
    }
}



