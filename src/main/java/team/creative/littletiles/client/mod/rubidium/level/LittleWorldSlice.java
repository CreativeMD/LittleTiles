package team.creative.littletiles.client.mod.rubidium.level;

import org.embeddedt.embeddium.api.render.chunk.EmbeddiumBlockAndTintGetter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class LittleWorldSlice implements EmbeddiumBlockAndTintGetter {
    
    public Level parent;
    
    public LittleWorldSlice() {}
    
    @Override
    public BlockState getBlockState(BlockPos pos) {
        return parent.getBlockState(pos);
    }
    
    @Override
    public FluidState getFluidState(BlockPos pos) {
        return parent.getFluidState(pos);
    }
    
    @Override
    public float getShade(Direction direction, boolean shaded) {
        return parent.getShade(direction, shaded);
    }
    
    @Override
    public int getBrightness(LightLayer type, BlockPos pos) {
        return parent.getBrightness(type, pos);
    }
    
    @Override
    public int getRawBrightness(BlockPos pos, int ambientDarkness) {
        return parent.getRawBrightness(pos, ambientDarkness);
    }
    
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return parent.getBlockEntity(pos);
    }
    
    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return parent.getBlockTint(pos, resolver);
    }
    
    @Override
    public int getHeight() {
        return parent.getHeight();
    }
    
    @Override
    public int getMinBuildHeight() {
        return parent.getMinBuildHeight();
    }
    
    @Override
    public LevelLightEngine getLightEngine() {
        return parent.getLightEngine();
    }
    
}
