package com.creativemd.littletiles.common.blocks;

import java.util.List;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISpecialBlockSelector {
	
	public void onDeselect(World world, ItemStack stack, EntityPlayer player);
	
	public boolean hasCustomBox(World world, ItemStack stack, EntityPlayer player, IBlockState state, RayTraceResult result, LittleTileVec absoluteHit);
	
	/**
	 * @return a list of absolute LittleTileBoxes (not relative to the pos)
	 */
	public List<LittleTileBox> getBox(World world, ItemStack stack, EntityPlayer player, RayTraceResult result, LittleTileVec absoluteHit);
	
	@SideOnly(Side.CLIENT)
	public boolean onClickBlock(World world, ItemStack stack, EntityPlayer player, RayTraceResult result, LittleTileVec absoluteHit);
	
}
