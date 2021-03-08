package dev.jimstockwell.rumelhart1985;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Maintains a copy of the provided outputs
 */
class Outputs
{
    final List<List<Double>> data; // immutable

    // layer starts with 0 = input layer
    double getOutput(int layer, int node)
    {
        return data.get(layer).get(node);
    }

    // TODO: combine common code in the two constructors
    Outputs(Double[][] outputs)
    {
        var builder = new ArrayList<List<Double>>(outputs.length);
        for(int i=0; i<outputs.length; i++)
        {
            builder.add(List.of(outputs[i]));
        }
        data = List.copyOf(builder);
    }

    Outputs(double[][] outputs)
    {
        var builder = new ArrayList<List<Double>>(outputs.length);
        for(int i=0; i<outputs.length; i++)
        {
            List<Double> boxed = 
                Arrays.stream(outputs[i]).boxed().collect(Collectors.toList());
            builder.add(boxed);
        }
        data = List.copyOf(builder);
    }

    /**
     * As an unmodifiable List of immutables.
     */
    List<List<Double>> getOutputsData()
    {
        return data;
    }

    int countOfLayersExcludingInput()
    {
        return data.size()-1;
    }

    int sizeOfNonInputLayer(int layer)
    {
        return data.get(layer+1).size();
    }
}

