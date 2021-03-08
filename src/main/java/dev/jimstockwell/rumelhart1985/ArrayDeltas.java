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

    private void setDelta(
        Target target,
        Outputs outputs,
        Weights weights,
        int layer, // excluding input
        int node)
    {
        double output = outputs.getOutput(layer+1,node);
        double fprime = output*(1-output); // TODO: don't hardcode formula
        if(layer == outputs.countOfLayersExcludingInput()-1)
        {
            double delta = (target.getTarget(node)-output)*fprime;
            deltas[layer][node] = delta;
        }
        else
        {
            double delta = 0;
            for(int propNode=0; propNode<size().get(layer+1); propNode++)
            {
                double deltaFrom = getDelta(layer+1,propNode);
                double weight = weights.getWeight(layer+1,node,propNode);
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

