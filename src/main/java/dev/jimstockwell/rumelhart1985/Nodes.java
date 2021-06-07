package dev.jimstockwell.rumelhart1985;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.function.ToDoubleFunction;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class Nodes
{
    final static class Coordinates {
        private final int layer;
        private final int node;
        public Coordinates(int layer, int node)
        {
            this.layer=layer;
            this.node=node;
        }
        int getLayer() { return layer; }
        int getNode()  { return node; }
    }

    @FunctionalInterface
    public interface Populator extends ToDoubleFunction<Coordinates> {}

    private final int[] structure;
    private final double[][] nodes;

    /**
     * Constructs a Nodes object based on an array representation.
     *
     * @param content an array of the contents of the Nodes.
     */
    public Nodes(double[][] content)
    {
        structure = Stream.of(content)
                          .mapToInt(level -> level.length)
                          .toArray();

        nodes = ArraysExtended.twoDCopyOf(content);
    }

    /**
     * Constructor specifying the structure of the Nodes
     * and a formula to use to populate each node.
     *
     * @param structure an array of counts of nodes on each layer.
     *                  So, for example, {1, 3} indicates
     *                  a set of nodes with one node on the first layer
     *                  and three nodes on the second layer.
     * @param f         a function applied to each node
     *                  to provide a value for the node.
     * @throws IllegalArgumentException if any element of
     *                  <code>structure</code> is negative.
     */
    public Nodes(int[] structure, Populator f)
    {
        this.structure = Arrays.copyOf(structure, structure.length);
        checkStructure(this.structure);

        nodes = makeAllNodes(f);
    }

    private static void checkStructure(int[] structure)
    {
        for(int count : structure)
        {
            if(count < 0) throw new IllegalArgumentException(
                String.format("structure includes negative value %d",count));
        }
    }

    private double[][] makeAllNodes(Populator f)
    {
        return IntStream
                .range(0,structure.length)
                .mapToObj(layer -> makeLayerOfNodes(layer,f))
                .toArray(double[][]::new);
    }

    private double[] makeLayerOfNodes(int layer, Populator f)
    {
        return IntStream
                .range(0,structure[layer])
                .mapToDouble(
                    node -> f.applyAsDouble(new Coordinates(layer,node)))
                .toArray();
    }

    /**
     * Constructor specifying component layers.
     * @param nodes the array of Nodes to assemble
     *              into a single bigger set of nodes.
     *              Unsurprisingly, lower indexes
     *              correspond to being put closer to the input layer.
     * @throws IllegalArgumentException if <code>nodes</code>
     *              contains null references
     */
    public Nodes(Nodes[] nodes)
    {
        for(int i=0; i<nodes.length; i++)
        {
            if(nodes[i]==null)
                throw new IllegalArgumentException(String.format(
                    "Nodes constructor element %d is null", i));
        }

        structure = Arrays.stream(nodes)
            .flatMapToInt(n -> Arrays.stream(n.structure))
            .toArray();
        this.nodes = Arrays.stream(nodes)
            .flatMap(n -> Arrays.stream(n.nodes))
            .toArray(double[][]::new);
    }

    /**
     * Gets the value of the node with the specified Coordinates.
     */
    public double get(Coordinates c)
    {
        Objects.checkIndex(c.layer,nodes.length);
        Objects.checkIndex(c.node,nodes[c.layer].length);

        return nodes[c.layer][c.node];
    }

    /**
     * Gets the value of the node with the specified layer and node indexes.
     * @param layer layer index of the node of interest
     * @param node node index of the node of interest
     * @return value of the specified node
     */
    public double get(int layer, int node)
    {
        Objects.checkIndex(layer,nodes.length);
        Objects.checkIndex(node,nodes[layer].length);

        return nodes[layer][node];
    }

    /**
     * Returns an Optional copy of the last layer.
     * Optional because a zero length network has no output layer.
     */
    public Optional<double[]> getLastLayer()
    {
        if(nodes.length == 0)
        {
            return Optional.empty();
        }

        final int lastIndex = nodes.length-1;
        double[] copyOfLastLayer = nodes[lastIndex].clone();

        return Optional.of(copyOfLastLayer);
    }

    public int[] getStructure()
    {
        return structure.clone();
    }

    /**
     * Gets the number of layers of Nodes.
     */
    public int numberOfLayers()
    {
        return structure.length;
    }

    /**
     * Gets the number of nodes on the specified layer.
     *
     * @param layer the index of the layer.
     */
    public int sizeOfLayer(int layer)
    {
        Objects.checkIndex(layer,structure.length);
        return structure[layer];
    }
}
