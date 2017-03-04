package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
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
		super(250, 250);
		this.stack = stack;
	}
	
	public static String getStringOfValue(double value)
	{
		String line = "";
		if(value >= 1)
			line += ((int)value) + " blocks";
		if(value % 1 != 0)
		{
			if(!line.equals(""))
				line += " ";
			int amount = (int) ((value%1)*4096);
			line += amount + " tile";
			if(amount != 1)
				line += "s";
		}
		return line;
	}
	
	@Override
	public void addContainerControls()
	{
		GuiScrollBox box = (GuiScrollBox) get("items");
		for (int i = 0; i < container.controls.size(); i++) {
			container.controls.get(i).onOpened();
			if(container.controls.get(i).name.startsWith("item"))
				box.addControl(container.controls.get(i).getGuiControl());
			else
				controls.add(container.controls.get(i).getGuiControl());
		}
	}

	@Override
	public void createControls() {
		
		GuiScrollBox box = new GuiScrollBox("items", 0, 0, 245, 150);
		
		controls.add(box);
		
		//ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		ArrayList<BlockEntry> map = ItemTileContainer.loadMap(stack);
		int i = 0;
		int cols = 2;
		for (BlockEntry entry : map) {
			if(!(entry.block instanceof BlockAir) && entry.block != null)
			{
				//stacks.add(entry.getItemStack());
				box.addControl(new GuiLabel(getStringOfValue(entry.value % 1), 28 + (i % cols) * 110, 11+(i/cols)*24));
				i++;
			}
		}
		
		
		/*ArrayList<String> lines = new ArrayList<String>();
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		ArrayList<BlockEntry> map = ItemTileContainer.loadMap(stack);
		for (BlockEntry entry : map) {
			if(!(entry.block instanceof BlockAir) && entry.block != null)
			{
				stacks.add(entry.getItemStack());
				lines.add(getStringOfValue(entry.value));
			}
		}
		controls.add(new GuiItemListBox("items", 5, 5, 130, 65, stacks, lines));
		controls.add(new GuiButton("drop", 145, 60){

			@Override
			public void onClicked(int x, int y, int button) {
				if(((GuiItemListBox) get("items")).getSelectedStack() != null)
				{
					NBTTagCompound nbt = new NBTTagCompound();
					ItemStack stack = ((GuiItemListBox) get("items")).getSelectedStack();
					stack.setCount(1);
					if(GuiScreen.isCtrlKeyDown())
						stack.setCount(64);
					stack.writeToNBT(nbt);
					sendPacketToServer(nbt);
				}
			}
			
		});*/
	}
	
	@Override
	public void receiveContainerPacket(NBTTagCompound nbt)
	{
		if(nbt.getBoolean("reload"))
		{
			nbt.removeTag("reload");
			stack.setTagCompound(nbt);
			controls.clear();
			createControls();
			refreshControls();
			container.controls.clear();
			((SubContainerTileContainer) container).stack = stack;
			container.createControls();
			container.refreshControls();
			addContainerControls();
			refreshControls();
		}
	}
	
}
