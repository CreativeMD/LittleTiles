package com.creativemd.littletiles.client.render;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PreviewTile;
import com.creativemd.littletiles.utils.ShiftHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.enchantment.Enchantment.Rarity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PreviewRenderer {
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	//public static ForgeDirection direction = ForgeDirection.UP;
	//public static ForgeDirection direction2 = ForgeDirection.EAST;
	
	/*public static void updateVertical()
	{
		if(direction == ForgeDirection.UNKNOWN)
			direction = ForgeDirection.DOWN;
		else if(direction == ForgeDirection.DOWN)
			direction = ForgeDirection.UP;
		else
			direction = ForgeDirection.UNKNOWN;
	}
	
	public static void updateHorizontal()
	{
		if(direction2 == ForgeDirection.WEST || direction2 == ForgeDirection.EAST)
			direction2 = ForgeDirection.NORTH;
		else
			direction2 = ForgeDirection.EAST;
	}*/
	
	public void processKey(EnumFacing direction)
	{
		LittleRotatePacket packet = new LittleRotatePacket(direction);
		packet.executeClient(mc.thePlayer);
		PacketHandler.sendPacketToServer(packet);
	}
	
	public static RayTraceResult markedHit = null;
	
	public static void moveMarkedHit(EnumFacing direction)
	{
		int posX = (int) markedHit.hitVec.xCoord;
		int posY = (int) markedHit.hitVec.yCoord;
		int posZ = (int) markedHit.hitVec.zCoord;
		double move = LittleTile.gridMCLength;
		if(GuiScreen.isCtrlKeyDown())
			move = 1;
		switch (direction) {
		case EAST:
			markedHit.hitVec = markedHit.hitVec.addVector(move, 0, 0);
			break;
		case WEST:
			markedHit.hitVec = markedHit.hitVec.addVector(-move, 0, 0);
			break;
		case UP:
			markedHit.hitVec = markedHit.hitVec.addVector(0, move, 0);
			break;
		case DOWN:
			markedHit.hitVec = markedHit.hitVec.addVector(0, -move, 0);
			break;
		case SOUTH:
			markedHit.hitVec = markedHit.hitVec.addVector(0, 0, move);
			break;
		case NORTH:
			markedHit.hitVec = markedHit.hitVec.addVector(0, 0, -move);
			break;
		default:
			break;
		}
		int blockX = markedHit.getBlockPos().getX();
		int blockY = markedHit.getBlockPos().getY();
		int blockZ = markedHit.getBlockPos().getZ();
		if(posX != (int) markedHit.hitVec.xCoord)
			blockX += ((int) markedHit.hitVec.xCoord) - posX;
		if(posY != (int) markedHit.hitVec.yCoord)
			blockY += ((int) markedHit.hitVec.yCoord) - posY;
		if(posZ != (int) markedHit.hitVec.zCoord)
			blockZ += ((int) markedHit.hitVec.zCoord) - posZ;
		
		markedHit = new RayTraceResult(markedHit.hitVec, markedHit.sideHit, new BlockPos(blockX, blockY, blockZ));
	}
	
	@SubscribeEvent
	public void tick(RenderWorldLastEvent event)
	{
		if(mc.thePlayer != null && mc.inGameHasFocus)
		{
			//mc.theWorld
			if(PlacementHelper.isLittleBlock(mc.thePlayer.getHeldItem(EnumHand.MAIN_HAND)))
			{
				if(GameSettings.isKeyDown(LittleTilesClient.flip) && !LittleTilesClient.pressedFlip)
				{
					LittleTilesClient.pressedFlip = true;
					int i4 = MathHelper.floor_double((double)(this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
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
					if(mc.thePlayer.rotationPitch > 45)
						direction = EnumFacing.DOWN;
					if(mc.thePlayer.rotationPitch < -45)
						direction = EnumFacing.UP;
					//System.out.println("f: " + i4 + ", pitch: " + mc.thePlayer.rotationPitch + ", direction: " + direction);
					LittleFlipPacket packet = new LittleFlipPacket(direction);
					packet.executeClient(mc.thePlayer);
					PacketHandler.sendPacketToServer(packet);
				}else if(!GameSettings.isKeyDown(LittleTilesClient.flip)){
					LittleTilesClient.pressedFlip = false;
				}
				
				RayTraceResult look = mc.objectMouseOver;
				if(markedHit != null)
					look = markedHit;
				
				if(look != null && look.typeOfHit == RayTraceResult.Type.BLOCK && mc.thePlayer.getHeldItem(EnumHand.MAIN_HAND) != null)
				{
					PlacementHelper helper = PlacementHelper.getInstance(mc.thePlayer);
					
					int posX = look.getBlockPos().getX();
					int posY = look.getBlockPos().getY();
					int posZ = look.getBlockPos().getZ();
					
					double x = (double)posX - TileEntityRendererDispatcher.staticPlayerX;
					double y = (double)posY - TileEntityRendererDispatcher.staticPlayerY;
					double z = (double)posZ - TileEntityRendererDispatcher.staticPlayerZ;
					
					if(!helper.canBePlacedInside(look.getBlockPos(), look.hitVec, look.sideHit))
					{
						switch(look.sideHit)
						{
						case EAST:
							x++;
							break;
						case WEST:
							x--;
							break;
						case UP:
							y++;
							break;
						case DOWN:
							y--;
							break;
						case SOUTH:
							z++;
							break;
						case NORTH:
							z--;
							break;
						default:
							break;
						}
					}
					
					if(GameSettings.isKeyDown(LittleTilesClient.mark) && !LittleTilesClient.pressedMark)
					{
						LittleTilesClient.pressedMark = true;
						if(markedHit == null)
						{							
							
							LittleTileVec vec = helper.getHitVec(look.hitVec, look.getBlockPos(), look.sideHit, false, false);
							Vec3d hitVec = new Vec3d(vec.getPosX(), vec.getPosY(), vec.getPosZ());
							
							int newX = look.getBlockPos().getX();
							int newY = look.getBlockPos().getY();
							int newZ = look.getBlockPos().getZ();
							if(!helper.canBePlacedInside(look.getBlockPos(), look.hitVec, look.sideHit))
							{
								switch(look.sideHit)
								{
								case EAST:
									newX++;
									break;
								case WEST:
									newX--;
									break;
								case UP:
									newY++;
									break;
								case DOWN:
									newY--;
									break;
								case SOUTH:
									newZ++;
									break;
								case NORTH:
									newZ--;
									break;
								default:
									break;
								}
							}
							hitVec = hitVec.addVector(newX, newY, newZ);
							look = markedHit = new RayTraceResult(hitVec, look.sideHit, look.getBlockPos());
							return ;
						}
						else
							markedHit = null;
					}else if(!GameSettings.isKeyDown(LittleTilesClient.mark)){
						LittleTilesClient.pressedMark = false;
					}
					
					//direction = ForgeDirection.UP;
					//direction2 = ForgeDirection.EAST;
					
					//Rotate Block
		            if(GameSettings.isKeyDown(LittleTilesClient.up) && !LittleTilesClient.pressedUp)
		            {
		            	LittleTilesClient.pressedUp = true;
		            	if(markedHit != null)
		            		moveMarkedHit(mc.thePlayer.isSneaking() ? EnumFacing.UP : EnumFacing.EAST);
		            	else
		            		processKey(EnumFacing.UP);
		            }else if(!GameSettings.isKeyDown(LittleTilesClient.up))
		            	LittleTilesClient.pressedUp = false;
		            
		            if(GameSettings.isKeyDown(LittleTilesClient.down) && !LittleTilesClient.pressedDown)
		            {
		            	LittleTilesClient.pressedDown = true;
		            	if(markedHit != null)
		            		moveMarkedHit(mc.thePlayer.isSneaking() ? EnumFacing.DOWN : EnumFacing.WEST);
		            	else
		            		processKey(EnumFacing.DOWN);
		            }else if(!GameSettings.isKeyDown(LittleTilesClient.down))
		            	LittleTilesClient.pressedDown = false;
		            
		            if(GameSettings.isKeyDown(LittleTilesClient.right) && !LittleTilesClient.pressedRight)
		            {
		            	LittleTilesClient.pressedRight = true;
		            	if(markedHit != null)
		            		moveMarkedHit(EnumFacing.SOUTH);
		            	else
		            		processKey(EnumFacing.SOUTH);
		            }else if(!GameSettings.isKeyDown(LittleTilesClient.right))
		            	LittleTilesClient.pressedRight = false;
		            
		            if(GameSettings.isKeyDown(LittleTilesClient.left) && !LittleTilesClient.pressedLeft)
		            {
		            	LittleTilesClient.pressedLeft = true;
		            	if(markedHit != null)
		            		moveMarkedHit(EnumFacing.NORTH);
		            	else
		            		processKey(EnumFacing.NORTH);
		            }else if(!GameSettings.isKeyDown(LittleTilesClient.left))
		            	LittleTilesClient.pressedLeft = false;
		            
					GL11.glEnable(GL11.GL_BLEND);
		            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		            GL11.glLineWidth(2.0F);
		            GL11.glDisable(GL11.GL_TEXTURE_2D);
		            GL11.glDepthMask(false);
		            
		            ArrayList<PreviewTile> previews = null;
		            
		            previews = helper.getPreviewTiles(mc.thePlayer.getHeldItem(EnumHand.MAIN_HAND), look, markedHit != null); //, direction, direction2);
		            
		            for (int i = 0; i < previews.size(); i++) {
						GL11.glPushMatrix();
						PreviewTile preview = previews.get(i);
						LittleTileBox previewBox = preview.getPreviewBox();
						CubeObject cube = previewBox.getCube();
						Vec3d size = previewBox.getSizeD();
						double cubeX = x+cube.minX+size.xCoord/2D;
						//if(posX < 0 && side != ForgeDirection.WEST && side != ForgeDirection.EAST)
							//cubeX = x+(1-cube.minX)+size.getPosX()/2D;
						double cubeY = y+cube.minY+size.yCoord/2D;
						//if(posY < 0 && side != ForgeDirection.DOWN)
							//cubeY = y-cube.minY+size.getPosY()/2D;
						double cubeZ = z+cube.minZ+size.zCoord/2D;
						//if(posZ < 0 && side != ForgeDirection.NORTH)
							//cubeZ = z-cube.minZ+size.getPosZ()/2D;
						/*double cubeX = x;
						if(posX < 0)
							x -= 1;
						double cubeY = y;
						double cubeZ = z;*/
						Vec3d color = preview.getPreviewColor();
						RenderHelper3D.renderBlock(cubeX, cubeY, cubeZ, size.xCoord, size.yCoord, size.zCoord, 0, 0, 0, color.xCoord, color.yCoord, color.zCoord, Math.sin(System.nanoTime()/200000000D)*0.2+0.5);
						GL11.glPopMatrix();
					}
		            
		            if(markedHit == null && mc.thePlayer.isSneaking())
		            {
		            	ArrayList<ShiftHandler> shifthandlers = new ArrayList<ShiftHandler>();
		            	
		            	 for (int i = 0; i < previews.size(); i++) 
		            		 if(previews.get(i).preview != null)
		            			 shifthandlers.addAll(previews.get(i).preview.shifthandlers);
		            	 
		            	 for (int i = 0; i < shifthandlers.size(); i++) {
		            		//GL11.glPushMatrix();
							shifthandlers.get(i).handleRendering(mc, x, y, z);
							//GL11.glPopMatrix();
						}
		            }
		            
		            GL11.glDepthMask(true);
		            GL11.glEnable(GL11.GL_TEXTURE_2D);
		            GL11.glDisable(GL11.GL_BLEND);
				}
			}else
				markedHit = null;
		}
	}
}
