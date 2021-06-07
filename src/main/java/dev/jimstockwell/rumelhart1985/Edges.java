package dev.jimstockwell.rumelhart1985;

import java.util.stream.IntStream;
import java.util.function.ToDoubleFunction;
import java.util.Arrays;
import java.util.Objects;

public final class Edges
{
    final static class Coordinates {
        private final int layer; // layer of weights, 0 is from inputs
        private final int outNode; // node in "to" layer, output-ward layer
        private final int inNode; // node in "from" layer, input-ward layer
        public Coordinates(int layer, int outNode, int inNode)
        {
            this.layer=layer;
            this.outNode=outNode;
            this.inNode=inNode;
        }
        int getLayer() { return layer; }
        int getOutNode()  { return outNode; }
        int getInNode()  { return inNode; }

        @Override
        public boolean equals(Object o)
        {
            if(this == o) return true;
            if(o == null) return false;
            if(!(o instanceof Coordinates)) return false;
            Coordinates that = (Coordinates) o;
            return
                that.layer == this.layer &&
                that.outNode == this.outNode &&
                that.inNode == this.inNode;
        }

        @Override
        public int hashCode()
        {
            int result = Integer.hashCode(layer);
            result = result * 31 + Integer.hashCode(outNode);
            result = result * 31 + Integer.hashCode(inNode);

            return result;
        }
    }

    /**
     * The functional type used to populate the this edges class
     */ 
    @FunctionalInterface
    public interface Populator extends ToDoubleFunction<Coordinates> {}

    private final int[] structure;
    private final double[][][] edges;

    /**
     * Constructs edges between adjacent layers of nodes,
     * initialized by the specified function.
     * This is the classic arrangement in weights
     * in a neural network.
     *
     * @param structure an array representing the number of nodes in each layer
     * @param f         the Populator function to use
     *                  to populate the edges with values
     * @throws          IllegalArgumentException
     *                  if stucture contains negative values
     */
    public Edges(int[] structure, Populator f)
    {
        Objects.requireNonNull(structure);
        Objects.requireNonNull(f);

        this.structure = Arrays.copyOf(structure,structure.length);

        Arrays.stream(this.structure)
            .filter(val -> val<0)
            .forEach(val -> {
                throw new IllegalArgumentException(
                    "structure element should be less than zero, "+
                    "but was [" + val +"]");
            });

        edges = makeAllEdges(f);
    }

    private double[][][] makeAllEdges(Populator f)
    {
        //
        // length - 1
        // because if there are two nodes,
        // they onnly need one edge to connect them.
        //
        final int layerCount = structure.length - 1;

        return IntStream
                .range(0,layerCount)
                .mapToObj(layer -> makeLayerOfEdges(layer,f))
                .toArray(double[][][]::new);
    }

    private double[][] makeLayerOfEdges(int layer, Populator f)
    {
        final int outNodeCount = structure[layer+1];
        return IntStream
            .range(0,outNodeCount)
            .mapToObj(outNode -> makeInboundEdges(layer,outNode,f))
            .toArray(double[][]::new);
    }

    private double[] makeInboundEdges(int layer, int outNode, Populator f)
    {
        final int inNodeCount = structure[layer];
        return IntStream
            .range(0,inNodeCount)                   // iterat input nodes
            .mapToDouble(inNode -> f.applyAsDouble( // get weight for it
                new Coordinates(layer,outNode,inNode)))
            .toArray();                             // gather weights together
    }

    /**
     * Returns the value for a specified edge
     * @param c the coordinates of the desired edge 
     * @return  the value for the specified edge
     * @throws  IndexOutOfBoundsException
     *          if the coordinate (specifying the edge) is out of bounds
     */
    public double get(Coordinates c)
    {
        Objects.checkIndex(c.layer,edges.length);
        Objects.checkIndex(c.outNode,edges[c.layer].length);
        Objects.checkIndex(c.inNode,edges[c.layer][c.outNode].length);

        return edges[c.layer][c.outNode][c.inNode];
    }

    /**
     * Returns the structure as a whole.
     * Such a structure is helpful for creating a new Edges.
     *
     * @return a copy of the structure used to create this Edges object.
     */
    public int[] getStructure()
    {
        return Arrays.copyOf(structure,structure.length);
    }
    
    /**
     * Returns the number of <b>node</b> layers.
     * So, if there are two layers of nodes, input and output,
     * with a single layer of edges between,
     * a count of two is returned.
     *
     * @return  the number of node layers
     */
    public int numberOfNodeLayers()
    {
        return structure.length;
    }

    /**
     * Returns the number of nodes in the specified nodes layer.
     *
     * @param layer the index of the layer at issue, 0=input layer
     * @return      the number of nodes in the specified layer
     */
    public int sizeOfNodeLayer(int layer)
    {
        java.util.Objects.checkIndex(layer,structure.length);
        return structure[layer];
    }

    /**
     * Returns whether this Edges is consistent with the specified structure.
     * They are consistent if they are the same.
     *
     * @param structure the structure to report consistency against
     * @return          true if the specified structure equals
     *                  this object's specified structure.
     */
    public boolean consistentWith(int[] structure)
    {
        return Arrays.equals(this.structure,structure);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null) return false;
        if(!(o instanceof Edges)) return false;
        Edges that = (Edges) o;
        return
            Arrays.equals(that.structure,this.structure) &&
            Arrays.deepEquals(that.edges, this.edges);
    }

    @Override
    public int hashCode()
    {
        int result = Arrays.hashCode(structure);
        result = result * 31 + Arrays.deepHashCode(edges);

        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int level=0; level<edges.length; level++)
        {
            if(level != 0)
            {
                sb.append(", ");
            }
            sb.append(Arrays.toString(edges[level]));
        }
        sb.append("]");
        return sb.toString();
    }
}
