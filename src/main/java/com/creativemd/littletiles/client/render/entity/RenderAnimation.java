package com.creativemd.littletiles.client.render.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.creativemd.littletiles.client.render.BlockLayerRenderBuffer;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.client.render.optifine.OptifineHelper;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.entity.EntityAABB;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.events.LittleEvent;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;
import shadersmod.client.ShadersRender;

public class RenderAnimation extends Render<EntityDoorAnimation> {
	
	public static Minecraft mc = Minecraft.getMinecraft();	
	public static final VertexBufferUploader uploader = new VertexBufferUploader();
	
	public RenderAnimation(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityDoorAnimation entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		if(entity.isDead)
			return ;
		
		if(entity.renderChunks == null)
			entity.createClient();
		
		/**===Setting up finished render-data===**/
		
		if(entity.renderQueue != null)
		{
			int i = 0;
			for (Iterator<TileEntityLittleTiles> iterator = entity.renderQueue.iterator(); iterator.hasNext();) {
				TileEntityLittleTiles te = iterator.next();
				if(!te.rendering.get())
				{
					BlockPos renderChunkPos = EntityDoorAnimation.getRenderChunkPos(te.getPos());
					LittleRenderChunk chunk = entity.renderChunks.get(renderChunkPos);
					if(chunk == null)
					{
						chunk = new LittleRenderChunk(renderChunkPos);
						entity.renderChunks.put(renderChunkPos, chunk);
					}
					
					i++;
					chunk.addRenderData(te);				
					iterator.remove();
				}
			}
		}
		
		for (LittleRenderChunk chunk : entity.renderChunks.values()) {
			chunk.uploadBuffer();	
		}
		
		if(entity.renderQueue != null && entity.renderQueue.isEmpty())
		{
			for (LittleRenderChunk chunk : entity.renderChunks.values()) {
				chunk.markCompleted();
			}
			entity.renderQueue = null;
		}
		
		/**===Render static part===**/
		
		Vec3d rotation = entity.getRotVector(partialTicks);
		
		LittleGridContext context = entity.getInsideBlockCenter().context;
		
		//SETUP OPENGL
		
		bindTexture( TextureMap.LOCATION_BLOCKS_TEXTURE );
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GlStateManager.color( 1.0f, 1.0f, 1.0f, 1.0f );
		
		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();

		GlStateManager.shadeModel( GL11.GL_SMOOTH );
		
		GlStateManager.glEnableClientState( 32884 );
		OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit );
		GlStateManager.glEnableClientState( 32888 );
		OpenGlHelper.setClientActiveTexture( OpenGlHelper.lightmapTexUnit );
		GlStateManager.glEnableClientState( 32888 );
		OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit );
		GlStateManager.glEnableClientState( 32886 );
		
		float f = (float)mc.getRenderViewEntity().posX;
        float f1 = (float)mc.getRenderViewEntity().posY + mc.getRenderViewEntity().getEyeHeight();
        float f2 = (float)mc.getRenderViewEntity().posZ;
		
		for (int i = 0; i < BlockRenderLayer.values().length; i++) {
			BlockRenderLayer layer = BlockRenderLayer.values()[i];
			
			if(FMLClientHandler.instance().hasOptifine() && OptifineHelper.isShaders())
				ShadersRender.preRenderChunkLayer(layer);
			
			for (LittleRenderChunk chunk : entity.renderChunks.values()) {
				
				if(layer == BlockRenderLayer.TRANSLUCENT)
					chunk.resortTransparency(LittleEvent.transparencySortingIndex, f, f1, f2);
				
				VertexBuffer buffer = chunk.getLayerBuffer(layer);
				
				if(buffer == null)
					continue;
				
				//Render buffer
				GlStateManager.pushMatrix();
				
				double posX = (chunk.pos.getX() - entity.getAxisChunkPos().getX()) * 16 - entity.getInsideChunkPos().getX();
				double posY = (chunk.pos.getY() - entity.getAxisChunkPos().getY()) * 16 - entity.getInsideChunkPos().getY();
				double posZ = (chunk.pos.getZ() - entity.getAxisChunkPos().getZ()) * 16 - entity.getInsideChunkPos().getZ();
				
				GlStateManager.translate(x, y, z);
				
				//GlStateManager.translate(entity.getInsideBlockCenter().getPosX()+entity.additionalAxis.getPosX(context)/2, entity.getInsideBlockCenter().getPosY()+entity.additionalAxis.getPosY(context)/2, entity.getInsideBlockCenter().getPosZ()+entity.additionalAxis.getPosZ(context)/2);
				GlStateManager.translate(entity.rotationCenterInsideBlock.x, entity.rotationCenterInsideBlock.y, entity.rotationCenterInsideBlock.z);
				
				GL11.glRotated(rotation.x, 1, 0, 0);
				GL11.glRotated(rotation.y, 0, 1, 0);
				GL11.glRotated(rotation.z, 0, 0, 1);
				
				GlStateManager.translate(posX, posY, posZ);
				
				//GlStateManager.translate(-entity.getInsideBlockCenter().getPosX()-entity.additionalAxis.getPosX(context)/2, -entity.getInsideBlockCenter().getPosY()-entity.additionalAxis.getPosY(context)/2, -entity.getInsideBlockCenter().getPosZ()-entity.additionalAxis.getPosZ(context)/2);
				GlStateManager.translate(-entity.rotationCenterInsideBlock.x, -entity.rotationCenterInsideBlock.y, -entity.rotationCenterInsideBlock.z);
				
    			//Render
    			if ( layer == BlockRenderLayer.TRANSLUCENT )
    			{
    				GlStateManager.enableBlend();
    				GlStateManager.disableAlpha();
    			}
    			else
    			{
    				GlStateManager.disableBlend();
    				GlStateManager.enableAlpha();
    			}
    			
    			

    			buffer.bindBuffer();
    			
    			if(FMLClientHandler.instance().hasOptifine() && OptifineHelper.isShaders())
    				ShadersRender.setupArrayPointersVbo();
    			else
				{
					GlStateManager.glVertexPointer( 3, 5126, 28, 0 );
					GlStateManager.glColorPointer( 4, 5121, 28, 12 );
					GlStateManager.glTexCoordPointer( 2, 5126, 28, 16 );
					OpenGlHelper.setClientActiveTexture( OpenGlHelper.lightmapTexUnit );
					GlStateManager.glTexCoordPointer( 2, 5122, 28, 24 );
					OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit );
				}
				
				buffer.drawArrays( GL11.GL_QUADS );
				buffer.unbindBuffer();
				
				GlStateManager.popMatrix();				
				
			}
			
			if(FMLClientHandler.instance().hasOptifine() && OptifineHelper.isShaders())
				ShadersRender.postRenderChunkLayer(layer);
		}
		
		for ( final VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
		{
			final VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
			final int i1 = vertexformatelement.getIndex();

			switch ( vertexformatelement$enumusage )
			{
				case POSITION:
					GlStateManager.glDisableClientState( 32884 );
					break;
				case UV:
					OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit + i1 );
					GlStateManager.glDisableClientState( 32888 );
					OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit );
					break;
				case COLOR:
					GlStateManager.glDisableClientState( 32886 );
					GlStateManager.resetColor();
			}
		}
		
		//Minecraft.getMinecraft().entityRenderer.disableLightmap();
		
		/**===Render dynamic part===**/
		
		GlStateManager.enableRescaleNormal();
		
		if(!entity.isWaitingForRender())
		{
			//Setup OPENGL
			for (Iterator<TileEntityLittleTiles> iterator = entity.blocks.iterator(); iterator.hasNext();) {
				TileEntityLittleTiles te = iterator.next();
				if(te.shouldRenderInPass(0))
				{
	                GlStateManager.pushMatrix();
	                
	                BlockPos blockpos = te.getPos();
	                
	                BlockPos newpos = te.getPos().subtract(entity.getAxisPos());
	                
	                GlStateManager.translate(x, y, z);
	        		
	                //GlStateManager.translate(entity.getInsideBlockCenter().getPosX()+entity.additionalAxis.getPosX(context)/2, entity.getInsideBlockCenter().getPosY()+entity.additionalAxis.getPosY(context)/2, entity.getInsideBlockCenter().getPosZ()+entity.additionalAxis.getPosZ(context)/2);
	                GlStateManager.translate(entity.rotationCenterInsideBlock.x, entity.rotationCenterInsideBlock.y, entity.rotationCenterInsideBlock.z);
	                
	        		GL11.glRotated(rotation.x, 1, 0, 0);
	        		GL11.glRotated(rotation.y, 0, 1, 0);
	        		GL11.glRotated(rotation.z, 0, 0, 1);
	        		
	        		GlStateManager.translate(- ((double)blockpos.getX() - TileEntityRendererDispatcher.staticPlayerX) + newpos.getX(), - ((double)blockpos.getY() -  TileEntityRendererDispatcher.staticPlayerY) + newpos.getY(), - ((double)blockpos.getZ() - TileEntityRendererDispatcher.staticPlayerZ) + newpos.getZ());
	        		
	        		//GlStateManager.translate(-entity.getInsideBlockCenter().getPosX()-entity.additionalAxis.getPosX(context)/2, -entity.getInsideBlockCenter().getPosY()-entity.additionalAxis.getPosY(context)/2, -entity.getInsideBlockCenter().getPosZ()-entity.additionalAxis.getPosZ(context)/2);
	        		GlStateManager.translate(-entity.rotationCenterInsideBlock.x, -entity.rotationCenterInsideBlock.y, -entity.rotationCenterInsideBlock.z);
	        		//Render TileEntity
	        		
	        		//GlStateManager.translate(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);
	        		
					TileEntityRendererDispatcher.instance.render(te, partialTicks, -1);
					
					GlStateManager.translate(-entity.getInsideBlockCenter().getPosX()-context.gridMCLength/2, -entity.getInsideBlockCenter().getPosY()-context.gridMCLength/2, -entity.getInsideBlockCenter().getPosZ()-context.gridMCLength/2);
					GlStateManager.popMatrix();
				}
			}
		}
		
		RenderHelper.enableStandardItemLighting();
		
		if (mc.getRenderManager().isDebugBoundingBox() && !mc.isReducedDebug())
		{
			GlStateManager.depthMask(false);
	        GlStateManager.disableTexture2D();
	        GlStateManager.disableLighting();
	        GlStateManager.disableCull();
	        GlStateManager.disableBlend();
	        
	        GlStateManager.glLineWidth(4.0F);
	        
	        GlStateManager.pushMatrix();
	        
	        GlStateManager.translate(x, y, z);
			
			//GlStateManager.translate(entity.getInsideBlockCenter().getPosX()+entity.additionalAxis.getPosX(context)/2, entity.getInsideBlockCenter().getPosY()+entity.additionalAxis.getPosY(context)/2, entity.getInsideBlockCenter().getPosZ()+entity.additionalAxis.getPosZ(context)/2);
			GlStateManager.translate(entity.rotationCenterInsideBlock.x, entity.rotationCenterInsideBlock.y, entity.rotationCenterInsideBlock.z);
	        
			GL11.glRotated(rotation.x, 1, 0, 0);
			GL11.glRotated(rotation.y, 0, 1, 0);
			GL11.glRotated(rotation.z, 0, 0, 1);
			
			GlStateManager.translate(entity.fakeWorld.offX(), entity.fakeWorld.offY(), entity.fakeWorld.offZ());
			
			//GlStateManager.translate(-entity.getInsideBlockCenter().getPosX()-entity.additionalAxis.getPosX(context)/2, -entity.getInsideBlockCenter().getPosY()-entity.additionalAxis.getPosY(context)/2, -entity.getInsideBlockCenter().getPosZ()-entity.additionalAxis.getPosZ(context)/2);
			GlStateManager.translate(-entity.rotationCenterInsideBlock.x, -entity.rotationCenterInsideBlock.y, -entity.rotationCenterInsideBlock.z);
			
			GlStateManager.translate(-x, -y, -z);
			
			 AxisAlignedBB entityBB = entity.getFakeWorldOrientatedBox(Minecraft.getMinecraft().player.getEntityBoundingBox());
			 
            for (EntityAABB bb : entity.worldCollisionBoxes) {
            	GlStateManager.pushMatrix();
            	boolean intersect = bb.intersects(entityBB);
            	RenderGlobal.drawBoundingBox(bb.minX - entity.posX + x, bb.minY - entity.posY + y, bb.minZ - entity.posZ + z,
            			bb.maxX - entity.posX + x, bb.maxY - entity.posY + y, bb.maxZ- entity.posZ + z, 1.0F, intersect ? 0.0F : 1.0F, intersect ? 0.0F : 1.0F, 1.0F);
            	GlStateManager.popMatrix();
			}
            
            /*GlStateManager.pushMatrix();
            
            double d0 = (Minecraft.getMinecraft().player.posX - Minecraft.getMinecraft().player.prevPosX) * (double)partialTicks;
            double d1 = (Minecraft.getMinecraft().player.posY - Minecraft.getMinecraft().player.prevPosY) * (double)partialTicks;
            double d2 = (Minecraft.getMinecraft().player.posZ - Minecraft.getMinecraft().player.prevPosZ) * (double)partialTicks;
            
        	RenderGlobal.drawBoundingBox(entityBB.minX - entity.posX + x + d0, entityBB.minY - entity.posY + y + d1, entityBB.minZ - entity.posZ + z + d2,
        			entityBB.maxX - entity.posX + x + d0, entityBB.maxY - entity.posY + y + d1, entityBB.maxZ- entity.posZ + z + d2, 1.0F, 1.0F, 1.0F, 1.0F);
        	GlStateManager.popMatrix();*/
            
            GlStateManager.popMatrix();
            
            GlStateManager.glLineWidth(2.0F);
            
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
		}
    }

	@Override
	protected ResourceLocation getEntityTexture(EntityDoorAnimation entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
