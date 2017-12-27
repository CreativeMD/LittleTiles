package com.creativemd.littletiles.common.utils.placing;

import java.util.HashMap;

public abstract class PlacementMode {
	
	private static HashMap<String, PlacementMode> modes = new HashMap<>();
	
	public static PlacementMode normal;
	
	public static PlacementMode getDefault()
	{
		return normal;
	}
	
	public static PlacementMode getModeOrDefault(String name)
	{
		PlacementMode mode = getMode(name);
		if(mode != null)
			return mode;
		return getDefault();
	}
	
	public static PlacementMode getMode(String name)
	{
		return modes.get(name);
	}
	
	public final String name;
	
	public final SelectionMode mode;
	
	public PlacementMode(String name, SelectionMode mode) {
		this.name = name;
		this.mode = mode;
		this.modes.put(name, this);
	}
	
	public static enum SelectionMode {
		LINES,
		PREVIEWS;
	}
}