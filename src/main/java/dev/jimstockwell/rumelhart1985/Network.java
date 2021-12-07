package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.twoDCopyOf;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.threeDCopyOf;

/**
 * Represents a neural network as described by Rumelhart in his 1985 paper.
 *
 * A design decision: Every network must have  an input and an output layer,
 * at minimum.
 * It is not required that the input or output layers have nodes.
 */
public final class Network
{
    //
    // Some comments on the structure this network imposes...
    //
    // The input units have no transfer function
    // the hidden units and output units do have a transfer function
    //
    // Weighted connections go from the input units,
    // to the hidden units, between the layers of hidden units,
    // and between the last layer of hidden units and the output units.
    // There are no weights from the output units.
    //

    //
    // Not all the following members are 3 indexes deep,
    // but for as many indexes as one does have,
    // the following structure is used:
    //
    // first index = layer in network
    // second index = unit within layer
    // third index = weight into that unit
    //
    
    private final int[] structure;  // layer 0 is inputs;
    private final double eta;         // gradient descent step size
    private final Weights weights;   
    private final double[][] theta; // layer 0 is first hidden units
    private final ActivationFunction activationFunction;

    /**
     * Any defensive copies are made and validation is done in Network,
     * so is not needed here.
     * Some methods do perform validation in the interest of fail fast.
     */
    public static final class Builder {
        private int[] structure = new int[] {0,0};
        private Weights w;
        private double[][] theta;
        private Double eta = 1.0;
        private ActivationFunction activationFunction = 
                new LogisticActivationFunction();

        /**
         * Returns a new Builder to make the specified Network.
         * If Network is every refactored into a subtype or subinterface,
         * "from" should stay with this specific implementation.
         * This is because Builder::from returns a builder
         * that may not be capable of representing just some arbitrary
         * Network interface.
         *
         * Should not need to validate Network's data
         * as the class is final.
         *
         * @param net   the network to copy
         * @return      a builder to build the specified network
         */
        public static Builder from(Network net)
        {
            Builder other = new Builder();
            other.structure = net.structure;
            other.w = net.weights;
            other.theta = net.theta;
            other.eta = net.eta;
            other.activationFunction = net.activationFunction;
            return other;
        }

        /**
         * Builds the specified Network.
         *
         * Weights and Theta defaults are set here
         * rather than in member variable declaration
         * because the defaults depend on "structure".
         * They are not set in withStructure because it isn't known there
         * whether defaults will be needed.
         * They are not set in the Network itself
         * because this builder class is responsible for
         * setting up the Network's parameters.
         *
         * @return the built Network
         */
        public Network build() {
            if(w==null) {
                Random rnd = new Random();
                w = new EdgeWeights(structure,(l,inNode,outNode)->rnd.nextDouble());
            }
            if(theta==null) {
                theta = defaultTheta(structure);
            }
            return new Network(this);
        }

        static double[][] defaultTheta(int[] structure)
        {
            final Random rnd = new Random();
            double[][] retval = new double[structure.length-1][];
            final int LAYER_AFTER_INPUT = 1;
            for(int layer=LAYER_AFTER_INPUT; layer<structure.length; layer++)
            {
                retval[layer-1] = new double[structure[layer]];
                for(int node=0; node<structure[layer]; node++)
                {
                    retval[layer-1][node] = rnd.nextDouble()*.01;
                }
            }
            return retval;
        }

        /**
         * Specifies the structure to use.
         *
         * @param structure the structure to use,
         *                  as specified by the number of nodes per layer,
         *                  input layer in array element 0.
         * @return this updated Builder
         */
        public Builder withStructure(int[] structure)
        {
            // Network should check too,
            // but we check here to fail fast.
            structureInvalidity(structure).ifPresent(
                s->{throw new IllegalArgumentException("structure: "+s);});

            this.structure = structure;
            return this;
        }

