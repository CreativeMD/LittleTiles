package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerStructure extends SubContainer{
	
	public ItemStack stack;
	public int index;
	
	public SubContainerStructure(EntityPlayer player, ItemStack stack) {
		super(player);
		this.stack = stack;
		this.index = player.inventory.currentItem;
	}

	@Override
	public void createControls() {
		
	}

	@Override
	public void onGuiPacket(int controlID, NBTTagCompound nbt, EntityPlayer player) {
		if(controlID == 0)
		{
			stack.stackTagCompound = nbt;
			player.inventory.mainInventory[index] = stack;
			//player.inventory.
			/*if(player.capabilities.isCreativeMode)
			{
				ItemStack multiTiles = new ItemStack(LittleTiles.multiTiles);
				multiTiles.stackTagCompound = nbt;
				WorldUtils.dropItem(player, multiTiles);
			}*/
			//closeLayer(null);
		}
	}
	
	

}
