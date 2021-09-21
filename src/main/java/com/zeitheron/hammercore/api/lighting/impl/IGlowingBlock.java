package com.zeitheron.hammercore.api.lighting.impl;

import com.zeitheron.hammercore.api.lighting.ColoredLight;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IGlowingBlock {
    
    ColoredLight produceColoredLight(World world, BlockPos pos, IBlockState state, float partialTicks);
}
