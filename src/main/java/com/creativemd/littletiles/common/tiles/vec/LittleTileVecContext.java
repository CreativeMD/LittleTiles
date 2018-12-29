package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;

import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LittleTileVecContext {
	
	public LittleTileVec vec;
	public LittleGridContext context;
	
	public LittleTileVecContext(String name, NBTTagCompound nbt) {
		loadFromNBT(name, nbt);
	}
	
	protected void loadFromNBT(String name, NBTTagCompound nbt) {
		int[] array = nbt.getIntArray(name);
		if (array.length == 3) // Loading vec
		{
			LittleTileVec vec = new LittleTileVec(name, nbt);
			this.context = LittleGridContext.get();
			this.vec = new LittleTileVec(vec.x, vec.y, vec.z);
		} else if (array.length == 4) {
			this.vec = new LittleTileVec(array[0], array[1], array[2]);
			this.context = LittleGridContext.get(array[3]);
		} else
			throw new InvalidParameterException("No valid coords given " + nbt);
	}
	
	public LittleTileVecContext(LittleGridContext context, LittleTileVec vec) {
		this.vec = vec;
		this.context = context;
	}
	
	public void convertTo(LittleGridContext to) {
		vec.convertTo(context, to);
		this.context = to;
	}
	
	public void convertToSmallest() {
		int size = vec.getSmallestContext(context);
		if (size < context.size)
			convertTo(LittleGridContext.get(size));
	}
	
	public void ensureBothAreEqual(LittleTileVecContext vec) {
		if (context != vec.context) {
			if (context.size > vec.context.size)
				vec.convertTo(context);
			else
				this.convertTo(vec.context);
		}
	}
	
	public void add(LittleTileVecContext vec) {
		ensureBothAreEqual(vec);
		this.vec.add(vec.vec);
		vec.convertToSmallest();
		this.convertToSmallest();
	}
	
	public void add(BlockPos pos) {
		this.vec.add(pos, context);
	}
	
	public void sub(LittleTileVecContext vec) {
		ensureBothAreEqual(vec);
		this.vec.sub(vec.vec);
		vec.convertToSmallest();
		this.convertToSmallest();
	}
	
	public void sub(BlockPos pos) {
		this.vec.sub(pos, context);
	}
	
	public LittleTileVecContext copy() {
		return new LittleTileVecContext(context, vec.copy());
	}
	
	public BlockPos getBlockPos() {
		return vec.getBlockPos(context);
	}
	
	public double getPosX() {
		return vec.getPosX(context);
	}
	
	public double getPosY() {
		return vec.getPosY(context);
	}
	
	public double getPosZ() {
		return vec.getPosZ(context);
	}
	
	public Vec3d getVec() {
		return vec.getVec(context);
	}
	
	public LittleTileVec getVec(LittleGridContext context) {
		if (context == this.context)
			return vec.copy();
		LittleTileVec newVec = vec.copy();
		newVec.convertTo(this.context, context);
		return newVec;
	}
	
	public void writeToNBT(String name, NBTTagCompound nbt) {
		nbt.setIntArray(name, new int[] { vec.x, vec.y, vec.z, context.size });
	}
	
	@Override
	public int hashCode() {
		return vec.hashCode();
	}
	
	@Override
	public boolean equals(Object paramObject) {
		if (paramObject instanceof LittleTileVecContext) {
			LittleTileVecContext otherVec = (LittleTileVecContext) paramObject;
			LittleGridContext oldContext = context;
			LittleGridContext oldContextPos = otherVec.context;
			
			ensureBothAreEqual(otherVec);
			
			boolean equal = context == otherVec.context && vec.equals(otherVec.vec);
			
			otherVec.convertTo(oldContextPos);
			convertTo(oldContext);
			
			return equal;
		}
		
		return super.equals(paramObject);
	}
	
	@Override
	public String toString() {
		return "[" + vec.x + "," + vec.y + "," + vec.z + ",grid:" + context.size + "]";
	}
}
