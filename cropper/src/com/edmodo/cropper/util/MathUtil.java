package com.edmodo.cropper.util;

public class MathUtil {

    /**
     * Calculates the distance between two points (x1, y1) and (x2, y2).
     */
    public static float calculateDistance(float x1, float y1, float x2, float y2) {

        final float side1 = x2 - x1;
        final float side2 = y2 - y1;

        return (float) Math.sqrt(side1 * side1 + side2 * side2);
    }
}
