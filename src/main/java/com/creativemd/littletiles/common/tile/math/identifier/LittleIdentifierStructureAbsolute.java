package com.creativemd.littletiles.common.tile.math.identifier;

import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleIdentifierStructureAbsolute extends LittleIdentifierAbsolute {
	
	public int attribute;
	
	public LittleIdentifierStructureAbsolute(LittleTile tile, int attribute) {
		super(tile);
		this.attribute = attribute;
	}
	
	public LittleIdentifierStructureAbsolute(TileEntity te, LittleGridContext context, int[] identifier, int attribute) {
		super(te, context, identifier);
		this.attribute = attribute;
	}
	
	public LittleIdentifierStructureAbsolute(BlockPos pos, LittleGridContext context, int[] identifier, int attribute) {
		super(pos, context, identifier);
		this.attribute = attribute;
	}
	
	public LittleIdentifierStructureAbsolute(NBTTagCompound nbt) {
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
	
	public LittleIdentifierStructureAbsolute copy() {
		return new LittleIdentifierStructureAbsolute(pos, context, identifier.clone(), attribute);
	}
	
}
