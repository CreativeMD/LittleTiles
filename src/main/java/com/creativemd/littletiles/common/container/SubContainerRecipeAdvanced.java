package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.littletiles.common.items.ItemRecipeAdvanced;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviewsStructure;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.selection.mode.SelectionMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerRecipeAdvanced extends SubContainerConfigure {
	
	public SubContainerRecipeAdvanced(EntityPlayer player, ItemStack stack) {
		super(player, stack);
	}
	
	public static void setLittlePreviewsContextSecretly(LittlePreviews previews, LittleGridContext context) {
		previews.context = context;
		if (previews.hasChildren())
			for (LittlePreviewsStructure child : previews.getChildren())
				setLittlePreviewsContextSecretly(child, context);
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
					setLittlePreviewsContextSecretly(previews, aimedGrid);
				else
					LittlePreviews.advancedScale(previews, aimedGrid.size, grid.size);
				previews.combinePreviewBlocks();
			}
			
			((ItemRecipeAdvanced) stack.getItem()).saveLittlePreview(stack, previews);
			mode.clearSelection(stack);
			
			sendNBTToGui(stack.getTagCompound());
			GuiHandler.openGui("recipeadvanced", new NBTTagCompound(), player);
		} else if (nbt.getBoolean("clear_content")) {
			LittleTilePreview.removePreviewTiles(stack);
			stack.getTagCompound().removeTag("structure");
			sendNBTToGui(stack.getTagCompound());
			GuiHandler.openGui("recipeadvanced", new NBTTagCompound(), player);
		} else if (nbt.getBoolean("set_structure")) {
			stack.setTagCompound(nbt.getCompoundTag("stack"));
		}
	}
	
}
