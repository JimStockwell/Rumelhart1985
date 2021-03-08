package dev.jimstockwell.rumelhart1985;

import java.util.List;

public interface Deltas
{
    /**
     * Gets the specified delta.
     */
    double getDelta(int layer, int node);

    /**
     * Gets a potentially unmodifyable List of the size of the Deltas structure.
     */
    List<Integer> size();
}
