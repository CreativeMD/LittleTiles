package com.creativemd.littletiles.common.gui.controls;

import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.controls.container.SlotControl;
import com.creativemd.creativecore.gui.controls.container.client.GuiSlotControl;
import com.creativemd.littletiles.common.container.SubContainerHammer;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.creativemd.littletiles.common.tiles.PlacementHelper;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SlotControlBlockEntry extends SlotControl {

	public SlotControlBlockEntry(Slot slot, BlockEntry entry) {
		super(slot);
		this.entry = entry;
	}
	
	public BlockEntry entry;
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiControl createGuiControl() {
		GuiControl control = new GuiSlotControlBlockEntry(slot.xPos, slot.yPos, this, entry);
		return control;
	}
	
	@Override
	public int getStackLimit(Slot slot, ItemStack stack)
	{
		return 4098;
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return 4098;
	}
}
