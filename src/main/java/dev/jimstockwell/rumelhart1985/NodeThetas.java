package dev.jimstockwell.rumelhart1985;

import java.util.function.ToIntFunction;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Value class representing the weights in a network.
 * Does not include node thetas.
 */
class NodeThetas implements Thetas
{
    final private Nodes nodes;

    NodeThetas(double[][] thetas)
    {
        Objects.requireNonNull(thetas);
        Thetas.checkConsistent(thetas);

        int[] structure = Stream.of(thetas)
                                .mapToInt(a->a.length)
                                .toArray();

        // A function to populate the Nodes based on the specified thetas
        Nodes.Populator populator = c -> thetas[c.getLayer()][c.getNode()];

        this.nodes = new Nodes(structure,populator);
    }

/*
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
*/

/*
    EdgeWeights(Double[][][] weights)
    {
        this(toPrimitives(Objects.requireNonNull(weights)));
    }
*/

    NodeThetas(int[] structure, BiFunction<Integer,Integer,Double> f)
    {
        this(structure,
            c -> f.apply(c.getLayer(),c.getNode()));
    }

    NodeThetas(int[] structure, Nodes.Populator populator)
    {
        this.nodes = new Nodes(structure,populator);
    }

    @Override
    public Thetas populate( BiFunction<Integer,Integer,Double> f)
    {
        return new NodeThetas(nodes.getStructure(), f);
    }

    @Override
    public boolean consistentWith(int[] structure)
    {
        return nodes.consistentWith(structure);
    }

    @Override
    public double getTheta(int layer, int node)
    {
        return nodes.get(new Nodes.Coordinates(layer,node));
    }

    @Override
    public int numberOfThetaLayers()
    {
        return nodes.numberOfLayers();
    }

    @Override
    public int sizeOfThetaLayer(int layer)
    {
        // Theta layer 0 == layer after Inputs
        return nodes.sizeOfLayer(layer);
    }

    @Override public int hashCode()
    {
        return nodes.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;
        NodeThetas that = (NodeThetas) o;
        return this.nodes.equals(that.nodes);
    }

    @Override
    public String toString()
    {
        return nodes.toString();
    }

    @Override
    public double[][] value() {
        return nodes.value();
    }
}




