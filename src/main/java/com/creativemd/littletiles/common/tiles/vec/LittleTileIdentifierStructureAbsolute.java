package com.creativemd.littletiles.common.tiles.vec;

import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleTileIdentifierStructureAbsolute extends LittleTileIdentifierAbsolute {
	
	public LittleStructureAttribute attribute;
	
	public LittleTileIdentifierStructureAbsolute(LittleTile tile, LittleStructureAttribute attribute) {
		super(tile);
		this.attribute = attribute;
	}
	
	public LittleTileIdentifierStructureAbsolute(TileEntity te, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute) {
		super(te, context, identifier);
		this.attribute = attribute;
	}
	
	public LittleTileIdentifierStructureAbsolute(BlockPos pos, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute) {
		super(pos, context, identifier);
		this.attribute = attribute;
	}
	
	public LittleTileIdentifierStructureAbsolute(NBTTagCompound nbt) {
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
	
	public LittleTileIdentifierStructureAbsolute copy() {
		return new LittleTileIdentifierStructureAbsolute(pos, context, identifier.clone(), attribute);
	}
	
}
