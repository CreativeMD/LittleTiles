package com.creativemd.littletiles.common.tile.math.vec;

import java.security.InvalidParameterException;

import javax.vecmath.Vector3d;

import com.creativemd.littletiles.common.utils.grid.IGridBased;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LittleVecContext implements IGridBased {
	
	protected LittleVec vec;
	protected LittleGridContext context;
	
	public LittleVecContext() {
		this(new LittleVec(0, 0, 0), LittleGridContext.getMin());
	}
	
	public LittleVecContext(String name, NBTTagCompound nbt) {
		int[] array = nbt.getIntArray(name);
		if (array.length == 3) // Loading vec
		{
			LittleVec vec = new LittleVec(name, nbt);
			this.context = LittleGridContext.get();
			this.vec = new LittleVec(vec.x, vec.y, vec.z);
		} else if (array.length == 4) {
			this.vec = new LittleVec(array[0], array[1], array[2]);
			this.context = LittleGridContext.get(array[3]);
		} else
			throw new InvalidParameterException("No valid coords given " + nbt);
	}
	
	public LittleVecContext(LittleVec vec, LittleGridContext context) {
		this.vec = vec;
		this.context = context;
	}
	
	@Override
	public LittleGridContext getContext() {
		return context;
	}
	
	@Override
	public void convertTo(LittleGridContext to) {
		vec.convertTo(context, to);
		this.context = to;
	}
	
	@Override
	public void convertToSmallest() {
		int size = vec.getSmallestContext(context);
		if (size < context.size)
			convertTo(LittleGridContext.get(size));
	}
	
	public void add(LittleVecContext vec) {
		ensureContext(vec, () -> this.vec.add(vec.vec));
	}
	
	public void add(BlockPos pos) {
		this.vec.add(pos, context);
	}
	
	public void sub(LittleVecContext vec) {
		ensureContext(vec, () -> this.vec.sub(vec.vec));
	}
	
	public void sub(BlockPos pos) {
		this.vec.sub(pos, context);
	}
	
	public LittleVecContext copy() {
		return new LittleVecContext(vec.copy(), context);
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
	
	public LittleVec getVec() {
		return vec;
	}
	
	public Vec3d getVec3d() {
		return vec.getVec(context);
	}
	
	public Vector3d getVector() {
		return vec.getVector(context);
	}
	
	public LittleVec getVec(LittleGridContext context) {
		if (context == this.context)
			return vec.copy();
		LittleVec newVec = vec.copy();
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
	
	@Deprecated
	public void overwriteContext(LittleGridContext context) {
		this.context = context;
	}
	
	@Override
	public boolean equals(Object paramObject) {
		if (paramObject instanceof LittleVecContext) {
			LittleVecContext otherVec = (LittleVecContext) paramObject;
			LittleGridContext oldContext = context;
			LittleGridContext oldContextPos = otherVec.context;
			
			if (getContext() != otherVec.getContext()) {
				if (getContext().size > otherVec.getContext().size)
					otherVec.convertTo(getContext());
				else
					convertTo(otherVec.getContext());
			}
			
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
