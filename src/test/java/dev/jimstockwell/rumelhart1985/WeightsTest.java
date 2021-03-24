package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

public class WeightsTest 
{
    @Test
    public void invalidShapedWeightsThrow()
    {
        // 2 inputs, 3 outputs
        new Weights(new double[][][] {{{11,12},{21,22},{31,32}}});
        
        // 2 inputs, 3 outputs, but missing one
        assertThrows(
            IllegalArgumentException.class,
            () -> new Weights(new double[][][] {{{11,12},{21,22},{9999}}}));
            
        // 1 input, 2 hidden, 3 outputs
        new Weights(new double[][][] {{{1},{2}},{{11,12},{21,22},{31,32}}});

        // 1 input, 2 hidden, 3 outputs, but missing one
        assertThrows(
            IllegalArgumentException.class,
            () -> new Weights(
                new double[][][] {{{1},{2}},{{11,12},{21,22},{9999}}}));
    }

    @Test
    public void structureIsCorrect()
    {
        assertArrayEquals(
            new int[] {2,3},
            new Weights(new double[][][] {{{11,12},{21,22},{31,32}}})
                .structure());
            
        assertArrayEquals(
            new int[] {1,2,3},
            new Weights(new double[][][] {{{1},{2}},{{11,12},{21,22},{31,32}}})
                .structure());
    }

    /**
     * If there is only an input layer,
     * there are no weights,
     * so we can't figure out the structure.
     * So, in that case, we throw an exception.
     */
    @Test
    public void structureThrowsWhenOnlyInputLayer()
    {
        assertThrows(
            IllegalStateException.class,
            () -> new Weights(new double[][][] {}).structure());
    }
}
