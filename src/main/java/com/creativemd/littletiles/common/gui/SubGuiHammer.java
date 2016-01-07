package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.gui.controls.GuiAvatarLabel;
import com.creativemd.creativecore.common.gui.controls.GuiButton;
import com.creativemd.creativecore.common.gui.controls.GuiColorPlate;
import com.creativemd.creativecore.common.gui.controls.GuiControl;
import com.creativemd.creativecore.common.gui.controls.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.controls.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.ControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.ControlClickEvent;
import com.creativemd.creativecore.common.packet.GuiControlPacket;
import com.creativemd.creativecore.common.packet.GuiUpdatePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

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
		controls.add(new GuiButton("<", 45, 10, 10, 20, 0));
		controls.add(new GuiButton(">", 75, 10, 10, 20, 1));
		controls.add(new GuiButton("<", 45, 30, 10, 20, 2));
		controls.add(new GuiButton(">", 75, 30, 10, 20, 3));
		controls.add(new GuiButton("<", 45, 50, 10, 20, 4));
		controls.add(new GuiButton(">", 75, 50, 10, 20, 5));
		controls.add(new GuiButton("HAMMER IT", 100, 10, 60, 20, 6));
		controls.add(new GuiSteppedSlider("colorX", 5, 75, 100, 10, 0, 255, 255));
		controls.add(new GuiSteppedSlider("colorY", 5, 85, 100, 10, 0, 255, 255));
		controls.add(new GuiSteppedSlider("colorZ", 5, 95, 100, 10, 0, 255, 255));
		controls.add(new GuiColorPlate("plate", 120, 80, 20, 20, Vec3.createVectorHelper(255, 255, 255)));
		GuiAvatarLabel label = new GuiAvatarLabel("", 100, 32, 0, null);
		label.name = "avatar";
		label.height = 60;
		controls.add(label);
		updateLabel();
	}
	
	public void updateLabel()
	{
		GuiAvatarLabel label = (GuiAvatarLabel) getControl("avatar");
		
		LittleTileSize size = new LittleTileSize(sizeX, sizeY, sizeZ);
		
		ItemStack dropstack = new ItemStack(LittleTiles.blockTile);
		dropstack.stackTagCompound = new NBTTagCompound();
		size.writeToNBT("size", dropstack.stackTagCompound);
		Block block = null;
		ItemStack slotStack = container.getSlots().get(0).getStack();
		if(slotStack != null)
			block = Block.getBlockFromItem(slotStack.getItem());
		if(block instanceof BlockAir || block == null)
			block = Blocks.stone;
		GuiColorPlate plate = (GuiColorPlate) getControl("plate");
		new LittleTileBlockColored(block, 0, plate.color).saveTile(dropstack.stackTagCompound);
		
		label.avatar = new AvatarItemStack(dropstack);
	}

	@Override
	public void drawOverlay(FontRenderer fontRenderer) {
		fontRenderer.drawString("" + sizeX, 62, 14, 0);
		fontRenderer.drawString("" + sizeY, 62, 34, 0);
		fontRenderer.drawString("" + sizeZ, 62, 54, 0);
	}
	
	@CustomEventSubscribe
	public void onChange(ControlChangedEvent event)
	{
		GuiColorPlate plate = (GuiColorPlate) getControl("plate");
		plate.color = Vec3.createVectorHelper((int) ((GuiSteppedSlider) getControl("colorX")).value, (int) ((GuiSteppedSlider) getControl("colorY")).value, (int) ((GuiSteppedSlider) getControl("colorZ")).value);
		updateLabel();
	}
	
	@CustomEventSubscribe
	public void onClicked(ControlClickEvent event)
	{
		if(event.source instanceof GuiButton)
		{
			GuiButton button = (GuiButton) event.source;
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
			
			if(button.id == 6)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setByte("sizeX", (byte) sizeX);
				nbt.setByte("sizeY", (byte) sizeY);
				nbt.setByte("sizeZ", (byte) sizeZ);
				GuiColorPlate plate = (GuiColorPlate) getControl("plate");
				int color = ColorUtils.RGBToInt(plate.color);
				if(color != ColorUtils.WHITE)
					nbt.setInteger("color", color);
				sendPacketToServer(button.id, nbt);
			}
		}
		updateLabel();
	}
	
}
