package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * Maintains a copy of the provided node outputs
 */
final class Outputs
{
    final Nodes nodes;

    Outputs(double[][] outputs)
    {
        nodes = new Nodes(
            Stream.of(outputs).mapToInt(level -> level.length).toArray(),
            coord -> outputs[coord.getLayer()][coord.getNode()]
        );
    }

    /**
     * Gets the output of the specified node
     * @param layer the layer index of unit of interest.
     *              Zero is the input layer.
     * @param node  the node index of the unit of interest
     */
    double get(int layer, int node)
    {
        return nodes.get(layer, node);
    }

    /**
     * Returns a copy of the final layer of output.
     */
    Optional<double[]> getLastLayer()
    {
        return nodes.getLastLayer(); // this is a copy
    }

    int countOfLayersExcludingInput()
    {
        return nodes.numberOfLayers()-1;
    }

    int sizeOfNonInputLayer(int layer)
    {
        return nodes.sizeOfLayer(layer+1);
    }

    int[] sizes()
    {
        return nodes.getStructure();
    }
}

