package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static java.time.Duration.ofMillis;
import static java.lang.Math.exp;
import java.util.Arrays;

public class NetworkTest 
{
    @Test
    public void useOfEmptyNetworkCausesNoExceptions()
    {
        Network net = new Network();
        net.w();
        net.theta();
        net.learn(Patterns.empty(), 1);
        net.loss(Patterns.empty());
        net.answer(new double[0]);
        net.outputs(new double[0]);
        assertEquals(net, new Network()); // no random weights or thetas
        assertEquals(net.hashCode(), new Network().hashCode());
    }

    @Test
    public void useOfEmptyNetworkDoesNotHangLearn()
    throws InterruptedException
    {
        Network net = new Network();
        assertTimeout(ofMillis(500), () -> {
            net.learn(Patterns.empty());
        });

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
        assertTrue(Arrays.deepEquals(w,net.w()));

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

        assertTrue(Arrays.deepEquals(theta,net.theta()));

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
        Network net = new Network().withStructure(new int[] {2,2})
                                   .withW(new double[][][] {{{.1,0},{0,.9}}})
                                   .withTheta(new double[][]{{0,0}});
        double[] answer = net.answer(new double[] {.9,.1});
        assertEquals(2, answer.length);
        assertEquals(answer[0],answer[1],1e-6);

        Network diffThetas =
            new Network().withStructure(new int[] {2,2})
                         .withW(new double[][][] {{{1,0},{0,2}}})
                         .withTheta(new double[][]{{0,-3}});
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

    @Test
    public void externalWeightsValidatedAgainstStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network()
            .withStructure(new int[]{1,1})
            .withW(new SimpleWeights(new double[][][]{{{1,2}}}))
            .withTheta(new double[][]{{0}});
        });
    }


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
    public void withStructureValidates()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            Network net = new Network().withStructure(new int[]{-1,2,1});
        });

        assertThrows(NullPointerException.class, () -> {
            Network net = new Network().withStructure(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Network net = new Network().withStructure(new int[]{0});
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Network net = new Network().withStructure(new int[]{});
        });
    }

    @Test
    public void learnMakesCorrectSizedWAndTheta()
    {
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
        assertNotEquals(net2.w()[0][0][1], net2.w()[0][0][0]);
        assertNotEquals(net2.theta()[0][0], net2.theta()[0][1]);
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
        Network learned = net.learn(
            new Patterns(new double[][][]{{{INPUT},{TARGET}}}),1);

        double new_w = learned.w()[0][0][0];
        assertEquals(
            ETA*(TARGET-answer[0])*answer[0]*(1-answer[0])*INPUT,
            new_w-ORIGINAL_W,
            (new_w-ORIGINAL_W)*1e-6);
        double new_θ = learned.theta()[0][0];
        assertEquals(
            ETA*(TARGET-answer[0])*answer[0]*(1-answer[0])*1,
            new_θ-ORIGINAL_θ,
            (new_θ-ORIGINAL_θ)*1e-6);
    }

    @Test
    public void learnWithCountUsesCorrectBase()
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

        Network learned0 = net.learn(
            new Patterns(new double[][][]{{{INPUT},{TARGET}}}),0);
        Network learned1 = net.learn(
            new Patterns(new double[][][]{{{INPUT},{TARGET}}}),1);

        assertEquals(net, learned0);
        assertNotEquals(net, learned1);
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
        Network learned =
            net.learn(new Patterns(new double[][][]{{input,{TARGET}}}),1);

        double new_w = learned.w()[0][0][0];
        assertEquals(
            ETA*(TARGET-answer[0])*answer[0]*(1-answer[0])*input[0],
            new_w-ORIGINAL_W,
            (new_w-ORIGINAL_W)*1e-6);

        new_w = learned.w()[0][0][1];
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
        Network learned = net.learn(
            new Patterns(new double[][][]{{input,target}}),1);
        double new_w;
        double new_θ;

        new_w = learned.w()[0][0][0];
        assertEquals(
            ETA*(target[0]-answer[0])*answer[0]*(1-answer[0])*input[0],
            new_w-originalW[0][0][0],
            (new_w-originalW[0][0][0])*1e-6);

        new_w = learned.w()[0][1][0];
        assertEquals(
            ETA*(target[1]-answer[1])*answer[1]*(1-answer[1])*input[0],
            new_w-originalW[0][1][0],
            Math.abs(new_w-originalW[0][1][0])*1e-6);

        new_θ = learned.theta()[0][0];
        assertEquals(
            ETA*(target[0]-answer[0])*answer[0]*(1-answer[0])*1,
            new_θ-originalTheta[0][0],
            (new_θ-originalTheta[0][0])*1e-6);

        new_θ = learned.theta()[0][1];
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
        Network learned =
            net.learn(new Patterns(new double[][][]{{input,target}}),1);
        double new_w;
        double new_θ;

        // output layer
        double delta1 = (target[0]-answer[0])*answer[0]*(1-answer[0]);
        new_w = learned.w()[1][0][0];
        assertEquals(
            ETA*delta1*net.outputs(input).get(1,0),
            new_w-originalW[1][0][0],
            Math.abs(new_w-originalW[1][0][0])*1e-6);

        new_θ = learned.theta()[1][0];
        assertEquals(
            ETA*delta1*1,
            new_θ-originalTheta[1][0],
            Math.abs(new_θ-originalTheta[1][0])*1e-6);
    }

    @Test
    public void structuresMatchUpFor231()
    {
        Patterns pattern = Patterns.xor().onePattern(0);
        int[] structure = {2, 3, 1};
        
        var net = new Network().withStructure(structure)
                     .withEta(.0001);

        assertEquals(2, net.w().length);
        assertEquals(3, net.w()[0].length);
        assertEquals(1, net.w()[1].length);
        assertEquals(2, net.w()[0][0].length);
        assertEquals(2, net.w()[0][1].length);
        assertEquals(2, net.w()[0][2].length);
        assertEquals(3, net.w()[1][0].length);

        net.learn(pattern,1);
    }

    @Test
    public void correctLossForOnePattern()
    {
        double[] target = {0.0, 1.0};
        double[] output1 = {1.0, 1.0};
        double[] output2 = {1.0, 0.5};
        double expectedLoss1 = 0.5 * (1+0);
        double expectedLoss2 = 0.5 * (1+.25);
        
        assertEquals(
            expectedLoss1,
            Network.lossForOnePattern(target,output1),
            .00001);

        assertEquals(
            expectedLoss2,
            Network.lossForOnePattern(target,output2),
            .00001);
    }

    @Test
    public void newParametersFromOneStepOfOnePatternReduceLoss()
    {
        Patterns pattern = Patterns.xor().onePattern(0);
        int[] structure = {
            pattern.getInputPattern(0).length,
            3,
            pattern.getOutputPattern(0).length};
        
        //
        // If we have eta very small,
        // I don't think we'll fail this test very often.
        // We'll pass it 50% of the time, per iteration,
        // even if learninging produces random results.
        // So, keep eta small, and do several iterations,
        // but not too many.  I'm not sure how to quantify that...
        //
        var originalNetwork = new Network().withStructure(structure)
                                           .withEta(.0001);
        double originalLoss = originalNetwork.loss(pattern);
        for(int i=0; i<5; i++)
        {
            double newLoss = originalNetwork.learn(pattern,1).loss(pattern);

            assertTrue( originalLoss - newLoss > 0 );
        }
    }

    @Test
    public void newParametersFromOneStepOfMultiplePatternsReduceLoss()
    {
        Patterns patterns = Patterns.xor();
        int[] structure = {
            patterns.getInputPattern(0).length,
            3,
            patterns.getOutputPattern(0).length};
        
        //
        // If we have eta very small,
        // I don't think we'll fail this test very often.
        // We'll pass it 50% of the time, per iteration,
        // even if learninging produces random results.
        // So, keep eta small, and do several iterations,
        // but not too many.  I'm not sure how to quantify that...
        //
        var originalNetwork = new Network().withStructure(structure)
                                           .withEta(.0001);
        double originalLoss = originalNetwork.loss(patterns);
        for(int i=0; i<5; i++)
        {
            double newLoss = originalNetwork.learn(patterns,1).loss(patterns);

            assertTrue( originalLoss - newLoss > 0 );
        }
    }

    @Test
    public void learnConverges()
    {
        Patterns patterns = Patterns.xor();
        int[] structure = {
            patterns.getInputPattern(0).length,
            5,
            patterns.getOutputPattern(0).length};

        var originalNetwork = new Network().withStructure(structure)
                                           .withEta(.1);
        var originalLoss = originalNetwork.loss(patterns);
        var finalNetwork = originalNetwork.learn(patterns);
        var finalLoss = finalNetwork.loss(patterns);

        assertEquals(0.0, finalNetwork.answer(new double[]{0,0})[0],.1);
        assertEquals(1.0, finalNetwork.answer(new double[]{0,1})[0],.1);
        assertEquals(1.0, finalNetwork.answer(new double[]{1,0})[0],.1);
        assertEquals(0.0, finalNetwork.answer(new double[]{1,1})[0],.1);
    }

    @Test
    public void learnKeepsNetworkAsValueObject()
    {
        Patterns patterns = Patterns.xor();
        int[] structure = {
            patterns.getInputPattern(0).length,
            5,
            patterns.getOutputPattern(0).length};

        var originalNetwork = new Network().withStructure(structure)
                                           .withEta(.1);
        var finalNetwork = originalNetwork.learn(patterns);
        
        assertEquals(originalNetwork, originalNetwork.withEta(.1));
        assertNotEquals(originalNetwork, finalNetwork);
    }

    @Test
    public void equalsWorks()
    {
        Network emptyNet = new Network();

        assertTrue(emptyNet.equals(emptyNet));
        assertFalse(emptyNet.equals(null));
        assertFalse(emptyNet.equals(new Object()));

        int[] structure = { 1, 1};
        Network withStruct = new Network().withStructure(structure).withEta(.1);
        assertEquals(withStruct,withStruct.withEta(.1));
        assertNotEquals(emptyNet, withStruct);

        assertEquals(new Network().withEta(1),new Network().withEta(1));
        assertNotEquals(new Network().withEta(1),new Network().withEta(2));

        assertEquals(
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{1.0}}),
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{1.0}}));

        // a difference in W
        assertNotEquals(
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{1.0}}),
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{2.0}}})
                         .withTheta(new double[][]{{1.0}}));

        // a difference in Theta
        assertNotEquals(
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{1.0}}),
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{2.0}}));

        // cached output doesn't make them unequal
        Network with0 = new Network().withStructure(structure)
                                     .withW(new double[][][]{{{1.0}}})
                                     .withTheta(new double[][]{{1.0}});
        with0.answer(new double[] {0});

        Network with1 = new Network().withStructure(structure)
                                     .withW(new double[][][]{{{1.0}}})
                                     .withTheta(new double[][]{{1.0}});
        with1.answer(new double[] {1});

        assertEquals(with0, with1);
    }

    //
    // conceivable, these tests might fail even if hashCode is working,
    // since hash codes can be equal for unequal items.
    // However, it is the design intent for Network that this occur
    // only very rarely.
    //
    @Test
    public void hashCodeWorks()
    {
        Network emptyNet = new Network();

        assertNotEquals(new Object().hashCode(), emptyNet.hashCode());

        int[] structure = { 1, 1};
        Network withStruct = new Network().withStructure(structure).withEta(.1);
        assertEquals(withStruct.hashCode(), withStruct.withEta(.1).hashCode());
        assertNotEquals(emptyNet.hashCode(), withStruct.hashCode());
        assertEquals(new Network().hashCode(), new Network().hashCode());
        
        //
        // This next one should have different hash codes
        // because weights and theta are initialized randomly.
        //
        assertNotEquals(
            new Network().withStructure(structure).hashCode(),
            new Network().withStructure(structure).hashCode());
        

        assertEquals(
            new Network().withEta(1).hashCode(),
            new Network().withEta(1).hashCode());
        assertNotEquals(
            new Network().withEta(1).hashCode(),
            new Network().withEta(2).hashCode());

        assertEquals(
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{1.0}})
                         .hashCode(),
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{1.0}})
                         .hashCode());

        // a difference in W
        assertNotEquals(
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{1.0}})
                         .hashCode(),
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{2.0}}})
                         .withTheta(new double[][]{{1.0}})
                         .hashCode());

        // a difference in Theta
        assertNotEquals(
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{1.0}})
                         .hashCode(),
            new Network().withStructure(structure)
                         .withW(new double[][][]{{{1.0}}})
                         .withTheta(new double[][]{{2.0}})
                         .hashCode());

        // cached output doesn't make them unequal
        Network with0 = new Network().withStructure(structure)
                                     .withW(new double[][][]{{{1.0}}})
                                     .withTheta(new double[][]{{1.0}});
        with0.answer(new double[] {0});

        Network with1 = new Network().withStructure(structure)
                                     .withW(new double[][][]{{{1.0}}})
                                     .withTheta(new double[][]{{1.0}});
        with1.answer(new double[] {1});

        assertEquals(with0, with1);
    }
    

    private String deepToString(double[] x)
    {
        Double[] boxed =
            java.util.stream.DoubleStream.of(x).boxed().toArray(Double[]::new);
        return Arrays.deepToString(boxed);
    }
}
