package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;

public interface ILittleTile {
	
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack);
	
	public void rotateLittlePreview(ItemStack stack, ForgeDirection direction);
	
	public LittleStructure getLittleStructure(ItemStack stack);
	
	//public ArrayList<LittleTile> getLittleTile(ItemStack stack);
}
