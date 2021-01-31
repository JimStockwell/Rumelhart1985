package dev.jimstockwell.rumelhart1985;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static java.lang.Math.exp;

import org.junit.Test;
import org.junit.Ignore;

public class AppTest 
{

    @Ignore("not ready yet")
    @Test
    public void doesBasicSteps()
    {
        Network net = new Network(new int[]{1,1}, new double[][]{{1}}, new double[][]{{0}});
        Patterns pats = new Patterns(new int[][][] {{{1},{0}},{{0},{1}}});
        Network educated = net.learn(pats);
        assertEquals(1.0,educated.answer(new double[]{0})[0],.01);
    }

    @Test
    public void reportsWCorrectly()
    {
        double[][] w = {{1e6,2e6},{1e5,2e5}};
        Network net = new Network(new int[]{1}, w, new double[][]{{0}});
        assertEquals(w.length, net.w().length);
        for(int i = 0; i < w.length; i++)
        {
            assertEquals(w[i].length, net.w()[i].length);
            for(int j = 0; j < w[i].length; j++)
            {
                assertEquals(w[i][j], net.w()[i][j], 0);
            }
        }

        // A copy, not a view, right?
        w[0][0] = 0;
        assertEquals(1e6,net.w()[0][0],0);
    }

    @Test
    public void reportsThetaCorrectly()
    {
        double[][] theta = {{1e6,2e6},{1e5}};
        Network net = new Network(new int[]{1}, new double[][]{{0}}, theta);
        assertEquals(theta.length, net.theta().length);
        for(int i = 0; i < theta.length; i++)
        {
            assertEquals(theta[i].length, net.theta()[i].length);
            for(int j = 0; j < theta[i].length; j++)
            {
                assertEquals(theta[i][j], net.theta()[i][j], 0);
            }
        }

        // A copy, not a view, right?
        theta[0][0] = 0;
        assertEquals(1e6,net.theta()[0][0],0);
    }

    private void assertActivationFunctionIsCorrect(double in, double w, double theta)
    {
        Network net = new Network(new int[]{1,1}, new double[][]{{w}}, new double[][]{{theta}});
        assertEquals(1/(1+exp(-(w*in+theta))), net.answer(new double[]{in})[0], .001/(1+exp(-(w*in+theta))));
    }

    @Test
    public void activationFunctionIsCorrect()
    {
        assertActivationFunctionIsCorrect(0,0,0);
        assertActivationFunctionIsCorrect(1,0,0);
        assertActivationFunctionIsCorrect(1,1,0);
        assertActivationFunctionIsCorrect(1,1,3);
    }

    @Test
    public void multipleInputsToOneNodeWork()
    {
        Network net1 = new Network(new int[]{1,1}, new double[][]{{1}}, new double[][]{{0}});
        Network net2diff = new Network(new int[]{2,1}, new double[][]{{.9,.1}}, new double[][]{{0}});
        Network net2same = new Network(new int[]{2,1}, new double[][]{{1,1}}, new double[][]{{0}});
    
        //
        // We will have one unit, with .1 and .9, summing to 1.0, two ways:
        // First with different weights,
        // then with different inputs
        //
        assertEquals(net1.answer(new double[]{1})[0], net2diff.answer(new double[]{1,1})[0], 1e-6);

        // Should be equal because input is just summed, and .1 and .9 sum to 1
        assertEquals(net1.answer(new double[]{1})[0], net2same.answer(new double[]{.1,.9})[0], 1e-6);
    }

}
