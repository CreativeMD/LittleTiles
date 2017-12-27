package com.creativemd.littletiles.common.utils.selection;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.nbt.NBTTagCompound;

public abstract class TileSelector {
	
	private static HashMap<String, Class<? extends TileSelector>> selectorTypes = new HashMap<>();
	
	public static void registerType(String id, Class<? extends TileSelector> type)
	{
		selectorTypes.put(id, type);
	}
	
	public static String getTypeID(Class<? extends TileSelector> type)
	{
		for (Entry<String, Class<? extends TileSelector>> entry : selectorTypes.entrySet()) {
			if(type == entry.getValue())
				return entry.getKey();
		}
		return null;
	}
	
	public static Class<? extends TileSelector> getType(String id)
	{
		return selectorTypes.get(id);
	}
	
	public static TileSelector loadSelector(NBTTagCompound nbt)
	{
		Class<? extends TileSelector> type = getType(nbt.getString("type"));
		if(type != null)
		{
			try {
				TileSelector selector = type.getConstructor().newInstance();
				selector.loadNBT(nbt);
				return selector;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Selector type " + nbt.getString("type") + " is missing an empty constructor!");
			}
		}else
			System.out.println("Selector " + nbt.getString("type") + " could not be found!");
		return null;		
	}
	
	public TileSelector() {
		
	}
	
	public NBTTagCompound writeNBT(NBTTagCompound nbt)
	{
		writeNBT(nbt);
		nbt.setString("type", getTypeID(this.getClass()));
		return nbt;
	}
	
	protected abstract void saveNBT(NBTTagCompound nbt);
	protected abstract void loadNBT(NBTTagCompound nbt);
	
	public abstract boolean is(LittleTile tile);
	
}
