package com.creativemd.littletiles.common.tile.math;

public class LittleUtils {
    
    public static final float EPSILON = 0.01F;
    public static final int scale = 5;
    
    public static boolean smallerThanAndEquals(double a, double b) {
        return a < b || equals(a, b);
    }
    
    public static boolean greaterThanAndEquals(double a, double b) {
        return a > b || equals(a, b);
    }
    
    public static boolean equals(double a, double b) {
        return a == b ? true : Math.abs(a - b) < EPSILON;
    }
    
    public static boolean equals(float a, float b) {
        return a == b ? true : Math.abs(a - b) < EPSILON;
    }
    
    public static double round(double valueToRound, int numberOfDecimalPlaces) {
        double multipicationFactor = Math.pow(10, numberOfDecimalPlaces);
        double interestedInZeroDPs = valueToRound * multipicationFactor;
        return Math.round(interestedInZeroDPs) / multipicationFactor;
    }
    
    public static float round(float valueToRound, int numberOfDecimalPlaces) {
        float multipicationFactor = (float) Math.pow(10, numberOfDecimalPlaces);
        float interestedInZeroDPs = valueToRound * multipicationFactor;
        return Math.round(interestedInZeroDPs) / multipicationFactor;
    }
    
    public static double round(double value) {
        return round(value, scale);
    }
    
    public static float round(float value) {
        return round(value, scale);
    }
    
}
