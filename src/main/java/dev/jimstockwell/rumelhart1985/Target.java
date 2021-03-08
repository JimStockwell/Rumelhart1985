package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

class Target
{
    final Double[] target;
    Target(Double[] target)
    {
        // TODO: safe and simple, or better make a copy?
        this.target = target;
    }
    Target(double[] target)
    {
        this.target = Arrays.stream(target).boxed().toArray(Double[]::new);
    }
    Double getTarget(int node)
    {
        return target[node];
    }
}


