package com.creativemd.littletiles.client.render;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.utils.LittleTilePreview;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ITilesRenderer { //is client-side effective only!
	
	public ArrayList<CubeObject> getRenderingCubes(ItemStack stack);
	
	public boolean hasBackground(ItemStack stack);
	
}
