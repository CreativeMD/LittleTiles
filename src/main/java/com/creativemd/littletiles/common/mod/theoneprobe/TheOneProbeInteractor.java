package com.creativemd.littletiles.common.mod.theoneprobe;

import com.creativemd.littletiles.common.block.BlockTile;

import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IEntityDisplayOverride;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import team.creative.littletiles.common.entity.EntityAnimation;

public class TheOneProbeInteractor implements IEntityDisplayOverride, IBlockDisplayOverride {
    
    @Override
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        return blockState.getBlock() instanceof BlockTile;
    }
    
    @Override
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, Entity entity, IProbeHitEntityData data) {
        return entity instanceof EntityAnimation;
    }
    
}
