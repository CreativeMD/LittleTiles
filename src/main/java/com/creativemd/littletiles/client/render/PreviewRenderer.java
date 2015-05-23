package com.creativemd.littletiles.client.render;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileSize;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;
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
					PlacementHelper helper = new PlacementHelper(mc.objectMouseOver, mc.thePlayer);	  
					
					double x = (double)helper.moving.blockX - TileEntityRendererDispatcher.staticPlayerX;
					double y = (double)helper.moving.blockY - TileEntityRendererDispatcher.staticPlayerY;
					double z = (double)helper.moving.blockZ - TileEntityRendererDispatcher.staticPlayerZ;
					
					GL11.glEnable(GL11.GL_BLEND);
		            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		            GL11.glLineWidth(2.0F);
		            GL11.glDisable(GL11.GL_TEXTURE_2D);
		            GL11.glDepthMask(false);          
		            
		            
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
		            
		            LittleTileSize size = helper.size;
		            size.rotateSize(direction);
		            size.rotateSize(direction2);
		            Vec3 vec = helper.getCenterPos(size);
		            
		            if(!helper.isSingle())
		            {
		            	helper.rotateTiles(direction);
		            	helper.rotateTiles(direction2);
			            for (int i = 0; i < helper.preview.size(); i++) {
							LittleTilePreview tile = helper.preview.get(i);
							Vec3 offset = helper.getOffset(i, size);
							
							double posX = vec.xCoord+offset.xCoord;
							double posY = vec.yCoord+offset.yCoord;
							double posZ = vec.zCoord+offset.zCoord;
							
							RenderHelper3D.renderBlock(posX - TileEntityRendererDispatcher.staticPlayerX, posY - TileEntityRendererDispatcher.staticPlayerY, posZ - TileEntityRendererDispatcher.staticPlayerZ,
									tile.size.getPosX(), tile.size.getPosY(), tile.size.getPosZ(), 0, 0, 0, 1, 1, 1, Math.sin(System.nanoTime()/200000000D)*0.2+0.5);
						}
		            
		           
		            	RenderHelper3D.renderBlock(vec.xCoord - TileEntityRendererDispatcher.staticPlayerX, vec.yCoord - TileEntityRendererDispatcher.staticPlayerY, vec.zCoord - TileEntityRendererDispatcher.staticPlayerZ,
							size.getPosX(), size.getPosY(), size.getPosZ(), 0, 0, 0, 1, 1, 1, 0.12);
		            }
		            else{
		            	LittleTilePreview tile = helper.preview.get(0);
		            	RenderHelper3D.renderBlock(vec.xCoord - TileEntityRendererDispatcher.staticPlayerX, vec.yCoord - TileEntityRendererDispatcher.staticPlayerY, vec.zCoord - TileEntityRendererDispatcher.staticPlayerZ,
								tile.size.getPosX(), tile.size.getPosY(), tile.size.getPosZ(), 0, 0, 0, 1, 1, 1, Math.sin(System.nanoTime()/200000000D)*0.2+0.5);
		            }
		            	
					
					GL11.glDepthMask(true);
		            GL11.glEnable(GL11.GL_TEXTURE_2D);
		            GL11.glDisable(GL11.GL_BLEND);
				}
			}
		}
	}
}
