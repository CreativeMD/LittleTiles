package com.creativemd.littletiles.client.render;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.gui.GuiRenderHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OverlayRenderer {
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
	@SubscribeEvent
	public void OnRender(RenderTickEvent event)
	{
		/*if(event.phase == Phase.END && mc.player != null && mc.inGameHasFocus && GuiScreen.isCtrlKeyDown())
		{
			ScaledResolution resolution = new ScaledResolution(mc);
			int width = resolution.getScaledWidth()/2;
			int height = resolution.getScaledHeight()/2;
			
			GuiRenderHelper.instance.drawGrayBackgroundRect(resolution.getScaledWidth()/2 - width / 2, resolution.getScaledHeight()/2 - height / 2, width, height);
		}*/
	}

}
