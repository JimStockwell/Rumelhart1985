package dev.jimstockwell.rumelhart1985;

public class ArrayDeltaW implements DeltaWGetter {
    private double[][][] deltaW;
    public ArrayDeltaW(double[][][] doublexxx)
    {
        deltaW = ArraysExtended.threeDCopyOf(doublexxx);
    }

    @Override
    public double[][][] getDeltaW()
    {
        return ArraysExtended.threeDCopyOf(deltaW);
    }
}

