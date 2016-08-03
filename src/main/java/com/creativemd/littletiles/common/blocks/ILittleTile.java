package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.utils.LittleTilePreview;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface ILittleTile {
	
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack);
	
	public void rotateLittlePreview(ItemStack stack, EnumFacing facing);
	
	public void flipLittlePreview(ItemStack stack, EnumFacing facing);
	
	public LittleStructure getLittleStructure(ItemStack stack);
}
