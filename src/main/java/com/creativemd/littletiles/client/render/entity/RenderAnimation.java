package com.creativemd.littletiles.client.render.entity;

import java.util.Iterator;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.BoxUtils;
import com.creativemd.creativecore.common.utils.math.box.CreativeAxisAlignedBB;
import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.creativecore.common.utils.math.vec.MatrixUtils;
import com.creativemd.creativecore.common.utils.math.vec.Ray2d;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.events.LittleEvent;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.optifine.shaders.ShadersRender;

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
			for (Iterator<TileEntityLittleTiles> iterator = entity.renderQueue.iterator(); iterator.hasNext();) {
				TileEntityLittleTiles te = iterator.next();
				
				if(!te.rendering.get())
				{
					if(te.getBuffer() == null)
					{
						RenderingThread.addCoordToUpdate(te, 0, false);
						continue;
					}
					BlockPos renderChunkPos = EntityDoorAnimation.getRenderChunkPos(te.getPos());
					LittleRenderChunk chunk = entity.renderChunks.get(renderChunkPos);
					if(chunk == null)
					{
						chunk = new LittleRenderChunk(renderChunkPos);
						entity.renderChunks.put(renderChunkPos, chunk);
					}
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
				
				GL11.glRotated(rotation.xCoord, 1, 0, 0);
				GL11.glRotated(rotation.yCoord, 0, 1, 0);
				GL11.glRotated(rotation.zCoord, 0, 0, 1);
				
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
	                
	        		GL11.glRotated(rotation.xCoord, 1, 0, 0);
	        		GL11.glRotated(rotation.yCoord, 0, 1, 0);
	        		GL11.glRotated(rotation.zCoord, 0, 0, 1);
	        		
	        		GlStateManager.translate(- ((double)blockpos.getX() - TileEntityRendererDispatcher.staticPlayerX) + newpos.getX(), - ((double)blockpos.getY() -  TileEntityRendererDispatcher.staticPlayerY) + newpos.getY(), - ((double)blockpos.getZ() - TileEntityRendererDispatcher.staticPlayerZ) + newpos.getZ());
	        		
	        		//GlStateManager.translate(-entity.getInsideBlockCenter().getPosX()-entity.additionalAxis.getPosX(context)/2, -entity.getInsideBlockCenter().getPosY()-entity.additionalAxis.getPosY(context)/2, -entity.getInsideBlockCenter().getPosZ()-entity.additionalAxis.getPosZ(context)/2);
	        		GlStateManager.translate(-entity.rotationCenterInsideBlock.x, -entity.rotationCenterInsideBlock.y, -entity.rotationCenterInsideBlock.z);
	        		//Render TileEntity
	        		
	        		//GlStateManager.translate(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);
	        		
					TileEntityRendererDispatcher.instance.renderTileEntity(te, partialTicks, -1);
					
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
	        
	        //GlStateManager.translate(x, y, z);
	        double rotY = entity.worldRotY - entity.prevWorldRotY;
	        Matrix3d rotationY = MatrixUtils.createRotationMatrixY(rotY);
	        AxisAlignedBB moveBB = BoxUtils.getRotatedSurrounding(entity.worldBoundingBox, entity.rotationCenter, entity.origin.rotation(), entity.origin.translation(), null, 0, rotationY, rotY, null, 0, null);
	        RenderGlobal.drawBoundingBox(moveBB.minX - entity.posX + x, moveBB.minY - entity.posY + y, moveBB.minZ - entity.posZ + z,
	        		moveBB.maxX - entity.posX + x, moveBB.maxY - entity.posY + y, moveBB.maxZ- entity.posZ + z, 1.0F, 1.0F, 1.0F, 1.0F);
	        
	        GlStateManager.popMatrix();
	        
	        
	        GlStateManager.pushMatrix();
	        
	        GlStateManager.translate(x, y, z);
			
			//GlStateManager.translate(entity.getInsideBlockCenter().getPosX()+entity.additionalAxis.getPosX(context)/2, entity.getInsideBlockCenter().getPosY()+entity.additionalAxis.getPosY(context)/2, entity.getInsideBlockCenter().getPosZ()+entity.additionalAxis.getPosZ(context)/2);
			GlStateManager.translate(entity.rotationCenterInsideBlock.x, entity.rotationCenterInsideBlock.y, entity.rotationCenterInsideBlock.z);
	        
			GL11.glRotated(rotation.xCoord, 1, 0, 0);
			GL11.glRotated(rotation.yCoord, 0, 1, 0);
			GL11.glRotated(rotation.zCoord, 0, 0, 1);
			
			GlStateManager.translate(entity.origin.offX(), entity.origin.offY(), entity.origin.offZ());
			
			//GlStateManager.translate(-entity.getInsideBlockCenter().getPosX()-entity.additionalAxis.getPosX(context)/2, -entity.getInsideBlockCenter().getPosY()-entity.additionalAxis.getPosY(context)/2, -entity.getInsideBlockCenter().getPosZ()-entity.additionalAxis.getPosZ(context)/2);
			GlStateManager.translate(-entity.rotationCenterInsideBlock.x, -entity.rotationCenterInsideBlock.y, -entity.rotationCenterInsideBlock.z);
			
			GlStateManager.translate(-x, -y, -z);
			
			AxisAlignedBB entityBB = entity.origin.getAxisAlignedBox(Minecraft.getMinecraft().player.getEntityBoundingBox());
			 
            for (OrientatedBoundingBox bb : entity.worldCollisionBoxes) {
            	GlStateManager.pushMatrix();
            	boolean intersect = bb.intersectsWith(entityBB);
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
            
            renderTempShit(entity, x, y, z);
            
            GlStateManager.glLineWidth(2.0F);
            
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
		}
    }
	
	public void renderTempShit(EntityDoorAnimation entity, double x, double y, double z)
	{
		GlStateManager.pushMatrix();
		if(entity.worldCollisionBoxes.size() > 1)
			return;
		
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		
		OrientatedBoundingBox aabb = entity.worldCollisionBoxes.get(0);
		AxisAlignedBB box = Minecraft.getMinecraft().player.getEntityBoundingBox();
		
		renderShitFace(aabb, box, EnumFacing.NORTH, x - entity.posX, y - entity.posY, z - entity.posZ);
		renderShitFace(aabb, box, EnumFacing.WEST, x - entity.posX, y - entity.posY, z - entity.posZ);		
    	
    	GlStateManager.popMatrix();
	}
	
	public void renderShitFace(OrientatedBoundingBox aabb, AxisAlignedBB other, EnumFacing facing, double x, double y, double z)
	{
		Axis axis = facing.getAxis();
		double alpha = 0.5;
		Vector3d color = new Vector3d(1, 0, 0);
		if(axis == Axis.Y)
			color.set(0, 1, 1);
		else if(axis == Axis.Z)
			color.set(0, 0, 1);
    	double closestValue = CreativeAxisAlignedBB.getValueOfFacing(other, facing.getOpposite());
    	Vector3d[] corners = BoxUtils.getOuterCorner(facing, aabb.origin, aabb);
    	
    	Vector3d outerCorner = corners[0];
    	
    	RenderHelper3D.renderBlock(outerCorner.x + x, outerCorner.y + y, outerCorner.z + z, 0.1, 0.1, 0.1, 0, 0, 0, color.x, color.y, color.z, alpha);
    	
    	
    	Axis one = RotationUtils.getDifferentAxisFirst(axis);
    	Axis two = RotationUtils.getDifferentAxisSecond(axis);
    	
    	double minOne = CreativeAxisAlignedBB.getMin(other, one);
    	double minTwo = CreativeAxisAlignedBB.getMin(other, two);
    	double maxOne = CreativeAxisAlignedBB.getMax(other, one);
    	double maxTwo = CreativeAxisAlignedBB.getMax(other, two);
    	
    	double outerCornerOne = RotationUtils.get(one, outerCorner);
    	double outerCornerTwo = RotationUtils.get(two, outerCorner);
    	double outerCornerAxis = RotationUtils.get(axis, outerCorner);
    	
    	Vector2d[] directions = new Vector2d[3];
    	
    	for (int i = 1; i <= 3; i++) { // Check all lines which connect to the outer corner
    		
    		Vector3d corner = corners[i];
    		
    		Ray2d line = new Ray2d(one, two, outerCorner, RotationUtils.get(one, corner) - outerCornerOne, RotationUtils.get(two, corner) - outerCornerTwo);
    		directions[i-1] = new Vector2d(line.directionOne, line.directionTwo);
    	}
    	
    	boolean minOneOffset = outerCornerOne >= minOne;
		boolean minTwoOffset = outerCornerTwo >= minTwo;
		boolean maxOneOffset = outerCornerOne > maxOne;
		boolean maxTwoOffset = outerCornerTwo > maxTwo;
		
		Vector2d[] vectors;
		
		if(minOneOffset == maxOneOffset && minTwoOffset == maxTwoOffset)
			vectors = new Vector2d[] {new Vector2d((minOneOffset ? maxOne : minOne) - outerCornerOne, (minTwoOffset ? maxTwo : minTwo) - outerCornerTwo)};
		else if(minOneOffset == maxOneOffset)
			vectors = new Vector2d[] {new Vector2d((minOneOffset ? maxOne : minOne) - outerCornerOne, minTwo - outerCornerTwo), new Vector2d((minOneOffset ? maxOne : minOne) - outerCornerOne, maxTwo - outerCornerTwo)};
		else if(minTwoOffset == maxTwoOffset) 
			vectors = new Vector2d[] {new Vector2d(minOne - outerCornerOne, (minTwoOffset ? maxTwo : minTwo) - outerCornerTwo), new Vector2d(maxOne - outerCornerOne, (minTwoOffset ? maxTwo : minTwo) - outerCornerTwo)};
		else
			vectors = new Vector2d[] {};
		
		for (int i = 0; i < vectors.length; i++) {
			
			Vector3d vector = new Vector3d();
			RotationUtils.setValue(vector, vectors[i].x + outerCornerOne, one);
			RotationUtils.setValue(vector, vectors[i].y + outerCornerTwo, two);
			RotationUtils.setValue(vector, closestValue, axis);
			
			RenderHelper3D.renderBlock(vector.x + x, vector.y + y, vector.z + z, 0.1, 0.1, 0.1, 0, 0, 0, color.x, color.y, color.z, alpha);
		}
		
		for (int i = 0; i < 3; i++) { // Calculate faces
			
			int indexFirst = i;
			int indexSecond = i == 2 ? 0 : i + 1;
			
			Vector2d first = directions[indexFirst];
			Vector2d second = directions[indexSecond];
			
			if(first.x == 0 || second.y == 0)
			{
				int temp = indexFirst;
				indexFirst = indexSecond;
				indexSecond = temp;
				first = directions[indexFirst];
				second = directions[indexSecond];
			}
			
			for (int j = 0; j < vectors.length; j++) {
				
				Vector2d vector = vectors[j];			
				
				if((OrientatedBoundingBox.isFurtherOrEqualThan(vector.x, first.x) || OrientatedBoundingBox.isFurtherOrEqualThan(vector.x, second.x) || OrientatedBoundingBox.isFurtherOrEqualThan(vector.x, first.x + second.x)) &&
						(OrientatedBoundingBox.isFurtherOrEqualThan(vector.y, first.y) || OrientatedBoundingBox.isFurtherOrEqualThan(vector.y, second.y) || OrientatedBoundingBox.isFurtherOrEqualThan(vector.y, first.y + second.y)))
				{					
					double t = (vector.x*second.y-vector.y*second.x)/(first.x*second.y-first.y*second.x);
					if(t <= 0 || t >= 1 || Double.isNaN(t))
						continue;
					
    				double s = (vector.y-t*first.y)/second.y;
    				if(s <= 0 || s >= 1 || Double.isNaN(s))
						continue;
    				
    				double valueAxis = outerCornerAxis + (RotationUtils.get(axis, corners[indexFirst+1]) - outerCornerAxis) * t + (RotationUtils.get(axis, corners[indexSecond+1]) - outerCornerAxis) * s;
    				
    				Vector3d vector2 = new Vector3d();
    				RotationUtils.setValue(vector2, first.x * t + outerCornerOne, one);
    				RotationUtils.setValue(vector2, second.y * s + outerCornerTwo, two);
    				RotationUtils.setValue(vector2, valueAxis, axis);
    				
    				RenderHelper3D.renderBlock(vector2.x + x, vector2.y + y, vector2.z + z, 0.1, 0.1, 0.1, 0, 0, 0, color.x, color.y, color.z+1.0, alpha);
				}
			}
			
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDoorAnimation entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
