package dev.jimstockwell.rumelhart1985;

import java.util.List;

public interface Deltas
{
    /**
     * Gets the specified delta.
     * @param layer the index of the layer of interest.
     *              Layer 0 is the first layer with deltas,
     *              that is, the first layer after the input layer. 
     * @param node  the index of the node of interest
     * @return      the delta of the specified layer and node
     */
    double getDelta(int layer, int node);

    /**
     * Gets a potentially unmodifyable List of the size of the Deltas structure.
     */
    List<Integer> size();
}
