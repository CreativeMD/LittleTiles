package com.creativemd.littletiles.common.tile.combine;

import com.creativemd.littletiles.common.tile.math.box.LittleBox;

public interface ICombinable {
	
	public LittleBox getBox();
	
	public void setBox(LittleBox box);
	
	public boolean isChildOfStructure();
	
	public boolean canCombine(ICombinable combinable);
	
	public void combine(ICombinable combinable);
	
	public ICombinable copy();
	
	public boolean fillInSpace(LittleBox otherBox, boolean[][][] filled);
	
}
