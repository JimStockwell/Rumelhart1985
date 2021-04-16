package dev.jimstockwell.rumelhart1985;

import java.util.function.IntFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.Random;

class DefaultWeightsMaker
{
    int[] structure;
    double scale;

    DefaultWeightsMaker(int[] structure, double scale)
    {
        this.structure = structure;
        this.scale = scale;
    }

    /**
     * Returns an array of weights
     * corresponding to the structure the instance was initialized with.
     *
     * A {0,0} structure
     * will return an array with one layer,
     * having zero output nodes:
     * double[1][0][].
     *
     */
    double[][][] get()
    {
        if(structure.length==0) return new double[0][][];

        IntFunction<double[][]> generateLayerFromIndex =
            layer -> generateLayerFromCounts(
                structure[layer],
                structure[layer+1]);

        return IntStream.range(0,structure.length-1)
                        .mapToObj(generateLayerFromIndex)
                        .toArray(double[][][]::new);
    }

    /**
     * Returns a layer of weights connecting two layers.
     *
     * The weights are indexed first by destination node
     * then by source node.
     *
     * A layer with 0 in and 0 out will return
     * a double[][] where the length of the first index is 0.
     * 
     * @param from the number of nodes on the layer closer to input
     * @param to the number of nodes on the layer closer to output
     * @returns a layer of weights
     */
    private double[][] generateLayerFromCounts( int from, int to)
    {
        final Random rnd = new Random();

        Supplier<double[]> inGenerator =
            () -> DoubleStream.generate(() -> rnd.nextDouble()*scale)
                              .limit(from)
                              .toArray();
        
        return Stream.generate(inGenerator)
                     .limit(to)
                     .toArray(double[][]::new);
    }
}
