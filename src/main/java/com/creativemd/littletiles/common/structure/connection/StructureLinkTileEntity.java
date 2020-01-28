package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureLinkTileEntity extends StructureLinkBaseRelative<TileEntityLittleTiles> {
	
	public StructureLinkTileEntity(StructureLinkBaseRelative link, TileEntityLittleTiles parent) {
		super(link.coord.getX(), link.coord.getY(), link.coord.getZ(), link.context, link.identifier.clone(), link.attribute, parent);
	}
	
	@Override
	public BlockPos getStructurePosition() {
		return getAbsolutePosition(parent);
	}
	
	@Override
	public StructureLinkTileEntity copy(TileEntityLittleTiles parent) {
		return new StructureLinkTileEntity(this, parent);
	}
	
	@Override
	protected void connect(World world, LittleTile mainTile) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void failedConnect(World world) {
		// TODO Auto-generated method stub
	}
	
}
