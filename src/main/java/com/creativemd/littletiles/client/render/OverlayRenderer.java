package com.creativemd.littletiles.client.render;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.gui.GuiRenderHelper;
import com.creativemd.littletiles.LittleTiles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OverlayRenderer {
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
	public static final ResourceLocation ingameTextures = new ResourceLocation(LittleTiles.modid, "textures/gui/ingameselect.png");
	
	@SubscribeEvent
	public void OnRender(RenderTickEvent event) {
		if (event.phase == Phase.END && mc.player != null && mc.inGameHasFocus) {
			if (PreviewRenderer.marked != null)
				PreviewRenderer.marked.renderOverlay(event.renderTickTime);
			
			/*
			 * if(GuiScreen.isCtrlKeyDown()) { ScaledResolution resolution = new
			 * ScaledResolution(mc); double width = resolution.getScaledWidth()*1.5; double
			 * height = resolution.getScaledHeight()*1.5;
			 * 
			 * //resolution.getScaledWidth()/2 - width / 2, resolution.getScaledHeight()/2 -
			 * height / 2
			 * 
			 * GlStateManager.pushMatrix();
			 * 
			 * RenderHelper.enableStandardItemLighting(); GlStateManager.disableLighting();
			 * GlStateManager.disableFog(); GlStateManager.enableBlend();
			 * 
			 * GlStateManager.enableAlpha();
			 * 
			 * GlStateManager.disableDepth();
			 * GlStateManager.translate(resolution.getScaledWidth()/2,
			 * resolution.getScaledHeight()/2, 0);
			 * 
			 * 
			 * 
			 * GlStateManager.pushMatrix();
			 * 
			 * GlStateManager.scale(width / 128D, height / 128D, 0);
			 * 
			 * GuiRenderHelper.instance.drawTexturedModalRect(ingameTextures, -128/2,
			 * -128/2, 0, 0, 128, 128); GlStateManager.popMatrix();
			 * 
			 * drawSlot("Drag-Box", -240, 0.17F, 0.8F);
			 * 
			 * drawSlot("Drag-Sphere", -120, 0.5F, 0.9F);
			 * 
			 * drawSlot("Cube", 0, 1, 1);
			 * 
			 * drawSlot("Bar", 120, 0.5F, 0.9F);
			 * 
			 * drawSlot("Plane", 240, 0.17F, 0.8F);
			 * 
			 * GlStateManager.enableDepth(); GlStateManager.popMatrix(); }
			 */
		}
	}
	
	public static void drawSlot(String title, int offset, float alpha, float scale) {
		GlStateManager.pushMatrix();
		
		boolean selected = offset == 0;
		
		GlStateManager.translate(offset, 0, 0);
		GlStateManager.color(1, 1, 1, alpha);
		int slotSizeInner = 48;
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(0, -slotSizeInner * 1.25 * scale * (selected ? 1.25 : 1), 0);
		
		if (selected)
			GlStateManager.scale(3 * scale, 3 * scale, 0);
		else
			GlStateManager.scale(2 * scale, 2 * scale, 0);
		GuiRenderHelper.instance.drawStringWithShadow(title, 0, 0, ColorUtils.RGBAToInt(new Color(255, 255, 255, (byte) (alpha * 255))));
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		
		GlStateManager.scale(2 * scale, 2 * scale, 0);
		if (selected)
			GuiRenderHelper.instance.drawTexturedModalRect(ingameTextures, -slotSizeInner / 2, -slotSizeInner / 2, 128, 0, slotSizeInner, slotSizeInner);
		GuiRenderHelper.instance.drawTexturedModalRect(ingameTextures, -slotSizeInner / 2, -slotSizeInner / 2, 128, 48, slotSizeInner, slotSizeInner);
		GlStateManager.popMatrix();
		
		GlStateManager.popMatrix();
	}
	
}
