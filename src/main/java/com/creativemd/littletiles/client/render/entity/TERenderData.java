package com.creativemd.littletiles.client.render.entity;

import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TERenderData {
	
	public final VertexBuffer buffer;
	public final BlockPos chunkPos;
	
	public TERenderData(VertexBuffer buffer, BlockPos pos) {
		this.buffer = buffer;
		this.chunkPos = pos;
	}
	
}
