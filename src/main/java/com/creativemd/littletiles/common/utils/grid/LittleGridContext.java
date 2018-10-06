package com.creativemd.littletiles.common.utils.grid;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.LittleTilesConfig;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleUtils;
import com.google.common.math.IntMath;

import net.minecraft.nbt.NBTTagCompound;

public class LittleGridContext {
	
	public static final int overallDefault = 16;
	
	@Deprecated
	public static final int oldHaldGridSize = 8;
	
	public static int[] gridSizes;
	public static int minSize;
	public static int exponent;
	public static int defaultSize;
	private static int defaultSizeIndex;
	public static LittleGridContext[] context;
	
	public static LittleGridContext loadGrid(int min, int defaultGrid, int scale, int exponent) {
		minSize = min;
		defaultSize = defaultGrid;
		gridSizes = new int[scale];
		context = new LittleGridContext[scale];
		int size = min;
		for (int i = 0; i < gridSizes.length; i++) {
			gridSizes[i] = size;
			context[i] = new LittleGridContext(size);
			if (context[i].isDefault)
				defaultSizeIndex = i;
			size *= exponent;
		}
		
		return get();
	}
	
	public static List<String> getNames() {
		List<String> names = new ArrayList<>();
		for (int i = 0; i < context.length; i++) {
			names.add(context[i].size + "");
		}
		return names;
	}
	
	public static LittleGridContext get(int size) {
		if (defaultSize == size)
			return context[LittleGridContext.defaultSizeIndex];
		for (int i = 0; i < context.length; i++) {
			if (context[i].size == size)
				return context[i];
		}
		throw new RuntimeException("Invalid gridsize = '" + size + "'!");
	}
	
	public static LittleGridContext get() {
		return context[LittleGridContext.defaultSizeIndex];
	}
	
	public static LittleGridContext get(NBTTagCompound nbt) {
		if (nbt.hasKey("grid"))
			return LittleGridContext.get(nbt.getInteger("grid"));
		return LittleGridContext.get();
	}
	
	public static LittleGridContext getOverall(NBTTagCompound nbt) {
		if (nbt.hasKey("grid"))
			return LittleGridContext.get(nbt.getInteger("grid"));
		return LittleGridContext.get(overallDefault);
	}
	
	public static LittleGridContext getMin() {
		return context[0];
	}
	
	public static LittleGridContext getMax() {
		return context[context.length - 1];
	}
	
	public static LittleGridContext min(LittleGridContext context, LittleGridContext context2) {
		if (context.size <= context2.size)
			return context;
		return context2;
	}
	
	public static LittleGridContext max(LittleGridContext context, LittleGridContext context2) {
		if (context.size >= context2.size)
			return context;
		return context2;
	}
	
	public static void remove(NBTTagCompound nbt) {
		nbt.removeTag("grid");
	}
	
	public final int size;
	public final double gridMCLength;
	public final int minPos;
	public final int maxPos;
	public final int maxTilesPerBlock;
	public final double minimumTileSize;
	public final boolean isDefault;
	
	/** doubled **/
	public final LittleTileVec rotationCenter;
	
	public final int[] minSizes;
	
	protected LittleGridContext(int gridSize) {
		size = gridSize;
		gridMCLength = 1D / gridSize;
		minPos = 0;
		maxPos = gridSize;
		maxTilesPerBlock = gridSize * gridSize * gridSize;
		minimumTileSize = 1D / maxTilesPerBlock;
		isDefault = defaultSize == gridSize;
		
		minSizes = new int[size];
		minSizes[0] = 1;
		for (int i = 1; i < minSizes.length; i++) {
			minSizes[i] = size / IntMath.gcd(i, size);
		}
		
		rotationCenter = new LittleTileVec(size, size, size);
	}
	
	public void set(NBTTagCompound nbt) {
		if (!isDefault || LittleTilesConfig.core.forceToSaveDefaultSize)
			nbt.setInteger("grid", size);
		else
			nbt.removeTag("grid");
	}
	
	public void setOverall(NBTTagCompound nbt) {
		if (size != overallDefault)
			nbt.setInteger("grid", size);
	}
	
	public int getMinGrid(int value) {
		return minSizes[Math.abs(value % size)];
	}
	
	public double toVanillaGrid(double grid) {
		return grid * gridMCLength;
	}
	
	public float toVanillaGrid(float grid) {
		return (float) (grid * gridMCLength);
	}
	
	public double toVanillaGrid(long grid) {
		return grid * gridMCLength;
	}
	
	public double toVanillaGrid(int grid) {
		return grid * gridMCLength;
	}
	
	public int toBlockOffset(int grid) {
		if (grid > 0)
			return (int) (grid / size);
		return (int) Math.floor(grid / (double) size);
	}
	
	public boolean isAtEdge(double pos) {
		return pos % gridMCLength == 0;
	}
	
	public int toGrid(int pos) {
		return pos * size;
	}
	
	public long toGridAccurate(double pos) {
		pos = LittleUtils.round(pos * size);
		if (pos < 0)
			return (long) Math.floor(pos);
		return (long) pos;
	}
	
	public int toGrid(double pos) {
		pos = LittleUtils.round(pos * size);
		if (pos < 0)
			return (int) Math.floor(pos);
		return (int) pos;
	}
	
	public LittleGridContext ensureContext(LittleGridContext context) {
		if (context.size > this.size)
			return context;
		return this;
	}
	
	@Override
	public String toString() {
		return "" + size;
	}
}
