package com.creativemd.littletiles.common.blocks;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface ISpecialBlockSelector {
	
	public LittleTileBox getBox(TileEntityLittleTiles te, LittleTile tile, BlockPos pos, EntityPlayer player);
	
}
