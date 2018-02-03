package com.creativemd.littletiles.client.render;

import net.minecraft.block.state.IBlockState;

public interface IFakeRenderingBlock {
	
	public IBlockState getFakeState(IBlockState state);
	
}
