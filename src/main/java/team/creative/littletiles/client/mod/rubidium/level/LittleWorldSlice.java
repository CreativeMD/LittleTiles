package team.creative.littletiles.client.mod.rubidium.level;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.biome.BiomeColorSource;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;

public class LittleWorldSlice extends WorldSlice {
    
    public static LittleWorldSlice createEmptySlice() {
        return CreativeHackery.allocateInstance(LittleWorldSlice.class);
    }
    
    public Level parent;
    
    public LittleWorldSlice(ClientLevel world) {
        super(world);
    }
    
    @Override
    public void copyData(ChunkRenderContext context) {}
    
    @Override
    public void reset() {}
    
    @Override
    public BlockState getBlockState(BlockPos pos) {
        return parent.getBlockState(pos);
    }
    
    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return parent.getBlockState(new BlockPos(x, y, z));
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
    public BlockEntity getBlockEntity(int x, int y, int z) {
        return parent.getBlockEntity(new BlockPos(x, y, z));
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
    public int getColor(BiomeColorSource source, int x, int y, int z) {
        Biome biome = parent.getBiome(new BlockPos(x, y, z)).value();
        return switch (source) {
            case GRASS -> biome.getGrassColor(x, z);
            case FOLIAGE -> biome.getFoliageColor();
            case WATER -> biome.getWaterColor();
        };
    }
    
}
