package team.creative.littletiles.client.render.cache.pipeline;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

/** Removes baked directional shade without changing AO or light sampling. */
final class LittleLightingView implements BlockAndTintGetter {
    private final BlockAndTintGetter delegate;
    LittleLightingView(BlockAndTintGetter delegate) { this.delegate = delegate; }

    @Override public float getShade(Direction direction, boolean shade) { return 1.0F; }
    @Override public float getShade(float x, float y, float z, boolean shade) { return 1.0F; }
    @Override public BlockState getBlockState(BlockPos pos) { return delegate.getBlockState(pos); }
    @Override public BlockEntity getBlockEntity(BlockPos pos) { return delegate.getBlockEntity(pos); }
    @Override public FluidState getFluidState(BlockPos pos) { return delegate.getFluidState(pos); }
    @Override public LevelLightEngine getLightEngine() { return delegate.getLightEngine(); }
    @Override public int getBlockTint(BlockPos pos, ColorResolver resolver) { return delegate.getBlockTint(pos, resolver); }
    @Override public int getHeight() { return delegate.getHeight(); }
    @Override public int getMinBuildHeight() { return delegate.getMinBuildHeight(); }
    @Override public int getBrightness(LightLayer layer, BlockPos pos) { return delegate.getBrightness(layer, pos); }
    @Override public int getRawBrightness(BlockPos pos, int darkness) { return delegate.getRawBrightness(pos, darkness); }
    @Override public AuxiliaryLightManager getAuxLightManager(BlockPos pos) { return delegate.getAuxLightManager(pos); }
    @Override public AuxiliaryLightManager getAuxLightManager(ChunkPos pos) { return delegate.getAuxLightManager(pos); }
    @Override public ModelData getModelData(BlockPos pos) { return delegate.getModelData(pos); }
}
