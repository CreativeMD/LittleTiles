package com.creativemd.littletiles.client.render.overlay;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTilesConfig;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.render.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.tiles.place.FixedHandler;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.MarkMode;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PreviewResult;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.placing.PlacementMode.PreviewMode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PreviewRenderer {
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public static MarkMode marked = null;
	
	public static boolean isCentered(EntityPlayer player, ILittleTile iTile) {
		if (iTile.snapToGridByDefault())
			return LittleAction.isUsingSecondMode(player) && marked == null;
		return LittleTilesConfig.building.invertStickToGrid == LittleAction.isUsingSecondMode(player) || marked != null;
	}
	
	public static boolean isFixed(EntityPlayer player, ILittleTile iTile) {
		if (iTile.snapToGridByDefault())
			return !LittleAction.isUsingSecondMode(player) && marked == null;
		return LittleTilesConfig.building.invertStickToGrid != LittleAction.isUsingSecondMode(player) && marked == null;
	}
	
	public static void handleUndoAndRedo(EntityPlayer player) {
		while (LittleTilesClient.undo.isPressed()) {
			try {
				if (LittleAction.canUseUndoOrRedo(player))
					LittleAction.undo();
			} catch (LittleActionException e) {
				LittleAction.handleExceptionClient(e);
			}
		}
		
		while (LittleTilesClient.redo.isPressed()) {
			try {
				if (LittleAction.canUseUndoOrRedo(player))
					LittleAction.redo();
			} catch (LittleActionException e) {
				LittleAction.handleExceptionClient(e);
			}
		}
	}
	
	@SubscribeEvent
	public void tick(RenderWorldLastEvent event) {
		if (mc.player != null && mc.inGameHasFocus && !mc.gameSettings.hideGUI) {
			World world = mc.world;
			EntityPlayer player = mc.player;
			ItemStack stack = mc.player.getHeldItemMainhand();
			
			if (!LittleAction.canPlace(player))
				return;
			
			handleUndoAndRedo(player);
			
			if (PlacementHelper.isLittleBlock(stack) && (marked != null || (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == Type.BLOCK && mc.objectMouseOver.sideHit != null))) {
				if (marked != null)
					marked.renderWorld(event.getPartialTicks());
				
				ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
				
				PositionResult position = marked != null ? marked.position.copy() : PlacementHelper.getPosition(world, mc.objectMouseOver, iTile.getPositionContext(stack));
				
				processRotateKeys(position.getContext());
				iTile.tickPreview(player, stack, position, mc.objectMouseOver);
				
				PlacementMode mode = iTile.getPlacementMode(stack);
				
				if (mode.getPreviewMode() == PreviewMode.PREVIEWS) {
					GL11.glEnable(GL11.GL_BLEND);
					OpenGlHelper.glBlendFunc(770, 771, 1, 0);
					GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					GL11.glDepthMask(false);
					
					boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
					PreviewResult result = PlacementHelper.getPreviews(world, stack, position, isCentered(player, iTile), isFixed(player, iTile), allowLowResolution, marked != null, mode);
					
					if (result != null) {
						processMarkKey(player, iTile, stack, result, result.isAbsolute());
						double x = position.pos.getX() - TileEntityRendererDispatcher.staticPlayerX;
						double y = position.pos.getY() - TileEntityRendererDispatcher.staticPlayerY;
						double z = position.pos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
						
						for (int i = 0; i < result.placePreviews.size(); i++) {
							
							PlacePreviewTile preview = result.placePreviews.get(i);
							List<LittleRenderingCube> cubes = preview.getPreviews(result.context);
							for (LittleRenderingCube cube : cubes) {
								GL11.glPushMatrix();
								cube.renderCubePreview(x, y, z, iTile);
								GL11.glPopMatrix();
							}
						}
						
						if (!result.isAbsolute() && marked == null && LittleAction.isUsingSecondMode(player) && result.singleMode) {
							ArrayList<FixedHandler> shifthandlers = new ArrayList<FixedHandler>();
							
							for (int i = 0; i < result.placePreviews.size(); i++)
								if (result.placePreviews.get(i).preview != null)
									shifthandlers.addAll(result.placePreviews.get(i).preview.fixedhandlers);
								
							for (int i = 0; i < shifthandlers.size(); i++) {
								// GL11.glPushMatrix();
								shifthandlers.get(i).handleRendering(result.context, mc, x, y, z);
								// GL11.glPopMatrix();
							}
						}
					}
					
					GL11.glDepthMask(true);
					GL11.glEnable(GL11.GL_TEXTURE_2D);
					GL11.glDisable(GL11.GL_BLEND);
				}
			} else
				marked = null;
		}
	}
	
	public void processMarkKey(EntityPlayer player, ILittleTile iTile, ItemStack stack, PreviewResult preview, boolean absolute) {
		while (LittleTilesClient.mark.isPressed()) {
			if (marked == null)
				marked = iTile.onMark(player, stack);
			
			if (marked != null && marked.processPosition(player, PlacementHelper.getPosition(player.world, mc.objectMouseOver, iTile.getPositionContext(stack)), preview, absolute))
				marked = null;
		}
	}
	
	public void processRotateKey(Rotation rotation) {
		LittleRotatePacket packet = new LittleRotatePacket(rotation);
		packet.executeClient(mc.player);
		PacketHandler.sendPacketToServer(packet);
	}
	
	public void processRotateKeys(LittleGridContext context) {
		while (LittleTilesClient.flip.isPressed()) {
			int i4 = MathHelper.floor(this.mc.player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			EnumFacing direction = null;
			switch (i4) {
			case 0:
				direction = EnumFacing.SOUTH;
				break;
			case 1:
				direction = EnumFacing.WEST;
				break;
			case 2:
				direction = EnumFacing.NORTH;
				break;
			case 3:
				direction = EnumFacing.EAST;
				break;
			}
			if (mc.player.rotationPitch > 45)
				direction = EnumFacing.DOWN;
			if (mc.player.rotationPitch < -45)
				direction = EnumFacing.UP;
			
			LittleFlipPacket packet = new LittleFlipPacket(direction.getAxis());
			packet.executeClient(mc.player);
			PacketHandler.sendPacketToServer(packet);
		}
		
		boolean repeated = marked != null;
		
		// Rotate Block
		while (LittleTilesClient.up.isPressed(repeated)) {
			if (marked != null)
				marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? EnumFacing.UP : EnumFacing.EAST);
			else
				processRotateKey(Rotation.Z_CLOCKWISE);
		}
		
		while (LittleTilesClient.down.isPressed(repeated)) {
			if (marked != null)
				marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? EnumFacing.DOWN : EnumFacing.WEST);
			else
				processRotateKey(Rotation.Z_COUNTER_CLOCKWISE);
		}
		
		while (LittleTilesClient.right.isPressed(repeated)) {
			if (marked != null)
				marked.move(context, EnumFacing.SOUTH);
			else
				processRotateKey(Rotation.Y_COUNTER_CLOCKWISE);
		}
		
		while (LittleTilesClient.left.isPressed(repeated)) {
			if (marked != null)
				marked.move(context, EnumFacing.NORTH);
			else
				processRotateKey(Rotation.Y_CLOCKWISE);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event) {
		EntityPlayer player = event.getPlayer();
		World world = player.world;
		ItemStack stack = player.getHeldItemMainhand();
		
		if (!LittleAction.canPlace(player))
			return;
		
		if ((event.getTarget().typeOfHit == Type.BLOCK || marked != null) && PlacementHelper.isLittleBlock(stack)) {
			if (marked != null)
				marked.renderBlockHighlight(player, event.getPartialTicks());
			
			ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
			PlacementMode mode = iTile.getPlacementMode(stack);
			if (mode.getPreviewMode() == PreviewMode.LINES) {
				BlockPos pos = event.getTarget().getBlockPos();
				IBlockState state = world.getBlockState(pos);
				
				PositionResult position = marked != null ? marked.position.copy() : PlacementHelper.getPosition(world, mc.objectMouseOver, iTile.getPositionContext(stack));
				
				boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
				
				PreviewResult result = PlacementHelper.getPreviews(world, stack, position, isCentered(player, iTile), isFixed(player, iTile), allowLowResolution, marked != null, mode);
				
				if (result != null) {
					processMarkKey(player, iTile, stack, result, result.isAbsolute());
					
					double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
					double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
					double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
					
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.glLineWidth(2.0F);
					GlStateManager.disableTexture2D();
					GlStateManager.depthMask(false);
					
					double x = position.pos.getX();
					double y = position.pos.getY();
					double z = position.pos.getZ();
					
					d0 -= x;
					d1 -= y;
					d2 -= z;
					
					for (int i = 0; i < result.placePreviews.size(); i++) {
						PlacePreviewTile preview = result.placePreviews.get(i);
						List<LittleRenderingCube> cubes = preview.getPreviews(result.context);
						for (LittleRenderingCube cube : cubes) {
							Vec3d color = ColorUtils.IntToVec(cube.color);
							float red;
							float green;
							float blue;
							if (color.x == 1 && color.y == 1 && color.z == 1)
								red = green = blue = 0;
							else {
								red = (float) color.x;
								green = (float) color.y;
								blue = (float) color.z;
							}
							cube.renderCubeLines(-d0, -d1, -d2, red, green, blue, 0.4F);
						}
					}
					
					GlStateManager.depthMask(true);
					GlStateManager.enableTexture2D();
					GlStateManager.disableBlend();
					
					event.setCanceled(true);
				}
			}
		}
	}
}
