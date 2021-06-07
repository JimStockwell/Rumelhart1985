package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.Random;


public class EdgeWeightsTest implements WeightsTest<EdgeWeights>
{
    @Override
    public EdgeWeights createWeights(double[][][] w)
    {
        return new EdgeWeights(w);
    }

    @Override
    public EdgeWeights createWeights(Double[][][] w)
    {
        return new EdgeWeights(w);
    }

    @Override
    public EdgeWeights createWeights(
        int[] s,
        Weights.ThreeIntFunction<Double> f)
    {
        return new EdgeWeights(s,f);
    }
}

