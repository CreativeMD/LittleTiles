package com.creativemd.littletiles.client.api;

import net.minecraft.block.state.IBlockState;

public interface IFakeRenderingBlock {
	
	public IBlockState getFakeState(IBlockState state);
	
}
