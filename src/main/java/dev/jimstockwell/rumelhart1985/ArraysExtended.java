package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;

class ArraysExtended {

    static double[][] twoDCopyOf(double[][] source)
    {
        final double[][] tmp = new double[source.length][];
        for(int i=0; i<source.length; i++)
        {
            tmp[i] = Arrays.copyOf(source[i],source[i].length);
        }
        return tmp;
    }

    static double[][][] threeDCopyOf(double[][][] source)
    {
        final double[][][] tmp = new double[source.length][][];
        for(int layer=0; layer<source.length; layer++)
        {
            tmp[layer] = twoDCopyOf(source[layer]);
        }
        return tmp;
    }
}
