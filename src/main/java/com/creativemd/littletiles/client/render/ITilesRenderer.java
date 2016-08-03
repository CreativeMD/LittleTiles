package com.creativemd.littletiles.client.render;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.CubeObject;

import net.minecraft.item.ItemStack;

public interface ITilesRenderer { //is client-side effective only!
	
	public ArrayList<CubeObject> getRenderingCubes(ItemStack stack);
	
	public boolean hasBackground(ItemStack stack);
	
}
