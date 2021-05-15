package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.stream.Stream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class SimpleWeightsTest
{
    // some sample weight arrays
    double[][][] arrayNoLayers = new double[][][] {};
    // This is ambiguous.
    // Could be any number of input nodes,
    // including no input nodes,
    double[][][] arrayAnyInZeroOut = new double[][][] {{}};
    double[][][] array01 = new double[][][] {{{}}};
    double[][][] array03 = new double[][][] {{{},{},{}}};
    double[][][] array23 = new double[][][] {{{11,12},{21,22},{31,32}}};
    double[][][] array123 =
        new double[][][] {{{1},{2}},{{11,12},{21,22},{31,32}}};
    double[][][] array103 =
        new double[][][] {{},{{},{},{}}};

    List<int[]> validStructures = new ArrayList<>();
    {
        validStructures.add(new int[] {});
        validStructures.add(new int[] {0});
        validStructures.add(new int[] {1});
        validStructures.add(new int[] {1,0});
        validStructures.add(new int[] {0,1});
        validStructures.add(new int[] {1,1});
        validStructures.add(new int[] {0,0,0});
        validStructures.add(new int[] {0,0,1});
        validStructures.add(new int[] {0,1,0});
        validStructures.add(new int[] {0,1,1});
        validStructures.add(new int[] {1,0,0});
        validStructures.add(new int[] {1,0,1});
        validStructures.add(new int[] {1,1,0});
        validStructures.add(new int[] {3,2,1});
    }

    // Property Tests
    @Test
    public void canCreateValidWeightsFromArray()
    {
        assertThrows(
            NullPointerException.class,
            () -> new SimpleWeights((double[][][])null)
        );
        assertThrows(
            NullPointerException.class,
            () -> new SimpleWeights((Double[][][])null)
        );
        new SimpleWeights(arrayNoLayers);
        new SimpleWeights(arrayAnyInZeroOut);
        new SimpleWeights(array01);
        new SimpleWeights(array03);
        new SimpleWeights(array23);
        new SimpleWeights(array123);
        new SimpleWeights(array103);
    }

    @Test
    public void canCreateDefaultWeights()
    {
        assertThrows(
            NullPointerException.class,
            () -> Weights.defaultW(null,10)
        );

        Consumer<int[]> processIt = (structure) -> {
            String msg = "for structure " + Arrays.toString(structure); 
            try {
                assertTrue(
                    Weights.defaultW(structure,10.0)
                           .consistentWith(structure),
                    msg);
            } catch (Exception e) {
                throw new RuntimeException(msg, e);
            }
        };

        validStructures.forEach(processIt);
    }

    @Test
    public void nextWeightsTestUnchangedUnderIdentity()
    {
        Consumer<int[]> processIt = (structure) -> {
            String msg = "for structure " + Arrays.toString(structure); 
            try {
                var w = Weights.defaultW(structure,10.0);
                assertEquals(w, w.nextWeights((l,n,wt)->0.0), msg);
            } catch (Exception e) {
                throw new RuntimeException(msg, e);
            }
        };

        validStructures.forEach(processIt);
    }

    @Test
    public void toPrimitivesIsReversable()
    {
        Consumer<int[]> processIt = (structure) -> {
            String msg = "for structure " + Arrays.toString(structure); 
            try {
                var w = Weights.defaultW(structure,10.0);
                assertEquals(w, new SimpleWeights(w.toPrimitives()), msg);
            } catch (Exception e) {
                throw new RuntimeException(msg, e);
            }
        };

        validStructures.forEach(processIt);
    }


    // TDD Tests
    @Test
    public void consistentWithWorks()
    {
        assertFalse(
            new SimpleWeights(array123).consistentWith(new int[] {1}));
        assertFalse(
            new SimpleWeights(array123).consistentWith(new int[] {1,2,4}));

        assertTrue(
            new SimpleWeights(array123).consistentWith(new int[] {1,2,3}));
        assertTrue(
            new SimpleWeights(new double[][][]{}).consistentWith(new int[] {}));
        assertTrue(
            new SimpleWeights(new double[][][]{}).consistentWith(new int[] {7}));
        assertFalse(
            new SimpleWeights(new double[][][]{}).consistentWith(new int[] {1,1}));
    }

    @Test
    public void invalidShapedWeightsThrow()
    {
        // 2 inputs, 3 outputs
        new SimpleWeights(array23);
        
        // 2 inputs, 3 outputs, but missing one
        assertThrows(
            IllegalArgumentException.class,
            () -> new SimpleWeights(new double[][][] {{{11,12},{21,22},{9999}}}));
            
        // 1 input, 2 hidden, 3 outputs
        new SimpleWeights(array123);

        // 1 input, 2 hidden, 3 outputs, but missing one
        assertThrows(
            IllegalArgumentException.class,
            () -> new SimpleWeights(
                new double[][][] {{{1},{2}},{{11,12},{21,22},{9999}}}));
    }

    @Test
    public void notAViewOrPartialView()
    {
        Double[][][] array = new Double[][][]
            {{{1.0}}};
        Weights w = new SimpleWeights(array);
        assertEquals(1,w.getWeight(0,0,0));
        array[0][0][0] = 2.0;
        assertEquals(1,w.getWeight(0,0,0));
    }

    @Test
    public void hashCodeWorks()
    {
        Double[][][] array = new Double[][][]
            {{{1.0}}};
        Double[][][] arrayAgain = new Double[][][]
            {{{1.0}}};
        Double[][][] array3 = new Double[][][]
            {{{3.0}}};
        Weights w1 = new SimpleWeights(array);
        Weights w2 = new SimpleWeights(arrayAgain);
        Weights w3 = new SimpleWeights(array3);
        assertEquals(w1.hashCode(),w2.hashCode());
        assertNotEquals(w1.hashCode(),w3.hashCode());
    }
}
