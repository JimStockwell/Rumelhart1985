package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.Assert.assertTrue;
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertNotEquals;
import static java.lang.Math.exp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
// import org.junit.Test;
// import org.junit.Ignore;

public class AppTest 
{
    @Disabled("Not supported yet")
    @Test
    public void doesBasicSteps()
    {
        Network net = new Network(
            new int[]{1,1},
            new double[][][]{{{1}}},
            new double[][]{{0}});
        Patterns pats = Patterns.flip();
        net.learn(pats);
        assertEquals(1.0,net.answer(new double[]{0})[0],.01);
    }

    @Test
    public void reportsWCorrectly()
    {
        double[][][] w = {{{1e6,2e6},{1e5,2e5}}};
        double[][] theta = {{0,0}};
        int struct[] = {2,2};
        Network net = new Network().withStructure(struct)
                                   .withTheta(theta)
                                   .withW(w);
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
        Network net = new Network().withStructure(new int[]{1,2,1})
                                   .withTheta(theta);
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

    private void assertActivationFunctionIsCorrect(
        double in,
        double w,
        double theta)
    {
        Network net = new Network().withStructure(new int[]{1,1})
                                   .withW(new double[][][]{{{w}}})
                                   .withTheta(new double[][]{{theta}});
        assertEquals(
            1/(1+exp(-(w*in+theta))),
            net.answer(new double[]{in})[0],
            .001/(1+exp(-(w*in+theta))));
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
    public void multipleInputsToOneNodeWorkForward()
    {
        Network net1 = new Network()
            .withStructure(new int[]{1,1})
            .withW(new double[][][]{{{1}}})
            .withTheta(new double[][]{{0}});
        Network net2diff = new Network()
            .withStructure(new int[]{2,1})
            .withW(new double[][][]{{{.9,.1}}})
            .withTheta(new double[][]{{0}});
        Network net2same = new Network()
            .withStructure(new int[]{2,1})
            .withW(new double[][][]{{{1,1}}})
            .withTheta(new double[][]{{0}});
    
        //
        // We will have one unit, with .1 and .9, summing to 1.0, two ways:
        // First with different weights,
        // then with different inputs
        //
        assertEquals(
            net1.answer(new double[]{1})[0],
            net2diff.answer(new double[]{1,1})[0],
            1e-6);

        // Should be equal because input is just summed, and .1 and .9 sum to 1
        assertEquals(
            net1.answer(new double[]{1})[0],
            net2same.answer(new double[]{.1,.9})[0],
            1e-6);
    }

    @Test
    public void multipleNodesOnALayerWorkForward()
    {
        Network net = new Network(
            new int[] {2,2},
            new double[][][] {{{.1,0},{0,.9}}},
            new double[][]{{0,0}});
        double[] answer = net.answer(new double[] {.9,.1});
        assertEquals(2, answer.length);
        assertEquals(answer[0],answer[1],1e-6);

        Network diffThetas = new Network(
            new int[] {2,2},
            new double[][][] {{{1,0},{0,2}}},
            new double[][]{{0,-3}});
        double[] answerDT2 = diffThetas.answer(new double[] {3,3});
        assertEquals(answerDT2[0],answerDT2[1],1e-6);
    }

    @Test
    public void multipleLayersWorkForward()
    {
        Network net1 = new Network()
            .withStructure(new int[] {1,1})
            .withW(new double[][][] {{{1}}})
            .withTheta(new double[][]{{0}});
        Network net2 = new Network()
            .withStructure(new int[] {1,1,1})
            .withW(new double[][][] {{{1}},{{1}}})
            .withTheta(new double[][]{{0},{0}});
        
        // net1 twice should == net2 once

        assertEquals(
            net1.answer(net1.answer(new double[] {1}))[0],
            net2.answer(new double[] {1})[0],
            1e-6
        );
    }

    @Test
    public void wTooLongIsCheckedAgainstStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network()
            .withStructure(new int[]{1,1})
            .withW(new double[][][]{{{1,2}}})
            .withTheta(new double[][]{{0}});
        });
    }

    @Test
    public void wTooShortIsCheckedAgainstStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network()
            .withStructure(new int[]{1,1})
            .withW(new double[][][]{{{}}})
            .withTheta(new double[][]{{0}});
        });
    }

    @Test
    public void wSecondNodeCheckedAgainstStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network()
            .withStructure(new int[]{1,2})
            .withW(new double[][][]{{{1.0},{1.1,1.11}}})
            .withTheta(new double[][]{{0,0}});
        });
    }

    @Test
    public void wNumberOfNodesCorrectVsStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network()
            .withStructure(new int[]{1,2})
            .withW(new double[][][]{{{1.0},{1.1},{1.2}}})
            .withTheta(new double[][]{{0,0}});
        });
    }

    @Test
    public void wNumberOfLayersCorrectVsStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network()
            .withStructure(new int[]{1,2,1})
            .withW(new double[][][]{{{1.0},{1.1}}})
            .withTheta(new double[][]{{0,0},{0}});
        });
    }

    @Test
    public void wEachLayerValidated()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network()
            .withStructure(new int[]{1,2,1})
            .withW(new double[][][]{{{1.0},{1.1}},{{}}})
            .withTheta(new double[][]{{0,0},{0}});
        });
    }

    // TODO: return a description of how exactly the argument is illegal
    @Test
    public void thetaNumberOfLayersValidated()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network()
            .withStructure(new int[]{1,2,1})
            .withW(new double[][][]{{{1.0},{1.1}},{{1,1}}})
            .withTheta(new double[][]{{0,0},{0},{0}});
        });
    }

    // TODO: return a description of how exactly the argument is illegal
    @Test
    public void thetaEachLayerValidated()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network()
            .withStructure(new int[]{1,2,1})
            .withW(new double[][][]{{{1},{1}},{{1,1}}})
            .withTheta(new double[][]{{0,0},{0,99}});
        });
    }

    @Test
    public void learnMakesCorrectSizedWAndTheta()
    {
        // Network net1 = new Network(new int[]{3});
        Network net1 = new Network().withStructure(new int[]{3});
        assertEquals(0, net1.w().length);
        assertEquals(0, net1.theta().length);

        // Network net2 = new Network(new int[]{3,2});
        Network net2 = new Network().withStructure(new int[]{3,2});
        assertEquals(1, net2.w().length);
        assertEquals(2, net2.w()[0].length);
        assertEquals(3, net2.w()[0][0].length);
        assertEquals(3, net2.w()[0][1].length);
        assertEquals(1, net2.theta().length);
        assertEquals(2, net2.theta()[0].length);
    }


    // If theta or w is missing,
    // some default is used.
    @Test
    public void learnMakesNonSymetricWAndTheta()
    {
        Network net2 = new Network().withStructure(new int[]{2,2});
        // TODO: Do we need 1e-9, or will this compare exactly?
        assertNotEquals(net2.w()[0][0][1], net2.w()[0][0][0], 1e-9);
        assertNotEquals(net2.theta()[0][0], net2.theta()[0][1], 1e-9);
    }

    @Test
    public void learnReturnsALog()
    {
        Network net = new Network().withStructure(new int[]{1,1});
        LearningLog log =
            net.learn(new Patterns(new double[][][] {{{1},{1}}} ));
    }

    @Test
    public void learningLogHasInitialNetwork()
    {
        int[] structure = {2,2};
        double[][][] w = {{{1e6,2e6},{1e5,2e5}}};
        double[][] theta = {{1e6,2e6}};

        Network net = new Network().withStructure(structure)
                                   .withW(w)
                                   .withTheta(theta);
// TODO: Bring this test back, or remove entirely.
//        LearningLog log = net.learn(Patterns.xor());
//        assertTrue(java.util.Arrays.deepEquals(log.net(0).w(),w));
//        assertTrue(java.util.Arrays.deepEquals(log.net(0).theta(),theta));
    }

    @Test
    public void correctParameterChanges1_1Network1Pat()
    {
        final double ORIGINAL_W = 1;
        final double ORIGINAL_θ = 0;
        final double ETA = 1;
        int[] structure = {1,1};
        double[][][] w = {{{ORIGINAL_W}}};
        double[][] theta = {{ORIGINAL_θ}};
        
        Network net = new Network()
                        .withStructure(structure)
                        .withW(w)
                        .withTheta(theta)
                        .withEta(ETA);

        double INPUT = 1.0;
        double TARGET = 1.0;
        double[] answer = net.answer(new double[] {INPUT});
        //
        // So...
        // put a pattern of {1} in,
        // figure what delta should be
        // and see if it is
        // 
        net.learn(new Patterns(new double[][][]{{{INPUT},{TARGET}}}));
        double new_w = net.w()[0][0][0];
        assertEquals(
            ETA*(TARGET-answer[0])*answer[0]*(1-answer[0])*INPUT,
            new_w-ORIGINAL_W,
            (new_w-ORIGINAL_W)*1e-6);
        double new_θ = net.theta()[0][0];
        assertEquals(
            ETA*(TARGET-answer[0])*answer[0]*(1-answer[0])*1,
            new_θ-ORIGINAL_θ,
            (new_θ-ORIGINAL_θ)*1e-6);
    }

    @Test
    public void correctParameterChanges2_1Network1Pat()
    {
        final double ORIGINAL_W = 1;
        final double ORIGINAL_θ = 0;
        final double ETA = 1;
        int[] structure = {2,1};
        double[][][] w = {{{ORIGINAL_W,ORIGINAL_W}}};
        double[][] theta = {{ORIGINAL_θ}};
        double[] input = {1,0};
        
        Network net = new Network()
                        .withStructure(structure)
                        .withW(w)
                        .withTheta(theta)
                        .withEta(ETA);


        double[] answer = net.answer(input);
        double TARGET = 1.0;
        net.learn(new Patterns(new double[][][]{{input,{TARGET}}}));
        double new_w;

        new_w = net.w()[0][0][0];
        assertEquals(
            ETA*(TARGET-answer[0])*answer[0]*(1-answer[0])*input[0],
            new_w-ORIGINAL_W,
            (new_w-ORIGINAL_W)*1e-6);

        new_w = net.w()[0][0][1];
        assertEquals(
            ETA*(TARGET-answer[0])*answer[0]*(1-answer[0])*input[1],
            new_w-ORIGINAL_W,
            (new_w-ORIGINAL_W)*1e-6);
    }

    @Test
    public void correctParameterChanges1_2Network1Pat()
    {
        final double ORIGINAL_W = 1;
        final double ETA = 1;
        int[] structure = {1,2};
        double[][][] originalW = {{{.75},{2}}};
        double[][] originalTheta = {{-1,1}};
        double[] input = {1};
        double[] target = {1.0,0.0};
        
        Network net = new Network()
                        .withStructure(structure)
                        .withW(originalW)
                        .withTheta(originalTheta)
                        .withEta(ETA);

        double[] answer = net.answer(input);
        net.learn(new Patterns(new double[][][]{{input,target}}));
        double new_w;
        double new_θ;

        new_w = net.w()[0][0][0];
        assertEquals(
            ETA*(target[0]-answer[0])*answer[0]*(1-answer[0])*input[0],
            new_w-originalW[0][0][0],
            (new_w-originalW[0][0][0])*1e-6);

        new_w = net.w()[0][1][0];
        assertEquals(
            ETA*(target[1]-answer[1])*answer[1]*(1-answer[1])*input[0],
            new_w-originalW[0][1][0],
            Math.abs(new_w-originalW[0][1][0])*1e-6);

        new_θ = net.theta()[0][0];
        assertEquals(
            ETA*(target[0]-answer[0])*answer[0]*(1-answer[0])*1,
            new_θ-originalTheta[0][0],
            (new_θ-originalTheta[0][0])*1e-6);

        new_θ = net.theta()[0][1];
        assertEquals(
            ETA*(target[1]-answer[1])*answer[1]*(1-answer[1])*1,
            new_θ-originalTheta[0][1],
            Math.abs(new_θ-originalTheta[0][1])*1e-6);
    }

    @Test
    public void correctParameterChanges1_1_1Network1Pat()
    {
        final double ETA = 1;
        int[] structure = {1,1,1};
        double[][][] originalW = {{{1}},{{.5}}};
        double[][] originalTheta = {{0},{1}};
        double[] input = {1.0};
        double[] target = {0.0};
        
        Network net = new Network()
                        .withStructure(structure)
                        .withW(originalW)
                        .withTheta(originalTheta)
                        .withEta(ETA);

        double[] answer = net.answer(input);
        net.learn(new Patterns(new double[][][]{{input,target}}));
        double new_w;
        double new_θ;

        // output layer
        double delta1 = (target[0]-answer[0])*answer[0]*(1-answer[0]);
        new_w = net.w()[1][0][0];
        assertEquals(
            ETA*delta1*net.outputs().get(1,0),
            new_w-originalW[1][0][0],
            Math.abs(new_w-originalW[1][0][0])*1e-6);

        new_θ = net.theta()[1][0];
        assertEquals(
            ETA*delta1*1,
            new_θ-originalTheta[1][0],
            Math.abs(new_θ-originalTheta[1][0])*1e-6);


    }
}

// TODO:
// Define a supertype "node"
// and a supertype "edge" with "weight" and connections.
