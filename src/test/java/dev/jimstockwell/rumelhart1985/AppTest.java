// TODO:
//   new network size == old network size
//   turn pattern ints into doubles
//   rename answer to output
//  
//
package dev.jimstockwell.rumelhart1985;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    @Test
    public void doesBasicSteps()
    {
        Network net = new Network(new int[]{1}, new double[][]{{1}}, new double[][]{{1}});
        Patterns pats = new Patterns(new int[][][] {{{1},{0}},{{0},{1}}});
        Network educated = net.learn(pats);
        assertArrayEquals(new int[]{1},educated.answer(new int[]{0}));
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
}
