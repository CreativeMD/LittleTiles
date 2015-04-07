package com.creativemd.littletiles.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LittleRenderHelper {
	
	public static Minecraft mc = Minecraft.getMinecraft();
	public static RenderBlocks renderer = RenderBlocks.getInstance();
	
	//public static ExtendedRenderBlocks renderBlocks = new ExtendedRenderBlocks(renderer);
	
	public static void renderBlock(Block block, double x, double y, double z, double width, double height, double length, double rotateX, double rotateY, double rotateZ, double red, double green, double blue, double alpha)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glRotated(rotateX, 1, 0, 0);
		GL11.glRotated(rotateY, 0, 1, 0);
		GL11.glRotated(rotateZ, 0, 0, 1);
		GL11.glScaled(width, height, length);
		GL11.glColor4d(red, green, blue, alpha);
		
		GL11.glBegin(GL11.GL_POLYGON);
		//GL11.glColor4d(red, green, blue, alpha);
		GL11.glNormal3f(0.0f, 1.0f, 0.0f);
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
		GL11.glEnd();
		
		GL11.glBegin(GL11.GL_POLYGON);
		//GL11.glColor4d(red, green, blue, alpha);
		GL11.glNormal3f(0.0f, 0.0f, 1.0f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glEnd();
	 
		GL11.glBegin(GL11.GL_POLYGON);
		//GL11.glColor4d(red, green, blue, alpha);
		GL11.glNormal3f(1.0f, 0.0f, 0.0f);
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glVertex3f(0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		GL11.glEnd();
	 
		GL11.glBegin(GL11.GL_POLYGON);
		//GL11.glColor4d(red, green, blue, alpha);
		GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
		GL11.glEnd();
	 
		GL11.glBegin(GL11.GL_POLYGON);
		//GL11.glColor4d(red, green, blue, alpha);
		GL11.glNormal3f(0.0f, -1.0f, 0.0f);
		GL11.glVertex3f(0.5f, -0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		GL11.glEnd();
	 
		GL11.glBegin(GL11.GL_POLYGON);
		//GL11.glColor4d(red, green, blue, alpha);
		GL11.glNormal3f(0.0f, 0.0f, -1.0f);
		GL11.glVertex3f(0.5f, 0.5f, -0.5f);
		GL11.glVertex3f(0.5f, -0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
		GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
		GL11.glEnd();
		
		
        GL11.glPopMatrix();
	}
	
	public static void renderBlock(Block block)
	{
		renderBlock(block, 0);
	}
	
	public static void renderBlock(Block block, int meta)
	{
		//Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		//renderer.useInventoryTint = false;
		//float f5 = 1F;
		//renderer.renderBlockAsItem(block, meta, f5);
		
	}
	
	/*public static void applyBlockRotation(RenderBlocks renderer, ForgeDirection direction)
	{
		//Default direction is East
		double minX = renderer.renderMinX-0.5D;
		double minY = renderer.renderMinY-0.5D;
		double minZ = renderer.renderMinZ-0.5D;
		double maxX = renderer.renderMaxX-0.5D;
		double maxY = renderer.renderMaxY-0.5D;
		double maxZ = renderer.renderMaxZ-0.5D;
		Vec3 min = RotationUtils.applyVectorRotation(Vec3.createVectorHelper(minX, minY, minZ), direction);
		Vec3 max = RotationUtils.applyVectorRotation(Vec3.createVectorHelper(maxX, maxY, maxZ), direction);
		
		min = min.addVector(0.5, 0.5, 0.5);
		max = max.addVector(0.5, 0.5, 0.5);
		
		if(min.xCoord < max.xCoord)
		{
			renderer.renderMinX = min.xCoord;
			renderer.renderMaxX = max.xCoord;
		}
		else
		{
			renderer.renderMinX = max.xCoord;
			renderer.renderMaxX = min.xCoord;
		}
		if(min.yCoord < max.yCoord)
		{
			renderer.renderMinY = min.yCoord;
			renderer.renderMaxY = max.yCoord;
		}
		else
		{
			renderer.renderMinY = max.yCoord;
			renderer.renderMaxY = min.yCoord;
		}
		if(min.zCoord < max.zCoord)
		{
			renderer.renderMinZ = min.zCoord;
			renderer.renderMaxZ = max.zCoord;
		}
		else
		{
			renderer.renderMinZ = max.zCoord;
			renderer.renderMaxZ = min.zCoord;
		}
	}
	
	public static void applyDirection(ForgeDirection direction)
	{
		int rotation = 0;
		switch(direction)
		{
		case EAST:
			rotation = 0;
			break;
		case NORTH:
			rotation = 90;
			break;
		case SOUTH:
			rotation = 270;
			break;
		case WEST:
			rotation = 180;
			break;
		case UP:
			GL11.glRotated(90, 1, 0, 0);
			GL11.glRotated(-90, 0, 0, 1);
			break;
		case DOWN:
			GL11.glRotated(-90, 1, 0, 0);
			GL11.glRotated(-90, 0, 0, 1);
			break;
		default:
			break;
		}
		GL11.glRotated(rotation, 0, 1, 0);
	}*/
	
}
