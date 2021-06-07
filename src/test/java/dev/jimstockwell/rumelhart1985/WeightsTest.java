package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.stream.Stream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.Random;


public interface WeightsTest<W extends Weights>
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

    /**
     * Provides a list of valid sample structures to test against.
     * It is "default" so that inheriting test classes
     * can access the list while in development.
     */
    default List<int[]> getValidStructures()
    {
        List<int[]> validStructures = new ArrayList<>();
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
        return validStructures;
    }

    W createWeights(double[][][] w);
    W createWeights(Double[][][] w);
    W createWeights( int[] s, Weights.ThreeIntFunction<Double> f);

    @Test
    default void hashCodeWorks() {
        assertEquals(
            createWeights(new int[] {1,1}, (l,n1,n2)->1.0),
            createWeights(new int[] {1,1}, (l,n1,n2)->1.0));

        // Yes, yes.  There is a 1/2^32 chance these will be equal by chance.
        assertNotEquals(
            createWeights(new int[] {1,1}, (l,n1,n2)->1.0),
            createWeights(new int[] {1,2}, (l,n1,n2)->1.0));

        // Yes, yes.  There is a 1/2^32 chance these will be equal by chance.
        assertNotEquals(
            createWeights(new int[] {1,1}, (l,n1,n2)->1.0),
            createWeights(new int[] {1,1}, (l,n1,n2)->2.0));
    }

    @Test
    default void equalsWorks() {
        assertEquals(
            new Edges(new int[] {1,1}, c->1.0),
            new Edges(new int[] {1,1}, c->1.0));
        assertNotEquals(
            new Edges(new int[] {1,1}, c->1.0),
            new Edges(new int[] {1,2}, c->1.0));
        assertNotEquals(
            new Edges(new int[] {1,1}, c->1.0),
            new Edges(new int[] {1,1}, c->2.0));
    }

    @Test
    default void canCreateValidWeightsFromArray()
    {
        assertThrows(
            NullPointerException.class,
            () -> createWeights((double[][][])null)
        );
        assertThrows(
            NullPointerException.class,
            () -> createWeights((Double[][][])null)
        );
        createWeights(arrayNoLayers);
        createWeights(arrayAnyInZeroOut);
        createWeights(array01);
        createWeights(array03);
        createWeights(array23);
        createWeights(array123);
        createWeights(array103);
    }

    @Test
    default void numberOfLayersIsCorrect()
    {
        
        Weights.ThreeIntFunction<Double> f = (a,b,c) -> 1.0;
        assertEquals(2,
            createWeights(new int[] {1,1,1}, f).numberOfWeightLayers());
        assertEquals(1,
            createWeights(new int[] {1,1}, f).numberOfWeightLayers());
        assertEquals(0,
            createWeights(new int[] {1}, f).numberOfWeightLayers());
    }

    @Test
    default void consistentWithWorks()
    {
        assertFalse(
            createWeights(array123).consistentWith(new int[] {1}));
        assertFalse(
            createWeights(array123).consistentWith(new int[] {1,2,4}));
        assertFalse(
            createWeights(array123).consistentWith(new int[] {2,2,3}));

        assertTrue(
            createWeights(array123).consistentWith(new int[] {1,2,3}));
        assertTrue(
            createWeights(new double[][][]{}).consistentWith(new int[] {}));
    }

    @Test
    default void invalidShapedWeightsThrow()
    {
        // 2 inputs, 3 outputs
        createWeights(array23);
        
        // 2 inputs, 3 outputs, but missing one
        assertThrows(
            Weights.InputLayerConsistencyException.class,
            () -> createWeights(new double[][][] {{{11,12},{21,22},{9999}}}));
            
        // 1 input, 2 hidden, 3 outputs
        createWeights(array123);

        // 1 input, 2 hidden, 3 outputs, but missing one
        assertThrows(
            Weights.ConsistencyException.class,
            () -> createWeights(
                new double[][][] {{{1},{2}},{{11,12},{21,22},{9999}}}));
    }

    @Test
    default void notAViewOrPartialView()
    {
        Double[][][] array = new Double[][][] {{{1.0}}};
        Weights w = createWeights(array);
        assertEquals(1,w.getWeight(0,0,0));
        array[0][0][0] = 2.0;
        assertEquals(1,w.getWeight(0,0,0));
    }

    @Test
    default void populateWorks()
    {
        Weights w = createWeights(
            new int[] {3,3,3},
            (l,o,i)->i*2+o*3+l*5.0);

        assertNotEquals(w, w.populate( (l,o,i)->0.0 ));
        assertEquals(w, w.populate( (l,o,i)->i*2+o*3+l*5.0));
    }

    @Test
    default void addWorks()
    {
        Weights w1 = createWeights(
            new int[] {3,3,3},
            (l,o,i)->i*2+o*3+l*5.0);
        Weights w2 = createWeights(
            new int[] {3,3,3},
            (l,o,i)->i*5+o*2+l*3.0);
        Weights zero = createWeights(
            new int[] {3,3,3},
            (l,o,i)->0.0);
        assertNotEquals(w2.add(w2), w1.add(w1));
        assertEquals(w1.add(w2), w2.add(w1));
        assertEquals(w1.add(zero), w1);
    }

}

