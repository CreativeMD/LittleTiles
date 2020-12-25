package com.creativemd.littletiles.common.tile.parent;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ParentTileList extends CopyOnWriteArrayList<LittleTile> implements IParentTileList {
	
	private int collisionChecks = 0;
	
	protected boolean checkCollision() {
		return collisionChecks > 0;
	}
	
	@Override
	public boolean add(LittleTile e) {
		super.add(e);
		added(e);
		return true;
	}
	
	@Override
	public void add(int index, LittleTile element) {
		super.add(index, element);
		added(element);
	}
	
	@Override
	public boolean addAll(Collection<? extends LittleTile> c) {
		if (super.addAll(c)) {
			for (LittleTile tile : c)
				added(tile);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends LittleTile> c) {
		if (super.addAll(index, c)) {
			for (LittleTile tile : c)
				added(tile);
			return true;
		}
		return false;
	}
	
	private void added(LittleTile tile) {
		if (tile.shouldCheckForCollision())
			collisionChecks++;
	}
	
	@Override
	public LittleTile remove(int index) {
		LittleTile tile = super.remove(index);
		if (tile != null)
			removed(tile);
		return tile;
	}
	
	private void removed(LittleTile tile) {
		if (tile.shouldCheckForCollision())
			collisionChecks--;
	}
	
	@Override
	public boolean remove(Object o) {
		if (super.remove(o)) {
			removed((LittleTile) o);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeIf(Predicate<? super LittleTile> filter) {
		if (super.removeIf(filter)) {
			collisionChecks = 0;
			for (LittleTile tile : this)
				added(tile);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object object : c)
			if (remove(object))
				changed = true;
		return changed;
	}
	
	@Override
	public void clear() {
		super.clear();
		collisionChecks = 0;
	}
	
	@Override
	public LittleTile set(int index, LittleTile element) {
		throw new UnsupportedOperationException();
	}
	
	public void read(NBTTagCompound nbt) {
		this.clear();
		this.addAll(LittleNBTCompressionTools.readTiles(nbt.getTagList("tiles", 10)));
		readExtra(nbt);
	}
	
	protected abstract void readExtra(NBTTagCompound nbt);
	
	public NBTTagCompound write() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("tiles", LittleNBTCompressionTools.writeTiles(this));
		writeExtra(nbt);
		return nbt;
	}
	
	protected abstract void writeExtra(NBTTagCompound nbt);
	
	@Override
	public LittleTile first() {
		return isEmpty() ? null : super.get(0);
	}
	
	@Override
	public LittleTile last() {
		return isEmpty() ? null : super.get(size() - 1);
	}
	
	@Override
	public abstract TileEntityLittleTiles getTe();
	
	@Override
	public abstract boolean isStructure();
	
	@Override
	public boolean isStructureChildSafe(LittleStructure structure) {
		try {
			return isStructureChild(structure);
		} catch (CorruptedConnectionException | NotYetConnectedException e) {
			return false;
		}
	}
	
	@Override
	public abstract boolean isStructureChild(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException;
	
	@Override
	public abstract boolean isMain();
	
	@Override
	public abstract LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException;
	
	@Override
	public abstract int getAttribute();
	
	@Override
	public abstract boolean isClient();
	
	@Override
	public World getWorld() {
		return getTe().getWorld();
	}
	
	@Override
	public BlockPos getPos() {
		return getTe().getPos();
	}
	
	@Override
	public LittleGridContext getContext() {
		return getTe().getContext();
	}
	
	public void unload() {}
}
