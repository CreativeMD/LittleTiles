package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.util.math.BlockPos;

public interface IStructureConnector {
	
	public BlockPos getStructurePosition();
	
	public LittleStructure getStructure();
	
	public boolean isConnected();
	
	public void connect(LittleStructure structure);
	
	public int getIndex();
	
	public int getAttribute();
	
	public boolean isLink();
	
	public default boolean isLinkToAnotherWorld() {
		return false;
	}
	
	public void reset();
	
}
