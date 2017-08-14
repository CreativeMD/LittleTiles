package com.creativemd.littletiles.client.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.ColoredCube;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
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
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
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
	
	@SubscribeEvent
	public void tick(RenderWorldLastEvent event)
	{
		if(mc.player != null && mc.inGameHasFocus)
		{
			World world = mc.world;
			EntityPlayer player = mc.player;
			ItemStack stack = mc.player.getHeldItemMainhand();
			
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
	            iTile.tickPreview(player, stack, position);
	            
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
	            	if(!absolute)
	            		processMarkKey(player, position, result);
		            
		            double x = (double)position.pos.getX() - TileEntityRendererDispatcher.staticPlayerX;
					double y = (double)position.pos.getY() - TileEntityRendererDispatcher.staticPlayerY;
					double z = (double)position.pos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
		            
		            for (int i = 0; i < result.placePreviews.size(); i++) {
						
						PlacePreviewTile preview = result.placePreviews.get(i);
						ArrayList<ColoredCube> cubes = preview.getPreviews();
						for (int j = 0; j < cubes.size(); j++) {
							GL11.glPushMatrix();
							ColoredCube cube = cubes.get(j);
							Vec3d size = cube.getSize();
							double cubeX = x+cube.minX+size.x/2D;
							if(absolute)
								cubeX -= x+TileEntityRendererDispatcher.staticPlayerX;
							
							double cubeY = y+cube.minY+size.y/2D;
							if(absolute)
								cubeY -= y+TileEntityRendererDispatcher.staticPlayerY;
							
							double cubeZ = z+cube.minZ+size.z/2D;
							if(absolute)
								cubeZ -= z+TileEntityRendererDispatcher.staticPlayerZ;
							
							Vec3d color = ColorUtils.IntToVec(cube.color);
							RenderHelper3D.renderBlock(cubeX, cubeY, cubeZ, size.x, size.y, size.z, 0, 0, 0, color.x, color.y, color.z, (Math.sin(System.nanoTime()/200000000D)*0.2+0.5) * iTile.getPreviewAlphaFactor());
							GL11.glPopMatrix();
						}
					}
		            
		            if(!absolute && markedPosition == null && player.isSneaking() && result.usedSize)
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
	
	public void processMarkKey(EntityPlayer player, PositionResult result, PreviewResult preview)
	{
		if(GameSettings.isKeyDown(LittleTilesClient.mark) && !LittleTilesClient.pressedMark)
		{
			LittleTilesClient.pressedMark = true;
			if(markedPosition == null)
			{
				boolean centered = isCentered(player);
				markedPosition = result.copy();
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
				
				if(!preview.usedSize && preview.placedFixed)
				{
					markedPosition.hit.subVec(preview.offset);
				}
			}
			else
				markedPosition = null;
		}else if(!GameSettings.isKeyDown(LittleTilesClient.mark)){
			LittleTilesClient.pressedMark = false;
		}
	}
	
	public void processRotateKey(EnumFacing direction)
	{
		LittleRotatePacket packet = new LittleRotatePacket(direction);
		packet.executeClient(mc.player);
		PacketHandler.sendPacketToServer(packet);
	}
	
	public void processRoateKeys()
	{
		if(GameSettings.isKeyDown(LittleTilesClient.flip) && !LittleTilesClient.pressedFlip)
		{
			LittleTilesClient.pressedFlip = true;
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
			
			LittleFlipPacket packet = new LittleFlipPacket(direction);
			packet.executeClient(mc.player);
			PacketHandler.sendPacketToServer(packet);
		}else if(!GameSettings.isKeyDown(LittleTilesClient.flip)){
			LittleTilesClient.pressedFlip = false;
		}
		
		//Rotate Block
        if(GameSettings.isKeyDown(LittleTilesClient.up) && !LittleTilesClient.pressedUp)
        {
        	LittleTilesClient.pressedUp = true;
        	if(markedPosition != null)
        		moveMarkedHit(mc.player.isSneaking() ? EnumFacing.UP : EnumFacing.EAST);
        	else
        		processRotateKey(EnumFacing.UP);
        }else if(!GameSettings.isKeyDown(LittleTilesClient.up))
        	LittleTilesClient.pressedUp = false;
        
        if(GameSettings.isKeyDown(LittleTilesClient.down) && !LittleTilesClient.pressedDown)
        {
        	LittleTilesClient.pressedDown = true;
        	if(markedPosition != null)
        		moveMarkedHit(mc.player.isSneaking() ? EnumFacing.DOWN : EnumFacing.WEST);
        	else
        		processRotateKey(EnumFacing.DOWN);
        }else if(!GameSettings.isKeyDown(LittleTilesClient.down))
        	LittleTilesClient.pressedDown = false;
        
        if(GameSettings.isKeyDown(LittleTilesClient.right) && !LittleTilesClient.pressedRight)
        {
        	LittleTilesClient.pressedRight = true;
        	if(markedPosition != null)
        		moveMarkedHit(EnumFacing.SOUTH);
        	else
        		processRotateKey(EnumFacing.SOUTH);
        }else if(!GameSettings.isKeyDown(LittleTilesClient.right))
        	LittleTilesClient.pressedRight = false;
        
        if(GameSettings.isKeyDown(LittleTilesClient.left) && !LittleTilesClient.pressedLeft)
        {
        	LittleTilesClient.pressedLeft = true;
        	if(markedPosition != null)
        		moveMarkedHit(EnumFacing.NORTH);
        	else
        		processRotateKey(EnumFacing.NORTH);
        }else if(!GameSettings.isKeyDown(LittleTilesClient.left))
        	LittleTilesClient.pressedLeft = false;
	}

	private void moveMarkedHit(EnumFacing facing)
	{
		LittleTileVec vec = new LittleTileVec(facing);
		vec.scale(GuiScreen.isCtrlKeyDown() ? LittleTile.gridSize : 1);
		markedPosition.subVec(vec);
	}
}
