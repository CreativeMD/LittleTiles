package com.creativemd.littletiles.common.tile.parent;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IParentTileList extends Iterable<LittleTile> {
	
	public LittleTile first();
	
	public LittleTile last();
	
	public int size();
	
	public boolean isStructure();
	
	public default boolean isStructureChildSafe(LittleStructure structure) {
		try {
			return isStructureChild(structure);
		} catch (CorruptedConnectionException | NotYetConnectedException e) {
			return false;
		}
	}
	
	public boolean isStructureChild(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException;
	
	public boolean isMain();
	
	public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException;
	
	public int getAttribute();
	
	public boolean isClient();
	
	public TileEntityLittleTiles getTe();
	
	public default World getWorld() {
		return getTe().getWorld();
	}
	
	public default BlockPos getPos() {
		return getTe().getPos();
	}
	
	public default LittleGridContext getContext() {
		return getTe().getContext();
	}
	
}
