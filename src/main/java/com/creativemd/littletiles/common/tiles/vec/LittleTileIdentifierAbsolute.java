package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;

import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleTileIdentifierAbsolute {
	
	public BlockPos pos;
	public LittleGridContext context;
	public int[] identifier;
	
	public LittleTileIdentifierAbsolute(LittleTile tile)
	{
		this(tile.te, tile.getContext(), tile.getIdentifier());
	}
	
	public LittleTileIdentifierAbsolute(TileEntity te, LittleGridContext context, int[] identifier)
	{
		this(te.getPos(), context, identifier);
	}
	
	public LittleTileIdentifierAbsolute(BlockPos pos, LittleGridContext context, int[] identifier)
	{
		this.pos = pos;
		this.identifier = identifier;
		this.context = context;
	}
	
	public LittleTileIdentifierAbsolute(NBTTagCompound nbt)
	{
		int[] array = nbt.getIntArray("pos");
		if(array.length == 3)
			pos = new BlockPos(array[0], array[1], array[2]);
		else
			throw new InvalidParameterException("No valid coord given " + nbt);
		
		identifier = nbt.getIntArray("id");
		context = LittleGridContext.get(nbt);
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
		nbt.setIntArray("id", identifier);
		context.set(nbt);
	}
	
	public static int[] convertTo(int[] identifier, LittleGridContext from, LittleGridContext to)
	{
		if(from == to)
			return identifier;
		identifier = identifier.clone();
		if(from.size < to.size)
		{
			int scale = to.size / from.size;
			identifier[0] *= scale;
			identifier[1] *= scale;
			identifier[2] *= scale;
			return identifier;
		}else{
			int size = from.getMinGrid(identifier[0]);
			size = Math.max(size, from.getMinGrid(identifier[1]));
			size = Math.max(size, from.getMinGrid(identifier[2]));
			if(size <= to.size)
			{
				int scale = from.size / to.size;
				identifier[0] /= scale;
				identifier[1] /= scale;
				identifier[2] /= scale;
				return identifier;
			}
		}
		return null;
	}
}
