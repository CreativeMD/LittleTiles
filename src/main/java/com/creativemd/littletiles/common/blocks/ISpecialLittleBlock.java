package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;

import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;

public interface ISpecialLittleBlock {
	
	public ArrayList<LittleTileBox> getCollisionBoxes(ArrayList<LittleTileBox> defaultBoxes, LittleTileBlock tile);
	
}
