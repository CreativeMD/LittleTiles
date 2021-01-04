package com.creativemd.littletiles.common.tile.registry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

import com.creativemd.littletiles.common.block.BlockLTFlowingLava.LittleFlowingLavaPreview;
import com.creativemd.littletiles.common.block.BlockLTFlowingWater.LittleFlowingWaterPreview;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

public class LittleTileRegistry {
	
	private static LinkedHashMap<Class<? extends LittleTile>, LittleTileType> tileIDs = new LinkedHashMap<>();
	private static LinkedHashMap<String, LittleTileType> invTileIDs = new LinkedHashMap<>();
	private static List<LittleTileType> typesOrdered = new ArrayList<>();
	private static LittleTileType defaultTileType;
	
	public static LittleTileType getTileType(String id) {
		return invTileIDs.getOrDefault(id, defaultTileType);
	}
	
	public static LittleTileType getTileType(Class<? extends LittleTile> clazz) {
		return tileIDs.getOrDefault(clazz, defaultTileType);
	}
	
	/** The id has to be unique and cannot be changed! **/
	public static LittleTileType registerTileType(Class<? extends LittleTile> clazz, String id, Predicate<NBTTagCompound> predicate, boolean saveId) {
		LittleTileType type = new LittleTileType(id, clazz, predicate, saveId);
		tileIDs.put(clazz, type);
		invTileIDs.put(id, type);
		typesOrdered.add(type);
		return type;
	}
	
	public static LittleTileType getTypeFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("tID"))
			return getTileType(nbt.getString("tID"));
		
		for (int i = typesOrdered.size() - 1; i >= 0; i--)
			if (typesOrdered.get(i).predicate.test(nbt))
				return typesOrdered.get(i);
		return defaultTileType;
	}
	
	public static LittleTile loadTile(NBTTagCompound nbt) {
		if (nbt.hasKey("tileID")) { // If it's the old tileentity
			if (nbt.hasKey("block")) {
				Block block = Block.getBlockFromName(nbt.getString("block"));
				int meta = nbt.getInteger("meta");
				LittleBox box = new LittleBox(new LittleVec("i", nbt), new LittleVec("a", nbt));
				box.add(new LittleVec(LittleGridContext.oldHalfGridSize, LittleGridContext.oldHalfGridSize, LittleGridContext.oldHalfGridSize));
				LittleTile tile = new LittleTile(block, meta);
				tile.setBox(box);
				return tile;
			}
		} else {
			LittleTileType type = getTypeFromNBT(nbt);
			
			LittleTile tile = type.createTile();
			
			if (tile != null) {
				try {
					tile.loadTile(nbt);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			return tile;
		}
		return null;
	}
	
	private static LinkedHashMap<String, LittlePreviewType> previewTypes = new LinkedHashMap<>();
	private static LinkedHashMap<Class<? extends LittlePreview>, LittlePreviewType> previewTypesInv = new LinkedHashMap<>();
	private static LittlePreviewType defaultPreviewType;
	
	public static Class<? extends LittlePreview> getDefaultPreviewClass() {
		return LittlePreview.class;
	}
	
	public static LittlePreviewType registerPreviewType(String id, Class<? extends LittlePreview> clazz) {
		LittlePreviewType type = new LittlePreviewType(id, clazz);
		previewTypes.put(id, type);
		previewTypesInv.put(clazz, type);
		return type;
	}
	
	public static Class<? extends LittlePreview> getPreviewClass(String id) {
		return previewTypes.getOrDefault(id, defaultPreviewType).clazz;
	}
	
	public static String getPreviewId(Class<? extends LittlePreview> clazz) {
		return previewTypesInv.getOrDefault(clazz, defaultPreviewType).id;
	}
	
	public static LittlePreview loadPreview(NBTTagCompound nbt) {
		if (nbt == null)
			return null;
		
		if (nbt.hasKey("type")) {
			LittlePreviewType type = previewTypes.get(nbt.getString("type"));
			if (type == null)
				type = defaultPreviewType;
			
			if (type != null) {
				LittlePreview preview = null;
				try {
					preview = type.clazz.getConstructor(NBTTagCompound.class).newInstance(nbt);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				return preview;
			} else
				throw new RuntimeException("Could find '" + nbt.getString("type") + "' preview type");
		}
		
		return new LittlePreview(nbt);
	}
	
	public static void initTiles() {
		defaultTileType = registerTileType(LittleTile.class, "BlockTileBlock", (x) -> true, false);
		registerTileType(LittleTileColored.class, "BlockTileColored", (x) -> x.hasKey("color"), false);
		
		defaultPreviewType = registerPreviewType("default", LittlePreview.class);
		
		// Outdated
		LittlePreview.registerPreviewType("water", LittleFlowingWaterPreview.class);
		LittlePreview.registerPreviewType("lava", LittleFlowingLavaPreview.class);
	}
}
