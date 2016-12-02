package com.creativemd.littletiles.client.render;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderCubeLayerCache;
import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.littletiles.client.render.BlockLayerRenderBuffer.RenderOverlapException;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class TileEntityTilesRenderer extends TileEntitySpecialRenderer<TileEntityLittleTiles> {
	
	private WorldVertexBufferUploader uploader = new WorldVertexBufferUploader();
	private VertexBufferUploader vertexUploader = new VertexBufferUploader();
	
	public static BlockRenderLayer[] layers = BlockRenderLayer.values();
	
	@Override
	public boolean isGlobalRenderer(TileEntityLittleTiles te)
    {
		AxisAlignedBB box = te.getRenderBoundingBox();
        if(Math.abs(box.maxX - box.minX) > 16)
        	return true;
        if(Math.abs(box.maxY - box.minY) > 16)
        	return true;
        if(Math.abs(box.maxZ - box.minZ) > 16)
        	return true;
        return false;
    }
	
	public void renderDebugBoundingBox(AxisAlignedBB axisalignedbb, double x, double y, double z)
    {
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        //float f = entityIn.width / 2.0F;
        //AxisAlignedBB axisalignedbb = entityIn.getEntityBoundingBox();
        RenderGlobal.func_189694_a(axisalignedbb.minX + x, axisalignedbb.minY + y, axisalignedbb.minZ + z, axisalignedbb.maxX + x, axisalignedbb.maxY + y, axisalignedbb.maxZ + z, 1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }
	
	@Override
	public void renderTileEntityAt(TileEntityLittleTiles te, double x, double y, double z, float partialTicks, int destroyStage) {
		//TODO Add rendering boundbox for testing
		/*AxisAlignedBB box = te.getRenderBoundingBox();
		double sizeX = box.maxX-box.minX;
		double sizeY = box.maxY-box.minY;
		double sizeZ = box.maxZ-box.minZ;
		RenderHelper3D.renderBlock(box.minX-TileEntityRendererDispatcher.staticPlayerX, box.minY-TileEntityRendererDispatcher.staticPlayerY, box.minZ-TileEntityRendererDispatcher.staticPlayerZ, sizeX, sizeY, sizeZ, 0, 0, 0, 1, 1, 1, 1);*/
		//renderDebugBoundingBox(te.getRenderBoundingBox(), x-te.getPos().getX(), y-te.getPos().getY(), z-te.getPos().getZ());
		//Render.renderOffsetAABB(te.getRenderBoundingBox(), x*2, y*2, z*2);
		
		for (LittleTile	tile : te.getRenderTiles()) {
			LittleTileVec cornerVec = tile.cornerVec;
			tile.renderTick(x, y, z, partialTicks);
		}
		
	}	
}
