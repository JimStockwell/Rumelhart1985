package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class NodesTest {
    @Test
    public void structureAndFunctionConstructorParametersAreChecked()
    {
        assertThrows(
            NullPointerException.class,
            () -> new Nodes(null, coords -> 0.0)
        );
        assertThrows(
            NullPointerException.class,
            () -> new Nodes(new int[] {1,2}, null)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new Nodes(new int[] {-1,2}, coords -> 0.0)
        );
    }

    @Test
    public void canWeIncludeNullNodes()
    {
        Nodes layer = new Nodes(new int[] {1}, coords -> 0.0);

        assertThrows(
            IllegalArgumentException.class,
            () -> new Nodes(new Nodes[] {layer,null,layer})
        );
    }

    @Test
    public void structureAndFunctionConstructorMakesDefensiveCopies()
    {
        int[] original = new int[] {1,2};
        int[] provided = Arrays.copyOf(original, original.length);
        Nodes nodes = new Nodes(provided, coords -> 0.0);

        provided[0] = 99;

        assertEquals(1, nodes.sizeOfLayer(0));
    }

    @Test
    public void constructorFromArrayWorks()
    {
        double[][] source = new double[][] {{1.0}};

        Nodes nodes = new Nodes(source);

        // Correct nodes made
        assertEquals(1, nodes.numberOfLayers());
        assertEquals(1, nodes.sizeOfLayer(0));
        assertEquals(1.0, nodes.get(0,0));

        // It is a copy, not just a view.
        source[0][0] = 2.0;
        assertEquals(1.0, nodes.get(0,0));
    }

    @Test
    public void getLastLayerWorks()
    {
        Nodes nodes = new Nodes(new int[] {3,3,3}, c->c.getLayer()+c.getNode());
        assertEquals(2.0,nodes.getLastLayer().get()[0]);
        assertEquals(3.0,nodes.getLastLayer().get()[1]);
        assertEquals(4.0,nodes.getLastLayer().get()[2]);

        double[] finalLayer = nodes.getLastLayer().get();
        finalLayer[0] = 99.0;
        assertEquals(2.0, nodes.getLastLayer().get()[0]);

        Nodes empty = new Nodes(new int[] {}, c->0.0);
        assertTrue(empty.getLastLayer().isEmpty());
    }
}
