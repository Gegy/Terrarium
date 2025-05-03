package dev.gegy.terrarium.backend.util;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.Arrays;

public class LogarithmicSliderMapper {
    private final double[] values;

    public LogarithmicSliderMapper(
            final int firstOrder,
            final int lastOrder,
            final int mantissaDigits,
            final int mantissaStep
    ) {
        final int mantissaMin = (int) Math.pow(10, mantissaDigits - 1);
        final int mantissaMax = (int) Math.pow(10, mantissaDigits);
        final DoubleList values = new DoubleArrayList();
        for (int order = firstOrder; order < lastOrder; order++) {
            final double factor = Math.pow(10, order - mantissaDigits + 1);
            for (int mantissa = mantissaMin; mantissa < mantissaMax; mantissa += mantissaStep) {
                values.add(mantissa * factor);
            }
        }
        values.add((int) Math.pow(10.0, lastOrder));
        this.values = values.toDoubleArray();
    }

    public double fromSlider(final double slider) {
        if (slider <= 0.0) {
            return values[0];
        } else if (slider >= 1.0) {
            return values[values.length - 1];
        }
        return values[Util.floorInt(slider * (values.length - 2)) + 1];
    }

    public double toSlider(final double value) {
        final int index = indexOf(value);
        if (index >= values.length) {
            return 1.0;
        } else if (index == 0) {
            return 0.0;
        }
        return (index - 0.5) / (values.length - 2);
    }

    private int indexOf(final double value) {
        final int i = Arrays.binarySearch(values, value);
        return i < 0 ? -i - 1 : i;
    }
}
