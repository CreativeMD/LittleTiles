package com.creativemd.littletiles.common.mod.theoneprobe;

import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IEntityDisplayOverride;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.EntityAnimation;

public class TheOneProbeInteractor implements IEntityDisplayOverride, IBlockDisplayOverride {
    
    @Override
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
        return blockState.getBlock() instanceof BlockTile;
    }
    
    @Override
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, net.minecraft.world.entity.Entity entity, IProbeHitEntityData data) {
        return entity instanceof EntityAnimation;
    }
    
}
