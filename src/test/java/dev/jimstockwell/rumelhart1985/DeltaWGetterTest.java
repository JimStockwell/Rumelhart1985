package dev.jimstockwell.rumelhart1985;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

//import static java.util.Arrays.deepEquals;

public interface DeltaWGetterTest<T extends DeltaWGetter>
{

    T createValue(double[][][] doublexxx);

    @Test
    default void isCallable()
    {
        T value = createValue(new double[][][] {{{1.0}}} );
        assertNotNull(value.getDeltaW());
    }

    @Test
    default void isSameAsSet()
    {
        final var valueIn = new double[][][] {{{1.0}}};
        final T holder = createValue(valueIn);
        final var valueOut = holder.getDeltaW();
        assertArrayEquals(valueOut,valueIn);
    }

    @Test
    default void inputIsIsolated()
    {
        final var original = new double[][][] {{{1.0}}};
        final var valueIn = ArraysExtended.threeDCopyOf(original);
        final T holder = createValue(valueIn);

        valueIn[0][0][0] = 2.0;
        final var valueOut = holder.getDeltaW();

        assertArrayEquals(original, valueOut);
    }

    @Test
    default void outputIsIsolated()
    {
        final var original = new double[][][] {{{1.0}}};
        final var valueIn = ArraysExtended.threeDCopyOf(original);
        final T holder = createValue(valueIn);

        final var valueOut = holder.getDeltaW();
        valueOut[0][0][0] = 2.0;
        final var valueOutAgain = holder.getDeltaW();

        assertArrayEquals(original, valueOutAgain);
    }
}
