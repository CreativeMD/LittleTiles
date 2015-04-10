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
	
	public static void renderBlock(double x, double y, double z, double width, double height, double length, double rotateX, double rotateY, double rotateZ, double red, double green, double blue, double alpha)
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
	
}
