package com.creativemd.littletiles.common.tile.place.fixed;

import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class SecondModeHandler {
	
	public LittleBox getBox(World world, BlockPos pos, LittleGridContext context, LittleBox suggested) {
		return suggested;
	}
	
}
