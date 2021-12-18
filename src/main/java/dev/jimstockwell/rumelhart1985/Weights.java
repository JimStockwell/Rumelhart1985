package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;

/**
 * Represents the weights in a network.
 *
 * Each given object is expected to return consistent results.
 * This is most simply done with an immutable class.
 * <p>
 * Does not include node thetas.
 */
public interface Weights
{
    /**
     * A functional interface for returning a value given a weight ID
     */
    @FunctionalInterface
    interface ThreeIntFunction<T> {
        /**
         * Determines a value given an edge coordinate
         * @param layer     the layer of the weight to determine a value for
         * @param outNode   the destination node of the weight
         *                  to determine a value for
         * @param inNode    the source node of the weight
         *                  to determine a value for
         * @return          the determined value
         */
        abstract T apply(int layer, int outNode, int inNode);
    }

    /**
     * Reports consistency of this Weights vs a specified structure.
     * @param structure the structure to check consistency against
     * @return          true when the structure is consistent with
     *                  this Weights data.
     */
    boolean consistentWith(int[] structure);

    /**
     * Returns a Weights with the same sturcture as this Weights,
     * but populated by the specified function.
     *
     * @param populator the function to populate the new Weights with
     * @return a new Weights with the specified values
     */
    Weights populate(Weights.ThreeIntFunction<Double> populator);

    /**
     * Returns a new Weights object
     * that is the result of element wise adding a new Weight to this Weight.
     *
     * @param addend    the Weights to add to this Weights object
     * @return          the sum of the addend and this Weights object
     *
     */
    default Weights add(Weights addend) {
        return populate((l,o,i)->this.getWeight(l,o,i)+addend.getWeight(l,o,i));
    }

    /**
     * Returns an individual weight in the Weights.
     *
     * @param layer         0 is the layer of weights
     *                      connected to the network inputs
     * @param outputNode    the node in the layer closer to network output
     * @param inputNode     the node in the layer closer to network input
     * @return              the value of the specified weight
     */
    double getWeight(int layer, int outputNode, int inputNode);

    /**
     * Returns the number of <b>weight</b> layers.
     * That is, one less than the number of node layers.
     * (Validated by ArrayLayers implementation.)
     *
     * @return  the number of weight layers.
     */
    int numberOfWeightLayers();
    
    /**
     * Returns the size of the specified weights layer
     * as measured by its "to" layer.
     *
     * @param index the index of the weights layer.
     *              0 is the layer of weights coming from the input layer.
     * @return      the quantity of output nodes in the given layer
     */
    int sizeOfWeightLayer(int index);

    /**
     * An exception thrown
     * when the input layer of a weights array is not self consistent.
     */
    @SuppressWarnings("serial")
    public class InputLayerConsistencyException extends IllegalArgumentException
    {
        /**
         * Constructs a consistency exception.
         *
         * @param inputCount1   one of two inconsistent (non equal) input counts
         * @param inputCount2   the second of two inconsistent input counts
         */
        public InputLayerConsistencyException(int inputCount1, int inputCount2)
        {
            super(String.format("inputCount1: %d, inputCount2: %d",
                inputCount1, inputCount2));
        }
    }

    /**
     * An exception thrown
     * when two adjacent layers of a weights array are not self consistent.
     */
    @SuppressWarnings("serial")
    public class ConsistencyException extends IllegalArgumentException
    {
        /**
         * Constructs a consistency exception.
         *
         * @param outLayer      index of the weight layer
         *                      containing the inconsistency
         * @param outNode       index of the output (to) node
         *                      with an input count mismatch
         * @param insPerNode    quantity of input nodes according to
         *                      the output node
         * @param insPerLayer   quantity of input nodes according to 
         *                      the number of nodes in the prior layer
         */
        public ConsistencyException(
            int outLayer,
            int outNode,
            int insPerNode,
            int insPerLayer)
        {
            super(String.format(
                "outLayer: %d, outNode: %d, insPerNode: %d, insPerLayer: %d",
                outLayer, outNode, insPerNode, insPerLayer));
        }
    }

    /**
     * Checks if the weights are internally consistent.
     *
     * @param   weights the 3D array of double weights
     * @return  the weights if they are internally consistent
     * @throws  Weights.InputLayerConsistencyException
     *          If layer 0 is not self consistent
     * @throws  Weights.ConsistencyException
     *          If two adjacent layers are not self consistent
     */
    static double[][][] checkConsistent(double[][][] weights)
    {
        // If there isn't even a layer 0,
        // it's kind of a boring set of weights,
        // but it is self consistent!

        if(weights.length == 0) return weights;

        //
        // Layer 0 doesn't need to match anything in weights
        // because there is no layer -1, that is, no input layer of nodes.
        // So instead, we check that each node at least has a consistent
        // number of inputs.
        //
        int[] variety =
            Arrays.stream(weights[0]).mapToInt(outNode->outNode.length)
                                     .distinct()
                                     .toArray();
        if( variety.length > 1)
            throw new Weights.InputLayerConsistencyException(variety[0],variety[1]);

        //
        // Each node on layer x should have an input from EACH node
        // on layer x-1.
        // Therefore, each node on layer x should have the same number
        // of inputs as there are nodes on layer x-1.
        // The following code checks this.
        //
        for(int layer=1; layer<weights.length; layer++)
        {
            for(int node=0; node<weights[layer].length; node++)
            {
                if( weights[layer][node].length != weights[layer-1].length)
                {
                    throw new ConsistencyException(
                        layer,
                        node,
                        weights[layer][node].length,
                        weights[layer-1].length);
                }
            }
        }
        return weights;
    }
}

