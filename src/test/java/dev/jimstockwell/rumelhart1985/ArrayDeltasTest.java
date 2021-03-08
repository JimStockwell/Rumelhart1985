package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
// import org.junit.Test;
// import org.junit.Ignore;

public class ArrayDeltasTest 
{
    @Test
    public void oneOutputNoHiddenLayer()
    {

        Target target = new Target(new Double[] {1.0} );
        Outputs outputs = new Outputs(new Double[][] {{.9},{.9}} );
        Weights weights = new Weights(new Double[][][] {{{.5}}} );

        Deltas deltas = new ArrayDeltas(target,outputs,weights);

        assertEquals(1, deltas.size().size());
        assertEquals(1, deltas.size().get(0));

        assertEquals((1.0-0.9)*(.9*.1), deltas.getDelta(0,0),0.000001);
    }

    @Test
    public void oneOutputWithHiddenLayer()
    {

        Target target = new Target(new Double[] {1.0} );
        Outputs outputs = new Outputs(new Double[][] {{.9},{.9},{.9}} );
        Weights weights = new Weights(new Double[][][] {{{.5}},{{.5}}} );

        Deltas deltas = new ArrayDeltas(target,outputs,weights);

        assertEquals(2, deltas.size().size());
        assertEquals(1, deltas.size().get(0));
        assertEquals(1, deltas.size().get(1));

        double prior = deltas.getDelta(1,0);
        double weight = .5;
        double fprime = .9*.1;
        assertEquals(prior*weight*fprime, deltas.getDelta(0,0),0.000001);
    }

    @Test
    public void multipleOutputWithHiddenLayer()
    {

        Target target = new Target(new Double[] {1.0,1.0} );
        Outputs outputs = new Outputs(new Double[][] {{.9},{.9},{.9,.8}} );
        Weights weights = new Weights(new Double[][][] {{{.5}},{{.5,.4}}} );

        Deltas deltas = new ArrayDeltas(target,outputs,weights);

        assertEquals(2, deltas.size().size());
        assertEquals(1, deltas.size().get(0));
        assertEquals(2, deltas.size().get(1));

        double prior1 = deltas.getDelta(1,0);
        double weight1 = .5;
        double prior2 = deltas.getDelta(1,1);
        double weight2 = .4;
        double fprime = .9*.1;
        assertEquals(
            (prior1*weight1+prior2*weight2)*fprime,
            deltas.getDelta(0,0),
            0.000001);
    }
}

