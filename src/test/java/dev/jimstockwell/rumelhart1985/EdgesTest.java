package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

class EdgeTest {
    @Test
    public void hashCodeWorks() {
        assertEquals(
            new Edges(new int[] {1,1}, c->1.0),
            new Edges(new int[] {1,1}, c->1.0));

        // Yes, yes.  There is a 1/2^32 chance these will be equal by chance.
        assertNotEquals(
            new Edges(new int[] {1,1}, c->1.0),
            new Edges(new int[] {1,2}, c->1.0));

        // Yes, yes.  There is a 1/2^32 chance these will be equal by chance.
        assertNotEquals(
            new Edges(new int[] {1,1}, c->1.0),
            new Edges(new int[] {1,1}, c->2.0));
    }

    @Test
    public void equalsWorks() {
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
    public void structureGetsCorrectly() {
        int[] structure = new int[] {3,2,1};

        final Edges edges = new Edges(structure, cords -> 1.0);

        assertEquals(3,edges.numberOfNodeLayers());
        assertEquals(3,edges.sizeOfNodeLayer(0));
        assertEquals(2,edges.sizeOfNodeLayer(1));
        assertEquals(1,edges.sizeOfNodeLayer(2));
    }

    @Test
    public void valuesGetCorrectly() {
        int[] structure = new int[] {3,2,1};
        Edges.Populator f = cords -> cords.getLayer()*2 +
                                  cords.getOutNode()*3 +
                                  cords.getInNode()*5;

        final Edges edges = new Edges(structure, f);

        assertEquals(0,edges.get(new Edges.Coordinates(0,0,0)));
        assertEquals(5,edges.get(new Edges.Coordinates(0,0,1)));
        assertEquals(10,edges.get(new Edges.Coordinates(0,0,2)));
        assertEquals(3,edges.get(new Edges.Coordinates(0,1,0)));
        assertEquals(8,edges.get(new Edges.Coordinates(0,1,1)));
        assertEquals(13,edges.get(new Edges.Coordinates(0,1,2)));
        assertEquals(2,edges.get(new Edges.Coordinates(1,0,0)));
        assertEquals(7,edges.get(new Edges.Coordinates(1,0,1)));
    }

    @Test
    public void nothingElseCalled() {
        Set<Edges.Coordinates> accum = new HashSet<>(8);
        int[] structure = new int[] {3,2,1};
        Edges.Populator f = cords -> {
            accum.add(cords);
            return 1.0;
        };

        final Edges edges = new Edges(structure, f);

        assertTrue(accum.remove(new Edges.Coordinates(0,0,0)));
        assertTrue(accum.remove(new Edges.Coordinates(0,0,1)));
        assertTrue(accum.remove(new Edges.Coordinates(0,0,2)));
        assertTrue(accum.remove(new Edges.Coordinates(0,1,0)));
        assertTrue(accum.remove(new Edges.Coordinates(0,1,1)));
        assertTrue(accum.remove(new Edges.Coordinates(0,1,2)));
        assertTrue(accum.remove(new Edges.Coordinates(1,0,0)));
        assertTrue(accum.remove(new Edges.Coordinates(1,0,1)));
        assertTrue(accum.isEmpty());
    }

    @Test
    public void definsiveCopiesWork() {
        int[] structure = new int[] {3,2,1};
        Edges.Populator f = cords -> 1.0;
        final Edges edges = new Edges(structure, f);

        structure[0] = 0;

        assertEquals(3,edges.sizeOfNodeLayer(0));
    }

    @Test
    public void structureValidated() {
        int[] structure = new int[] {3,2,1};
        Edges.Populator f = cords -> 1.0;
        final Edges edges = new Edges(structure, f);

        structure[0] = 0;

        assertEquals(3,edges.sizeOfNodeLayer(0));
    }

    @Test
    public void throwsWhenBadStructure() {
        int[] badStructure = new int[] {3,-2,1};
        Edges.Populator f = cords -> 1.0;
        assertThrows(IllegalArgumentException.class, () -> {
            new Edges(badStructure,f);
        });
    }

    @Test
    public void throwsWhenBadCoordinates() {
        int[] structure = new int[] {3,2,1};
        Edges.Populator f = cords -> 1.0;
        Edges edges = new Edges(structure, f);
        List<Edges.Coordinates> badCoordinates = List.of(
            new Edges.Coordinates(-1,0,0),
            new Edges.Coordinates(42,0,0),
            new Edges.Coordinates(0,42,0),
            new Edges.Coordinates(0,0,42));
        
        for(var badCoord : badCoordinates)
        {
            assertThrows(IndexOutOfBoundsException.class, () -> {
                edges.get(badCoord);
            });
        }
    }
}
