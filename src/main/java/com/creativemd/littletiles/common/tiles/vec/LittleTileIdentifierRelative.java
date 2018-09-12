package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;
import java.util.Arrays;

import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleTileIdentifierRelative {
	
	protected BlockPos coord;
	public LittleGridContext context;
	public int[] identifier;
	
	public LittleTileIdentifierRelative(TileEntity te, BlockPos coord, LittleGridContext context, int[] identifier) {
		this(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), coord, context, identifier);
	}
	
	public LittleTileIdentifierRelative(BlockPos origin, BlockPos coord, LittleGridContext context, int[] identifier) {
		this(origin.getX(), origin.getY(), origin.getZ(), coord, context, identifier);
	}
	
	public LittleTileIdentifierRelative(int baseX, int baseY, int baseZ, BlockPos coord, LittleGridContext context, int[] identifier) {
		this(coord.getX() - baseX, coord.getY() - baseY, coord.getZ() - baseZ, context, identifier);
	}
	
	protected LittleTileIdentifierRelative(int relativeX, int relativeY, int relativeZ, LittleGridContext context, int[] identifier) {
		this.coord = new BlockPos(relativeX, relativeY, relativeZ);
		this.context = context;
		this.identifier = identifier;
	}
	
	public LittleTileIdentifierRelative(String id, NBTTagCompound nbt) {
		if (nbt.hasKey(id + "coord")) {
			int[] array = nbt.getIntArray(id + "coord");
			if (array.length == 3)
				coord = new BlockPos(array[0], array[1], array[2]);
			else
				throw new InvalidParameterException("No valid coord given " + nbt);
		} else if (nbt.hasKey(id + "coordX"))
			coord = new BlockPos(nbt.getInteger(id + "coordX"), nbt.getInteger(id + "coordY"), nbt.getInteger(id + "coordZ"));
		if (nbt.hasKey(id + "pos")) {
			LittleTileVec position = new LittleTileVec(id + "pos", nbt);
			identifier = new int[] { position.x, position.y, position.z };
		} else
			identifier = nbt.getIntArray("id");
		context = LittleGridContext.get(nbt);
	}
	
	public LittleTileIdentifierRelative(NBTTagCompound nbt) {
		this("", nbt);
	}
	
	public BlockPos getAbsolutePosition(TileEntity te) {
		return getAbsolutePosition(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
	}
	
	public BlockPos getAbsolutePosition(BlockPos origin) {
		return getAbsolutePosition(origin.getX(), origin.getY(), origin.getZ());
	}
	
	public BlockPos getAbsolutePosition(int x, int y, int z) {
		return new BlockPos(coord.getX() + x, coord.getY() + y, coord.getZ() + z);
	}
	
	public void writeToNBT(String id, NBTTagCompound nbt) {
		/*
		 * nbt.setInteger(id + "coordX", coord.getX()); nbt.setInteger(id + "coordY",
		 * coord.getY()); nbt.setInteger(id + "coordZ", coord.getZ());
		 */
		nbt.setIntArray(id + "coord", new int[] { coord.getX(), coord.getY(), coord.getZ() });
		// position.writeToNBT(id + "pos", nbt);
		nbt.setIntArray("id", identifier);
		context.set(nbt);
	}
	
	public void writeToNBT(NBTTagCompound nbt) {
		writeToNBT("", nbt);
	}
	
	@Override
	public String toString() {
		return "coord:[" + coord.getX() + "," + coord.getY() + "," + coord.getZ() + "]|position:" + Arrays.toString(identifier);
	}
	
	public LittleTileIdentifierRelative copy() {
		return new LittleTileIdentifierRelative(coord.getX(), coord.getY(), coord.getZ(), context, identifier.clone());
	}
	
}
