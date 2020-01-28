package com.creativemd.littletiles.common.tile.math.identifier;

import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleIdentifierStructureRelative extends LittleIdentifierRelative {
	
	public int attribute;
	
	public LittleIdentifierStructureRelative(TileEntity te, BlockPos coord, LittleGridContext context, int[] identifier, int attribute) {
		super(te, coord, context, identifier);
		this.attribute = attribute;
	}
	
	public LittleIdentifierStructureRelative(BlockPos origin, BlockPos coord, LittleGridContext context, int[] identifier, int attribute) {
		super(origin, coord, context, identifier);
		this.attribute = attribute;
	}
	
	public LittleIdentifierStructureRelative(int baseX, int baseY, int baseZ, BlockPos coord, LittleGridContext context, int[] identifier, int attribute) {
		super(baseX, baseY, baseZ, coord, context, identifier);
		this.attribute = attribute;
	}
	
	protected LittleIdentifierStructureRelative(int relativeX, int relativeY, int relativeZ, LittleGridContext context, int[] identifier, int attribute) {
		super(relativeX, relativeY, relativeZ, context, identifier);
		this.attribute = attribute;
	}
	
	public LittleIdentifierStructureRelative(NBTTagCompound nbt) {
		super(nbt);
		if (nbt.hasKey("attr"))
			this.attribute = LittleStructureAttribute.loadOld(nbt.getInteger("attr"));
		else
			this.attribute = nbt.getInteger("type");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("type", attribute);
		return nbt;
	}
	
	@Override
	public String toString() {
		return super.toString() + "|" + attribute;
	}
	
	@Override
	public LittleIdentifierStructureRelative copy() {
		return new LittleIdentifierStructureRelative(coord.getX(), coord.getY(), coord.getZ(), context, identifier.clone(), attribute);
	}
	
}
