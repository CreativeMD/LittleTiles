package com.creativemd.littletiles.client.render.overlay;

import java.util.List;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.MarkMode;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementMode.PreviewMode;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.creativemd.littletiles.common.util.place.PlacementPreview;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
	
	public static final ResourceLocation WHITE_TEXTURE = new ResourceLocation(LittleTiles.modid, "textures/preview.png");
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public static MarkMode marked = null;
	
	public static boolean isCentered(EntityPlayer player, ILittleTile iTile) {
		if (iTile.snapToGridByDefault())
			return LittleAction.isUsingSecondMode(player) && marked == null;
		return LittleTiles.CONFIG.building.invertStickToGrid == LittleAction.isUsingSecondMode(player) || marked != null;
	}
	
	public static boolean isFixed(EntityPlayer player, ILittleTile iTile) {
		if (iTile.snapToGridByDefault())
			return !LittleAction.isUsingSecondMode(player) && marked == null;
		return LittleTiles.CONFIG.building.invertStickToGrid != LittleAction.isUsingSecondMode(player) && marked == null;
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
				
				PlacementPosition position = marked != null ? marked.position.copy() : PlacementHelper.getPosition(world, mc.objectMouseOver, iTile.getPositionContext(stack), iTile, stack);
				
				processRotateKeys(stack, position.getContext());
				iTile.tickPreview(player, stack, position, mc.objectMouseOver);
				
				PlacementMode mode = iTile.getPlacementMode(stack);
				
				if (mode.getPreviewMode() == PreviewMode.PREVIEWS) {
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
					GlStateManager.enableTexture2D();
					mc.renderEngine.bindTexture(WHITE_TEXTURE);
					GlStateManager.depthMask(false);
					
					boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
					PlacementPreview result = PlacementHelper.getPreviews(world, stack, position, isCentered(player, iTile), isFixed(player, iTile), allowLowResolution, marked != null, mode);
					
					if (result != null) {
						processMarkKey(player, iTile, stack, result);
						double x = position.getPos().getX() - TileEntityRendererDispatcher.staticPlayerX;
						double y = position.getPos().getY() - TileEntityRendererDispatcher.staticPlayerY;
						double z = position.getPos().getZ() - TileEntityRendererDispatcher.staticPlayerZ;
						
						List<PlacePreview> placePreviews = result.getPreviews();
						
						for (int i = 0; i < placePreviews.size(); i++) {
							PlacePreview preview = placePreviews.get(i);
							List<LittleRenderingCube> cubes = preview.getPreviews(result.context);
							for (LittleRenderingCube cube : cubes) {
								GlStateManager.pushMatrix();
								cube.renderCubePreview(x, y, z, iTile);
								GlStateManager.popMatrix();
							}
						}
						
						if (position.positingCubes != null)
							for (LittleRenderingCube cube : position.positingCubes) {
								GlStateManager.pushMatrix();
								cube.renderCubePreview(x, y, z, iTile);
								GlStateManager.popMatrix();
							}
					}
					
					GlStateManager.depthMask(true);
					GlStateManager.enableTexture2D();
					GlStateManager.disableBlend();
				}
			} else
				marked = null;
		}
	}
	
	public void processMarkKey(EntityPlayer player, ILittleTile iTile, ItemStack stack, PlacementPreview preview) {
		while (LittleTilesClient.mark.isPressed()) {
			if (marked == null)
				marked = iTile.onMark(player, stack);
			
			if (marked != null && marked.processPosition(player, PlacementHelper.getPosition(player.world, mc.objectMouseOver, iTile.getPositionContext(stack), iTile, stack), preview))
				marked = null;
		}
	}
	
	public void processRotateKeys(ItemStack stack, LittleGridContext context) {
		while (LittleTilesClient.flip.isPressed())
			LittleEventHandler.processFlipKey(mc.player, stack);
		
		boolean repeated = marked != null;
		
		// Rotate Block
		while (LittleTilesClient.up.isPressed(repeated)) {
			if (marked != null)
				marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? EnumFacing.UP : EnumFacing.EAST);
			else
				LittleEventHandler.processRotateKey(mc.player, Rotation.Z_CLOCKWISE, stack);
		}
		
		while (LittleTilesClient.down.isPressed(repeated)) {
			if (marked != null)
				marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? EnumFacing.DOWN : EnumFacing.WEST);
			else
				LittleEventHandler.processRotateKey(mc.player, Rotation.Z_COUNTER_CLOCKWISE, stack);
		}
		
		while (LittleTilesClient.right.isPressed(repeated)) {
			if (marked != null)
				marked.move(context, EnumFacing.SOUTH);
			else
				LittleEventHandler.processRotateKey(mc.player, Rotation.Y_COUNTER_CLOCKWISE, stack);
		}
		
		while (LittleTilesClient.left.isPressed(repeated)) {
			if (marked != null)
				marked.move(context, EnumFacing.NORTH);
			else
				LittleEventHandler.processRotateKey(mc.player, Rotation.Y_CLOCKWISE, stack);
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
				
				PlacementPosition position = marked != null ? marked.position.copy() : PlacementHelper.getPosition(world, mc.objectMouseOver, iTile.getPositionContext(stack), iTile, stack);
				
				boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
				
				PlacementPreview result = PlacementHelper.getPreviews(world, stack, position, isCentered(player, iTile), isFixed(player, iTile), allowLowResolution, marked != null, mode);
				
				if (result != null) {
					processMarkKey(player, iTile, stack, result);
					
					double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
					double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
					double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
					
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.glLineWidth(2.0F);
					GlStateManager.enableTexture2D();
					mc.renderEngine.bindTexture(WHITE_TEXTURE);
					GlStateManager.depthMask(false);
					
					double x = position.getPos().getX();
					double y = position.getPos().getY();
					double z = position.getPos().getZ();
					
					d0 -= x;
					d1 -= y;
					d2 -= z;
					
					List<PlacePreview> placePreviews = result.getPreviews();
					for (int i = 0; i < placePreviews.size(); i++) {
						PlacePreview preview = placePreviews.get(i);
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
					
					if (position.positingCubes != null)
						for (LittleRenderingCube cube : position.positingCubes) {
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
					
					GlStateManager.depthMask(true);
					GlStateManager.enableTexture2D();
					GlStateManager.disableBlend();
					
					event.setCanceled(true);
				}
			}
		}
	}
}
