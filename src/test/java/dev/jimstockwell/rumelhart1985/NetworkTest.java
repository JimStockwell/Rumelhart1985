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
    private Weights makeWeights(double[][][] w)
    {
        return new EdgeWeights(w);
    }

    @Test
    public void useOfEmptyNetworkCausesNoExceptions()
    {
        Network net = new Network.Builder().build();
        net.getW();
        net.theta();
        net.learn(Patterns.empty(), 1);
        net.loss(Patterns.empty());
        net.answer(new double[0]);
        net.outputs(new double[0]);
        //
        // This equals comparison isn't to find the answer,
        // but to show it doesn't cause an exception.
        //
        net.equals(new Network.Builder().build());
    }

    @Test
    public void useOfEmptyNetworkDoesNotHangLearn()
    throws InterruptedException
    {
        Network net = new Network.Builder().build();
        assertTimeout(ofMillis(500), () -> {
            net.learn(Patterns.empty());
        });

    }

    @Test
    public void reportsWCorrectly()
    {
        var weightsArray = new double[][][] {{{1e6,2e6},{1e5,2e5}}};
        // Weights w = makeWeights(weightsArray);
        Weights w = makeWeights(weightsArray);
        double[][] theta = {{0,0}};
        int struct[] = {2,2};
        Network net = new Network.Builder().withStructure(struct)
                                           .withTheta(theta)
                                           .withW(w)
                                           .build();
        Weights gottenW = net.getW();

        assertTrue(w==gottenW);
    }

    @Test
    public void reportsThetaCorrectly()
    {
        double[][] theta = {{1e6,2e6},{1e5}};
        Network net = new Network.Builder().withStructure(new int[]{1,2,1})
                                           .withTheta(theta)
                                           .build();

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
        Network net = new Network.Builder().withStructure(new int[]{1,1})
                                   .withW(makeWeights(new double[][][]{{{w}}}))
                                   .withTheta(new double[][]{{theta}})
                                   .build();
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
        Network net1 = new Network.Builder()
            .withStructure(new int[]{1,1})
            .withW(makeWeights(new double[][][]{{{1}}}))
            .withTheta(new double[][]{{0}}).build();
        Network net2diff = new Network.Builder()
            .withStructure(new int[]{2,1})
            .withW(makeWeights(new double[][][]{{{.9,.1}}}))
            .withTheta(new double[][]{{0}}).build();
        Network net2same = new Network.Builder()
            .withStructure(new int[]{2,1})
            .withW(makeWeights(new double[][][]{{{1,1}}}))
            .withTheta(new double[][]{{0}}).build();
    
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
        Network net = new Network.Builder().withStructure(new int[] {2,2})
                                   .withW(makeWeights(new double[][][] {{{.1,0},{0,.9}}}))
                                   .withTheta(new double[][]{{0,0}}).build();
        double[] answer = net.answer(new double[] {.9,.1});
        assertEquals(2, answer.length);
        assertEquals(answer[0],answer[1],1e-6);

        Network diffThetas =
            new Network.Builder().withStructure(new int[] {2,2})
                         .withW(makeWeights(new double[][][] {{{1,0},{0,2}}}))
                         .withTheta(new double[][]{{0,-3}}).build();
        double[] answerDT2 = diffThetas.answer(new double[] {3,3});
        assertEquals(answerDT2[0],answerDT2[1],1e-6);
    }

    @Test
    public void multipleLayersWorkForward()
    {
        Network net1 = new Network.Builder()
            .withStructure(new int[] {1,1})
            .withW(makeWeights(new double[][][] {{{1}}}))
            .withTheta(new double[][]{{0}}).build();
        Network net2 = new Network.Builder()
            .withStructure(new int[] {1,1,1})
            .withW(makeWeights(new double[][][] {{{1}},{{1}}}))
            .withTheta(new double[][]{{0},{0}}).build();
        
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
        var tooLongW = makeWeights(new double[][][]{{{1,2}}});
        assertThrows(IllegalArgumentException.class, () -> {
            Network net = new Network.Builder()
                .withStructure(new int[]{1,1})
                .withW(tooLongW)
                .withTheta(new double[][]{{0}}).build();
        });
    }

    @Test
    public void wTooShortIsCheckedAgainstStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network.Builder()
            .withStructure(new int[]{1,1})
            .withW(makeWeights(new double[][][]{{{}}}))
            .withTheta(new double[][]{{0}}).build();
        });
    }

    @Test
    public void wSecondNodeCheckedAgainstStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network.Builder()
            .withStructure(new int[]{1,2})
            .withW(makeWeights(new double[][][]{{{1.0},{1.1,1.11}}}))
            .withTheta(new double[][]{{0,0}}).build();
        });
    }

    @Test
    public void wNumberOfNodesCorrectVsStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network.Builder()
            .withStructure(new int[]{1,2})
            .withW(makeWeights(new double[][][]{{{1.0},{1.1},{1.2}}}))
            .withTheta(new double[][]{{0,0}}).build();
        });
    }

    @Test
    public void wNumberOfLayersCorrectVsStructure()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network.Builder()
            .withStructure(new int[]{1,2,1})
            .withW(makeWeights(new double[][][]{{{1.0},{1.1}}}))
            .withTheta(new double[][]{{0,0},{0}}).build();
        });
    }

    @Test
    public void wEachLayerValidated()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network.Builder()
            .withStructure(new int[]{1,2,1})
            .withW(makeWeights(new double[][][]{{{1.0},{1.1}},{{}}}))
            .withTheta(new double[][]{{0,0},{0}}).build();
        });
    }

    @Test
    public void thetaNumberOfLayersValidated()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network.Builder()
            .withStructure(new int[]{1,2,1})
            .withW(makeWeights(new double[][][]{{{1.0},{1.1}},{{1,1}}}))
            .withTheta(new double[][]{{0,0},{0},{0}}).build();
        });
    }

    @Test
    public void thetaEachLayerValidated()
    {
        assertThrows(IllegalArgumentException.class, () -> {
        Network net = new Network.Builder()
            .withStructure(new int[]{1,2,1})
            .withW(makeWeights(new double[][][]{{{1},{1}},{{1,1}}}))
            .withTheta(new double[][]{{0,0},{0,99}}).build();
        });
    }

    @Test
    public void withStructureValidates()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            Network net = new Network.Builder().withStructure(new int[]{-1,2,1}).build();
        });

        assertThrows(NullPointerException.class, () -> {
            Network net = new Network.Builder().withStructure(null).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Network net = new Network.Builder().withStructure(new int[]{0}).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Network net = new Network.Builder().withStructure(new int[]{}).build();
        });
    }

    @Test
    public void learnMakesCorrectSizedWAndTheta()
    {
        // Network net2 = new Network(new int[]{3,2});
        Network net2 = new Network.Builder().withStructure(new int[]{3,2}).build();
        assertEquals(1, net2.getW().numberOfWeightLayers());
        assertEquals(2, net2.getW().sizeOfWeightLayer(0));
        assertEquals(1, net2.theta().length);
        assertEquals(2, net2.theta()[0].length);
    }


    // If theta or w is missing,
    // some default is used.
    @Test
    public void learnMakesNonSymetricWAndTheta()
    {
        Network net2 = new Network.Builder().withStructure(new int[]{2,2}).build();
        assertNotEquals(net2.getW().getWeight(0,0,1), net2.getW().getWeight(0,0,0));
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
        
        Network net = new Network.Builder()
                        .withStructure(structure)
                        .withW(makeWeights(w))
                        .withTheta(theta)
                        .withEta(ETA).build();

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

        double new_w = learned.getW().getWeight(0,0,0);
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
        
        Network net = new Network.Builder()
                        .withStructure(structure)
                        .withW(makeWeights(w))
                        .withTheta(theta)
                        .withEta(ETA).build();

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
        
        Network net = new Network.Builder()
                        .withStructure(structure)
                        .withW(makeWeights(w))
                        .withTheta(theta)
                        .withEta(ETA).build();


        double[] answer = net.answer(input);
        double TARGET = 1.0;
        Network learned =
            net.learn(new Patterns(new double[][][]{{input,{TARGET}}}),1);

        double new_w = learned.getW().getWeight(0,0,0);
        assertEquals(
            ETA*(TARGET-answer[0])*answer[0]*(1-answer[0])*input[0],
            new_w-ORIGINAL_W,
            (new_w-ORIGINAL_W)*1e-6);

        new_w = learned.getW().getWeight(0,0,1);
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
        
        Network net = new Network.Builder()
                        .withStructure(structure)
                        .withW(makeWeights(originalW))
                        .withTheta(originalTheta)
                        .withEta(ETA).build();

        double[] answer = net.answer(input);
        Network learned = net.learn(
            new Patterns(new double[][][]{{input,target}}),1);
        double new_w;
        double new_θ;

        new_w = learned.getW().getWeight(0,0,0);
        assertEquals(
            ETA*(target[0]-answer[0])*answer[0]*(1-answer[0])*input[0],
            new_w-originalW[0][0][0],
            (new_w-originalW[0][0][0])*1e-6);

        new_w = learned.getW().getWeight(0,1,0);
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
        
        Network net = new Network.Builder()
                        .withStructure(structure)
                        .withW(makeWeights(originalW))
                        .withTheta(originalTheta)
                        .withEta(ETA).build();

        double[] answer = net.answer(input);
        Network learned =
            net.learn(new Patterns(new double[][][]{{input,target}}),1);
        double new_w;
        double new_θ;

        // output layer
        double delta1 = (target[0]-answer[0])*answer[0]*(1-answer[0]);
        new_w = learned.getW().getWeight(1,0,0);
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
        
        var net = new Network.Builder().withStructure(structure)
                     .withEta(.0001).build();

        assertEquals(2, net.getW().numberOfWeightLayers());
        assertEquals(3, net.getW().sizeOfWeightLayer(0));
        assertEquals(1, net.getW().sizeOfWeightLayer(1));
        assertTrue(net.getW().consistentWith(structure));

        net.learn(pattern,1); // Check that net matches the pattern too
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
        var originalNetwork = new Network.Builder().withStructure(structure)
                                           .withEta(.0001).build();
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
        var originalNetwork = new Network.Builder().withStructure(structure)
                                           .withEta(.0001).build();
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

        var originalNetwork = new Network.Builder().withStructure(structure)
                                           .withEta(.1).build();
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

        var builder = new Network.Builder().withStructure(structure)
                                           .withEta(.1);
        var originalNetwork = builder.build();
        var finalNetwork = originalNetwork.learn(patterns);
        
        assertNotEquals(originalNetwork, finalNetwork);
    }

    @Test
    public void equalsWorks()
    {
        Network emptyNet = new Network.Builder().build();
        int[] structure = { 1, 1};

        assertTrue(emptyNet.equals(emptyNet));
        assertFalse(emptyNet.equals(null));
        assertFalse(emptyNet.equals(new Object()));

        assertEquals(
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}}).build(),
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}}).build());

        // a difference in eta
        assertNotEquals(
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}})
                         .withEta(1.0)
                         .build(),
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}})
                         .withEta(2.0)
                         .build());

        // a difference in W
        assertNotEquals(
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}}).build(),
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{2.0}}}))
                         .withTheta(new double[][]{{1.0}}).build());

        // a difference in Theta
        assertNotEquals(
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}}).build(),
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{2.0}}).build());

        // cached output doesn't make them unequal
        Network with0 = new Network.Builder().withStructure(structure)
                                     .withW(makeWeights(new double[][][]{{{1.0}}}))
                                     .withTheta(new double[][]{{1.0}}).build();
        with0.answer(new double[] {0});

        Network with1 = new Network.Builder().withStructure(structure)
                                     .withW(makeWeights(new double[][][]{{{1.0}}}))
                                     .withTheta(new double[][]{{1.0}}).build();
        with1.answer(new double[] {1});

        assertEquals(with0, with1);
    }

    //
    // conceivably, these tests might fail even if hashCode is working,
    // since hash codes can be equal for unequal items.
    // However, it is the design intent for Network that this occur
    // only very rarely.
    //
    @Test
    public void hashCodeWorks()
    {
        Network emptyNet = new Network.Builder().build();

        assertNotEquals(new Object().hashCode(), emptyNet.hashCode());

        int[] structure = { 1, 1};
        var builder = new Network.Builder()
            .withStructure(structure)
            .withW(makeWeights(new double[][][] {{{1.0}}}))
            .withTheta(new double[][] {{1.0}});

        assertEquals(
            builder.build().hashCode(),
            builder.build().hashCode());
        assertNotEquals(emptyNet.hashCode(), builder.build().hashCode());
        
        //
        // This next one should have different hash codes
        // because weights and theta are initialized randomly.
        //
        assertNotEquals(
            new Network.Builder().withStructure(structure).build().hashCode(),
            new Network.Builder().withStructure(structure).build().hashCode());
        

        assertEquals(
            new Network.Builder().withEta(1).build().hashCode(),
            new Network.Builder().withEta(1).build().hashCode());
        assertNotEquals(
            new Network.Builder().withEta(1).build().hashCode(),
            new Network.Builder().withEta(2).build().hashCode());

        assertEquals(
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}}).build()
                         .hashCode(),
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}}).build()
                         .hashCode());

        // a difference in W
        assertNotEquals(
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}})
                         .build()
                         .hashCode(),
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{2.0}}}))
                         .withTheta(new double[][]{{1.0}})
                         .build()
                         .hashCode());

        // a difference in Theta
        assertNotEquals(
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{1.0}}).build()
                         .hashCode(),
            new Network.Builder().withStructure(structure)
                         .withW(makeWeights(new double[][][]{{{1.0}}}))
                         .withTheta(new double[][]{{2.0}}).build()
                         .hashCode());

        // cached output doesn't make them unequal
        Network with0 = new Network.Builder().withStructure(structure)
                                     .withW(makeWeights(new double[][][]{{{1.0}}}))
                                     .withTheta(new double[][]{{1.0}}).build();
        with0.answer(new double[] {0});

        Network with1 = new Network.Builder().withStructure(structure)
                                     .withW(makeWeights(new double[][][]{{{1.0}}}))
                                     .withTheta(new double[][]{{1.0}}).build();
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
