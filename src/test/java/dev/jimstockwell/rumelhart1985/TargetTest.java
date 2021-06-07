package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class TargetTest {
    @Test
    public void boxedConstructorWorks() {
        var target = new Target(new Double[] {0.0,1.0});

        assertEquals(0.0, target.getTarget(0));
        assertEquals(1.0, target.getTarget(1));
    }

    @Test
    public void defensiveCopiesOfInput() {
        double[] input = new double[] {0.0, 1.0};
        Target target = new Target(input);

        input[0] = 99.0;

        assertEquals(0.0, target.getTarget(0));
    }

    @Test
    public void failFastForNulls() {
        assertThrows(
            NullPointerException.class,
            () -> new Target((Double[]) null)
        );
        assertThrows(
            NullPointerException.class,
            () -> new Target((double[]) null)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new Target(new Double[] {0.0, null, 2.0})
        );
    }
}

