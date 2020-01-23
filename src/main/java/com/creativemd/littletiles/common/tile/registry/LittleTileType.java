package com.creativemd.littletiles.common.tile.registry;

import java.util.function.Predicate;

import com.creativemd.littletiles.common.tile.LittleTile;

import net.minecraft.nbt.NBTTagCompound;

public class LittleTileType {
	
	public final String id;
	public final Class<? extends LittleTile> clazz;
	public final Predicate<NBTTagCompound> predicate;
	public final boolean saveId;
	
	public LittleTileType(String id, Class<? extends LittleTile> clazz, Predicate<NBTTagCompound> predicate, boolean saveId) {
		this.id = id;
		this.clazz = clazz;
		this.predicate = predicate;
		this.saveId = saveId;
	}
	
	public LittleTile createTile() {
		try {
			return clazz.getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Invalid type " + id, e);
		}
	}
}
