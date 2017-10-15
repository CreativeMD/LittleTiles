package com.creativemd.littletiles.client.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedQuad;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedOrdinaryBox;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.EnumFaceDirection.VertexInformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LittleSlicedOrdinaryRenderingCube extends LittleRenderingCube {
	
	protected LittleDynamicCube dynamicCube;
	
	public LittleSlicedOrdinaryRenderingCube(CubeObject cube, LittleTileSlicedOrdinaryBox box, Block block, int meta) {
		super(cube, box, block, meta);
		dynamicCube = new LittleDynamicCube(this, box.slice, box.getSize());
	}
	
	@Override
	public CubeObject offset(BlockPos pos)
	{
		return new LittleSlicedOrdinaryRenderingCube(new CubeObject(minX-pos.getX(), minY-pos.getY(), minZ-pos.getZ(), maxX-pos.getX(), maxY-pos.getY(), maxZ-pos.getZ(), this), (LittleTileSlicedOrdinaryBox) this.box, block, meta);
	}
	
	public float getVertexInformationPositionX(VertexInformation info, BlockPos pos)
	{
		return dynamicCube.getX(info) - pos.getX();
	}
	
	public float getVertexInformationPositionY(VertexInformation info, BlockPos pos)
	{
		return dynamicCube.getY(info) - pos.getY();
	}
	
	public float getVertexInformationPositionZ(VertexInformation info, BlockPos pos)
	{
		return dynamicCube.getZ(info) - pos.getZ();
	}

	@Override
	public void renderCubePreview(boolean absolute, double x, double y, double z, ILittleTile iTile) {
		
		double cubeX = x;
		if(absolute)
			cubeX -= x+TileEntityRendererDispatcher.staticPlayerX;
		
		double cubeY = y;
		if(absolute)
			cubeY -= y+TileEntityRendererDispatcher.staticPlayerY;
		
		double cubeZ = z;
		if(absolute)
			cubeZ -= z+TileEntityRendererDispatcher.staticPlayerZ;
		
		Vec3d color = ColorUtils.IntToVec(this.color);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(cubeX, cubeY, cubeZ);
		GlStateManager.enableRescaleNormal();
		
		GlStateManager.color((float)color.x, (float)color.y, (float)color.z, (float)(Math.sin(System.nanoTime()/200000000D)*0.2+0.5) * iTile.getPreviewAlphaFactor());
		
		LittleTileSlicedOrdinaryBox box = (LittleTileSlicedOrdinaryBox) this.box;
		
		LittleTileSize size = box.getSize();
		
		for (int i = 0; i < EnumFacing.VALUES.length; i++) {
			EnumFacing facing = EnumFacing.VALUES[i];
			if(box.slice.shouldRenderSide(facing, size))
				renderFace(dynamicCube, facing, facing.getAxis() == box.slice.axis, box.slice);
		}
		
        GlStateManager.popMatrix();
	}
	
	public static void renderFace(LittleDynamicCube cube, EnumFacing facing, boolean isTraingle, LittleSlice slice)
	{
		GL11.glBegin(GL11.GL_POLYGON);
		
		Vec3i normal = facing.getDirectionVec();
		GlStateManager.glNormal3f(normal.getX(), normal.getY(), normal.getZ());
		
		Vector3f vec = new Vector3f();
		EnumFaceDirection face = EnumFaceDirection.getFacing(facing);
		
		//int index = isTraingle ? (facing.getAxisDirection() == AxisDirection.POSITIVE) ? slice.traingleOrderPositive : slice.traingleOrderNegative : 0;
		//for (int i = 0; i < (isTraingle ? 3 : 4); i++) {
			
			//index++;
			//if(index >= 4)
				//index = 0;
			
			//VertexInformation info = face.getVertexInformation(index);
		
			//GlStateManager.glVertex3f(vec.x, vec.y, vec.z);
		//}
		
		for (int i = 0; i < 4; i++) {			
			vec = cube.get(face.getVertexInformation(i), vec);
			
			GlStateManager.glVertex3f(vec.x, vec.y, vec.z);
		}
		
		GlStateManager.glEnd();
	}
	
	@Override
	public List<BakedQuad> getBakedQuad(BlockPos offset, IBlockState state, IBakedModel blockModel, EnumFacing facing, long rand, boolean overrideTint, int defaultColor)
	{
		LittleTileSlicedOrdinaryBox box = (LittleTileSlicedOrdinaryBox) this.box;
		LittleTileSize size = box.getSize();
		
		if(!box.slice.shouldRenderSide(facing, size))
			return Collections.emptyList();
		
		List<BakedQuad> blockQuads = blockModel.getQuads(state, facing, rand);
		if(blockQuads.isEmpty())
			return Collections.emptyList();
		
		List<BakedQuad> quads = new ArrayList<>();
		
		boolean isTraingle = facing.getAxis() == box.slice.axis;
		Vector3f vec = new Vector3f();
		
		int color = this.color != -1 ? this.color : defaultColor;
		
		for(int i = 0; i < blockQuads.size(); i++)
		{
			BakedQuad quad = new CreativeBakedQuad(blockQuads.get(i), this, color, overrideTint && (defaultColor == -1 || blockQuads.get(i).hasTintIndex()) && color != -1, facing);
			
			EnumFaceDirection direction = EnumFaceDirection.getFacing(facing);
			
			for (int k = 0; k < 4; k++) {
				
				VertexInformation vertex = direction.getVertexInformation(k);
				
				int index = k * quad.getFormat().getIntegerSize();
				//float newX = getVertexInformationPosition(vertex.xIndex);
				//float newY = getVertexInformationPosition(vertex.yIndex);
				//float newZ = getVertexInformationPosition(vertex.zIndex);
				
				vec = dynamicCube.get(vertex, vec);
				
				quad.getVertexData()[index] = Float.floatToIntBits(vec.x);
				quad.getVertexData()[index+1] = Float.floatToIntBits(vec.y);
				quad.getVertexData()[index+2] = Float.floatToIntBits(vec.z);
				
				if(keepVU)
					continue;
				
				int uvIndex = index + quad.getFormat().getUvOffsetById(0) / 4;
				
				float newX = getVertexInformationPositionX(vertex, offset);
				float newY = getVertexInformationPositionY(vertex, offset);
				float newZ = getVertexInformationPositionZ(vertex, offset);
				
				float uMin = 0;
				float uMax = 1;
				float vMin = 0;
				float vMax = 1;
				
				float u = uMin;
				float v = vMin;
				
				switch(facing)
				{
				case EAST:
					newY = vMax-newY;
					newZ = uMax-newZ;
				case WEST:
					if(facing == EnumFacing.WEST)
						newY = vMax-newY;
					u = newZ;
					v = newY;
					break;
				case DOWN:
					newZ = vMax-newZ;
				case UP:
					u = newX;
					v = newZ;
					break;
				case NORTH:
					newY = vMax-newY;
					newX = uMax-newX;
				case SOUTH:
					if(facing == EnumFacing.SOUTH)
						newY = vMax-newY;
					u = newX;
					v = newY;
					break;
				}
				
				u *= 16;
				v *= 16;
				
				quad.getVertexData()[uvIndex] = Float.floatToRawIntBits(quad.getSprite().getInterpolatedU(u));
				quad.getVertexData()[uvIndex + 1] = Float.floatToRawIntBits(quad.getSprite().getInterpolatedV(v));
			}
			quads.add(quad);
		}
		return quads;
	}
}
