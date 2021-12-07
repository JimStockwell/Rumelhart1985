package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Represents a collection of patterns to use with the network
 */
public class Patterns
{
    final int IN = 0;
    final int OUT = 1;

    // patterns[whichPattern][inputOroutput][node]
    private double[][][] patterns;

    /**
     * Constructs a Patterns object from an array.
     * @param p the patterns in an array,
     *          indexed by pattern number,
     *          whether input or output,
     *          and node number
     */
    Patterns(double [][][] p)
    {
        if(p == null) throw new NullPointerException();

        if(!sideHasUniformLength(p, IN)) throw new IllegalArgumentException();
        if(!sideHasUniformLength(p, OUT)) throw new IllegalArgumentException();

        patterns = ArraysExtended.threeDCopyOf(p);
    }
    
    private boolean sideHasUniformLength(double[][][] pats, int inOrOut)
    {
        return Arrays.stream(pats)
                     .mapToInt(onePat->onePat[inOrOut].length)
                     .distinct()
                     .count() <= 1;
    }

    /**
     * The number of patterns in this Patterns
     *
     * @return the number of patterns in this Patterns
     */
    public int size()
    {
        return patterns.length;
    }

    /**
     * Gets the specified input pattern
     *
     * @param index the index of the pattern to get the input values of
     * @return      the input values for the specified pattern
     */
    public double[] getInputPattern(int index)
    {
        return copyOfPatternHalf(index, IN);
    }
    
    /**
     * Gets the specified output pattern
     *
     * @param index the index of the pattern to get the output values for
     * @return      the output values for the specified pattern
     */
    public double[] getOutputPattern(int index)
    {
        return copyOfPatternHalf(index, OUT);
    }
    
    private double[] copyOfPatternHalf(int patternIndex, int inOutIndex)
    {
        return Arrays.copyOf(
            patterns[patternIndex][inOutIndex],
            patterns[patternIndex][inOutIndex].length);
    }

    /**
     * Returns the specified pattern
     *
     * @param index the pattern to return
     * @return      the specified pattern, alone, in a Patterns object
     */
    public Patterns onePattern(int index)
    {
        var patternsAsArray = new double[][][] {patterns[index]};
        return new Patterns(patternsAsArray);
    }

    /**
     * A collection of patterns representing an XORs.
     * @return an XOR Pattern
     */
    public static Patterns xor()
    {
        return new Patterns(new double[][][]
        {
            {{0,0},{0}},
            {{0,1},{1}},
            {{1,0},{1}},
            {{1,1},{0}}
        });
    }

    /**
     * A collection of patterns representing NOT.
     * @return a NOT Pattern
     */
    public static Patterns flip()
    {
        return new Patterns(new double[][][]
        {
            {{0},{1}},
            {{1},{0}}
        });
    }

    /**
     * An empty Pattern
     * @return an empty pattern
     */
    public static Patterns empty()
    {
        return new Patterns(new double[][][]
        {
            {{},{}}
        });
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;
        Patterns pat = (Patterns) o;
        return Arrays.deepEquals(pat.patterns, this.patterns);
    }

    @Override
    public int hashCode()
    {
        return Arrays.deepHashCode(patterns);
    }

    @Override
    public String toString()
    {
        return Arrays.deepToString(patterns);
    }
}


