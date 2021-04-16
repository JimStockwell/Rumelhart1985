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
interface Weights
{
    // TODO: make this an instance method
    // so that it can be overridden.
    static Weights defaultW(int[] structure, double scale)
    {
        var asArray = new DefaultWeightsMaker(structure,scale).get();
        return new SimpleWeights(asArray);
    }

    boolean consistentWith(int[] structure);

    /**
     * @param adjuster amount to add to the weight
     */
    Weights nextWeights(Weights.ThreeIntFunction<Double> adjuster);

    @FunctionalInterface
    interface ThreeIntFunction<T> {
        abstract T getAdjustment(int layer, int outNode, int inNode);
    }

    double getWeight(int layer, int outputNode, int inputNode);

    double[][][] toPrimitives();
}

