package org.zeith.lux.api.light;

import org.zeith.lux.api.event.GatherLightsEvent;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILightBlockHandler
{
    void createLights(World world, BlockPos pos, IBlockState state, GatherLightsEvent e);
    
    default void update(IBlockState state, BlockPos pos)
    {
    }
}
