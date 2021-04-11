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
