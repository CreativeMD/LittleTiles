package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;
import java.util.Arrays;

import com.creativemd.littletiles.common.structure.attributes.LittleStructureAttribute;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleTileStructureCoord extends LittleTileRelativeCoord {
	
	public LittleStructureAttribute attribute;
	
	public LittleTileStructureCoord(TileEntity te, BlockPos coord, int[] identifier, LittleStructureAttribute attribute)
	{
		super(te, coord, identifier);
		this.attribute = attribute;
	}
	
	public LittleTileStructureCoord(BlockPos origin, BlockPos coord, int[] identifier, LittleStructureAttribute attribute)
	{
		super(origin, coord, identifier);
		this.attribute = attribute;
	}
	
	public LittleTileStructureCoord(int baseX, int baseY, int baseZ, BlockPos coord, int[] identifier, LittleStructureAttribute attribute)
	{
		super(baseX, baseY, baseZ, coord, identifier);
		this.attribute = attribute;
	}
	
	public LittleTileStructureCoord(String id, NBTTagCompound nbt)
	{
		super(id, nbt);
		this.attribute = LittleStructureAttribute.get(nbt.getInteger("attr"));
	}
	
	protected LittleTileStructureCoord(int relativeX, int relativeY, int relativeZ, int[] identifier, LittleStructureAttribute attribute)
	{
		super(relativeX, relativeY, relativeZ, identifier);
		this.attribute = attribute;
	}
	
	public LittleTileStructureCoord(NBTTagCompound nbt)
	{
		this("", nbt);
	}
	
	public void writeToNBT(String id, NBTTagCompound nbt)
	{
		super.writeToNBT(id, nbt);
		nbt.setInteger("attr", attribute.ordinal());
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "|" + attribute;
	}
	
	public LittleTileStructureCoord copy()
	{
		return new LittleTileStructureCoord(coord.getX(), coord.getY(), coord.getZ(), identifier.clone(), attribute);
	}

}
