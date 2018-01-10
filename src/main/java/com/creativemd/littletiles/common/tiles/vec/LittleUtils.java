package com.creativemd.littletiles.common.tiles.vec;

import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;

public class LittleUtils {
	
	public static float toVanillaGrid(float grid)
	{
		return grid / LittleTile.gridSize;
	}
	
	public static double toVanillaGrid(int grid)
	{
		return grid / (double) LittleTile.gridSize;
	}
	
	public static int toBlockOffset(int grid)
	{
		if(grid > 0)
			return (int) (grid / LittleTile.gridSize);
		return (int) Math.floor(grid /(double)LittleTile.gridSize);
	}
	
	public static boolean isAtEdge(double pos)
	{
		return pos % LittleTile.gridMCLength == 0;
	}
	
	public static int toGrid(int pos)
	{
		return pos * LittleTile.gridSize;
	}
	
	public static int toGrid(double pos)
	{
		pos = round(pos * LittleTile.gridSize);
		if(pos < 0)
			return (int) Math.floor(pos);
		return (int) pos;
	}
	
	public static final float EPSILON = 0.01F;
	public static final int scale = 5;
	
	public static boolean smallerThanAndEquals(double a, double b)
	{
		return a < b || equals(a, b);
	}
	
	public static boolean greaterThanAndEquals(double a, double b)
	{
		return a > b || equals(a, b);
	}
	
	public static boolean equals(double a, double b)
	{
		return a == b ? true : Math.abs(a - b) < EPSILON;
	}
	
	public static boolean equals(float a, float b)
	{
		return a == b ? true : Math.abs(a - b) < EPSILON;
	}
	
	public static double round(double valueToRound, int numberOfDecimalPlaces)
	{
	    double multipicationFactor = Math.pow(10, numberOfDecimalPlaces);
	    double interestedInZeroDPs = valueToRound * multipicationFactor;
	    return Math.round(interestedInZeroDPs) / multipicationFactor;
	}
	
	public static float round(float valueToRound, int numberOfDecimalPlaces)
	{
		float multipicationFactor = (float) Math.pow(10, numberOfDecimalPlaces);
		float interestedInZeroDPs = valueToRound * multipicationFactor;
	    return Math.round(interestedInZeroDPs) / multipicationFactor;
	}
	
	public static double round(double value)
	{
		/*int pow = 10;
		int realScale = scale + (int) (Math.log10(value) + 1);
	    for (int i = 1; i < realScale; i++)
	        pow *= 10;
	    double tmp = value * pow;
	    return ( (double) ( (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;*/
		return round(value, scale);
	}
	
	public static float round(float value)
	{
	    /*int pow = 10;
	    int realScale = scale + (int) (Math.log10(value) + 1);
	    for (int i = 1; i < realScale; i++)
	        pow *= 10;
	    float tmp = value * pow;
	    return ( (float) ( (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;*/
		return round(value, scale);
	}
	
}
