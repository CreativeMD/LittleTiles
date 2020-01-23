package com.creativemd.littletiles.common.utils.selection.selector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public abstract class TileSelector {
	
	private static HashMap<String, Class<? extends TileSelector>> selectorTypes = new HashMap<>();
	
	public static void registerType(String id, Class<? extends TileSelector> type) {
		selectorTypes.put(id, type);
	}
	
	public static String getTypeID(Class<? extends TileSelector> type) {
		for (Entry<String, Class<? extends TileSelector>> entry : selectorTypes.entrySet()) {
			if (type == entry.getValue())
				return entry.getKey();
		}
		return null;
	}
	
	public static Class<? extends TileSelector> getType(String id) {
		return selectorTypes.get(id);
	}
	
	public static TileSelector loadSelector(String id, NBTTagCompound nbt) {
		Class<? extends TileSelector> type = getType(id);
		if (type != null) {
			try {
				TileSelector selector = type.getConstructor().newInstance();
				selector.loadNBT(nbt);
				return selector;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Selector type " + nbt.getString("type") + " is missing an empty constructor!");
			}
		} else
			System.out.println("Selector " + nbt.getString("type") + " could not be found!");
		return null;
	}
	
	public static TileSelector loadSelector(NBTTagCompound nbt) {
		Class<? extends TileSelector> type = getType(nbt.getString("type"));
		if (type != null) {
			try {
				TileSelector selector = type.getConstructor().newInstance();
				selector.loadNBT(nbt);
				return selector;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Selector type " + nbt.getString("type") + " is missing an empty constructor!");
			}
		} else
			System.out.println("Selector " + nbt.getString("type") + " could not be found!");
		return null;
	}
	
	static {
		// Init Selectors
		registerType("any", AnySelector.class);
		registerType("and", AndSelector.class);
		registerType("or", OrSelector.class);
		registerType("not", NotSelector.class);
		registerType("block", TileSelectorBlock.class);
		registerType("state", StateSelector.class);
		registerType("color", ColorSelector.class);
		registerType("nostructure", NoStructureSelector.class);
	}
	
	public TileSelector() {
		
	}
	
	public NBTTagCompound writeNBT(NBTTagCompound nbt) {
		saveNBT(nbt);
		nbt.setString("type", getTypeID(this.getClass()));
		return nbt;
	}
	
	protected abstract void saveNBT(NBTTagCompound nbt);
	
	protected abstract void loadNBT(NBTTagCompound nbt);
	
	public abstract boolean is(LittleTile tile);
	
	public static LittleBoxes getAbsoluteBoxes(World world, BlockPos pos, BlockPos pos2, TileSelector selector) {
		LittleBoxes boxes = new LittleBoxes(pos, LittleGridContext.getMin());
		
		int minX = Math.min(pos.getX(), pos2.getX());
		int maxX = Math.max(pos.getX(), pos2.getX());
		int minY = Math.min(pos.getY(), pos2.getY());
		int maxY = Math.max(pos.getY(), pos2.getY());
		int minZ = Math.min(pos.getZ(), pos2.getZ());
		int maxZ = Math.max(pos.getZ(), pos2.getZ());
		
		MutableBlockPos position = new MutableBlockPos();
		
		for (int posX = minX; posX <= maxX; posX++) {
			for (int posY = minY; posY <= maxY; posY++) {
				for (int posZ = minZ; posZ <= maxZ; posZ++) {
					
					position.setPos(posX, posY, posZ);
					
					TileEntityLittleTiles te = BlockTile.loadTe(world, position);
					
					if (te == null)
						continue;
					
					for (LittleTile tile : te)
						if (selector.is(tile))
							boxes.addBox(tile);
				}
			}
		}
		
		return boxes;
	}
	
	public static List<LittleBox> getBoxes(World world, BlockPos pos, TileSelector selector) {
		List<LittleBox> boxes = new ArrayList<>();
		TileEntityLittleTiles te = BlockTile.loadTe(world, pos);
		for (LittleTile tile : te) {
			if (selector.is(tile))
				boxes.add(tile.box);
		}
		return boxes;
	}
	
}
