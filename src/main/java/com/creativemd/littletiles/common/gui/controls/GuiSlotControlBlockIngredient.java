package com.creativemd.littletiles.common.gui.controls;

import java.util.ArrayList;

import com.creativemd.creativecore.common.gui.controls.container.SlotControl;
import com.creativemd.creativecore.common.gui.controls.container.client.GuiSlotControl;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredient;

import net.minecraft.item.ItemStack;

public class GuiSlotControlBlockIngredient extends GuiSlotControl {
	
	public GuiSlotControlBlockIngredient(int x, int y, SlotControl slot, BlockIngredient ingredient) {
		super(x, y, slot);
	}
	
	@Override
	public ItemStack getStackToRender() {
		ItemStack stack = super.getStackToRender();
		if (stack.getItem() instanceof ItemBlockTiles) {
			if (stack.getCount() >= LittleGridContext.get().maxTilesPerBlock) {
				LittleTilePreview preview = LittleTilePreview.loadPreviewFromNBT(stack.getTagCompound());
				stack = new ItemStack(preview.getPreviewBlock(), stack.getCount() / LittleGridContext.get().maxTilesPerBlock, preview.getPreviewBlockMeta());
			} else {
				stack = stack.copy();
				stack.setCount(1);
				new LittleTileSize(LittleGridContext.get().size, LittleGridContext.get().size, LittleGridContext.get().size).writeToNBT("size", stack.getTagCompound());
			}
		}
		return stack;
	}
	
	@Override
	public ArrayList<String> getTooltip() {
		ArrayList<String> tooltip = super.getTooltip();
		BlockIngredient ingredient = ((SlotControlBlockIngredient) slot).ingredient;
		if (ingredient != null) {
			int blocks = (int) ingredient.value;
			double pixel = (ingredient.value - blocks) * LittleGridContext.get().maxTilesPerBlock;
			String line = "volume: ";
			if (blocks > 0)
				line += blocks + " blocks ";
			if (pixel > 0)
				line += (Math.round(pixel * 100) / 100) + " pixel";
			tooltip.add(line);
		}
		return tooltip;
	}
	
}
