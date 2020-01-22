package com.creativemd.littletiles.common.tiles.math.identifier;

import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleIdentifierStructureAbsolute extends LittleIdentifierAbsolute {
	
	public LittleStructureAttribute attribute;
	
	public LittleIdentifierStructureAbsolute(LittleTile tile, LittleStructureAttribute attribute) {
		super(tile);
		this.attribute = attribute;
	}
	
	public LittleIdentifierStructureAbsolute(TileEntity te, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute) {
		super(te, context, identifier);
		this.attribute = attribute;
	}
	
	public LittleIdentifierStructureAbsolute(BlockPos pos, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute) {
		super(pos, context, identifier);
		this.attribute = attribute;
	}
	
	public LittleIdentifierStructureAbsolute(NBTTagCompound nbt) {
		super(nbt);
		this.attribute = LittleStructureAttribute.get(nbt.getInteger("attr"));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("attr", attribute.ordinal());
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
