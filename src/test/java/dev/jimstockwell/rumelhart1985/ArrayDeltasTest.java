package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArrayDeltasTest 
{
    final ActivationFunction af = new LogisticActivationFunction();

    private Weights makeWeights(Double[][][] w)
    {
        return new EdgeWeights(w);
    }

    @Test
    public void oneOutputNoHiddenLayer()
    {

        Target target = new Target(new Double[] {1.0} );
        Outputs outputs = new Outputs(new double[][] {{.9},{.9}} );
        Weights weights = makeWeights(new Double[][][] {{{.5}}} );

        Deltas deltas = new ArrayDeltas(target,outputs,weights,af);

        assertEquals(1, deltas.size().size());
        assertEquals(1, deltas.size().get(0));

        assertEquals((1.0-0.9)*(.9*.1), deltas.getDelta(0,0),0.000001);
    }

    @Test
    public void oneOutputWithHiddenLayer()
    {

        Target target = new Target(new Double[] {1.0} );
        Outputs outputs = new Outputs(new double[][] {{.9},{.9},{.9}} );
        Weights weights = makeWeights(new Double[][][] {{{.5}},{{.5}}} );

        Deltas deltas = new ArrayDeltas(target,outputs,weights,af);

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

        Outputs outputs = new Outputs(new double[][] {{.9},{.9},{.9,.8}} );
        Target target = new Target(new Double[] {1.0,1.0} );
        Weights weights = makeWeights(new Double[][][] {{{.5}},{{.5},{.4}}} );

        Deltas deltas = new ArrayDeltas(target,outputs,weights,af);

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

    @Test
    public void canConstruct231Weight()
    {
        Outputs outputs =
            new Outputs(new double[][] {{.9,.9},{.9,.9,.9},{.9}} );
        Target target = new Target(new Double[] {1.0} );
        Weights weights = makeWeights(new Double[][][] 
            {{{.1,.1},{.2,.2},{.3,.3}}, // 3 sets of 2
            {{.1,.1,.1}}});             // 1 set of 3
        Deltas deltas = new ArrayDeltas(target,outputs,weights,af);
    }
}

