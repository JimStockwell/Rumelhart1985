package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArrayDeltaWTest implements DeltaWGetterTest<ArrayDeltaW>
{
    @Override
    public ArrayDeltaW createValue(double[][][] doublexxx)
    {
        return new ArrayDeltaW(doublexxx);
    }
}
