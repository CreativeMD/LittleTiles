package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.gui.controls.GuiButton;
import com.creativemd.creativecore.common.gui.controls.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.GuiColorPlate;
import com.creativemd.creativecore.common.gui.controls.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.GuiInvSelector;
import com.creativemd.creativecore.common.gui.controls.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.controls.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.ControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.ControlClickEvent;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.oredict.OreDictionary;

public class SubGuiChisel extends SubGui {
	
	public ItemStack stack;
	
	public SubGuiChisel(ItemStack stack) {
		super(200, 200);
		this.stack = stack;
	}
	
	@Override
	public void createControls() {
		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();
		
		controls.add(new GuiCheckBox("any", "any", 5, 5, true));
		controls.add(new GuiInvSelector("filter", 40, 5, 140, container.player, true));
		controls.add(new GuiTextfield("search", "", 40, 27, 140, 20));
		controls.add(new GuiCheckBox("meta", "Metadata", 40, 50, true));
		
		controls.add(new GuiCheckBox("replace", "Replace with", 5, 73, false));
		
		controls.add(new GuiInvSelector("replacement", 40, 87, 140, container.player, true));
		controls.add(new GuiTextfield("search2", "", 40, 109, 140, 20));
		controls.add(new GuiCheckBox("metaR", "Force metadata", 40, 130, true));
		
		
		Vec3 color = Vec3.createVectorHelper(255, 255, 255);
		controls.add(new GuiCheckBox("colorize", "Colorize", 5, 150, false));
		
		controls.add(new GuiSteppedSlider("colorX", 5, 165, 100, 10, 0, 255, (int)color.xCoord));
		controls.add(new GuiSteppedSlider("colorY", 5, 175, 100, 10, 0, 255, (int)color.yCoord));
		controls.add(new GuiSteppedSlider("colorZ", 5, 185, 100, 10, 0, 255, (int)color.zCoord));
		controls.add(new GuiColorPlate("plate", 120, 170, 20, 20, color));
		
		controls.add(new GuiButton("run", "Do it!", 150, 170, 40));
	}
	
	@CustomEventSubscribe
	public void onClicked(ControlClickEvent event)
	{
		if(event.source.is("run"))
		{
			boolean any = ((GuiCheckBox)getControl("any")).value;
			boolean replace = ((GuiCheckBox)getControl("replace")).value;
			boolean colorize = ((GuiCheckBox)getControl("colorize")).value;
			boolean meta = ((GuiCheckBox)getControl("meta")).value;
			boolean metaR = ((GuiCheckBox)getControl("metaR")).value;
			
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("x1", stack.stackTagCompound.getInteger("x1"));
			nbt.setInteger("y1", stack.stackTagCompound.getInteger("y1"));
			nbt.setInteger("z1", stack.stackTagCompound.getInteger("z1"));
			nbt.setInteger("x2", stack.stackTagCompound.getInteger("x2"));
			nbt.setInteger("y2", stack.stackTagCompound.getInteger("y2"));
			nbt.setInteger("z2", stack.stackTagCompound.getInteger("z2"));
			
			GuiInvSelector filter = (GuiInvSelector) getControl("filter");
			GuiInvSelector replacement = (GuiInvSelector) getControl("replacement");
			ItemStack stackFilter = filter.getStack();
			if(!any && stackFilter != null)
			{
				nbt.setString("filterBlock", Block.blockRegistry.getNameForObject(Block.getBlockFromItem(stackFilter.getItem())));
				if(meta)
					nbt.setInteger("filterMeta", stackFilter.getItemDamage());
			}
			
			
			if(replace)
			{
				ItemStack stackReplace = replacement.getStack();
				if(stackReplace != null)
				{
					Block replacementBlock = Block.getBlockFromItem(stackReplace.getItem());
					if(!SubContainerHammer.isBlockValid(replacementBlock))
					{
						openButtonDialogDialog("Invalid replacement block!", "ok");
						return ;
					}
					nbt.setString("replaceBlock", Block.blockRegistry.getNameForObject(replacementBlock));
					if(metaR)
						nbt.setInteger("replaceMeta", stackReplace.getItemDamage());
				}
			}
			
			if(colorize)
			{
				GuiColorPlate plate = (GuiColorPlate) getControl("plate");
				nbt.setInteger("color", ColorUtils.RGBToInt(plate.color));
			}
			
			if(!replace && !colorize)
				openButtonDialogDialog("You have to select a task!", "ok");
			else
				sendPacketToServer(0, nbt);
		}
	}
	
	@CustomEventSubscribe
	public void onChanged(ControlChangedEvent event)
	{
		GuiColorPlate plate = (GuiColorPlate) getControl("plate");
		plate.color = Vec3.createVectorHelper((int) ((GuiSteppedSlider) getControl("colorX")).value, (int) ((GuiSteppedSlider) getControl("colorY")).value, (int) ((GuiSteppedSlider) getControl("colorZ")).value);
		
		if(event.source.is("search"))
		{
			GuiInvSelector inv = (GuiInvSelector) getControl("filter");
			inv.search = ((GuiTextfield)event.source).text.toLowerCase();
			inv.updateItems(container.player);
			inv.closeBox();
		}
		if(event.source.is("search2"))
		{
			GuiInvSelector inv = (GuiInvSelector) getControl("replacement");
			inv.search = ((GuiTextfield)event.source).text.toLowerCase();
			inv.updateItems(container.player);
			inv.closeBox();
		}
	}
	
	@Override
	public void drawOverlay(FontRenderer fontRenderer) {
		
	}

}
