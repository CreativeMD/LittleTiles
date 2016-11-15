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
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.BlockRenderLayer;

public class TileEntityTilesRenderer extends TileEntitySpecialRenderer<TileEntityLittleTiles> {
	
	private WorldVertexBufferUploader uploader = new WorldVertexBufferUploader();
	private VertexBufferUploader vertexUploader = new VertexBufferUploader();
	
	public static BlockRenderLayer[] layers = BlockRenderLayer.values();
	
	@Override
	public void renderTileEntityFast(TileEntityLittleTiles te, double x, double y, double z, float partialTicks, int destroyStage, net.minecraft.client.renderer.VertexBuffer buffer)
	{
		BlockLayerRenderBuffer teBuffer = te.getBuffer();
		if(teBuffer != null && !teBuffer.isDrawing())
		{
			try {
				teBuffer.setDrawing();
				
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
				
				for (int i = 0; i < layers.length; i++) {
					BlockRenderLayer layer = layers[i];
					VertexBuffer layerBuffer = teBuffer.getBufferByLayer(layer);
					if(layerBuffer == null)
					{
						net.minecraft.client.renderer.VertexBuffer tempBuffer = teBuffer.getTemporaryBufferByLayer(layer);
						if(tempBuffer != null)
						{
							layerBuffer = new net.minecraft.client.renderer.vertex.VertexBuffer(DefaultVertexFormats.BLOCK);
					        
					        vertexUploader.setVertexBuffer(layerBuffer);
					        vertexUploader.draw(tempBuffer);
					        
					        teBuffer.setBufferByLayer(layerBuffer, layer);
					        teBuffer.setTemporaryBufferByLayer(null, layer);
						}
					}
					
					if(layerBuffer != null)
					{
						GlStateManager.pushMatrix();
						
						//GlStateManager.translate( -TileEntityRendererDispatcher.staticPlayerX + te.getPos().getX(),-TileEntityRendererDispatcher.staticPlayerY + te.getPos().getY(),-TileEntityRendererDispatcher.staticPlayerZ + te.getPos().getZ());
		    			GlStateManager.translate(x, y, z);
						
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

						layerBuffer.bindBuffer();
						{
							GlStateManager.glVertexPointer( 3, 5126, 28, 0 );
							GlStateManager.glColorPointer( 4, 5121, 28, 12 );
							GlStateManager.glTexCoordPointer( 2, 5126, 28, 16 );
							OpenGlHelper.setClientActiveTexture( OpenGlHelper.lightmapTexUnit );
							GlStateManager.glTexCoordPointer( 2, 5122, 28, 24 );
							OpenGlHelper.setClientActiveTexture( OpenGlHelper.defaultTexUnit );
						}
						layerBuffer.drawArrays( GL11.GL_QUADS );
						OpenGlHelper.glBindBuffer( OpenGlHelper.GL_ARRAY_BUFFER, 0 );
						//GlStateManager.resetColor();

						for ( final VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements() )
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
						GlStateManager.popMatrix();
					}
				}
				
				teBuffer.setFinishedDrawing();
				
				te.deleteOldBuffer();
				
			} catch (RenderOverlapException e) {
				e.printStackTrace();
			}
		}
		
		if(!te.getRenderTiles().isEmpty())
		{
			GlStateManager.pushMatrix();
			int i = te.getWorld().getCombinedLight(te.getPos(), 0);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
            GlStateManager.popMatrix();
		}
	}
	
	@Override
	public void renderTileEntityAt(TileEntityLittleTiles te, double x, double y, double z, float partialTicks, int destroyStage) {
		for (LittleTile	tile : te.getRenderTiles()) {
			LittleTileVec cornerVec = tile.cornerVec;
			tile.renderTick(x, y, z, partialTicks);
		}
		
	}	
}
