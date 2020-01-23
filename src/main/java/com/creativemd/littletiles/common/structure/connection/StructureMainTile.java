package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tile.LittleTile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureMainTile implements IStructureConnector<LittleTile> {
	
	public final LittleTile parent;
	public final LittleStructure structure;
	
	public StructureMainTile(LittleTile tile, LittleStructure structure) {
		this.parent = tile;
		this.structure = structure;
	}
	
	@Override
	public LittleStructure getStructureWithoutLoading() {
		return structure;
	}
	
	@Override
	public LittleStructure getStructure(World world) {
		return structure;
	}
	
	@Override
	public boolean isConnected(World world) {
		return true;
	}
	
	@Override
	public LittleStructureAttribute getAttribute() {
		return structure.getAttribute();
	}
	
	@Override
	public boolean isLink() {
		return false;
	}
	
	@Override
	public boolean is(LittleTile mainTile) {
		return mainTile == parent;
	}
	
	@Override
	public void reset() {
		
	}
	
	@Override
	public IStructureConnector copy(LittleTile parent) {
		return new StructureMainTile(parent, structure);
	}
	
	@Override
	public BlockPos getStructurePosition() {
		return parent.te.getPos();
	}
	
	@Override
	public void setLoadedStructure(LittleStructure structure) {
		new RuntimeException("Cannot set structure of main tile!").printStackTrace();
	}
	
}
