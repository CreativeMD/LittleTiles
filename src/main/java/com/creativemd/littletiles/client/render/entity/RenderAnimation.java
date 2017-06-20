package com.creativemd.littletiles.client.render.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.render.BlockLayerRenderBuffer;
import com.creativemd.littletiles.client.render.RenderUploader;
import com.creativemd.littletiles.client.render.optifine.OptifineVertexBuffer;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

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
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import shadersmod.client.ShadersRender;

public class RenderAnimation extends Render<EntityDoorAnimation> {
	
	public static final VertexBufferUploader uploader = new VertexBufferUploader();
	
	public RenderAnimation(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityDoorAnimation entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
        /*double d0 = entityIn.posX - this.prevRenderSortX;
        double d1 = entityIn.posY - this.prevRenderSortY;
        double d2 = entityIn.posZ - this.prevRenderSortZ;

        if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)
        {
            this.prevRenderSortX = entityIn.posX;
            this.prevRenderSortY = entityIn.posY;
            this.prevRenderSortZ = entityIn.posZ;
            int k = 0;

            for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : this.renderInfos)
            {
                if (renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk.isLayerStarted(blockLayerIn) && k++ < 15)
                {
                    this.renderDispatcher.updateTransparencyLater(renderglobal$containerlocalrenderinformation.renderChunk);
                }
            }
        }*/
		
		if(entity.renderData == null)
		{
			entity.createClient();
		}
		
		if(entity.renderQueue == null)
			return ;
		
		/**===Setting up finished render-data===**/
		ArrayList<TileEntityLittleTiles> TEtoRemove = new ArrayList<>();
		for (Iterator<TileEntityLittleTiles> iterator = entity.renderQueue.iterator(); iterator.hasNext();) {
			TileEntityLittleTiles te = iterator.next();
			if(!te.rendering.get())
			{
				BlockLayerRenderBuffer layers = te.getBuffer();
				if(layers != null)
				{
					for (int i = 0; i < BlockRenderLayer.values().length; i++) {
						BlockRenderLayer layer = BlockRenderLayer.values()[i];
						net.minecraft.client.renderer.VertexBuffer tempBuffer = layers.getBufferByLayer(layer);
						if(tempBuffer != null)
						{
							VertexBuffer bufferToCreate = new VertexBuffer(LittleTilesClient.getBlockVertexFormat());
							uploader.setVertexBuffer(bufferToCreate);
							uploader.draw(tempBuffer);
							entity.renderData.add(layer, new TERenderData(bufferToCreate, EntityDoorAnimation.getRenderChunkPos(te.getPos()), te.getPos()));
						}
					}
					TEtoRemove.add(te);
				}
			}
		}
		entity.renderQueue.removeAll(TEtoRemove);
		
		/**===Render static part===**/
		
		Vec3d rotation = entity.getRotVector(partialTicks);
		
		
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
		
		
		
		for (Iterator<BlockRenderLayer> iterator = entity.renderData.getKeys().iterator(); iterator.hasNext();) {
			
			BlockRenderLayer layer = iterator.next();
			
			if(FMLClientHandler.instance().hasOptifine() && OptifineVertexBuffer.isShaders())
				ShadersRender.preRenderChunkLayer(layer);
			
			List<TERenderData> blocksToRender = entity.renderData.getValues(layer);
			for (int i = 0; i < blocksToRender.size(); i++) {
				TERenderData data = blocksToRender.get(i);
				
				//Render buffer
				GlStateManager.pushMatrix();
				
				double posX = (data.chunkPos.getX() - entity.getAxisChunkPos().getX()) * 16 - entity.getInsideChunkPos().getX();
				double posY = (data.chunkPos.getY() - entity.getAxisChunkPos().getY()) * 16 - entity.getInsideChunkPos().getY();
				double posZ = (data.chunkPos.getZ() - entity.getAxisChunkPos().getZ()) * 16 - entity.getInsideChunkPos().getZ();
				
				
				
				GlStateManager.translate(x, y, z);
				
				GlStateManager.translate(entity.getInsideBlockCenter().getPosX()+LittleTile.gridMCLength/2, entity.getInsideBlockCenter().getPosY()+LittleTile.gridMCLength/2, entity.getInsideBlockCenter().getPosZ()+LittleTile.gridMCLength/2);
				
				
				GL11.glRotated(rotation.xCoord, 1, 0, 0);
				GL11.glRotated(rotation.yCoord, 0, 1, 0);
				GL11.glRotated(rotation.zCoord, 0, 0, 1);
				//GlStateManager.rotate((float)entity.progress/(float)entity.duration * 90F, 0, 1, 0);
				
				GlStateManager.translate(posX, posY, posZ);
				
				GlStateManager.translate(-entity.getInsideBlockCenter().getPosX()-LittleTile.gridMCLength/2, -entity.getInsideBlockCenter().getPosY()-LittleTile.gridMCLength/2, -entity.getInsideBlockCenter().getPosZ()-LittleTile.gridMCLength/2);
				
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

    			data.buffer.bindBuffer();
    			if(FMLClientHandler.instance().hasOptifine() && OptifineVertexBuffer.isShaders())
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
				
				data.buffer.drawArrays( GL11.GL_QUADS );
				
				data.buffer.unbindBuffer();

				
				GlStateManager.popMatrix();
			}
			
			if(FMLClientHandler.instance().hasOptifine() && OptifineVertexBuffer.isShaders())
				ShadersRender.postRenderChunkLayer(layer);
		}
		
		
		
		for ( final VertexFormatElement vertexformatelement : LittleTilesClient.getBlockVertexFormat().getElements())
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
	        		
	        		GlStateManager.translate(entity.getInsideBlockCenter().getPosX()+LittleTile.gridMCLength/2, entity.getInsideBlockCenter().getPosY()+LittleTile.gridMCLength/2, entity.getInsideBlockCenter().getPosZ()+LittleTile.gridMCLength/2);
	        		
	        		GL11.glRotated(rotation.xCoord, 1, 0, 0);
	        		GL11.glRotated(rotation.yCoord, 0, 1, 0);
	        		GL11.glRotated(rotation.zCoord, 0, 0, 1);
	        		
	        		GlStateManager.translate(- ((double)blockpos.getX() - TileEntityRendererDispatcher.staticPlayerX) + newpos.getX(), - ((double)blockpos.getY() -  TileEntityRendererDispatcher.staticPlayerY) + newpos.getY(), - ((double)blockpos.getZ() - TileEntityRendererDispatcher.staticPlayerZ) + newpos.getZ());
	        		
					GlStateManager.translate(-entity.getInsideBlockCenter().getPosX()-LittleTile.gridMCLength/2, -entity.getInsideBlockCenter().getPosY()-LittleTile.gridMCLength/2, -entity.getInsideBlockCenter().getPosZ()-LittleTile.gridMCLength/2);
					//Render TileEntity
	        		
	        		//GlStateManager.translate(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);
	        		
					TileEntityRendererDispatcher.instance.renderTileEntity(te, partialTicks, -1);
					
					GlStateManager.translate(-entity.getInsideBlockCenter().getPosX()-LittleTile.gridMCLength/2, -entity.getInsideBlockCenter().getPosY()-LittleTile.gridMCLength/2, -entity.getInsideBlockCenter().getPosZ()-LittleTile.gridMCLength/2);
					GlStateManager.popMatrix();
				}
			}
		}
		
		RenderHelper.enableStandardItemLighting();
    }

	@Override
	protected ResourceLocation getEntityTexture(EntityDoorAnimation entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
