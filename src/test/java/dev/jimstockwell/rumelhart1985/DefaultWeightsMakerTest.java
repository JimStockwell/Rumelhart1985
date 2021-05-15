package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

public class DefaultWeightsMakerTest 
{
    @Test
    public void minimalWeightsAreCorrect()
    {
        var dwm = new DefaultWeightsMaker(new int[] {0,0}, 1.0);
        double[][][] w = dwm.get();
        // should have one layer connecting two layers of nodes
        assertEquals(1, w.length);
        // should have no output nodes on the one "layer" of weights.
        assertEquals(0, w[0].length);
    }
}

