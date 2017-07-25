package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;

import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

public interface ISpecialLittleBlock {
	
	public ArrayList<LittleTileBox> getCollisionBoxes(ArrayList<LittleTileBox> defaultBoxes, LittleTileBlock tile);
	
}
