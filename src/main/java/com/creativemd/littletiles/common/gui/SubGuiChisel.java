package com.creativemd.littletiles.common.gui;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPlate;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiInvSelector;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SubGuiChisel extends SubGui {
	
	public ItemStack stack;
	
	public SubGuiChisel(ItemStack stack) {
		super(200, 200);
		this.stack = stack;
	}
	
	@Override
	public void createControls() {
		if(stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());
		
		controls.add(new GuiCheckBox("any", "any", 5, 5, false));
		controls.add(new GuiInvSelector("filter", 40, 5, 130, container.player, true));
		controls.add(new GuiTextfield("search", "", 40, 27, 140, 14));
		controls.add(new GuiCheckBox("meta", "Metadata", 40, 50, true));
		
		controls.add(new GuiCheckBox("replace", "Replace with", 5, 70, false));
		
		controls.add(new GuiInvSelector("replacement", 40, 87, 130, container.player, true));
		controls.add(new GuiTextfield("search2", "", 40, 109, 140, 14));
		controls.add(new GuiCheckBox("metaR", "Force metadata", 40, 130, true));
		
		
		Color color = new Color(255, 255, 255);
		controls.add(new GuiCheckBox("colorize", "Colorize", 5, 143, false));
		
		controls.add(new GuiColorPicker("picker", 5, 160, color));
		
		controls.add(new GuiButton("run", "Do it!", 150, 170, 40){
			
			@Override
			public void onClicked(int x, int y, int button) {
				boolean any = ((GuiCheckBox)get("any")).value;
				boolean replace = ((GuiCheckBox)get("replace")).value;
				boolean colorize = ((GuiCheckBox)get("colorize")).value;
				boolean meta = ((GuiCheckBox)get("meta")).value;
				boolean metaR = ((GuiCheckBox)get("metaR")).value;
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("x1", stack.getTagCompound().getInteger("x1"));
				nbt.setInteger("y1", stack.getTagCompound().getInteger("y1"));
				nbt.setInteger("z1", stack.getTagCompound().getInteger("z1"));
				nbt.setInteger("x2", stack.getTagCompound().getInteger("x2"));
				nbt.setInteger("y2", stack.getTagCompound().getInteger("y2"));
				nbt.setInteger("z2", stack.getTagCompound().getInteger("z2"));
				
				GuiInvSelector filter = (GuiInvSelector) get("filter");
				GuiInvSelector replacement = (GuiInvSelector) get("replacement");
				ItemStack stackFilter = filter.getStack();
				if(!any && stackFilter != null)
				{
					nbt.setString("filterBlock", Block.REGISTRY.getNameForObject(Block.getBlockFromItem(stackFilter.getItem())).toString());
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
						nbt.setString("replaceBlock", Block.REGISTRY.getNameForObject(replacementBlock).toString());
						if(metaR)
							nbt.setInteger("replaceMeta", stackReplace.getItemDamage());
					}
				}
				
				if(colorize)
				{
					GuiColorPicker picker = (GuiColorPicker) get("picker");
					nbt.setInteger("color", ColorUtils.RGBAToInt(picker.color));
				}
				
				if(!replace && !colorize)
					openButtonDialogDialog("You have to select a task!", "ok");
				else
					sendPacketToServer(nbt);
			}
		});
	}
	
	@CustomEventSubscribe
	public void onChanged(GuiControlChangedEvent event)
	{
		if(event.source.is("search"))
		{
			GuiInvSelector inv = (GuiInvSelector) get("filter");
			inv.search = ((GuiTextfield)event.source).text.toLowerCase();
			inv.updateItems(container.player);
			inv.closeBox();
		}
		if(event.source.is("search2"))
		{
			GuiInvSelector inv = (GuiInvSelector) get("replacement");
			inv.search = ((GuiTextfield)event.source).text.toLowerCase();
			inv.updateItems(container.player);
			inv.closeBox();
		}
	}

}
