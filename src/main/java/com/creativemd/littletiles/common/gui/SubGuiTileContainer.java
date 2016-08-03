package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiItemListBox;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiTileContainer extends SubGui{
	
	public ItemStack stack;
	
	public SubGuiTileContainer(ItemStack stack)
	{
		super();
		this.stack = stack;
	}
	
	public static String getStringOfValue(float value)
	{
		String line = "";
		if(value >= 1)
			line += ((int)value) + " blocks";
		if(value % 1 != 0)
		{
			if(!line.equals(""))
				line += " ";
			line += ((int) ((value%1)*4096)) + " tiles";
		}
		return line;
	}

	@Override
	public void createControls() {
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		if(stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());
		ArrayList<BlockEntry> map = ItemTileContainer.loadMap(stack);
		for (BlockEntry entry : map) {
			if(!(entry.block instanceof BlockAir) && entry.block != null)
			{
				stacks.add(entry.getItemStack());
				lines.add(getStringOfValue(entry.value));
			}
		}
		controls.add(new GuiItemListBox("items", 5, 5, 140, 75, stacks, lines));
		controls.add(new GuiButton("drop", 145, 60, 30, 20){

			@Override
			public void onClicked(int x, int y, int button) {
				if(((GuiItemListBox) get("items")).getSelectedStack() != null)
				{
					NBTTagCompound nbt = new NBTTagCompound();
					ItemStack stack = ((GuiItemListBox) get("items")).getSelectedStack();
					stack.stackSize = 1;
					if(GuiScreen.isCtrlKeyDown())
						stack.stackSize = 64;
					stack.writeToNBT(nbt);
					sendPacketToServer(nbt);
				}
			}
			
		});
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		if(nbt.getBoolean("needUpdate"))
		{
			nbt.removeTag("needUpdate");
			stack.setTagCompound(nbt);
			controls.clear();
			onOpened();
		}
	}
	
}
