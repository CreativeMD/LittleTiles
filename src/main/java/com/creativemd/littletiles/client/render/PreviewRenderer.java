package com.creativemd.littletiles.client.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.ColoredCube;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.LittleTilesConfig;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.api.ISpecialBlockSelector;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.FixedHandler;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.MarkMode;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PreviewResult;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.placing.PlacementMode.SelectionMode;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TextComponentString;
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
	
	public static boolean isCentered(EntityPlayer player)
	{
		return LittleTilesConfig.building.invertStickToGrid == LittleAction.isUsingSecondMode(player) || marked != null;
	}
	
	public static boolean isFixed(EntityPlayer player)
	{
		return LittleTilesConfig.building.invertStickToGrid != LittleAction.isUsingSecondMode(player) && marked == null;
	}
	
	public static void handleUndoAndRedo(EntityPlayer player)
	{
		while (LittleTilesClient.undo.isPressed())
		{
			try {
				LittleAction.undo();
			} catch (LittleActionException e) {
				player.sendStatusMessage(new TextComponentString(e.getLocalizedMessage()), true);
			}
		}
		
		while (LittleTilesClient.redo.isPressed())
		{
			try {
				LittleAction.redo();
			} catch (LittleActionException e) {
				player.sendStatusMessage(new TextComponentString(e.getLocalizedMessage()), true);
			}
		}
	}
	
	@SubscribeEvent
	public void tick(RenderWorldLastEvent event)
	{
		if(mc.player != null && mc.inGameHasFocus)
		{
			World world = mc.world;
			EntityPlayer player = mc.player;
			ItemStack stack = mc.player.getHeldItemMainhand();
			
			handleUndoAndRedo(player);
			
			if(PlacementHelper.isLittleBlock(stack) && (marked != null || (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == Type.BLOCK)))
			{
				if(marked != null)
					marked.renderWorld(event.getPartialTicks());
				
				ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
				
				PositionResult position = marked != null ? marked.position : PlacementHelper.getPosition(world, mc.objectMouseOver, iTile.getPositionContext(stack));
				
				processRotateKeys(position.getContext());
				
	            iTile.tickPreview(player, stack, position, mc.objectMouseOver);
	            
	            PlacementMode mode = iTile.getPlacementMode(stack);
	            
	            if(mode.mode == SelectionMode.PREVIEWS)
	            {
					GL11.glEnable(GL11.GL_BLEND);
		            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		            GL11.glDisable(GL11.GL_TEXTURE_2D);
		            GL11.glDepthMask(false);
		            
		            
		            
		            boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
		            PreviewResult result = PlacementHelper.getPreviews(world, stack, position, isCentered(player), isFixed(player), allowLowResolution, marked != null, mode);
		            boolean absolute = result.isAbsolute();
		            
		            if(result != null)
		            {
		            	//if(!absolute)
		            	processMarkKey(player, iTile, stack, position, result, absolute);
			            
			            double x = (double)position.pos.getX() - TileEntityRendererDispatcher.staticPlayerX;
						double y = (double)position.pos.getY() - TileEntityRendererDispatcher.staticPlayerY;
						double z = (double)position.pos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
			            
			            for (int i = 0; i < result.placePreviews.size(); i++) {
							
							PlacePreviewTile preview = result.placePreviews.get(i);
							List<LittleRenderingCube> cubes = preview.getPreviews(result.context);
							for (LittleRenderingCube cube : cubes) {
								GL11.glPushMatrix();
								cube.renderCubePreview(absolute, x, y, z, iTile);
								GL11.glPopMatrix();
							}
						}
			            
			            if(!absolute && marked == null && LittleAction.isUsingSecondMode(player) && result.singleMode)
			            {
			            	ArrayList<FixedHandler> shifthandlers = new ArrayList<FixedHandler>();
			            	
			            	 for (int i = 0; i < result.placePreviews.size(); i++) 
			            		 if(result.placePreviews.get(i).preview != null)
			            			 shifthandlers.addAll(result.placePreviews.get(i).preview.fixedhandlers);
			            	 
			            	 for (int i = 0; i < shifthandlers.size(); i++) {
			            		//GL11.glPushMatrix();
								shifthandlers.get(i).handleRendering(result.context, mc, x, y, z);
								//GL11.glPopMatrix();
							}
			            }
		            }
		            
		            GL11.glDepthMask(true);
		            GL11.glEnable(GL11.GL_TEXTURE_2D);
		            GL11.glDisable(GL11.GL_BLEND);
	            }
			}else
				marked = null;
		}
	}
	
	public void processMarkKey(EntityPlayer player, ILittleTile iTile, ItemStack stack, PositionResult result, PreviewResult preview, boolean absolute)
	{
		while (LittleTilesClient.mark.isPressed())
		{
			if(marked == null)
				marked = iTile.onMark(player, stack);
			
			if(marked != null && marked.processPosition(player, result.copy(), preview, absolute))
				marked = null;
		}
	}
	
	public void processRotateKey(Rotation rotation)
	{
		LittleRotatePacket packet = new LittleRotatePacket(rotation);
		packet.executeClient(mc.player);
		PacketHandler.sendPacketToServer(packet);
	}
	
	public void processRotateKeys(LittleGridContext context)
	{
		while (LittleTilesClient.flip.isPressed())
		{
			int i4 = MathHelper.floor((double)(this.mc.player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			EnumFacing direction = null;
			switch(i4)
			{
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
			if(mc.player.rotationPitch > 45)
				direction = EnumFacing.DOWN;
			if(mc.player.rotationPitch < -45)
				direction = EnumFacing.UP;
			
			LittleFlipPacket packet = new LittleFlipPacket(direction.getAxis());
			packet.executeClient(mc.player);
			PacketHandler.sendPacketToServer(packet);
		}
		
		//Rotate Block
        while (LittleTilesClient.up.isPressed())
        {
        	if(marked != null)
        		marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? EnumFacing.UP : EnumFacing.EAST);
        	else
        		processRotateKey(Rotation.Z_CLOCKWISE);
        }
        
        while (LittleTilesClient.down.isPressed())
        {
        	if(marked != null)
        		marked.move(context, LittleAction.isUsingSecondMode(mc.player) ? EnumFacing.DOWN : EnumFacing.WEST);
        	else
        		processRotateKey(Rotation.Z_COUNTER_CLOCKWISE);
        }
        
        while (LittleTilesClient.right.isPressed())
        {
        	if(marked != null)
        		marked.move(context, EnumFacing.SOUTH);
        	else
        		processRotateKey(Rotation.Y_COUNTER_CLOCKWISE);
        }
        
        while (LittleTilesClient.left.isPressed())
        {
        	if(marked != null)
        		marked.move(context, EnumFacing.NORTH);
        	else
        		processRotateKey(Rotation.Y_CLOCKWISE);
        }
	}
	
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event)
	{
		EntityPlayer player = event.getPlayer();
		World world = player.world;
		ItemStack stack = player.getHeldItemMainhand();
		if((event.getTarget().typeOfHit == Type.BLOCK || marked != null) && PlacementHelper.isLittleBlock(stack))
		{
			if(marked != null)
				marked.renderBlockHighlight(player, event.getPartialTicks());
			
			ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
			PlacementMode mode = iTile.getPlacementMode(stack);
			if(mode.mode == SelectionMode.LINES)
			{
				BlockPos pos = event.getTarget().getBlockPos();
				IBlockState state = world.getBlockState(pos);
				
				PositionResult position = marked != null ? marked.position : PlacementHelper.getPosition(world, mc.objectMouseOver, iTile.getPositionContext(stack));
				
	            boolean allowLowResolution = marked != null ? marked.allowLowResolution() : true;
	            
	            PreviewResult result = PlacementHelper.getPreviews(world, stack, position, isCentered(player), isFixed(player), allowLowResolution, marked != null, mode);
	            boolean absolute = result.isAbsolute();
	            
	            if(result != null)
	            {
		            double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)event.getPartialTicks();
			        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)event.getPartialTicks();
			        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)event.getPartialTicks();
			        
			        GlStateManager.enableBlend();
		            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		            GlStateManager.glLineWidth(2.0F);
		            GlStateManager.disableTexture2D();
		            GlStateManager.depthMask(false);
		            
		            double x = absolute ? 0 : position.pos.getX();
					double y = absolute ? 0 : position.pos.getY();
					double z = absolute ? 0 : position.pos.getZ();
					
					d0 -= x;
					d1 -= y;
					d2 -= z;
		            
		            for (int i = 0; i < result.placePreviews.size(); i++) {
						PlacePreviewTile preview = result.placePreviews.get(i);
						List<LittleRenderingCube> cubes = preview.getPreviews(result.context);
						for (LittleRenderingCube cube : cubes) {
							RenderGlobal.drawSelectionBoundingBox(cube.getAxis().grow(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
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
