package com.creativemd.littletiles.common.tiles.vec;

import com.creativemd.littletiles.common.structure.attributes.LittleStructureAttribute;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleTileIdentifierStructure extends LittleTileIdentifierRelative {

	public LittleStructureAttribute attribute;

	public LittleTileIdentifierStructure(TileEntity te, BlockPos coord, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute) {
		super(te, coord, context, identifier);
		this.attribute = attribute;
	}

	public LittleTileIdentifierStructure(BlockPos origin, BlockPos coord, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute) {
		super(origin, coord, context, identifier);
		this.attribute = attribute;
	}

	public LittleTileIdentifierStructure(int baseX, int baseY, int baseZ, BlockPos coord, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute) {
		super(baseX, baseY, baseZ, coord, context, identifier);
		this.attribute = attribute;
	}

	public LittleTileIdentifierStructure(String id, NBTTagCompound nbt) {
		super(id, nbt);
		this.attribute = LittleStructureAttribute.get(nbt.getInteger("attr"));
	}

	protected LittleTileIdentifierStructure(int relativeX, int relativeY, int relativeZ, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute) {
		super(relativeX, relativeY, relativeZ, context, identifier);
		this.attribute = attribute;
	}

	public LittleTileIdentifierStructure(NBTTagCompound nbt) {
		this("", nbt);
	}

	public void writeToNBT(String id, NBTTagCompound nbt) {
		super.writeToNBT(id, nbt);
		nbt.setInteger("attr", attribute.ordinal());
	}

	@Override
	public String toString() {
		return super.toString() + "|" + attribute;
	}

	public LittleTileIdentifierStructure copy() {
		return new LittleTileIdentifierStructure(coord.getX(), coord.getY(), coord.getZ(), context, identifier.clone(), attribute);
	}

}
