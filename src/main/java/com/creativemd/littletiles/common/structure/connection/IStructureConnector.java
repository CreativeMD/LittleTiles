package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IStructureConnector<T> {
	
	public BlockPos getStructurePosition();
	
	public LittleStructure getStructure(World world);
	
	public LittleStructure getStructureWithoutLoading();
	
	public boolean isConnected(World world);
	
	public void setLoadedStructure(LittleStructure structure);
	
	public int getAttribute();
	
	public boolean isLink();
	
	public boolean is(LittleTile mainTile);
	
	public default boolean isLinkToAnotherWorld() {
		return false;
	}
	
	public void reset();
	
	public IStructureConnector copy(T parent);
	
}
