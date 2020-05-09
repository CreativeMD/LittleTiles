package com.creativemd.littletiles.common.tileentity;

import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.IStructureConnector;
import com.creativemd.littletiles.common.tile.LittleTile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class SubTileList implements IStructureConnector {
	
	private final TileList parent;
	private final CopyOnWriteArrayList<LittleTile> tiles = new CopyOnWriteArrayList<LittleTile>();
	
	private LittleStructure structure;
	private int structureIndex;
	private int attribute;
	private BlockPos relativePos;
	
	public SubTileList(TileList parent) {
		this.parent = parent;
	}
	
	public boolean isStructure() {
		return structureIndex != -1;
	}
	
	public TileEntityLittleTiles getTe() {
		return parent.te;
	}
	
	@Override
	public int getAttribute() {
		return attribute;
	}
	
	@Override
	public BlockPos getStructurePosition() {
		return relativePos.add(getTe().getPos());
	}
	
	@Override
	public LittleStructure getStructure() {
		if (structure != null)
			return structure;
		
		if (relativePos == null)
			throw new RuntimeException("Mainblock structure missing!");
		
		World world = getTe().getWorld();
		
		BlockPos absoluteCoord = getStructurePosition();
		Chunk chunk = world.getChunkFromBlockCoords(absoluteCoord);
		if (WorldUtils.checkIfChunkExists(chunk)) {
			TileEntity te = world.getTileEntity(absoluteCoord);
			if (te instanceof TileEntityLittleTiles) {
				LittleTile tile = ((TileEntityLittleTiles) te).getTile(context, identifier);
				if (tile != null && tile.isChildOfStructure() && !tile.connection.isLink())
					connect(world, tile);
			}
			
			if (connectedStructure == null && !world.isRemote)
				failedConnect(world);
			
			return connectedStructure != null;
		}
		
		return false;
	}
	
	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void connect(LittleStructure structure) {
		if (relativePos == null)
			throw new RuntimeException("Cannot set structure of main block");
		
	}
	
	@Override
	public boolean isLink() {
		return relativePos != null;
	}
	
	@Override
	public void reset() {
		if (relativePos == null)
			throw new RuntimeException("Cannot reset structure of main block");
		
		structure = null;
	}
	
}
