package com.creativemd.littletiles.client.render;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.PlacementHelper.PreviewTile;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.utils.InsideShiftHandler;
import com.creativemd.littletiles.utils.ShiftHandler;
import com.google.common.collect.ForwardingObject;
import com.ibm.icu.text.PluralRules.PluralType;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ibxm.Player;

@SideOnly(Side.CLIENT)
public class PreviewRenderer {
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public static ForgeDirection direction = ForgeDirection.EAST;
	public static ForgeDirection direction2 = ForgeDirection.EAST;
	
	@SubscribeEvent
	public void tick(RenderHandEvent event)
	{
		if(mc.thePlayer != null && mc.inGameHasFocus)
		{
			if(mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK && mc.thePlayer.getHeldItem() != null)
			{
				if(PlacementHelper.isLittleBlock(mc.thePlayer.getHeldItem()))
				{
					PlacementHelper helper = PlacementHelper.getInstance(mc.thePlayer);
					//Rotate Block
		            if(GameSettings.isKeyDown(LittleTilesClient.up) && !LittleTilesClient.pressedUp)
		            {
		            	LittleTilesClient.pressedUp = true;
		            	direction = direction.getRotation(ForgeDirection.NORTH);
		            }else if(!GameSettings.isKeyDown(LittleTilesClient.up))
		            	LittleTilesClient.pressedUp = false;
		            
		            if(GameSettings.isKeyDown(LittleTilesClient.down) && !LittleTilesClient.pressedDown)
		            {
		            	LittleTilesClient.pressedDown = true;
		            	direction = direction.getRotation(ForgeDirection.SOUTH);
		            }else if(!GameSettings.isKeyDown(LittleTilesClient.down))
		            	LittleTilesClient.pressedDown = false;
		            
		            if(GameSettings.isKeyDown(LittleTilesClient.right) && !LittleTilesClient.pressedRight)
		            {
		            	LittleTilesClient.pressedRight = true;
		            	direction2 = direction2.getRotation(ForgeDirection.UP);
		            }else if(!GameSettings.isKeyDown(LittleTilesClient.right))
		            	LittleTilesClient.pressedRight = false;
		            
		            if(GameSettings.isKeyDown(LittleTilesClient.left) && !LittleTilesClient.pressedLeft)
		            {
		            	LittleTilesClient.pressedLeft = true;
		            	direction2 = direction2.getRotation(ForgeDirection.DOWN);
		            }else if(!GameSettings.isKeyDown(LittleTilesClient.left))
		            	LittleTilesClient.pressedLeft = false;
					
					int posX = mc.objectMouseOver.blockX;
					int posY = mc.objectMouseOver.blockY;
					int posZ = mc.objectMouseOver.blockZ;
					
					double x = (double)posX - TileEntityRendererDispatcher.staticPlayerX;
					//if(posX < 0)
						//x--;
					double y = (double)posY - TileEntityRendererDispatcher.staticPlayerY;
					//if(posY < 0)
						//y--;
					double z = (double)posZ - TileEntityRendererDispatcher.staticPlayerZ;
					//if(posZ < 0)
						//z--;
					
					ForgeDirection side = ForgeDirection.getOrientation(mc.objectMouseOver.sideHit);
					if(!helper.canBePlacedInside(posX, posY, posZ, mc.objectMouseOver.hitVec, side))
					{
						switch(side)
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
					GL11.glEnable(GL11.GL_BLEND);
		            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		            GL11.glLineWidth(2.0F);
		            GL11.glDisable(GL11.GL_TEXTURE_2D);
		            GL11.glDepthMask(false);
		            
		            ArrayList<PreviewTile> previews = helper.getPreviewTiles(mc.thePlayer.getHeldItem(), mc.objectMouseOver, direction, direction2);
		            
		            for (int i = 0; i < previews.size(); i++) {
						GL11.glPushMatrix();
						PreviewTile preview = previews.get(i);
						CubeObject cube = preview.box.getCube();
						LittleTileSize size = preview.box.getSize();
						double cubeX = x+cube.minX+size.getPosX()/2D;
						//if(posX < 0 && side != ForgeDirection.WEST && side != ForgeDirection.EAST)
							//cubeX = x+(1-cube.minX)+size.getPosX()/2D;
						double cubeY = y+cube.minY+size.getPosY()/2D;
						//if(posY < 0 && side != ForgeDirection.DOWN)
							//cubeY = y-cube.minY+size.getPosY()/2D;
						double cubeZ = z+cube.minZ+size.getPosZ()/2D;
						//if(posZ < 0 && side != ForgeDirection.NORTH)
							//cubeZ = z-cube.minZ+size.getPosZ()/2D;
						/*double cubeX = x;
						if(posX < 0)
							x -= 1;
						double cubeY = y;
						double cubeZ = z;*/
						RenderHelper3D.renderBlock(cubeX, cubeY, cubeZ, size.getPosX(), size.getPosY(), size.getPosZ(), 0, 0, 0, 1, 1, 1, Math.sin(System.nanoTime()/200000000D)*0.2+0.5);
						GL11.glPopMatrix();
					}
		            
		            if(mc.thePlayer.isSneaking())
		            {
		            	ArrayList<ShiftHandler> shifthandlers = new ArrayList<ShiftHandler>();
		            	
		            	 for (int i = 0; i < previews.size(); i++) 
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
			}
		}
	}
}
