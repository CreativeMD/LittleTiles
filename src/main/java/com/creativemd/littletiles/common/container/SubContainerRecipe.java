package com.creativemd.littletiles.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerRecipe extends SubContainerConfigure {
	
	public SubContainerRecipe(EntityPlayer player, ItemStack stack) {
		super(player, stack);
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		if (nbt.getBoolean("clear_content")) {
			/*LittleTilePreview.removePreviewTiles(stack);
			stack.getTagCompound().removeTag("structure");*/
			stack.setTagCompound(null);
			sendNBTToGui(new NBTTagCompound());
		} else if (nbt.getBoolean("set_structure")) {
			stack.setTagCompound(nbt.getCompoundTag("stack"));
		}
	}
	
}
