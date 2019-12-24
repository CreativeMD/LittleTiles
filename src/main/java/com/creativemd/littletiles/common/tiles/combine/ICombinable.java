package com.creativemd.littletiles.common.tiles.combine;

import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

public interface ICombinable {
	
	public LittleTileBox getBox();
	
	public void setBox(LittleTileBox box);
	
	public boolean isChildOfStructure();
	
	public boolean canCombine(ICombinable combinable);
	
	public void combine(ICombinable combinable);
	
	public ICombinable copy();
	
	public boolean fillInSpace(LittleTileBox otherBox, boolean[][][] filled);
	
}
