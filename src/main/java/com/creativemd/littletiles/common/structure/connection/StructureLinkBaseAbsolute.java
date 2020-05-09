package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.IStructureConnector;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.identifier.LittleIdentifierStructureAbsolute;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public abstract class StructureLinkBaseAbsolute<T> extends LittleIdentifierStructureAbsolute implements IStructureConnector<T> {
	
	protected LittleStructure connectedStructure;
	protected final T parent;
	
	public StructureLinkBaseAbsolute(LittleTile tile, int attribute, T parent) {
		super(tile, attribute);
		this.parent = parent;
	}
	
	public StructureLinkBaseAbsolute(TileEntity te, LittleGridContext context, int[] identifier, int attribute, T parent) {
		super(te, context, identifier, attribute);
		this.parent = parent;
	}
	
	public StructureLinkBaseAbsolute(BlockPos pos, LittleGridContext context, int[] identifier, int attribute, T parent) {
		super(pos, context, identifier, attribute);
		this.parent = parent;
	}
	
	public StructureLinkBaseAbsolute(NBTTagCompound nbt, T parent) {
		super(nbt);
		this.parent = parent;
	}
	
	protected abstract void connect(World world, LittleTile mainTile);
	
	protected abstract void failedConnect(World world);
	
	@Override
	public BlockPos getStructurePosition() {
		return pos;
	}
	
	@Override
	public LittleStructure getStructureWithoutLoading() {
		return connectedStructure;
	}
	
	protected World getWorld(World world) {
		return world;
	}
	
	@Override
	public boolean isConnected(World world) {
		if (connectedStructure != null)
			return true;
		
		world = getWorld(world);
		
		if (world == null)
			return false;
		
		BlockPos absoluteCoord = getStructurePosition();
		Chunk chunk = world.getChunkFromBlockCoords(absoluteCoord);
		if (WorldUtils.checkIfChunkExists(chunk)) {
			TileEntity te = world.getTileEntity(absoluteCoord);
			if (te instanceof TileEntityLittleTiles) {
				LittleTile tile = ((TileEntityLittleTiles) te).getTile(context, identifier);
				if (tile != null && tile.isChildOfStructure() && !tile.connection.isLink()) {
					connect(world, tile);
				}
			}
			
			if (connectedStructure == null && !world.isRemote) {
				failedConnect(world);
			}
			
			return connectedStructure != null;
		}
		
		return false;
		
	}
	
	@Override
	public LittleStructure getStructure(World world) {
		if (isConnected(world))
			return connectedStructure;
		return null;
	}
	
	@Override
	public void setLoadedStructure(LittleStructure structure) {
		this.connectedStructure = structure;
		this.attribute = structure.getAttribute();
	}
	
	@Override
	public void reset() {
		this.connectedStructure = null;
	}
	
	@Override
	public boolean isLink() {
		return true;
	}
	
	@Override
	public boolean is(LittleTile mainTile) {
		return mainTile.is(context, identifier);
	}
	
	@Override
	public int getAttribute() {
		return attribute;
	}
	
}
