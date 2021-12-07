package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;
import java.util.Arrays;

/**
 * Calculates deltas given targets, outputs, weights, and an activation function.
 */
public final class ArrayDeltas implements Deltas
{
    final Nodes deltas;
    final ActivationFunction af;

    ArrayDeltas(
        Target target,
        Outputs outputs,
        Weights weights,
        ActivationFunction af)
    {
        Objects.requireNonNull(target);
        Objects.requireNonNull(outputs);
        Objects.requireNonNull(weights);

        final int effectiveOutputCount = outputs.getLastLayer()
                                                .orElse(new double[] {})
                                                .length;
        if(target.size() != effectiveOutputCount)
            throw new IllegalArgumentException(
                "target size ["+target.size()+"] != " +
                "final outputs size ["+effectiveOutputCount+"]");

        if(weights.numberOfWeightLayers() !=
            outputs.countOfLayersExcludingInput())
                throw new IllegalArgumentException(
                    "weight layers ["+weights.numberOfWeightLayers()+
                    "] != output layers -1 ["+ outputs
                                            .countOfLayersExcludingInput());

        this.af = Objects.requireNonNull(af,"af must not be null");

        deltas = calcAll(target,outputs,weights);
    }

    private Nodes calcAll(
        Target target,
        Outputs outputs,
        Weights weights
    )
    {
        final int layerCount = outputs.countOfLayersExcludingInput();
        Nodes[] separateLayers = new Nodes[layerCount];

        for(int i=layerCount-1; i>=0; i--)
        {
            separateLayers[i] = calcLayer(
                target,
                outputs,
                weights,
                i,
                i==layerCount-1 ? null : separateLayers[i+1]);
        }
        return new Nodes(separateLayers);
    }

    private Nodes calcLayer(
        Target target,
        Outputs outputs,
        Weights weights,
        int layerIdx, // 0 = first non-input layer
        Nodes nextLayer)
    {
        assert layerIdx >= 0;
        assert layerIdx < outputs.countOfLayersExcludingInput();
        assert layerIdx == outputs.countOfLayersExcludingInput()-1 ||
                           nextLayer != null;

        final int nodeCount = outputs.sizeOfNonInputLayer(layerIdx);
        assert nodeCount >= 0 : "Node count < 0";
        assert nodeCount == weights.sizeOfWeightLayer(layerIdx);

        final Nodes.Populator formula = c -> calcDelta(
            target, outputs, weights, layerIdx, c.getNode(), nextLayer);

        int[] layerStructure = { nodeCount };
        return new Nodes(layerStructure, formula);
    }

    /**
     * Calculate delta for the one specified node.
     */
    private double calcDelta(
        Target target,
        Outputs outputs,
        Weights weights,
        int layerIdx, // 0 = first layer after inputs
        int node,
        Nodes nextLayer)
    {
        assert node >= 0;
        assert node < outputs.sizeOfNonInputLayer(layerIdx) :
            "node too big... "+
            "layerIdx: "+layerIdx+
            ", node: "+node+
            ", output sizes: "+Arrays.toString(outputs.sizes());
        assert node < weights.sizeOfWeightLayer(layerIdx);

        // layerIdx+1 because we are interested in the NEXT layer
        assert nextLayer==null ||
            nextLayer.sizeOfLayer(0) == outputs.sizeOfNonInputLayer(layerIdx+1);

        // layer parameter to Output::get includes input layer, so +1
        final double output = outputs.get(layerIdx+1,node);
        final double fprime = af.slopeForOutput(output);


        IntToDoubleFunction deltaContributionForIndex =
            fromNode -> nextLayer.get(new Nodes.Coordinates(0,fromNode)) *
                        weights.getWeight(layerIdx+1,fromNode,node) *
                        fprime;
        
        if(layerIdx == outputs.countOfLayersExcludingInput()-1)
        {
            assert node < target.size();
            return (target.getTarget(node)-output)*fprime;
        }
        else
        {
            assert nextLayer != null : "nextLayer:null, layerIdx="+layerIdx;
            // sourceIndexes are indexes of the nodes
            // that delta propigates back from
            IntStream sourceIndexes =
                IntStream.range(0,nextLayer.sizeOfLayer(0));
            return sourceIndexes.mapToDouble(deltaContributionForIndex).sum();
        }

    }

    public double getDelta(int layer, int node)
    {
        return deltas.get(new Nodes.Coordinates(layer,node));
    }

    public List<Integer> size()
    {
        List<Integer> builder = new ArrayList<>(deltas.numberOfLayers());
        for(int i=0; i<deltas.numberOfLayers(); i++)
        {
            builder.add(deltas.sizeOfLayer(i));
        }
        return builder;
    }
}

