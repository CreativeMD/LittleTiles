package team.creative.littletiles.client.mod.continuity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

/** Presents the LittleTiles container position as its material block to CTM predicates. */
public final class MaterialBlockView implements BlockAndTintGetter {
    private final BlockAndTintGetter delegate;
    private final BlockPos materialPos;
    private final BlockState materialState;

    public MaterialBlockView(BlockAndTintGetter delegate, BlockPos materialPos, BlockState materialState) {
        this.delegate = delegate;
        this.materialPos = materialPos;
        this.materialState = materialState;
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return pos.equals(materialPos) ? null : delegate.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return pos.equals(materialPos) ? materialState : delegate.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return pos.equals(materialPos) ? materialState.getFluidState() : delegate.getFluidState(pos);
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return delegate.getMinBuildHeight();
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return delegate.getShade(direction, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return delegate.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return delegate.getBlockTint(pos, resolver);
    }
}
