package com.creativemd.littletiles.common.container;

import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerRecipe extends SubContainerConfigure {
	
	public SubContainerRecipe(EntityPlayer player, ItemStack stack) {
		super(player, stack);
	}
	
	public static void setLittlePreviewsContextSecretly(LittlePreviews previews, LittleGridContext context) {
		previews.context = context;
		if (previews.hasChildren())
			for (LittlePreviews child : previews.getChildren())
				setLittlePreviewsContextSecretly(child, context);
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
