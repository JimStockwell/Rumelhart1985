package dev.jimstockwell.rumelhart1985;

import java.util.List;

/**
 * The deltas found in back progagation of a neural net.
 *
 * There is one delta for each node in the network,
 * except there are no deltas for the input node.
 */
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
     * @return a list of the number of nodes on each layer.
     *         Layer 0 is the first layer with deltas, that is,
     *         the first layer after the input layer.
     */
    List<Integer> size();
}
