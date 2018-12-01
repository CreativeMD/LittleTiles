package com.creativemd.littletiles.common.gui.controls;

import org.lwjgl.input.Keyboard;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.SmoothValue;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class GuiTileViewer extends GuiParent implements IAnimationControl {
	
	public EntityAnimation animation;
	public LittleGridContext context;
	public LittleTileSize size;
	public LittleTileVec min;
	
	public SmoothValue scale = new SmoothValue(200, 40);
	public SmoothValue offsetX = new SmoothValue(100);
	public SmoothValue offsetY = new SmoothValue(100);
	
	public EnumFacing viewDirection = EnumFacing.EAST;
	
	public boolean visibleAxis = false;
	
	public EnumFacing.Axis normalAxis = null;
	public EnumFacing.Axis axisDirection = EnumFacing.Axis.Y;
	
	public int axisX = 1;
	public int axisY = 1;
	public int axisZ = 1;
	
	private boolean even;
	
	public void setEven(boolean even) {
		this.even = even;
		if (even) {
			axisX = axisX % 2 == 0 ? axisX : axisX - 1;
			axisY = axisY % 2 == 0 ? axisY : axisY - 1;
			axisZ = axisZ % 2 == 0 ? axisZ : axisZ - 1;
		} else {
			axisX = axisX % 2 == 0 ? axisX + 1 : axisX;
			axisY = axisY % 2 == 0 ? axisY + 1 : axisY;
			axisZ = axisZ % 2 == 0 ? axisZ + 1 : axisZ;
		}
	}
	
	public boolean grabbed = false;
	
	public GuiTileViewer(String name, int x, int y, int width, int height, LittleGridContext context) {
		super(name, x, y, width, height);
		this.context = context;
		this.marginWidth = 0;
	}
	
	@Override
	public boolean hasMouseOverEffect() {
		return false;
	}
	
	public void updateNormalAxis() {
		
		switch (axisDirection) {
		case X:
			if (size.sizeY >= size.sizeZ)
				normalAxis = EnumFacing.Axis.Y;
			else
				normalAxis = EnumFacing.Axis.Z;
			break;
		case Y:
			if (size.sizeX >= size.sizeZ)
				normalAxis = EnumFacing.Axis.Z;
			else
				normalAxis = EnumFacing.Axis.X;
			break;
		case Z:
			if (size.sizeX >= size.sizeY)
				normalAxis = EnumFacing.Axis.Y;
			else
				normalAxis = EnumFacing.Axis.X;
			break;
		default:
			break;
		}
	}
	
	public void changeNormalAxis() {
		switch (axisDirection) {
		case X:
			if (normalAxis == EnumFacing.Axis.Z)
				normalAxis = EnumFacing.Axis.Y;
			else
				normalAxis = EnumFacing.Axis.Z;
			break;
		case Y:
			if (normalAxis == EnumFacing.Axis.Z)
				normalAxis = EnumFacing.Axis.X;
			else
				normalAxis = EnumFacing.Axis.Z;
			break;
		case Z:
			if (normalAxis == EnumFacing.Axis.Y)
				normalAxis = EnumFacing.Axis.X;
			else
				normalAxis = EnumFacing.Axis.Y;
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {
		if (animation == null)
			return;
		
		scale.tick();
		offsetX.tick();
		offsetY.tick();
		
		GlStateManager.pushMatrix();
		
		// Vec3 offset = Vec3.createVectorHelper(p_72443_0_, p_72443_2_, p_72443_4_);
		GlStateManager.translate(this.width / 2 + offsetX.current(), this.height / 2 + offsetY.current(), 0);
		//GlStateManager.scale(4, 4, 4);
		GlStateManager.scale(this.scale.current(), this.scale.current(), this.scale.current());
		GlStateManager.translate(-offsetX.current() * 2, -offsetY.current() * 2, 0);
		
		if (viewDirection.getAxis() != EnumFacing.Axis.Y)
			GlStateManager.rotate(180, 0, 0, 1);
		EnumFacing facing = viewDirection;
		switch (viewDirection) {
		case EAST:
			GlStateManager.rotate(180, 0, 1, 0);
			facing = EnumFacing.SOUTH;
			break;
		case WEST:
			facing = EnumFacing.NORTH;
			break;
		case UP:
			GlStateManager.rotate(-90, 1, 0, 0);
			break;
		case DOWN:
			GlStateManager.rotate(90, 1, 0, 0);
			break;
		case SOUTH:
			GlStateManager.rotate(90, 0, 1, 0);
			facing = EnumFacing.EAST;
			break;
		case NORTH:
			GlStateManager.rotate(-90, 0, 1, 0);
			facing = EnumFacing.WEST;
			break;
		}
		
		GlStateManager.translate(-min.getPosX(context), -min.getPosY(context), -min.getPosZ(context));
		
		GlStateManager.pushMatrix();
		
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		
		LittleDoorHandler.client.render.doRender(animation, 0, 0, 0, 0, 1.0F);
		
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		GlStateManager.disableBlend();
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		
		GlStateManager.popMatrix();
		
		GlStateManager.disableTexture2D();
		GlStateManager.disableDepth();
		GlStateManager.disableLighting();
		
		if (visibleAxis) {
			
			GlStateManager.pushMatrix();
			
			GlStateManager.translate((int) Math.ceil(-size.getPosX(context) / 2), (int) Math.ceil(-size.getPosY(context) / 2), (int) Math.ceil(-size.getPosZ(context) / 2));
			
			CubeObject cube = new CubeObject(context.toVanillaGrid(axisX / 2F) - (float) context.gridMCLength / 2, context.toVanillaGrid(axisY / 2F) - (float) context.gridMCLength / 2, context.toVanillaGrid(axisZ / 2F) - (float) context.gridMCLength / 2, context.toVanillaGrid(axisX / 2F) + (float) context.gridMCLength / 2, context.toVanillaGrid(axisY / 2F) + (float) context.gridMCLength / 2, context.toVanillaGrid(axisZ / 2F) + (float) context.gridMCLength / 2);
			RenderCubeObject normalCube = new RenderCubeObject(cube, Blocks.WOOL, 0);
			normalCube.minX += context.gridMCLength / 3;
			normalCube.minY += context.gridMCLength / 3;
			normalCube.minZ += context.gridMCLength / 3;
			normalCube.maxX -= context.gridMCLength / 3;
			normalCube.maxY -= context.gridMCLength / 3;
			normalCube.maxZ -= context.gridMCLength / 3;
			normalCube.keepVU = true;
			float min = (float) (-10000 * 1 / scale.aimed());
			float max = -min;
			switch (normalAxis) {
			case X:
				normalCube.minX = min;
				normalCube.maxX = max;
				break;
			case Y:
				normalCube.minY = min;
				normalCube.maxY = max;
				break;
			case Z:
				normalCube.minZ = min;
				normalCube.maxZ = max;
				break;
			default:
				break;
			}
			
			RenderHelper3D.renderBlock(normalCube.minX + normalCube.getSize(Axis.X) / 2, normalCube.minY + normalCube.getSize(Axis.Y) / 2, normalCube.minZ + normalCube.getSize(Axis.Z) / 2, normalCube.getSize(Axis.X), normalCube.getSize(Axis.Y), normalCube.getSize(Axis.Z), 0, 0, 0, 1, 1, 1, 0.2);
			RenderHelper3D.renderBlock(cube.minX + context.gridMCLength / 2, cube.minY + context.gridMCLength / 2, cube.minZ + context.gridMCLength / 2, cube.getSize(Axis.X), cube.getSize(Axis.Y), cube.getSize(Axis.Z), 0, 0, 0, 0, 1, 0, 1);
			
			GlStateManager.enableTexture2D();
			
			GlStateManager.popMatrix();
		}
		
		GlStateManager.enableDepth();
		
		GlStateManager.disableBlend();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		
		String xAxis = getXFacing().getAxis().name();
		if (getXFacing().getAxisDirection() == AxisDirection.POSITIVE)
			xAxis += " ->";
		else
			xAxis = "<- " + xAxis;
		String yAxis = getYFacing().getAxis().name();
		if (getYFacing().getAxisDirection() == AxisDirection.POSITIVE)
			yAxis += " ->";
		else
			yAxis = "<- " + yAxis;
		
		helper.drawStringWithShadow(xAxis, 0, 0, width, 14, ColorUtils.WHITE);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(14, 0, 0);
		GlStateManager.rotate(90, 0, 0, 1);
		helper.drawStringWithShadow(yAxis, 0, 0, width, 14, ColorUtils.WHITE);
		GlStateManager.popMatrix();
		GlStateManager.disableDepth();
		
	}
	
	public EnumFacing getXFacing() {
		switch (viewDirection) {
		case EAST:
			return EnumFacing.EAST;
		case WEST:
			return EnumFacing.WEST;
		case UP:
			return EnumFacing.EAST;
		case DOWN:
			return EnumFacing.EAST;
		case SOUTH:
			return EnumFacing.NORTH;
		case NORTH:
			return EnumFacing.SOUTH;
		}
		return EnumFacing.EAST;
	}
	
	public EnumFacing getYFacing() {
		switch (viewDirection) {
		case EAST:
			return EnumFacing.DOWN;
		case WEST:
			return EnumFacing.DOWN;
		case UP:
			return EnumFacing.SOUTH;
		case DOWN:
			return EnumFacing.NORTH;
		case SOUTH:
			return EnumFacing.DOWN;
		case NORTH:
			return EnumFacing.DOWN;
		}
		return EnumFacing.DOWN;
	}
	
	public EnumFacing getZFacing() {
		switch (viewDirection) {
		case EAST:
			return EnumFacing.NORTH;
		case WEST:
			return EnumFacing.NORTH;
		case UP:
			return EnumFacing.DOWN;
		case DOWN:
			return EnumFacing.DOWN;
		case SOUTH:
			return EnumFacing.WEST;
		case NORTH:
			return EnumFacing.WEST;
		}
		return EnumFacing.NORTH;
	}
	
	@Override
	public boolean mouseScrolled(int posX, int posY, int scrolled) {
		if (scrolled > 0)
			scale.set(scale.aimed() * scrolled * 1.5);
		else if (scrolled < 0)
			scale.set(scale.aimed() / (scrolled * -1.5));
		return true;
	}
	
	@Override
	public boolean mousePressed(int posX, int posY, int button) {
		grabbed = true;
		lastPosition = new Vec3d(posX, posY, 0);
		return true;
	}
	
	public Vec3d lastPosition;
	
	@Override
	public void mouseMove(int posX, int posY, int button) {
		// Vec3d mouse = getParent().getMousePos();
		if (grabbed) {
			Vec3d currentPosition = new Vec3d(posX, posY, 0);
			if (lastPosition != null) {
				Vec3d move = lastPosition.subtract(currentPosition);
				double percent = 0.5;
				offsetX.set(offsetX.aimed() + (1D / scale.aimed() * move.x * percent));
				offsetY.set(offsetY.aimed() + (1D / scale.aimed() * move.y * percent));
			}
			lastPosition = currentPosition;
		}
	}
	
	@Override
	public void mouseReleased(int posX, int posY, int button) {
		if (this.grabbed) {
			lastPosition = null;
			grabbed = false;
		}
	}
	
	@Override
	public boolean onKeyPressed(char character, int key) {
		if (key == Keyboard.KEY_ADD) {
			scale.set(scale.aimed() * 2);
			return true;
		}
		if (key == Keyboard.KEY_SUBTRACT) {
			scale.set(scale.aimed() / 2);
			return true;
		}
		double percent = 1;
		if (key == Keyboard.KEY_UP) {
			offsetY.set(offsetY.aimed() - (1D / scale.aimed() * percent));
			return true;
		}
		if (key == Keyboard.KEY_DOWN) {
			offsetY.set(offsetY.aimed() + (1D / scale.aimed() * percent));
			return true;
		}
		if (key == Keyboard.KEY_RIGHT) {
			offsetX.set(offsetX.aimed() + (1D / scale.aimed() * percent));
			return true;
		}
		if (key == Keyboard.KEY_LEFT) {
			offsetX.set(offsetX.aimed() - (1D / scale.aimed() * percent));
			return true;
		}
		
		return false;
	}
	
	public void updateViewDirection() {
		switch (axisDirection) {
		case X:
			viewDirection = EnumFacing.SOUTH;
			break;
		case Y:
			viewDirection = EnumFacing.UP;
			break;
		case Z:
			viewDirection = EnumFacing.EAST;
			break;
		default:
			break;
		}
		updateNormalAxis();
	}
	
	@Override
	public void onLoaded(EntityAnimation animation, LittleTileBox entireBox, LittleGridContext context, AxisAlignedBB box, LittlePreviews previews) {
		this.animation = animation;
		this.size = previews.getSize();
		this.min = previews.getMinVec();
		updateNormalAxis();
	}
}
