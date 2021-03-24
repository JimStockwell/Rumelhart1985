package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ArrayDeltas implements Deltas
{
    double[][] deltas;

    ArrayDeltas(Target target, Outputs outputs, Weights weights)
    {
        int layerCount = outputs.countOfLayersExcludingInput();
        deltas = new double[layerCount][];
        //
        // handle each layer 0 to count-1, from last to first
        // (keeping in mind that layer is counting the input layer as -1.)
        //
        for(int i=layerCount-1; i>=0; i--)
        {
            final int nodeCount = outputs.sizeOfNonInputLayer(i);
            deltas[i] = new double[nodeCount];
            for(int node=0; node<nodeCount; node++)
            {
                setDelta(target,outputs,weights,i,node);
            }
        }
    }

    /**
     * This sets the given layer
     * based on the layers closer to the output.
     */
    private void setDelta(
        Target target,
        Outputs outputs,
        Weights weights,
        int layer, // excluding input
        int node) // node to set delta for
    {
        double output = outputs.get(layer+1,node);
        double fprime = output*(1-output); // TODO: don't hardcode formula
        if(layer == outputs.countOfLayersExcludingInput()-1)
        {
            double delta = (target.getTarget(node)-output)*fprime;
            deltas[layer][node] = delta;
        }
        else
        {
            double delta = 0;
            //
            // Step through propigating nodes.
            // These nodes are on the layer one closer to output than "layer".
            //
            for(int propNode=0; propNode<size().get(layer+1); propNode++)
            {
                double deltaFrom = getDelta(layer+1,propNode);
                double weight = weights.getWeight(layer+1, propNode, node);
                delta += deltaFrom*weight*fprime;
            }
            deltas[layer][node] = delta;
        }
    }

    public double getDelta(int layer, int node)
    {
        return deltas[layer][node];
    }

    public List<Integer> size()
    {
        List<Integer> builder = new ArrayList<>(deltas.length);
        for(int i=0; i<deltas.length; i++)
        {
            builder.add(deltas[i].length);
        }
        return List.copyOf(builder);
    }
}

