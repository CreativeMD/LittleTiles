package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;

import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.gui.SubGui.ControlEvent;
import com.creativemd.creativecore.common.gui.controls.GuiButtonControl;
import com.creativemd.creativecore.common.gui.controls.GuiControl;
import com.creativemd.creativecore.common.gui.controls.GuiTextfield;
import com.creativemd.creativecore.common.packet.GuiControlPacket;
import com.creativemd.creativecore.common.packet.GuiUpdatePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;

public class SubGuiHammer extends SubGui {
	
	public SubGuiHammer()
	{
		
	}
	
	public int sizeX = 1;
	public int sizeY = 1;
	public int sizeZ = 1;
	
	@Override
	public void createControls() {
		controls.add(new GuiButtonControl("<", 50, 20, 10, 20, 0));
		controls.add(new GuiButtonControl(">", 80, 20, 10, 20, 1));
		controls.add(new GuiButtonControl("<", 50, 40, 10, 20, 2));
		controls.add(new GuiButtonControl(">", 80, 40, 10, 20, 3));
		controls.add(new GuiButtonControl("<", 50, 60, 10, 20, 4));
		controls.add(new GuiButtonControl(">", 80, 60, 10, 20, 5));
		controls.add(new GuiButtonControl("HAMMER IT", 130, 40, 60, 20, 6));
	}

	@Override
	public void drawForeground(FontRenderer fontRenderer) {
		fontRenderer.drawString("" + sizeX, 62, 14, 0);
		fontRenderer.drawString("" + sizeY, 62, 34, 0);
		fontRenderer.drawString("" + sizeZ, 62, 54, 0);
	}
	
	@Override
	public void onControlEvent(GuiControl control, ControlEvent event)
	{
		if(control instanceof GuiButtonControl)
		{
			GuiButtonControl button = (GuiButtonControl) control;
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
				PacketHandler.sendPacketToServer(new GuiControlPacket(button.id, nbt));
			}
		}
	}
}
