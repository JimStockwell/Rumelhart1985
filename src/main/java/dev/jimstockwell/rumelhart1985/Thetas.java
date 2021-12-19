package dev.jimstockwell.rumelhart1985;

import java.util.function.BiFunction;

/**
 * Represents the thetas in a network.
 * (Thetas are the position parameter for the activation function.)
 *
 * Each given object is expected to return consistent results.
 * This is most simply done with an immutable class.
 */
public interface Thetas
{
    /**
     * Reports consistency of this vs a specified structure.
     * @param structure the structure to check consistency against
     * @return          true when the given structure is consistent with
     *                  this data.
     */
    boolean consistentWith(int[] structure);

    /**
     * Returns a Theta with the same sturcture as this Theta,
     * but populated by the specified function.
     *
     * @param populator the function to populate the new Weights with
     * @return a new Thetas with the specified values
     */
    Thetas populate(BiFunction<Integer,Integer,Double> populator);

    /**
     * Returns a new Thetas object
     * that is the result of element wise adding a new Weight to this Weight.
     *
     * @param addend    the Thetas to add to this Thetas object
     * @return          the sum of the addend and this Thetas object
     *
     */
    default Thetas add(Thetas addend) {
        return populate((l,n)->this.getTheta(l,n)+addend.getTheta(l,n));
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
    double getTheta(int layer, int node);

    /**
     * Returns a two index array of the theta values.
     * Temporary, for interoperability with old version of thetas.
     * 
     * @return  thetas as a two index array
     */
    @Deprecated()
    double[][] value();

    /**
     * Returns the number of <b>theta</b> layers.
     *
     * @return  the number of theta layers.
     */
    int numberOfThetaLayers();
    
    /**
     * Returns the size of the specified thetas layer.
     *
     * @param index the index of the thetas layer.
     * @return      the quantity of output nodes in the given layer
     */
    int sizeOfThetaLayer(int index);

    /**
     * Checks if the thetas are internally consistent.
     * This is for similarity to Weights.
     * For theta, this really does nothing.
     *
     * @param   weights the 2D array of double thetas
     * @return  the weights if they are internally consistent
     */
    static double[][] checkConsistent(double[][] thetas)
    {
        return thetas;
    }
}


