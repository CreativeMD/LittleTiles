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
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.PlacementHelper;
import com.creativemd.littletiles.common.tiles.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.tiles.PlacementHelper.PreviewResult;
import com.creativemd.littletiles.common.tiles.place.FixedHandler;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PreviewRenderer {
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public static PositionResult markedPosition = null;
	
	public static boolean isCentered(EntityPlayer player)
	{
		return LittleTiles.invertedShift == player.isSneaking() || markedPosition != null;
	}
	
	public static boolean isFixed(EntityPlayer player)
	{
		return LittleTiles.invertedShift != player.isSneaking() && markedPosition == null;
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
			
			if(PlacementHelper.isLittleBlock(stack) && (markedPosition != null || (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == Type.BLOCK)))
			{
				PositionResult position = markedPosition != null ? markedPosition : PlacementHelper.getPosition(world, mc.objectMouseOver);
				
				processRoateKeys();
	            
				GL11.glEnable(GL11.GL_BLEND);
	            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
	            GL11.glDisable(GL11.GL_TEXTURE_2D);
	            GL11.glDepthMask(false);
	            
	            ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
	            iTile.tickPreview(player, stack, position, mc.objectMouseOver);
	            
	            boolean absolute = iTile.arePreviewsAbsolute();
	            
	            PreviewResult result = null;
	            if(absolute)
	            {
	            	result = new PreviewResult();
	            	List<LittleTilePreview> tiles = iTile.getLittlePreview(stack, true);
	            	for (int i = 0; i < tiles.size(); i++) {
	            		result.placePreviews.add(tiles.get(i).getPlaceableTile(null, true, null));
					}	            	
	            }else
	            	result = PlacementHelper.getPreviews(world, stack, position, isCentered(player), isFixed(player), true);
	            
	            if(result != null)
	            {
	            	//if(!absolute)
	            	processMarkKey(player, position, result, absolute);
		            
		            double x = (double)position.pos.getX() - TileEntityRendererDispatcher.staticPlayerX;
					double y = (double)position.pos.getY() - TileEntityRendererDispatcher.staticPlayerY;
					double z = (double)position.pos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
		            
		            for (int i = 0; i < result.placePreviews.size(); i++) {
						
						PlacePreviewTile preview = result.placePreviews.get(i);
						List<LittleRenderingCube> cubes = preview.getPreviews();
						for (LittleRenderingCube cube : cubes) {
							GL11.glPushMatrix();
							cube.renderCubePreview(absolute, x, y, z, iTile);
							GL11.glPopMatrix();
						}
					}
		            
		            if(!absolute && markedPosition == null && player.isSneaking() && result.singleMode)
		            {
		            	ArrayList<FixedHandler> shifthandlers = new ArrayList<FixedHandler>();
		            	
		            	 for (int i = 0; i < result.placePreviews.size(); i++) 
		            		 if(result.placePreviews.get(i).preview != null)
		            			 shifthandlers.addAll(result.placePreviews.get(i).preview.fixedhandlers);
		            	 
		            	 for (int i = 0; i < shifthandlers.size(); i++) {
		            		//GL11.glPushMatrix();
							shifthandlers.get(i).handleRendering(mc, x, y, z);
							//GL11.glPopMatrix();
						}
		            }
	            }
	            
	            GL11.glDepthMask(true);
	            GL11.glEnable(GL11.GL_TEXTURE_2D);
	            GL11.glDisable(GL11.GL_BLEND);
			}else
				markedPosition = null;
		}
	}
	
	public void processMarkKey(EntityPlayer player, PositionResult result, PreviewResult preview, boolean absolute)
	{
		while (LittleTilesClient.mark.isPressed())
		{
			if(markedPosition == null)
			{
				boolean centered = isCentered(player);
				markedPosition = result.copy();
				if(!absolute)
				{
					markedPosition.hit = preview.box.getCenter();
					
					LittleTileVec center = preview.size.calculateCenter();
					LittleTileVec centerInv = preview.size.calculateInvertedCenter();
					
					switch(result.facing)
					{
					case EAST:
						markedPosition.hit.x -= center.x;
						break;
					case WEST:
						markedPosition.hit.x += centerInv.x;
						break;
					case UP:
						markedPosition.hit.y -= center.y;
						break;
					case DOWN:
						markedPosition.hit.y += centerInv.y;
						break;
					case SOUTH:
						markedPosition.hit.z -= center.z;
						break;
					case NORTH:
						markedPosition.hit.z += centerInv.z;
						break;
					default:
						break;
					}
					
					if(!preview.singleMode && preview.placedFixed)
					{
						markedPosition.hit.sub(preview.offset);
					}
				}
			}
			else
				markedPosition = null;
		}
	}
	
	public void processRotateKey(Rotation rotation)
	{
		LittleRotatePacket packet = new LittleRotatePacket(rotation);
		packet.executeClient(mc.player);
		PacketHandler.sendPacketToServer(packet);
	}
	
	public void processRoateKeys()
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
        	if(markedPosition != null)
        		moveMarkedHit(mc.player.isSneaking() ? EnumFacing.UP : EnumFacing.EAST);
        	else
        		processRotateKey(Rotation.Z_CLOCKWISE);
        }
        
        while (LittleTilesClient.down.isPressed())
        {
        	if(markedPosition != null)
        		moveMarkedHit(mc.player.isSneaking() ? EnumFacing.DOWN : EnumFacing.WEST);
        	else
        		processRotateKey(Rotation.Z_COUNTER_CLOCKWISE);
        }
        
        while (LittleTilesClient.right.isPressed())
        {
        	if(markedPosition != null)
        		moveMarkedHit(EnumFacing.SOUTH);
        	else
        		processRotateKey(Rotation.Y_COUNTER_CLOCKWISE);
        }
        
        while (LittleTilesClient.left.isPressed())
        {
        	if(markedPosition != null)
        		moveMarkedHit(EnumFacing.NORTH);
        	else
        		processRotateKey(Rotation.Y_CLOCKWISE);
        }
	}

	private void moveMarkedHit(EnumFacing facing)
	{
		LittleTileVec vec = new LittleTileVec(facing);
		vec.scale(GuiScreen.isCtrlKeyDown() ? LittleTile.gridSize : 1);
		markedPosition.subVec(vec);
	}
}
