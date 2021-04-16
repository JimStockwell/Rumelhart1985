package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import java.util.function.Predicate;

//
// TODO: make immutable 1D, 2D, and 3D arrayList<Double> classes
// and pass those around instead of making defensive copies of arrays.
//
class Patterns
{
    final int IN = 0;
    final int OUT = 1;

    // patterns[whichPattern][inputOroutput][node]
    private double[][][] patterns;

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

    int size()
    {
        return patterns.length;
    }

    double[] getInputPattern(int index)
    {
        return copyOfPatternHalf(index, IN);
    }
    
    double[] getOutputPattern(int index)
    {
        return copyOfPatternHalf(index, OUT);
    }
    
    private double[] copyOfPatternHalf(int patternIndex, int inOutIndex)
    {
        return Arrays.copyOf(
            patterns[patternIndex][inOutIndex],
            patterns[patternIndex][inOutIndex].length);
    }

    Patterns onePattern(int index)
    {
        var patternsAsArray = new double[][][] {patterns[index]};
        return new Patterns(patternsAsArray);
    }

    static Patterns xor()
    {
        return new Patterns(new double[][][]
        {
            {{0,0},{0}},
            {{0,1},{1}},
            {{1,0},{1}},
            {{1,1},{0}}
        });
    }

    static Patterns flip()
    {
        return new Patterns(new double[][][]
        {
            {{0},{1}},
            {{1},{0}}
        });
    }

    static Patterns empty()
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


