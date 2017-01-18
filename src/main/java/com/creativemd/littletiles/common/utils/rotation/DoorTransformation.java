package com.creativemd.littletiles.common.utils.rotation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.creativemd.littletiles.common.entity.EntityAnimation;

import net.minecraft.nbt.NBTTagCompound;

public abstract class DoorTransformation {
	
	private static HashMap<String, Class<? extends DoorTransformation>> transformationTypes = new HashMap<>();
	
	public static void registerTransformationType(String id, Class<? extends DoorTransformation> classType)
	{
		if(transformationTypes.containsKey(id))
			throw new IllegalArgumentException("id '" + id + "' is already taken");
		transformationTypes.put(id, classType);
	}
	
	public static String getIDFromClass(Class<? extends DoorTransformation> classType)
	{
		for (Iterator<Entry<String, Class<? extends DoorTransformation>>> iterator = transformationTypes.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Class<? extends DoorTransformation>> type = iterator.next();
			if(type.getValue() == classType)
				return type.getKey();
		}
		return "";
	}
	
	static
	{
		registerTransformationType("ordinary", OrdinaryDoorTransformation.class);
	}
	
	public static DoorTransformation loadFromNBT(NBTTagCompound nbt)
	{
		
		Class<? extends DoorTransformation> type = transformationTypes.get(nbt.getString("id"));
		if(type != null)
		{
			try {
				DoorTransformation transformation = type.getConstructor().newInstance();
				transformation.readFromNBT(nbt);
				return transformation;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException(type.getClass() + " does not contain an empty constructor.");
			}
		}else
			throw new IllegalArgumentException("id '" + nbt.getString("id") + "' could not be loaded.");
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		writeToNBTExtra(nbt);
		nbt.setString("id", getIDFromClass(this.getClass()));
		return nbt;
	}
	
	protected abstract void writeToNBTExtra(NBTTagCompound nbt);
	
	protected abstract void readFromNBT(NBTTagCompound nbt);
	
	public abstract void performTransformation(EntityAnimation animation, double progress);
	
}
