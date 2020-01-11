package com.creativemd.littletiles.common.structure.exception;

import net.minecraft.util.math.BlockPos;

public class MissingTileEntity extends StructureException {
	
	public BlockPos pos;
	
	public MissingTileEntity(BlockPos pos) {
		super("Missing block at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
	}
	
}
