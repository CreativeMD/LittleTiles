package com.creativemd.littletiles.common.structure.signal.logic;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.nbt.NBTTagCompound;

public abstract class SignalEvent {
	
	private static HashMap<String, Class<? extends SignalEvent>> eventTypes = new HashMap<>();
	private static HashMap<Class<? extends SignalEvent>, String> eventTypesInv = new HashMap<>();
	
	public static void registerEventType(String id, Class<? extends SignalEvent> type) {
		if (eventTypes.containsKey(id))
			throw new IllegalArgumentException("id " + id + " is already taken");
		try {
			type.getConstructor(NBTTagCompound.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Invalid event type missing nbt constructor " + id);
		}
		eventTypes.put(id, type);
		eventTypesInv.put(type, id);
	}
	
	public static SignalEvent loadFromNBT(NBTTagCompound nbt) {
		Class<? extends SignalEvent> type = get(nbt.getString("id"));
		if (type == null)
			throw new RuntimeException("No event type found for " + nbt.getString("id"));
		try {
			return type.getConstructor(NBTTagCompound.class).newInstance(nbt);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Invalid event type " + nbt.getString("id"));
		}
	}
	
	public static Class<? extends SignalEvent> get(String id) {
		return eventTypes.get(id);
	}
	
	public static String get(Class<? extends SignalEvent> type) {
		return eventTypesInv.get(type);
	}
	
	public SignalEvent(NBTTagCompound nbt) {
		
	}
	
	public abstract void update(LittleStructure structure, int id, boolean[] stateBefore, boolean[] state);
	
	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("id", get(this.getClass()));
		writeToNBTExtra(nbt);
		return nbt;
	}
	
	protected abstract void writeToNBTExtra(NBTTagCompound nbt);
	
}
