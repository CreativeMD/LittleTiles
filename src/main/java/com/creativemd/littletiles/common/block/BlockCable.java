package com.creativemd.littletiles.common.block;

import com.creativemd.littletiles.LittleTiles;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;

public class BlockCable extends BlockRotatedPillar {
	
	public BlockCable() {
		super(Material.CIRCUITS);
		setCreativeTab(LittleTiles.littleTab);
	}
	
	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockRotatedPillar.AXIS).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BlockRotatedPillar.AXIS, Axis.values()[meta]);
	}
	
}
