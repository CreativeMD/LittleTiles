package com.creativemd.littletiles.client.render.overlay;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.gui.controls.GuiActionDisplay;
import com.creativemd.littletiles.client.tooltip.CompiledActionMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OverlayRenderer {
	
	private static Minecraft mc = Minecraft.getMinecraft();
	protected static ScaledResolution scaledResolution;
	
	private OverlayGui gui = new OverlayGui();
	private GuiActionDisplay actionDisplay = new GuiActionDisplay("action", 0, 0, 100).setMessageCount(1).setLinesCount(4);
	
	public OverlayRenderer() {
		add(new OverlayControl(actionDisplay, OverlayPositionType.ACTION_BAR) {
			@Override
			public void resize() {
				super.resize();
				this.control.width = this.parent.width;
			}
		});
	}
	
	public void addMessage(CompiledActionMessage message) {
		actionDisplay.addMessage(message);
	}
	
	public void add(OverlayControl control) {
		gui.add(control);
	}
	
	@SubscribeEvent
	public void render(RenderTickEvent event) {
		if (event.phase == Phase.END && mc.player != null && mc.inGameHasFocus) {
			
			scaledResolution = new ScaledResolution(mc);
			if (gui.width != scaledResolution.getScaledWidth() || gui.height != scaledResolution.getScaledHeight()) {
				gui.width = scaledResolution.getScaledWidth();
				gui.height = scaledResolution.getScaledHeight();
				gui.resize();
			}
			
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			GL11.glStencilMask(~0);
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
			
			GL11.glStencilFunc(GL11.GL_ALWAYS, 0x1, 0x1);
			GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
			
			GlStateManager.pushMatrix();
			
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			gui.renderControl(GuiRenderHelper.instance, 1F, GuiControl.getScreenRect());
			GlStateManager.popMatrix();
			
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		}
	}
	
	@SubscribeEvent
	public void tick(ClientTickEvent event) {
		if (event.phase == Phase.END && mc.player != null && mc.inGameHasFocus) {
			gui.onTick();
		}
	}
	
	public static enum OverlayPositionType {
		
		CENTER {
			@Override
			protected void positionControl(GuiControl control, int width, int height) {
				control.posX = width / 2 - control.width / 2;
				control.posY = height / 2 - control.height / 2;
			}
		},
		ACTION_BAR {
			
			@Override
			protected void positionControl(GuiControl control, int width, int height) {
				control.posX = width / 2 - control.width / 2;
				control.posY = height - control.height - 68;
			}
		};
		
		protected abstract void positionControl(GuiControl control, int width, int height);
		
		public void positionControl(GuiControl control) {
			if (scaledResolution != null)
				positionControl(control, scaledResolution.getScaledWidth() - LittleTilesClient.overlay.gui.getContentOffset() * 2,
				        scaledResolution.getScaledHeight() - LittleTilesClient.overlay.gui.getContentOffset() * 2);
		}
	}
	
}
