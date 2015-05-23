package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileSize;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

public interface ILittleTile {
	
	public LittleTilePreview getLittlePreview(ItemStack stack);
	
	public ArrayList<LittleTile> getLittleTile(ItemStack stack, World world, int x, int y, int z);
}
