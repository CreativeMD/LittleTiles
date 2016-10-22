package com.creativemd.littletiles.common.blocks;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public interface ISpecialBlockSelector {
	
	public LittleTileBox getBox(TileEntityLittleTiles te, LittleTile tile, BlockPos pos, EntityPlayer player, RayTraceResult result);
	
	public LittleTileBox getBox(World world, BlockPos pos, IBlockState state, EntityPlayer player, RayTraceResult result);
	
}
