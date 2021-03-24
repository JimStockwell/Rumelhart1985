package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import        org.junit.jupiter.api.function.Executable;

import org.junit.jupiter.api.Test;

public class PatternsTest 
{
    @Test
    public void onePatternGetsSelectedPattern()
    {
        assertEquals(
            new Patterns(new double[][][] {{{0,1},{1}}}),
            Patterns.xor().onePattern(1)
        );
    }

    @Test
    public void throwsWhenConstructorGivenNull()
    {
        Executable bad = () -> new Patterns(null);
        assertThrows(NullPointerException.class, bad);
    }

    @Test
    public void throwsWhenGivenARaggedArray()
    {
        var raggedIn = new double[][][] {{{0,1},{1}},{{0,1,2},{1}}};
        var raggedOut = new double[][][] {{{0,1},{1,2}},{{0,1},{1}}};
        Executable badInPattern = () -> new Patterns(raggedIn);
        Executable badOutPattern = () -> new Patterns(raggedOut);
        assertThrows(IllegalArgumentException.class, badInPattern);
        assertThrows(IllegalArgumentException.class, badOutPattern);
    }

    @Test
    public void zeroLengthConstructorIsOkay()
    {
        var pat = new Patterns(new double[][][] {});
        assertEquals(0, pat.size());
    }

    @Test
    public void containsACopyNotAView()
    {
        var source = new double[][][] {{{0,1},{1}}};
        Patterns first = new Patterns(source);
        Patterns firstAgain = new Patterns(source);

        source[0][0][0] = 0.5;
        Patterns second = new Patterns(source);

        assertEquals(first,firstAgain);
        assertNotEquals(first,second);
    }

}
