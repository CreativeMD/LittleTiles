package com.creativemd.littletiles.client.render;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ITilesRenderer { //It should be Client-side only, but the server can implement it!
	
	public boolean hasBackground(ItemStack stack);
	
}
