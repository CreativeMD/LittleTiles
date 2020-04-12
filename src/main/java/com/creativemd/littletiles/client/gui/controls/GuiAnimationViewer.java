package com.creativemd.littletiles.client.gui.controls;

import javax.vecmath.Vector3d;

import org.lwjgl.util.glu.Project;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.utils.math.SmoothValue;
import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.littletiles.client.world.LittleAnimationHandlerClient;
import com.creativemd.littletiles.common.entity.AnimationPreview;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class GuiAnimationViewer extends GuiControl implements IAnimationControl {
	
	public EntityAnimation animation;
	public LittleGridContext context;
	public LittleVec min;
	
	public SmoothValue rotX = new SmoothValue(200);
	public SmoothValue rotY = new SmoothValue(200);
	public SmoothValue rotZ = new SmoothValue(200);
	public SmoothValue distance = new SmoothValue(200);
	
	public boolean grabbed = false;
	public int grabX;
	public int grabY;
	
	public GuiAnimationViewer(String name, int x, int y, int width, int height) {
		super(name, x, y, width, height);
		this.marginWidth = 0;
	}
	
	@Override
	public boolean hasMouseOverEffect() {
		return false;
	}
	
	@Override
	public boolean canOverlap() {
		return false;
	}
	
	@Override
	public void mouseMove(int x, int y, int button) {
		super.mouseMove(x, y, button);
		if (grabbed) {
			rotY.set(rotY.aimed() + x - grabX);
			rotX.set(rotX.aimed() + y - grabY);
			grabX = x;
			grabY = y;
		}
	}
	
	@Override
	public boolean mousePressed(int x, int y, int button) {
		if (button == 0) {
			grabbed = true;
			grabX = x;
			grabY = y;
			return true;
		}
		return false;
	}
	
	@Override
	public void mouseReleased(int x, int y, int button) {
		if (button == 0)
			grabbed = false;
	}
	
	@Override
	public boolean mouseScrolled(int x, int y, int scrolled) {
		distance.set(Math.max(distance.aimed() + scrolled * -(GuiScreen.isCtrlKeyDown() ? 5 : 1), 0));
		return true;
	}
	
	@Override
	protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {
		if (animation == null)
			return;
		
		rotX.tick();
		rotY.tick();
		rotZ.tick();
		distance.tick();
		
		GlStateManager.disableDepth();
		
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		GlStateManager.translate(width / 2D, height / 2D, 0);
		
		GlStateManager.pushMatrix();
		
		//mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		//mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		
		int x = getPixelOffsetX();
		int y = getPixelOffsetY() - 1;
		int scale = getGuiScale();
		GlStateManager.viewport(x * scale, y * scale, width * scale, height * scale);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		Project.gluPerspective(90, (float) width / (float) height, 0.05F, 16 * 16);
		GlStateManager.matrixMode(5888);
		GlStateManager.loadIdentity();
		//GlStateManager.matrixMode(5890);
		GlStateManager.translate(0, 0, -distance.current());
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableDepth();
		
		Vector3d rotationCenter = new Vector3d(animation.center.rotationCenter);
		rotationCenter.y -= 75;
		
		GlStateManager.rotate((float) rotX.current(), 1, 0, 0);
		GlStateManager.rotate((float) rotY.current(), 0, 1, 0);
		GlStateManager.rotate((float) rotZ.current(), 0, 0, 1);
		
		GlStateManager.translate(-min.getPosX(context), -min.getPosY(context), -min.getPosZ(context));
		
		GlStateManager.translate(-rotationCenter.x, -rotationCenter.y, -rotationCenter.z);
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(TileEntityRendererDispatcher.staticPlayerX, TileEntityRendererDispatcher.staticPlayerY, TileEntityRendererDispatcher.staticPlayerZ);
		GlStateManager.translate(0, -75, 0);
		
		LittleAnimationHandlerClient.render.doRender(animation, 0, 0, 0, 0, TickUtils.getPartialTickTime());
		
		GlStateManager.popMatrix();
		
		GlStateManager.matrixMode(5888);
		
		GlStateManager.popMatrix();
		
		GlStateManager.disableLighting();
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		GlStateManager.disableDepth();
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		
		GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.loadIdentity();
		mc.entityRenderer.setupOverlayRendering();
		GlStateManager.disableDepth();
	}
	
	@Override
	public void onLoaded(AnimationPreview animationPreview) {
		this.animation = animationPreview.animation;
		this.distance.setStart(animationPreview.context.toVanillaGrid(animationPreview.entireBox.getLongestSide()) / 2D + 2);
		this.context = animationPreview.context;
		this.min = animationPreview.entireBox.getMinVec();
	}
}
