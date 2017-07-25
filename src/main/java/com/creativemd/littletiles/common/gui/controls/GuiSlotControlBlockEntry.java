package com.creativemd.littletiles.common.gui.controls;

import com.creativemd.creativecore.gui.controls.container.SlotControl;
import com.creativemd.creativecore.gui.controls.container.client.GuiSlotControl;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;

import net.minecraft.item.ItemStack;

public class GuiSlotControlBlockEntry extends GuiSlotControl {

	public BlockEntry entry;
	
	public GuiSlotControlBlockEntry(int x, int y, SlotControl slot, BlockEntry entry) {
		super(x, y, slot);
		this.entry = entry;
	}
	
	@Override
	public ItemStack getStackToRender()
	{
		ItemStack stack = super.getStackToRender();
		if(stack.getItem() instanceof ItemBlockTiles)
		{
			stack = stack.copy();
			new LittleTileSize(LittleTile.gridSize/4*3, LittleTile.gridSize/4*3, LittleTile.gridSize/4*3).writeToNBT("size", stack.getTagCompound());
		}
		return stack;
	}

}
