package com.creativemd.littletiles.client;

import org.lwjgl.opengl.GL11;

import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
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
			if(mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) //mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlockTiles && mc.objectMouseOver != null)
			{
				double x = (double)mc.objectMouseOver.blockX - TileEntityRendererDispatcher.staticPlayerX;
				double y = (double)mc.objectMouseOver.blockY - TileEntityRendererDispatcher.staticPlayerY;
				double z = (double)mc.objectMouseOver.blockZ - TileEntityRendererDispatcher.staticPlayerZ;
				
				//TODO Clean up this code
				//GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_BLEND);
	            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
	            GL11.glLineWidth(2.0F);
	            GL11.glDisable(GL11.GL_TEXTURE_2D);
	            GL11.glDepthMask(false);
				//GL11.glColor4f(1.0f,1.0f,1.0f,1.0f);
				//GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_EMISSION);

				//GL11.glEnable(GL11.GL_COLOR_MATERIAL);
				//GL11.glCOlor
				//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	            PlacementHelper helper = new PlacementHelper(mc.objectMouseOver, mc.thePlayer);
	            LittleTileVec size = new LittleTileVec(5, 3, 1);
	            Vec3 vec = helper.getLookingPos();
	            ForgeDirection direction = ForgeDirection.getOrientation(mc.objectMouseOver.sideHit);
	            
	            switch(direction)
	            {
				case EAST:
					x += size.getPosX()/2;
				case WEST:
					if(direction == ForgeDirection.WEST)
						x -= size.getPosX()/2;
					y += size.getPosY()/(size.sizeY*2D);
					if(size.sizeY%2 == 0)
						y += 1D/32D;
					z += size.getPosZ()/(size.sizeZ*2D);
					if(size.sizeZ%2 == 0)
						z += 1D/32D;
					break;
				case UP:
					y += size.getPosY()/2;
				case DOWN:
					if(direction == ForgeDirection.DOWN)
						y -= size.getPosY()/2;
					x += size.getPosX()/(size.sizeX*2D);
					if(size.sizeX%2 == 0)
						x += 1D/32D;
					z += size.getPosZ()/(size.sizeZ*2D);
					if(size.sizeZ%2 == 0)
						z += 1D/32D;
					break;
				case SOUTH:
					z += size.getPosZ()/2;
					break;
				case NORTH:
					if(direction == ForgeDirection.NORTH)
						z -= size.getPosZ()/2;
					x += size.getPosX()/(size.sizeX*2D);
					if(size.sizeX%2 == 0)
						x += 1D/32D;
					y += size.getPosY()/(size.sizeY*2D);
					if(size.sizeY%2 == 0)
						y += 1D/32D;
					break;
				default:
					break;
	            }
	            
				LittleRenderHelper.renderBlock(Blocks.stone, x+vec.xCoord, y+vec.yCoord, z+vec.zCoord, size.getPosX(), size.getPosY(), size.getPosZ(), 0, 0, 0, 1, 1, 1, Math.sin(System.nanoTime()/200000000D)*0.2+0.5);
				
				GL11.glDepthMask(true);
	            GL11.glEnable(GL11.GL_TEXTURE_2D);
	            GL11.glDisable(GL11.GL_BLEND);
				TileEntity tileEntity = mc.theWorld.getTileEntity(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
				if(tileEntity instanceof TileEntityLittleTiles)
				{
					//Render preview
					
					//TODO Add rendering for preview
				}
			}
		}
	}
}
