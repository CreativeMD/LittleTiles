package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.utils.converting.StructureStringUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerImport extends SubContainer {

	public final InventoryBasic slot = new InventoryBasic("slot", false, 1);

	public SubContainerImport(EntityPlayer player) {
		super(player);
	}

	@Override
	public void createControls() {
		addSlotToContainer(new Slot(slot, 0, 10, 10));
		addPlayerSlotsToContainer(player);
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		ItemStack stack = slot.getStackInSlot(0);
		if((stack != null && stack.getItem() instanceof ItemRecipe) || (getPlayer().capabilities.isCreativeMode && stack == null))
		{
			
			slot.setInventorySlotContents(0, StructureStringUtils.importStructure(nbt.getString("text")));
		}
	}
	
	@Override
	public void onClosed()
	{
		super.onClosed();
		WorldUtils.dropItem(getPlayer(), slot.getStackInSlot(0));
	}

}
