package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.utils.LittleTilePreview;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public interface ILittleTile {
	
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack);
	
	public void saveLittlePreview(ItemStack stack, ArrayList<LittleTilePreview> previews);
	
	public default void rotateLittlePreview(ItemStack stack, EnumFacing facing)
	{
		ArrayList<LittleTilePreview> previews = getLittlePreview(stack);
		for (int i = 0; i < previews.size(); i++) {
			LittleTilePreview preview = previews.get(i);
			preview.rotatePreview(facing);
		}
		saveLittlePreview(stack, previews);
	}
	
	public default void flipLittlePreview(ItemStack stack, EnumFacing facing)
	{
		ArrayList<LittleTilePreview> previews = getLittlePreview(stack);
		for (int i = 0; i < previews.size(); i++) {
			LittleTilePreview preview = previews.get(i);
			preview.flipPreview(facing);
		}
		saveLittlePreview(stack, previews);
	}
	
	public LittleStructure getLittleStructure(ItemStack stack);
}
