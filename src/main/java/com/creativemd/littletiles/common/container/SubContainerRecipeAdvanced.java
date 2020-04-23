package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.littletiles.common.item.ItemRecipeAdvanced;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.selection.mode.SelectionMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerRecipeAdvanced extends SubContainerRecipe {
	
	public SubContainerRecipeAdvanced(EntityPlayer player, ItemStack stack) {
		super(player, stack);
	}
	
	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		if (nbt.getBoolean("save_selection")) {
			SelectionMode mode = ItemRecipeAdvanced.getSelectionMode(stack);
			LittlePreviews previews = mode.getPreviews(player.world, stack, nbt.getBoolean("includeVanilla"), nbt.getBoolean("includeCB"), nbt.getBoolean("includeLT"), nbt.getBoolean("remember_structure"));
			
			if (nbt.hasKey("grid")) {
				LittleGridContext grid = LittleGridContext.get(nbt.getInteger("grid"));
				previews.convertTo(grid);
				LittleGridContext aimedGrid = LittleGridContext.get(nbt.getInteger("aimedGrid"));
				if (aimedGrid.size > grid.size)
					LittlePreviews.setLittlePreviewsContextSecretly(previews, aimedGrid);
				else
					LittlePreviews.advancedScale(previews, aimedGrid.size, grid.size);
				previews.combinePreviewBlocks();
			}
			
			previews.removeOffset();
			
			((ItemRecipeAdvanced) stack.getItem()).saveLittlePreview(stack, previews);
			mode.clearSelection(stack);
			
			sendNBTToGui(stack.getTagCompound());
			GuiHandler.openGui("recipeadvanced", new NBTTagCompound(), player);
		}
		
		super.onPacketReceive(nbt);
		
		if (nbt.getBoolean("clear_content"))
			GuiHandler.openGui("recipeadvanced", new NBTTagCompound(), player);
	}
	
}