        /**
         * Specifies the Weights to use.
         *
         * @param w the Weights to use.
         *          It is expected not to be effectively immutable.
         *          It is required to be non-null,
         * 
         * @return  a reference to this builder
         */
        public Builder withW(Weights w)
        {
            this.w = Objects.requireNonNull(w);
            return this;
        }

        /**
         * Specifies the Theta values to use.
         *
         * @param theta the Theta values to use.
         *
         * @return a reference to this builder
         */
        public Builder withTheta(double[][] theta)
        {
            thetaIsValidOrThrow(theta);
            this.theta = twoDCopyOf(theta);
            return this;
        }

        private void thetaIsValidOrThrow(double[][] theta)
        {
            if(theta == null) throw new IllegalArgumentException();
            if(structure.length-1 != theta.length)
                throw new IllegalArgumentException();
            if(!structureLayersConsistentWithTheta(theta))
                throw new IllegalArgumentException();
        }

        private boolean structureLayersConsistentWithTheta(double[][] theta)
        {
            IntPredicate badLayer = i -> structure[i+1] != theta[i].length;

            return IntStream.range(0,structure.length-1)
                            .filter(badLayer)
                            .count() == 0;
        }

        /**
         * Specifies the Eta value, that is, the learning step size.
         *
         * @param eta the Eta values to use.
         *
         * @return a reference to this builder
         */
        public Builder withEta(double eta) { this.eta = eta; return this; }
    }

    /**
     * Builds a new Network from the specified Builder.
     * <p>
     * Builder's structure field is defensively copied here and validated.
     * <p>
     * Builder's w is not copied, and must be effectively immutable.
     * It is validated against structure.
     *
     * @param builder   the specified builder
     */
    public Network(Builder builder)
    {
        String nullBuilderMsg = "builder must not be null";
        if(builder==null) throw new NullPointerException(nullBuilderMsg);

        structure = Arrays.copyOf(builder.structure, builder.structure.length);
        structureInvalidity(structure).ifPresent(
            s->{throw new IllegalArgumentException("builder: "+s);}
        );

        eta = builder.eta;
        weights = builder.w;
        wIsValidOrThrow(weights);
        theta = builder.theta;
        activationFunction = builder.activationFunction;
    }

    /**
     * Validates a proposed "structure".
     * @param s the proposed structure
     * @return an Optional string describing any non-validity
     */
    static public Optional<String> structureInvalidity(int[] s)
    {
        if(s.length < 2)
            return Optional.of(
                "Require structure length >= 2 but is length "
                    + s.length);

        IntPredicate badLayer = count -> count < 0;
        if(IntStream.of(s).filter(badLayer).count() != 0)
            return Optional.of(
                "Structure counts must all be > 0 " +
                    "but at least one was not");

        return Optional.empty();
    }

