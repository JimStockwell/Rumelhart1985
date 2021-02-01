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
        Network net = new Network(new int[]{1,1}, new double[][][]{{{1}}}, new double[][]{{0}});
        Patterns pats = new Patterns(new int[][][] {{{1},{0}},{{0},{1}}});
        Network educated = net.learn(pats);
        assertEquals(1.0,educated.answer(new double[]{0})[0],.01);
    }

    @Test
    public void reportsWCorrectly()
    {
        double[][][] w = {{{1e6,2e6},{1e5,2e5}}};
        Network net = new Network(new int[]{2,2}, w, new double[][]{{0,0}});
        assertEquals(w[0].length, net.w()[0].length);
        for(int i = 0; i < w[0].length; i++)
        {
            assertEquals(w[0][i].length, net.w()[0][i].length);
            for(int j = 0; j < w[0][i].length; j++)
            {
                assertEquals(w[0][i][j], net.w()[0][i][j], 0);
            }
        }

        // A copy, not a view, right?
        w[0][0][0] = 0;
        assertEquals(1e6,net.w()[0][0][0],0);
    }

    @Test
    public void reportsThetaCorrectly()
    {
        double[][] theta = {{1e6,2e6},{1e5}};
        Network net = new Network(new int[]{1,2,1}, null, theta);
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
        Network net = new Network(new int[]{1,1}, new double[][][]{{{w}}}, new double[][]{{theta}});
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
        Network net1 = new Network(new int[]{1,1}, new double[][][]{{{1}}}, new double[][]{{0}});
        Network net2diff = new Network(new int[]{2,1}, new double[][][]{{{.9,.1}}}, new double[][]{{0}});
        Network net2same = new Network(new int[]{2,1}, new double[][][]{{{1,1}}}, new double[][]{{0}});
    
        //
        // We will have one unit, with .1 and .9, summing to 1.0, two ways:
        // First with different weights,
        // then with different inputs
        //
        assertEquals(net1.answer(new double[]{1})[0], net2diff.answer(new double[]{1,1})[0], 1e-6);

        // Should be equal because input is just summed, and .1 and .9 sum to 1
        assertEquals(net1.answer(new double[]{1})[0], net2same.answer(new double[]{.1,.9})[0], 1e-6);
    }

    @Test
    public void multipleNodesOnALayerWork()
    {
        Network net = new Network(new int[] {2,2}, new double[][][] {{{.1,0},{0,.9}}}, new double[][]{{0,0}});
        double[] answer = net.answer(new double[] {.9,.1});
        assertEquals(2, answer.length);
        assertEquals(answer[0],answer[1],1e-6);

        Network diffThetas = new Network(new int[] {2,2}, new double[][][] {{{1,0},{0,2}}}, new double[][]{{0,-3}});
        double[] answerDT2 = diffThetas.answer(new double[] {3,3});
        assertEquals(answerDT2[0],answerDT2[1],1e-6);
    }

    @Test
    public void multipleLayersWork()
    {
        Network net1 = new Network(new int[] {1,1}, new double[][][] {{{1}}}, new double[][]{{0}});
        Network net2 = new Network(new int[] {1,1,1}, new double[][][] {{{1}},{{1}}}, new double[][]{{0},{0}});
        
        // net1 twice should == net2 once

        assertEquals(
            net1.answer(net1.answer(new double[] {1}))[0],
            net2.answer(new double[] {1})[0],
            1e-6
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void wTooLongIsCheckedAgainstStructure()
    {
        Network net = new Network(new int[]{1,1}, new double[][][]{{{1,2}}}, new double[][]{{0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void wTooShortIsCheckedAgainstStructure()
    {
        Network net = new Network(new int[]{1,1}, new double[][][]{{{}}}, new double[][]{{0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void wSecondNodeCheckedAgainstStructure()
    {
        Network net = new Network(new int[]{1,2}, new double[][][]{{{1.0},{1.1,1.11}}}, new double[][]{{0,0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void wNumberOfNodesCorrectVsStructure()
    {
        Network net = new Network(new int[]{1,2}, new double[][][]{{{1.0},{1.1},{1.2}}}, new double[][]{{0,0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void wNumberOfLayersCorrectVsStructure()
    {
        Network net = new Network(new int[]{1,2,1}, new double[][][]{{{1.0},{1.1}}}, new double[][]{{0,0},{0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void wEachLayerValidated()
    {
        Network net = new Network(new int[]{1,2,1}, new double[][][]{{{1.0},{1.1}},{{}}}, new double[][]{{0,0},{0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void thetaNumberOfLayersValidated()
    {
        Network net = new Network(new int[]{1,2,1}, new double[][][]{{{1.0},{1.1}},{{1,1}}}, new double[][]{{0,0},{0},{0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void thetaEachLayerValidated()
    {
        Network net = new Network(new int[]{1,2,1}, new double[][][]{{{1},{1}},{{1,1}}}, new double[][]{{0,0},{0,99}});
    }
}
