package com.creativemd.littletiles.common.tile.math.location;

import java.util.Arrays;

import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LocalStructureLocation {
	
	public final BlockPos pos;
	public final int index;
	
	public LocalStructureLocation(BlockPos pos, int index) {
		this.pos = pos;
		this.index = index;
	}
	
	public LocalStructureLocation(LittleStructure structure) {
		this(structure.getPos(), structure.getIndex());
	}
	
	public LocalStructureLocation(NBTTagCompound nbt) {
		int[] posArray = nbt.getIntArray("pos");
		if (posArray.length != 3)
			throw new IllegalArgumentException("Invalid pos array length " + Arrays.toString(posArray));
		
		pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
		index = nbt.getInteger("index");
	}
	
	public NBTTagCompound write() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
		nbt.setInteger("index", index);
		return nbt;
	}
	
	public LittleStructure find(World world) throws LittleActionException {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityLittleTiles) {
			IStructureTileList structure = ((TileEntityLittleTiles) te).getStructure(index);
			if (structure != null)
				return structure.getStructure();
			throw new LittleActionException.StructureNotFoundException();
		} else
			throw new LittleActionException.TileEntityNotFoundException();
	}
}