    private void wIsValidOrThrow(Weights w)
    {
        if(w == null) throw new IllegalArgumentException();

        if(structure != null) {
            if(structure.length-1 != w.numberOfWeightLayers())
            {
                String msg = "Structure length [" + structure.length
                    + "] is required to be one greater than w's number "
                    + "of weight layers ["+w.numberOfWeightLayers()+"]."
                    + " Structure is: "+Arrays.toString(structure);
                throw new IllegalArgumentException(msg);
            }

            if(!w.consistentWith(structure))
                    throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the weights in use, as a Weights type.
     * <p>
     * Strandard implementations of Weights are immutable.
     * If a particular implementation is not,
     * then it should at least act like it is.
     * Thus, we return a reference to the original rather than a copy.
     *
     * @return  the weights in use, as a Weights type
     */
    public Weights getW()
    {
        return this.weights;
    }

    /**
     * Returns the thetas for the network
     *
     * @return  the thetas for the network
     * @throws  IllegalStateException if Network is not initialized.
     */
    public double[][] theta()
    {
        return twoDCopyOf(this.theta);
    }

    /**
     * Updates the network for the given patterns and number of iterations.
     *
     * @param pats          the patterns to train the network with
     * @param iterations    the number of learning iterations to do with these
     *                      patterns. One iteration is a pass with each pattern.
     * @return              the updated Network
     */
    public Network learn(Patterns pats, int iterations)
    {
        return Stream.iterate(this, net -> oneLearningPass(net, pats))
                     .skip(iterations)
                     .findFirst()
                     .get();
    }

    Network learn(Patterns pats)
    {
        return Stream.iterate(this, net -> oneLearningPass(net, pats))
                     .dropWhile(net -> !net.closeEnough(pats))
                     .findFirst()
                     .get();
    }

    /**
     * Does one learning pass with the provided patterns.
     * @param netIn the network we are going to upate with the learning
     * @param pats the patterns to use in this learning pass
     * @return a new network, updated for the set of patterns
     */
    private static Network oneLearningPass(Network netIn, Patterns pats)
    {
        Network net = netIn;
        for(int patIdx=0; patIdx<pats.size(); patIdx++)
        {
            net = sweepBackAndForward(net, pats, patIdx);
        }
        return net;
    }

    /**
     * Does a back and forward pass for a single pattern,
     * returning a new network.
     * @param in the original network
     * @param pats the collection of patterns that this pattern will come from
     * @param i the index of the particular pattern to sweep forward and back
     * @return a new network, updated for the one pattern
     */
    private static Network sweepBackAndForward(Network in, Patterns pats, int i)
    {
            Outputs outputs = sweepForward(in, pats.getInputPattern(i));
            return sweepBack(pats.getOutputPattern(i), outputs, in);
    }

    double loss(Patterns pats)
    {
        return IntStream.range(0, pats.size())
                        .mapToDouble(idx -> loss(pats,idx))
                        .sum();
    }

    private double loss(Patterns pats, int idx)
    {
        return lossForOnePattern(
            answer(pats.getInputPattern(idx)),
            pats.getOutputPattern(idx));
    }

    static double lossForOnePattern(double[] output, double[] target)
    {
        if(output.length != target.length) throw new IllegalArgumentException();

        IntToDoubleFunction lossAtIndex =
            i -> 0.5*(target[i]-output[i])*(target[i]-output[i]);

        return IntStream.range(0,output.length)
                        .mapToDouble(lossAtIndex)
                        .sum();
    }

    private boolean closeEnough(Patterns pats)
    {
        IntPredicate closeEnoughByIndex =
            patIdx -> closeEnoughForOnePattern(
                    answer(pats.getInputPattern(patIdx)),
                    pats.getOutputPattern(patIdx));

        return IntStream.range(0,pats.size())
                        .allMatch(closeEnoughByIndex);
    }

    private static
    boolean closeEnoughForOnePattern(double[] output, double[] target)
    {
        if(output.length != target.length) throw new IllegalArgumentException();

        IntFunction<Boolean> okayAtIndex =
            i -> Math.abs(target[i]-output[i]) < .1;

        return IntStream.range(0,output.length)
                        .mapToObj(okayAtIndex)
                        .reduce(true,(a,b)->a && b);
    }

    /**
     * Do a back propigation pass.
     * @param targetArray the Network output target
     * @param outputs outputs of each node for the current pattern
     * @param net the network, including weights, that we are sweeping back on
     * @return a new Network, updated by the back propigation pass.
     */
    private static Network sweepBack(
        double[] targetArray,
        Outputs outputs,
        Network net)
    {
        Target target = new Target(targetArray);

        Deltas deltas = new ArrayDeltas(
            target,
            outputs,
            net.weights,
            net.activationFunction);

        Weights weights = newWeights(net, outputs, deltas);
        double[][] theta = newTheta(net, deltas);
        return Builder.from(net)
                      .withW(weights)
                      .withTheta(theta)
                      .build();
    }

    /**
     * Returns the "next" weights
     * as we evolve the parameters one step at a time
     * toward a minimum loss.
     *
     * This is based on a single input/output patten.
     *
     * @param outputs   the output at each node
     *                  for the current weights and input pattern
     * @param deltas    the deltas at each node
     *                  for the current input and output pattern.
     * @return          new adjusted weights
     */
    private static Weights newWeights(
        Network net,
        Outputs outputs,
        Deltas deltas)
    {
        Weights.ThreeIntFunction<Double> weightChange = (layer,out,in) ->
             net.eta * deltas.getDelta(layer,out) * outputs.get(layer,in);

        Weights deltaW = net.weights.populate(weightChange);
        return net.weights.add(deltaW);
    }

    /**
     * Returns the "next" thetas
     * as we evolve the parameters one step at a time
     * toward a minimum loss.
     */
    private static double[][] newTheta(Network net, Deltas deltas)
    {
        return IntStream.range(1,net.structure.length)
                        .mapToObj(layer->newLayerOfThetas(net, layer,deltas))
                        .toArray(double[][]::new);
    }

    private static double[] newLayerOfThetas(
        Network net,
        int layer,
        Deltas deltas)
    {
        IntToDoubleFunction getTheta = 
            node -> deltas.getDelta(layer-1,node)*net.eta +
                    net.theta[layer-1][node];

        return IntStream.range(0,net.structure[layer])
                        .mapToDouble(getTheta)
                        .toArray();
    }


    private static Outputs sweepForward(Network start, double[] inputPattern)
    {
        double[][] outs = new double[start.structure.length][];
        outs[0] = inputPattern;

        for(int layer=1; layer<start.structure.length; layer++)
        {
            assert outs.length > layer-1;
            assert layer-1 >= 0;
            assert start.weights.numberOfWeightLayers() >= 1;
            assert start.theta.length > layer-1;

            outs[layer] =
                answerOneLayer(
                    start.activationFunction,
                    outs[layer-1],
                    start.weights,
                    layer-1,
                    start.theta[layer-1]);
        }

        return new Outputs(outs);
    }

    private static double[] answerOneLayer(
        ActivationFunction activationFunction,
        double[] input,
        Weights w,
        int wLayer,
        double[] theta)
    {
        IntToDoubleFunction nodeToOutput =
            node -> unitOutput(activationFunction, input, w, wLayer, node, theta[node]);

        return IntStream.range(0, theta.length)
                        .mapToDouble(nodeToOutput)
                        .toArray();
    }

    /**
     * Computes the output for a unit with the specified inputs and weights
     * @param inputs the inputs for this unit
     * @param weights the weights for this set of inputs
     * @return the output for this unit
     */
    private static double unitOutput(
        ActivationFunction activationFunction,
        double[] inputs,
        Weights weights,
        int wLayer,
        int outNode,
        double theta)
    {
        IntToDoubleFunction weightedIn =
            inNode -> inputs[inNode] * weights.getWeight(wLayer,outNode,inNode);
        double netpj = IntStream.range(0, inputs.length)
                                .mapToDouble(weightedIn)
                                .sum();
        
        return activationFunction.f(netpj, theta);
    }

    /**
     * Returns a copy of the network output layer's outputs.
     * @param inputPattern  the inputs
     * @return              the outputs.
     *                      If there is no network, so no output pattern,
     *                      a zero length answer array is returned.
     */
    double[] answer(double[] inputPattern)
    {
        return sweepForward(this, inputPattern).getLastLayer()
                                               .orElse(new double[] {});
    }
    
    Outputs outputs(double[] inputPattern)
    {
        return sweepForward(this, inputPattern);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;
        Network that = (Network) o;
        return
            Arrays.equals(that.structure, this.structure) &&
            that.eta == this.eta &&
            Objects.equals(that.weights, this.weights) &&
            Arrays.deepEquals(that.theta, this.theta);
    }

    @Override
    public int hashCode()
    {
        //
        // just simply Objects::hash will not work
        // because of arrays
        //
        return Arrays.hashCode(structure) * 31 +
               Objects.hash(eta) * 31 +
               Objects.hashCode(weights) * 31 + // might be null
               Arrays.deepHashCode(theta);
    }
}


