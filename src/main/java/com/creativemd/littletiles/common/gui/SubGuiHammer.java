package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPlate;
import com.creativemd.creativecore.gui.controls.gui.GuiIDButton;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3i;

public class SubGuiHammer extends SubGui {
	
	public SubGuiHammer()
	{
		super(200, 200);
	}
	
	public boolean isColored = false;
	
	public int sizeX = 1;
	public int sizeY = 1;
	public int sizeZ = 1;
	
	@Override
	public void createControls() {
		controls.add(new GuiIDButton("<", 45, 10, 0));
		controls.add(new GuiIDButton(">", 75, 10, 1));
		controls.add(new GuiIDButton("<", 45, 30, 2));
		controls.add(new GuiIDButton(">", 75, 30, 3));
		controls.add(new GuiIDButton("<", 45, 50, 4));
		controls.add(new GuiIDButton(">", 75, 50, 5));
		controls.add(new GuiIDButton("HAMMER IT", 100, 10, 6));
		controls.add(new GuiSteppedSlider("colorX", 5, 75, 100, 10, 0, 255, 255));
		controls.add(new GuiSteppedSlider("colorY", 5, 85, 100, 10, 0, 255, 255));
		controls.add(new GuiSteppedSlider("colorZ", 5, 95, 100, 10, 0, 255, 255));
		controls.add(new GuiColorPlate("plate", 120, 80, 20, 20, new Vec3i(255, 255, 255)));
		
		controls.add(new GuiLabel("sizeX", "" + sizeX, 62, 14));
		controls.add(new GuiLabel("sizeY", "" + sizeY, 62, 34));
		controls.add(new GuiLabel("sizeZ", "" + sizeZ, 62, 54, 14));
		
		GuiAvatarLabel label = new GuiAvatarLabel("", 100, 32, 0, null);
		label.name = "avatar";
		label.height = 60;
		controls.add(label);
		updateLabel();
	}
	
	public void updateLabel()
	{
		GuiAvatarLabel label = (GuiAvatarLabel) get("avatar");
		
		LittleTileSize size = new LittleTileSize(sizeX, sizeY, sizeZ);
		
		ItemStack dropstack = new ItemStack(LittleTiles.blockTile);
		dropstack.setTagCompound(new NBTTagCompound());
		size.writeToNBT("size", dropstack.getTagCompound());
		Block block = null;
		ItemStack slotStack = container.getSlots().get(0).getStack();
		int meta = 0;
		if(slotStack != null)
		{
			block = Block.getBlockFromItem(slotStack.getItem());
			meta = slotStack.getItemDamage();
		}
		if(block instanceof BlockAir || block == null)
			block = Blocks.STONE;
		GuiColorPlate plate = (GuiColorPlate) get("plate");
		new LittleTileBlockColored(block, meta, plate.getColor()).saveTile(dropstack.getTagCompound());
		
		label.avatar = new AvatarItemStack(dropstack);
	}
	
	@CustomEventSubscribe
	public void onChange(GuiControlChangedEvent event)
	{
		GuiColorPlate plate = (GuiColorPlate) get("plate");
		plate.setColor(new Vec3i((int) ((GuiSteppedSlider) get("colorX")).value, (int) ((GuiSteppedSlider) get("colorY")).value, (int) ((GuiSteppedSlider) get("colorZ")).value));
		updateLabel();
	}
	
	@CustomEventSubscribe
	public void onClicked(GuiControlClickEvent event)
	{
		if(event.source instanceof GuiIDButton)
		{
			GuiIDButton button = (GuiIDButton) event.source;
			switch (button.id) {
			case 0:
				sizeX--;
				break;
			case 1:
				sizeX++;
				break;
			case 2:
				sizeY--;
				break;
			case 3:
				sizeY++;
				break;
			case 4:
				sizeZ--;
				break;
			case 5:
				sizeZ++;
				break;
			}
			if(sizeX < 1)
				sizeX = 16;
			if(sizeX > 16)
				sizeX = 1;
			if(sizeY < 1)
				sizeY = 16;
			if(sizeY > 16)
				sizeY = 1;
			if(sizeZ < 1)
				sizeZ = 16;
			if(sizeZ > 16)
				sizeZ = 1;
			
			((GuiLabel) get("sizeX")).caption = "" + sizeX;
			((GuiLabel) get("sizeY")).caption = "" + sizeY;
			((GuiLabel) get("sizeZ")).caption = "" + sizeZ;
			
			if(button.id == 6)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setByte("sizeX", (byte) sizeX);
				nbt.setByte("sizeY", (byte) sizeY);
				nbt.setByte("sizeZ", (byte) sizeZ);
				GuiColorPlate plate = (GuiColorPlate) get("plate");
				int color = ColorUtils.RGBAToInt(plate.getColor());
				if(color != ColorUtils.WHITE)
					nbt.setInteger("color", color);
				sendPacketToServer(nbt);
			}
		}
		updateLabel();
	}
	
}
