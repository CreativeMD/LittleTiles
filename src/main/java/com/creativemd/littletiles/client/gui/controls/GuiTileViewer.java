package com.creativemd.littletiles.client.gui.controls;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import org.lwjgl.input.Keyboard;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlEvent;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.SmoothValue;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.littletiles.client.world.LittleAnimationHandlerClient;
import com.creativemd.littletiles.common.entity.AnimationPreview;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class GuiTileViewer extends GuiParent implements IAnimationControl {
	
	public EntityAnimation animation;
	public LittleGridContext context;
	public LittleVec size;
	public LittleVec min;
	
	public SmoothValue scale = new SmoothValue(200, 40);
	public SmoothValue offsetX = new SmoothValue(100);
	public SmoothValue offsetY = new SmoothValue(100);
	
	private EnumFacing viewDirection;
	
	public boolean visibleAxis = false;
	public boolean visibleNormalAxis = false;
	
	private Axis normalAxis;
	private Axis axisDirection;
	
	private EnumFacing xFacing;
	private EnumFacing yFacing;
	private EnumFacing zFacing;
	
	public SmoothValue rotX = new SmoothValue(400);
	public SmoothValue rotY = new SmoothValue(400);
	public SmoothValue rotZ = new SmoothValue(400);
	
	private LittleBox box;
	private LittleGridContext axisContext;
	private boolean even;
	
	public LittleBox getBox() {
		return box;
	}
	
	public LittleGridContext getAxisContext() {
		return axisContext;
	}
	
	public void setAxis(LittleBox box, LittleGridContext context) {
		this.box = box.copy();
		this.axisContext = context;
		raiseEvent(new GuiTileViewerAxisChangedEvent(this));
	}
	
	public boolean isEven() {
		return even;
	}
	
	public void setEven(boolean even) {
		boolean changed = this.even != even;
		this.even = even;
		
		if (!changed || box == null)
			return;
		
		if (even) {
			box.minX -= 1;
			box.minY -= 1;
			box.minZ -= 1;
		} else {
			box.minX += 1;
			box.minY += 1;
			box.minZ += 1;
		}
		raiseEvent(new GuiTileViewerAxisChangedEvent(this));
	}
	
	public boolean grabbed = false;
	
	public GuiTileViewer(String name, int x, int y, int width, int height, LittleGridContext context) {
		super(name, x, y, width, height);
		this.context = context;
		this.marginWidth = 0;
		
		setViewAxis(Axis.Y);
		rotX.setStart(rotX.aimed());
		rotY.setStart(rotY.aimed());
		rotZ.setStart(rotZ.aimed());
	}
	
	public void setNormalAxis(Axis normalAxis) {
		this.normalAxis = normalAxis;
	}
	
	public Axis getNormalAxis() {
		return normalAxis;
	}
	
	public EnumFacing getViewDirection() {
		return viewDirection;
	}
	
	public void setViewDirection(EnumFacing facing) {
		this.viewDirection = facing;
		updateNormalAxis();
		
		switch (facing.getOpposite()) {
		case EAST:
			rotX.set(0);
			rotY.set(-90);
			rotZ.set(0);
			break;
		case WEST:
			rotX.set(0);
			rotY.set(90);
			rotZ.set(0);
			break;
		case UP:
			rotX.set(90);
			rotY.set(0);
			rotZ.set(0);
			break;
		case DOWN:
			rotX.set(-90);
			rotY.set(0);
			rotZ.set(0);
			break;
		case SOUTH:
			rotX.set(0);
			rotY.set(-180);
			rotZ.set(0);
			break;
		case NORTH:
			rotX.set(0);
			rotY.set(0);
			rotZ.set(0);
			break;
		}
		
		Vec3i direction = EnumFacing.EAST.getDirectionVec();
		Vector3d vec = new Vector3d(direction.getX(), direction.getY(), direction.getZ());
		
		transform(vec);
		
		if (vec.x != 0)
			if (vec.x < 0)
				xFacing = EnumFacing.EAST;
			else
				xFacing = EnumFacing.WEST;
		else if (vec.y != 0)
			if (vec.y < 0)
				yFacing = EnumFacing.EAST;
			else
				yFacing = EnumFacing.WEST;
		else if (vec.z != 0)
			if (vec.z < 0)
				zFacing = EnumFacing.EAST;
			else
				zFacing = EnumFacing.WEST;
			
		direction = EnumFacing.UP.getDirectionVec();
		vec = new Vector3d(direction.getX(), direction.getY(), direction.getZ());
		
		transform(vec);
		
		if (vec.x != 0)
			if (vec.x < 0)
				xFacing = EnumFacing.UP;
			else
				xFacing = EnumFacing.DOWN;
		else if (vec.y != 0)
			if (vec.y < 0)
				yFacing = EnumFacing.UP;
			else
				yFacing = EnumFacing.DOWN;
		else if (vec.z != 0)
			if (vec.z < 0)
				zFacing = EnumFacing.UP;
			else
				zFacing = EnumFacing.DOWN;
			
		direction = EnumFacing.SOUTH.getDirectionVec();
		vec = new Vector3d(direction.getX(), direction.getY(), direction.getZ());
		
		transform(vec);
		
		if (vec.x != 0)
			if (vec.x < 0)
				xFacing = EnumFacing.SOUTH;
			else
				xFacing = EnumFacing.NORTH;
		else if (vec.y != 0)
			if (vec.y < 0)
				yFacing = EnumFacing.SOUTH;
			else
				yFacing = EnumFacing.NORTH;
		else if (vec.z != 0)
			if (vec.z < 0)
				zFacing = EnumFacing.SOUTH;
			else
				zFacing = EnumFacing.NORTH;
	}
	
	public Axis getAxis() {
		return axisDirection;
	}
	
	public void setViewAxis(Axis axis) {
		this.axisDirection = axis;
		setViewDirection(EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, axisDirection));
		
	}
	
	@Override
	public boolean hasMouseOverEffect() {
		return false;
	}
	
	public void updateNormalAxis() {
		if (size == null)
			return;
		
		Axis one = RotationUtils.getDifferentAxisFirst(axisDirection);
		Axis two = RotationUtils.getDifferentAxisSecond(axisDirection);
		if (size.get(one) >= size.get(two))
			normalAxis = two;
		else
			normalAxis = one;
	}
	
	public void changeNormalAxis() {
		normalAxis = RotationUtils.getDifferentAxis(axisDirection, normalAxis);
	}
	
	@Override
	protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {
		if (animation == null)
			return;
		
		scale.tick();
		offsetX.tick();
		offsetY.tick();
		
		rotX.tick();
		rotY.tick();
		rotZ.tick();
		
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(this.width / 2 - offsetX.current(), this.height / 2 - offsetY.current(), 0);
		GlStateManager.scale(-this.scale.current(), -this.scale.current(), -this.scale.current());
		GlStateManager.translate(offsetX.current() * 2, offsetY.current() * 2, 0);
		
		GlStateManager.rotate((float) rotX.current(), 1, 0, 0);
		GlStateManager.rotate((float) rotY.current(), 0, 1, 0);
		GlStateManager.rotate((float) rotZ.current(), 0, 0, 1);
		
		GlStateManager.translate(-min.getPosX(context), -min.getPosY(context), -min.getPosZ(context));
		
		GlStateManager.pushMatrix();
		
		GlStateManager.enableDepth();
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		//mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		//mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		
		GlStateManager.translate(TileEntityRendererDispatcher.staticPlayerX, TileEntityRendererDispatcher.staticPlayerY, TileEntityRendererDispatcher.staticPlayerZ);
		GlStateManager.translate(0, -75, 0);
		LittleAnimationHandlerClient.render.doRender(animation, 0, 0, 0, 0, TickUtils.getPartialTickTime());
		
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
			//GlStateManager.translate((int) Math.ceil(-size.getPosX(context) / 2), (int) Math.ceil(-size.getPosY(context) / 2), (int) Math.ceil(-size.getPosZ(context) / 2));
			CubeObject cube = new CubeObject(box.getBox(axisContext));
			RenderCubeObject normalCube = new RenderCubeObject(cube, Blocks.WOOL, 0);
			normalCube.minX += axisContext.pixelSize / 3;
			normalCube.minY += axisContext.pixelSize / 3;
			normalCube.minZ += axisContext.pixelSize / 3;
			normalCube.maxX -= axisContext.pixelSize / 3;
			normalCube.maxY -= axisContext.pixelSize / 3;
			normalCube.maxZ -= axisContext.pixelSize / 3;
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
			
			if (visibleNormalAxis)
				RenderHelper3D.renderBlock(normalCube.minX + normalCube.getSize(Axis.X) / 2, normalCube.minY + normalCube.getSize(Axis.Y) / 2, normalCube.minZ + normalCube.getSize(Axis.Z) / 2, normalCube.getSize(Axis.X), normalCube.getSize(Axis.Y), normalCube.getSize(Axis.Z), 0, 0, 0, 1, 1, 1, 0.2);
			RenderHelper3D.renderBlock(cube.minX + cube.getSize(Axis.X) / 2, cube.minY + cube.getSize(Axis.Y) / 2, cube.minZ + cube.getSize(Axis.Z) / 2, cube.getSize(Axis.X), cube.getSize(Axis.Y), cube.getSize(Axis.Z), 0, 0, 0, 0, 1, 0, 1);
			
			GlStateManager.popMatrix();
		}
		
		GlStateManager.enableTexture2D();
		GlStateManager.disableDepth();
		
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
		
	}
	
	private void transform(Vector3d vec) {
		Matrix3d matrix = new Matrix3d();
		
		if (rotZ.aimed() != 0) {
			matrix.rotZ(Math.toRadians(rotZ.aimed()));
			matrix.transform(vec);
		}
		
		if (rotY.aimed() != 0) {
			matrix.rotY(Math.toRadians(rotY.aimed()));
			matrix.transform(vec);
		}
		
		if (rotX.aimed() != 0) {
			matrix.rotX(Math.toRadians(rotX.aimed()));
			matrix.transform(vec);
		}
		
		vec.x = Math.round(vec.x);
		vec.y = Math.round(vec.y);
		vec.z = Math.round(vec.z);
	}
	
	public EnumFacing getXFacing() {
		return xFacing;
	}
	
	public EnumFacing getYFacing() {
		return yFacing;
	}
	
	public EnumFacing getZFacing() {
		return zFacing;
	}
	
	public void moveX(int distance) {
		move(getXFacing().getAxis(), distance * getXFacing().getAxisDirection().getOffset());
	}
	
	public void moveY(int distance) {
		move(getYFacing().getAxis(), -distance * getYFacing().getAxisDirection().getOffset());
	}
	
	protected void move(Axis axis, int distance) {
		switch (axis) {
		case X:
			box.minX += distance;
			box.maxX += distance;
			break;
		case Y:
			box.minY += distance;
			box.maxY += distance;
			break;
		case Z:
			box.minZ += distance;
			box.maxZ += distance;
			break;
		default:
			break;
		}
		raiseEvent(new GuiTileViewerAxisChangedEvent(this));
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
		if (isAnyControlFocused() || !visibleAxis)
			return false;
		
		if (key == Keyboard.KEY_ADD) {
			scale.set(scale.aimed() * 2);
			return true;
		}
		if (key == Keyboard.KEY_SUBTRACT) {
			scale.set(scale.aimed() / 2);
			return true;
		}
		double percent = 1;
		if (key == Keyboard.KEY_W) {
			offsetY.set(offsetY.aimed() - (1D / scale.aimed() * percent));
			return true;
		}
		if (key == Keyboard.KEY_S) {
			offsetY.set(offsetY.aimed() + (1D / scale.aimed() * percent));
			return true;
		}
		if (key == Keyboard.KEY_D) {
			offsetX.set(offsetX.aimed() + (1D / scale.aimed() * percent));
			return true;
		}
		if (key == Keyboard.KEY_A) {
			offsetX.set(offsetX.aimed() - (1D / scale.aimed() * percent));
			return true;
		}
		
		if (key == Keyboard.KEY_UP) {
			moveY(GuiScreen.isCtrlKeyDown() ? context.size : 1);
			return true;
		}
		if (key == Keyboard.KEY_DOWN) {
			moveY(-(GuiScreen.isCtrlKeyDown() ? context.size : 1));
			return true;
		}
		if (key == Keyboard.KEY_RIGHT) {
			moveX(GuiScreen.isCtrlKeyDown() ? context.size : 1);
			return true;
		}
		if (key == Keyboard.KEY_LEFT) {
			moveX(-(GuiScreen.isCtrlKeyDown() ? context.size : 1));
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onLoaded(AnimationPreview animationPreview) {
		this.animation = animationPreview.animation;
		this.size = animationPreview.previews.getSize();
		this.min = animationPreview.previews.getMinVec();
		this.context = animationPreview.previews.getContext();
		updateNormalAxis();
	}
	
	public static class GuiTileViewerAxisChangedEvent extends GuiControlEvent {
		
		public GuiTileViewerAxisChangedEvent(GuiControl source) {
			super(source);
		}
		
		@Override
		public boolean isCancelable() {
			return false;
		}
		
	}
}
