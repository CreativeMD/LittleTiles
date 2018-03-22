package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;

import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

public class LittleTilePos {
	
	public BlockPos pos;
	public LittleTileVecContext contextVec;
	
	public LittleTilePos(String name, NBTTagCompound nbt)
	{
		int[] array = nbt.getIntArray(name);
		if(array.length == 3) //Loading vec
		{
			LittleTileVec vec = new LittleTileVec(name, nbt);
			LittleGridContext context = LittleGridContext.get();
			this.pos = vec.getBlockPos(context);
			this.contextVec = new LittleTileVecContext(context, new LittleTileVec(vec.x - pos.getX() * context.size, vec.y - pos.getY() * context.size, vec.z - pos.getZ() * context.size));
		}
		else if(array.length == 7)
		{
			this.pos = new BlockPos(array[0], array[1], array[2]);
			this.contextVec = new LittleTileVecContext(LittleGridContext.get(array[3]), new LittleTileVec(array[4], array[5], array[6]));
		}
		else
			throw new InvalidParameterException("No valid coords given " + nbt);
	}
	
	public LittleTilePos(RayTraceResult result, LittleGridContext context)
	{
		long x = context.toGridAccurate(result.hitVec.x);
		long y = context.toGridAccurate(result.hitVec.y);
		long z = context.toGridAccurate(result.hitVec.z);
		this.pos = new BlockPos((int) Math.floor(context.toVanillaGrid(x)), (int) Math.floor(context.toVanillaGrid(y)), (int) Math.floor(context.toVanillaGrid(z)));
		this.contextVec = new LittleTileVecContext(context, new LittleTileVec((int) (x - context.toGridAccurate(pos.getX())), (int) (y - context.toGridAccurate(pos.getY())), (int) (z - context.toGridAccurate(pos.getZ()))));
		//if(result.sideHit.getAxisDirection() == AxisDirection.POSITIVE && !context.isAtEdge(RotationUtils.get(result.sideHit.getAxis(), result.hitVec)))
			//contextVec.vec.setAxis(result.sideHit.getAxis(), contextVec.vec.getAxis(result.sideHit.getAxis()) + 1);
	}
	
	public LittleTilePos(BlockPos pos, LittleGridContext context)
	{
		this(pos, new LittleTileVecContext(context, new LittleTileVec(0, 0, 0)));
	}
	
	public LittleTilePos(BlockPos pos, LittleGridContext context, LittleTileVec vec)
	{
		this(pos, new LittleTileVecContext(context, vec));
	}
	
	public LittleTilePos(BlockPos pos, LittleTileVecContext contextVec)
	{
		this.pos = pos;
		this.contextVec = contextVec;
	}
	
	public void convertToSmallest()
	{
		this.contextVec.convertToSmallest();
	}
	
	public void convertTo(LittleGridContext to)
	{
		this.contextVec.convertTo(to);
	}
	
	public void ensureBothAreEqual(LittleTilePos pos)
	{
		this.contextVec.ensureBothAreEqual(pos.contextVec);
	}
	
	public LittleTileVecContext getRelative(LittleTilePos pos)
	{
		ensureBothAreEqual(pos);
		LittleTileVecContext newVec = new LittleTileVecContext(getContext(), new LittleTileVec(getContext(), this.pos.subtract(pos.pos)));
		newVec.vec.add(this.contextVec.vec);
		newVec.vec.sub(pos.contextVec.vec);
		
		pos.convertToSmallest();
		convertToSmallest();
		return newVec;
	}
	
	public void add(LittleTilePos pos)
	{
		this.pos = this.pos.add(pos.pos);
		ensureBothAreEqual(pos);
		this.contextVec.vec.add(pos.contextVec.vec);
		
		pos.convertToSmallest();
		convertToSmallest();
	}
	
	public void sub(LittleTilePos pos)
	{
		this.pos = this.pos.subtract(pos.pos);
		ensureBothAreEqual(pos);
		this.contextVec.vec.sub(pos.contextVec.vec);
		
		pos.convertToSmallest();
		convertToSmallest();
	}
	
	public void add(Vec3i vec)
	{
		pos = pos.add(vec);
	}
	
	public void sub(Vec3i vec)
	{
		pos = pos.subtract(vec);
	}

	
	public void add(LittleTileVecContext vec)
	{
		contextVec.add(vec);		
	}
	
	public void sub(LittleTileVecContext vec)
	{
		contextVec.sub(vec);
	}
	
	public void removeInternalBlockOffset()
	{
		LittleGridContext context = getContext();
		//Larger
		if(contextVec.vec.x >= context.size)
		{
			int amount = contextVec.vec.x / context.size;
			contextVec.vec.x -= amount * context.size;
			pos = pos.add(amount, 0, 0);
		}
		if(contextVec.vec.y >= context.size)
		{
			int amount = contextVec.vec.y / context.size;
			contextVec.vec.y -= amount * context.size;
			pos = pos.add(0, amount, 0);
		}
		if(contextVec.vec.z >= context.size)
		{
			int amount = contextVec.vec.z / context.size;
			contextVec.vec.z -= amount * context.size;
			pos = pos.add(0, 0, amount);
		}
		
		//Smaller
		if(contextVec.vec.x < 0)
		{
			int amount = (int) Math.ceil(Math.abs(contextVec.vec.x / (double) context.size));
			contextVec.vec.x += amount * context.size;
			pos = pos.add(-amount, 0, 0);
		}
		if(contextVec.vec.y < 0)
		{
			int amount = (int) Math.ceil(Math.abs(contextVec.vec.y / (double) context.size));
			contextVec.vec.y += amount * context.size;
			pos = pos.add(0, -amount, 0);
		}
		if(contextVec.vec.z < 0)
		{
			int amount = (int) Math.ceil(Math.abs(contextVec.vec.z / (double) context.size));
			contextVec.vec.z += amount * context.size;
			pos = pos.add(0, 0, -amount);
		}
	}

	public LittleTilePos copy()
	{
		return new LittleTilePos(pos, contextVec.copy());
	}
	
	public double getPosX()
	{
		return pos.getX() + contextVec.getPosX();
	}
	
	public double getPosY()
	{
		return pos.getY() + contextVec.getPosY();
	}
	
	public double getPosZ()
	{
		return pos.getZ() + contextVec.getPosZ();
	}
	
	public Vec3d getVec()
	{
		return new Vec3d(getPosX(), getPosY(), getPosZ());
	}
	
	public void writeToNBT(String name, NBTTagCompound nbt)
	{
		nbt.setIntArray(name, new int[]{pos.getX(), pos.getY(), pos.getZ(), contextVec.context.size, contextVec.vec.x, contextVec.vec.y, contextVec.vec.z});
	}
	
	public LittleGridContext getContext()
	{
		return contextVec.context;
	}
	
	@Override
	public int hashCode() {
		return pos.hashCode();
	}
	
	@Override
	public boolean equals(Object paramObject) {
		if(paramObject instanceof LittleTilePos)
		{
			LittleTilePos pos = (LittleTilePos) paramObject;
			return pos.pos.equals(this.pos) && pos.contextVec.equals(this.contextVec);			
		}
		return super.equals(paramObject);
	}
	
	@Override
	public String toString() {
		return "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ",grid:" + contextVec.context.size + "," + contextVec.vec.x + "," + contextVec.vec.y + "," + contextVec.vec.z + "]"; 
	}
}
