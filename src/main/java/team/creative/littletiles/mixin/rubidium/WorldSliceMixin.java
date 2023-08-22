package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.biome.BiomeColorSource;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import team.creative.littletiles.client.mod.rubidium.level.WorldSliceExtender;

@Mixin(WorldSlice.class)
public class WorldSliceMixin implements WorldSliceExtender {
    
    @Unique
    private Level parent;
    
    @Override
    public void setParent(Level level) {
        this.parent = level;
    }
    
    @Inject(method = "copyData(Lme/jellysquid/mods/sodium/client/world/cloned/ChunkRenderContext;)V", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void copyData(ChunkRenderContext context, CallbackInfo info) {
        if (parent != null)
            info.cancel();
    }
    
    @Inject(method = "reset()V", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void reset(CallbackInfo info) {
        if (parent != null)
            info.cancel();
    }
    
    @Inject(method = "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("HEAD"), require = 1, cancellable = true,
            remap = false)
    public void getBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> info) {
        if (parent != null)
            info.setReturnValue(parent.getBlockState(pos));
    }
    
    @Inject(method = "getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void getBlockState(int x, int y, int z, CallbackInfoReturnable<BlockState> info) {
        if (parent != null)
            info.setReturnValue(parent.getBlockState(new BlockPos(x, y, z)));
    }
    
    @Inject(method = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;", at = @At("HEAD"), require = 1, cancellable = true,
            remap = false)
    public void getFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> info) {
        if (parent != null)
            info.setReturnValue(parent.getFluidState(pos));
    }
    
    @Inject(method = "getShade(Lnet/minecraft/core/Direction;Z)F", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void getShade(Direction direction, boolean shaded, CallbackInfoReturnable<Float> info) {
        if (parent != null)
            info.setReturnValue(parent.getShade(direction, shaded));
    }
    
    @Inject(method = "getBrightness(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void getBrightness(LightLayer type, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        if (parent != null)
            info.setReturnValue(parent.getBrightness(type, pos));
    }
    
    @Inject(method = "getRawBrightness(Lnet/minecraft/core/BlockPos;I)I", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void getRawBrightness(BlockPos pos, int ambientDarkness, CallbackInfoReturnable<Integer> info) {
        if (parent != null)
            info.setReturnValue(parent.getRawBrightness(pos, ambientDarkness));
    }
    
    @Inject(method = "getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At("HEAD"), require = 1, cancellable = true,
            remap = false)
    public void getBlockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> info) {
        if (parent != null)
            info.setReturnValue(parent.getBlockEntity(pos));
    }
    
    @Inject(method = "getBlockEntity(III)Lnet/minecraft/world/level/block/entity/BlockEntity;", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void getBlockEntity(int x, int y, int z, CallbackInfoReturnable<BlockEntity> info) {
        if (parent != null)
            info.setReturnValue(parent.getBlockEntity(new BlockPos(x, y, z)));
    }
    
    @Inject(method = "getBlockTint(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/ColorResolver;)I", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void getBlockTint(BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> info) {
        if (parent != null)
            info.setReturnValue(parent.getBlockTint(pos, resolver));
    }
    
    @Inject(method = "getHeight()I", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void getHeight(CallbackInfoReturnable<Integer> info) {
        if (parent != null)
            info.setReturnValue(parent.getHeight());
    }
    
    @Inject(method = "getMinBuildHeight()I", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void getMinBuildHeight(CallbackInfoReturnable<Integer> info) {
        if (parent != null)
            info.setReturnValue(parent.getMinBuildHeight());
    }
    
    @Inject(method = "getColor(Lme/jellysquid/mods/sodium/client/world/biome/BiomeColorSource;III)I", at = @At("HEAD"), require = 1, cancellable = true, remap = false)
    public void getColor(BiomeColorSource source, int x, int y, int z, CallbackInfoReturnable<Integer> info) {
        if (parent != null) {
            Biome biome = parent.getBiome(new BlockPos(x, y, z)).value();
            info.setReturnValue(switch (source) {
                case GRASS -> biome.getGrassColor(x, z);
                case FOLIAGE -> biome.getFoliageColor();
                case WATER -> biome.getWaterColor();
            });
        }
    }
    
}
