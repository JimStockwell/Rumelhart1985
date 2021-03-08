package dev.jimstockwell.rumelhart1985;

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.twoDCopyOf;
import static dev.jimstockwell.rumelhart1985.ArraysExtended.threeDCopyOf;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}

class Patterns
{
    double[][][] patterns;

    Patterns(double [][][] p)
    {
        patterns = p;
    }

    int size()
    {
        return patterns.length;
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
}

class LearningLog
{
    private ArrayList<Network> netList = new ArrayList<>();

    void add(Network network) {
        netList.add(network);
    }

    Network net(int step)
    {
        return netList.get(step);
    }
}

