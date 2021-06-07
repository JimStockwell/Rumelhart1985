package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import java.util.Objects;

class Target
{
    private final double[] target;

    /**
     * Constructs a Target from an array of Doubles.
     * @param target    an array of Doubles specifying the target,
     *                  one per output node
     * @throws IllegalArgumentException if target contains any nulls
     */
    Target(Double[] target)
    {
        checkNoNulls(target);

        this.target = Arrays.stream(target)
                            .mapToDouble(Double::doubleValue)
                            .toArray();
    }

    private void checkNoNulls(Double[] x)
    {
        if(Arrays.stream(x).anyMatch(e -> e == null))
        {
            throw new IllegalArgumentException(
                "target contains a null reference");
        }
    }

    Target(double[] target)
    {
        this.target = Arrays.copyOf(target, target.length);
    }

    /**
     * Gets the target value for the specified output node.
     *
     * @param node  the index of the output node of interest
     * @return      the target value for the indicated node
     */
    double getTarget(int node)
    {
        return target[node];
    }

    /**
     * Returns the size of the target vector
     */
    int size()
    {
        return target.length;
    }
}


