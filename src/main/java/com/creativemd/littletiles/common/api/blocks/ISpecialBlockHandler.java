package com.creativemd.littletiles.common.api.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public interface ISpecialBlockHandler {
	
	public default List<LittleTileBox> getCollisionBoxes(LittleTileBlock tile, List<LittleTileBox> defaultBoxes)
	{
		return defaultBoxes;
	}
	
	public default boolean onBlockActivated(LittleTileBlock tile, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return false;
	}
	
	public default void onTileExplodes(LittleTileBlock tile, Explosion explosion)
	{
		
	}
	
	public default void randomDisplayTick(LittleTileBlock tile, IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		
	}
	
	public default boolean isMaterial(LittleTileBlock tile, Material material)
	{
		return tile.getBlockState().getMaterial() == material;
	}
	
}
