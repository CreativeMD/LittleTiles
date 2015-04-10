package com.creativemd.littletiles.client;

import org.lwjgl.opengl.GL11;

import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PreviewRenderer {
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	@SubscribeEvent
	public void tick(RenderHandEvent event)
	{
		if(mc.thePlayer != null && mc.inGameHasFocus)
		{
			//TODO Add more Items for rendering
			if(mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlockTiles)
			{
				
				double x = (double)mc.objectMouseOver.blockX - TileEntityRendererDispatcher.staticPlayerX;
				double y = (double)mc.objectMouseOver.blockY - TileEntityRendererDispatcher.staticPlayerY;
				double z = (double)mc.objectMouseOver.blockZ - TileEntityRendererDispatcher.staticPlayerZ;
				
				//TODO Clean up this code
				GL11.glEnable(GL11.GL_BLEND);
	            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
	            GL11.glLineWidth(2.0F);
	            GL11.glDisable(GL11.GL_TEXTURE_2D);
	            GL11.glDepthMask(false);
	            
	            PlacementHelper helper = new PlacementHelper(mc.objectMouseOver, mc.thePlayer);	            
	            
	            //Rotate Block
	            //TODO Enhance and clean up rotation code!
	            boolean saveItem = false;
	            if(GameSettings.isKeyDown(LittleTilesClient.up) && !LittleTilesClient.pressedUp)
	            {
	            	helper.tile.size.rotateVec(ForgeDirection.UP);
	            	saveItem = true;
	            	LittleTilesClient.pressedUp = true;
	            }else if(!GameSettings.isKeyDown(LittleTilesClient.up))
	            	LittleTilesClient.pressedUp = false;
	            
	            if(GameSettings.isKeyDown(LittleTilesClient.down) && !LittleTilesClient.pressedDown)
	            {
	            	helper.tile.size.rotateVec(ForgeDirection.DOWN);
	            	saveItem = true;
	            	LittleTilesClient.pressedDown = true;
	            }else if(!GameSettings.isKeyDown(LittleTilesClient.down))
	            	LittleTilesClient.pressedDown = false;
	            
	            if(GameSettings.isKeyDown(LittleTilesClient.right) && !LittleTilesClient.pressedRight)
	            {
	            	byte tempSizeX = helper.tile.size.sizeX;
	            	byte tempSizeY = helper.tile.size.sizeY;
	            	byte tempSizeZ = helper.tile.size.sizeZ;
	            	helper.tile.size.sizeX = tempSizeX;
	            	helper.tile.size.sizeY = tempSizeZ;
	            	helper.tile.size.sizeZ = tempSizeY;
	            	saveItem = true;
	            	LittleTilesClient.pressedRight = true;
	            }else if(!GameSettings.isKeyDown(LittleTilesClient.right))
	            	LittleTilesClient.pressedRight = false;
	            
	            if(GameSettings.isKeyDown(LittleTilesClient.left) && !LittleTilesClient.pressedLeft)
	            {
	            	byte tempSizeX = helper.tile.size.sizeX;
	            	byte tempSizeY = helper.tile.size.sizeY;
	            	byte tempSizeZ = helper.tile.size.sizeZ;
	            	helper.tile.size.sizeX = tempSizeX;
	            	helper.tile.size.sizeY = tempSizeY;
	            	helper.tile.size.sizeZ = tempSizeZ;
	            	saveItem = true;
	            	LittleTilesClient.pressedLeft = true;
	            }else if(!GameSettings.isKeyDown(LittleTilesClient.left))
	            	LittleTilesClient.pressedLeft = false;
	            
	            if(saveItem)
	            {
	            	ItemBlockTiles.saveLittleTile(mc.thePlayer.getHeldItem(), helper.tile);
	            }   
	            
	            Vec3 vec = helper.getCenterPos();
	            LittleTileVec size = helper.getRelativeSize();
	            
				LittleRenderHelper.renderBlock(vec.xCoord - TileEntityRendererDispatcher.staticPlayerX, vec.yCoord - TileEntityRendererDispatcher.staticPlayerY, vec.zCoord - TileEntityRendererDispatcher.staticPlayerZ,
						size.getPosX(), size.getPosY(), size.getPosZ(), 0, 0, 0, 1, 1, 1, Math.sin(System.nanoTime()/200000000D)*0.2+0.5);
				
				GL11.glDepthMask(true);
	            GL11.glEnable(GL11.GL_TEXTURE_2D);
	            GL11.glDisable(GL11.GL_BLEND);
			}
		}
	}
}
