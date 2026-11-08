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

/** Animation geometry for AO, with light levels and directional shade from its parent. */
final class LittleAnimationLightingView implements BlockAndTintGetter {
    private final BlockAndTintGetter animation;
    private final BlockAndTintGetter parent;

    LittleAnimationLightingView(BlockAndTintGetter animation, BlockAndTintGetter parent) {
        this.animation = animation;
        this.parent = parent;
    }

    private BlockAndTintGetter geometry(BlockPos pos) {
        return animation.getBlockState(pos).isAir() ? parent : animation;
    }

    @Override public BlockState getBlockState(BlockPos pos) { return geometry(pos).getBlockState(pos); }
    @Override public BlockEntity getBlockEntity(BlockPos pos) { return geometry(pos).getBlockEntity(pos); }
    @Override public FluidState getFluidState(BlockPos pos) { return geometry(pos).getFluidState(pos); }
    @Override public ModelData getModelData(BlockPos pos) { return geometry(pos).getModelData(pos); }
    @Override public float getShade(Direction direction, boolean shade) { return parent.getShade(direction, shade); }
    @Override public float getShade(float x, float y, float z, boolean shade) { return parent.getShade(x, y, z, shade); }
    @Override public LevelLightEngine getLightEngine() { return parent.getLightEngine(); }
    @Override public int getBlockTint(BlockPos pos, ColorResolver resolver) { return parent.getBlockTint(pos, resolver); }
    @Override public int getHeight() { return parent.getHeight(); }
    @Override public int getMinBuildHeight() { return parent.getMinBuildHeight(); }
    @Override public int getBrightness(LightLayer layer, BlockPos pos) { return parent.getBrightness(layer, pos); }
    @Override public int getRawBrightness(BlockPos pos, int darkness) { return parent.getRawBrightness(pos, darkness); }
    @Override public AuxiliaryLightManager getAuxLightManager(BlockPos pos) { return parent.getAuxLightManager(pos); }
    @Override public AuxiliaryLightManager getAuxLightManager(ChunkPos pos) { return parent.getAuxLightManager(pos); }
}
