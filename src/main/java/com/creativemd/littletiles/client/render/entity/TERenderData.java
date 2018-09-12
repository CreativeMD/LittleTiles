package com.creativemd.littletiles.client.render.entity;

import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TERenderData {

	public final VertexBuffer buffer;
	public final BlockPos chunkPos;
	public final BlockPos tePos;

	public TERenderData(VertexBuffer buffer, BlockPos pos, BlockPos tePos) {
		this.buffer = buffer;
		this.chunkPos = pos;
		this.tePos = tePos;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof TERenderData)
			return ((TERenderData) object).tePos.equals(tePos);
		return false;
	}

}
