package com.creativemd.littletiles.common.utils.grid;

import java.util.HashMap;

import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleUtils;
import com.google.common.math.IntMath;

public class LittleGridContext {
	
	public static final int overallDefault = 16;
	
	@Deprecated
	public static final int oldHaldGridSize = 8;
	
	public static int[] gridSizes;
	public static int minSize;
	public static int defaultSize;
	public static LittleGridContext[] context;
	
	public static LittleGridContext loadGrid(int min, int defaultGrid, int scale)
	{
		minSize = min;
		defaultSize = defaultGrid;
		gridSizes = new int[scale];
		context = new LittleGridContext[scale];
		int size = min;
		for (int i = 0; i < gridSizes.length; i++) {
			gridSizes[i] = size;
			context[i] = new LittleGridContext(size, i);
			size *= 2;
		}
		
		return get();
	}
	
	public static LittleGridContext get(int size)
	{
		return context[(size/minSize)-1];
	}
	
	public static LittleGridContext get()
	{
		return get(defaultSize);
	}
	
	public final int size;
	public final int halfGridSize;
	public final double gridMCLength;
	public final int minPos;
	public final int maxPos;
	public final int maxTilesPerBlock;
	public final double minimumTileSize;
	public final boolean isDefault;
	
	public final int[] minSizes;
	
	public LittleGridContext(int gridSize, int index) {
		size = gridSize;
		halfGridSize = gridSize/2;
		gridMCLength = 1D/gridSize;
		minPos = 0;
		maxPos = gridSize;
		maxTilesPerBlock = gridSize*gridSize*gridSize;
		minimumTileSize = 1D/maxTilesPerBlock;
		isDefault = overallDefault == gridSize;
		
		minSizes = new int[size];
		minSizes[0] = 1;
		for (int i = 1; i < minSizes.length; i++) {
			minSizes[i] = size/IntMath.gcd(i, size);
		}
	}
	
	public int getMinGrid(int value)
	{
		return minSizes[value % size];
	}
	
	public float toVanillaGrid(float grid)
	{
		return (float) (grid * gridMCLength);
	}
	
	public double toVanillaGrid(int grid)
	{
		return grid * gridMCLength;
	}
	
	public int toBlockOffset(int grid)
	{
		if(grid > 0)
			return (int) (grid / size);
		return (int) Math.floor(grid /(double)size);
	}
	
	public boolean isAtEdge(double pos)
	{
		return pos % gridMCLength == 0;
	}
	
	public int toGrid(int pos)
	{
		return pos * size;
	}
	
	public int toGrid(double pos)
	{
		pos = LittleUtils.round(pos * size);
		if(pos < 0)
			return (int) Math.floor(pos);
		return (int) pos;
	}
}
