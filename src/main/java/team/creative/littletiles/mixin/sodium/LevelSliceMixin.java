package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.caffeinemc.mods.sodium.client.services.PlatformLevelAccess;
import net.caffeinemc.mods.sodium.client.services.PlatformModelAccess;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.client.mod.sodium.level.LittleLevelSliceExtender;

@Mixin(LevelSlice.class)
public class LevelSliceMixin implements LittleLevelSliceExtender {
    
    @Unique
    public Level overwrite;
    
    @Override
    public void setLevel(Level level) {
        this.overwrite = level;
    }
    
    @Inject(at = @At("HEAD"), method = "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", cancellable = true, require = 1)
    public void getBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> info) {
        if (overwrite != null)
            info.setReturnValue(overwrite.getBlockState(pos));
    }
    
    @Inject(at = @At("HEAD"), method = "getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;", cancellable = true, require = 1)
    public void getBlockState(int blockX, int blockY, int blockZ, CallbackInfoReturnable<BlockState> info) {
        if (overwrite != null)
            info.setReturnValue(overwrite.getBlockState(new BlockPos(blockX, blockY, blockZ)));
    }
    
    @Inject(at = @At("HEAD"), method = "getBrightness(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I", cancellable = true, require = 1)
    public void getBrightness(LightLayer type, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        if (overwrite != null)
            info.setReturnValue(overwrite.getBrightness(type, pos));
        
    }
    
    @Inject(at = @At("HEAD"), method = "getRawBrightness(Lnet/minecraft/core/BlockPos;I)I", cancellable = true, require = 1)
    public void getRawBrightness(BlockPos pos, int ambientDarkness, CallbackInfoReturnable<Integer> info) {
        if (overwrite != null)
            info.setReturnValue(overwrite.getRawBrightness(pos, ambientDarkness));
        
    }
    
    @Inject(at = @At("HEAD"), method = "getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", cancellable = true, require = 1)
    public void getBlockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> info) {
        if (overwrite != null)
            info.setReturnValue(overwrite.getBlockEntity(pos));
    }
    
    @Inject(at = @At("HEAD"), method = "getBlockEntity(III)Lnet/minecraft/world/level/block/entity/BlockEntity;", cancellable = true, require = 1)
    public void getBlockEntity(int blockX, int blockY, int blockZ, CallbackInfoReturnable<BlockEntity> info) {
        if (overwrite != null)
            info.setReturnValue(overwrite.getBlockEntity(new BlockPos(blockX, blockY, blockZ)));
    }
    
    @Inject(at = @At("HEAD"), method = "getBlockTint(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/ColorResolver;)I", cancellable = true, require = 1)
    public void getBlockTint(BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> info) {
        if (overwrite != null)
            info.setReturnValue(overwrite.getBlockTint(pos, resolver));
    }
    
    @Inject(at = @At("HEAD"), method = "getBlockEntityRenderData(Lnet/minecraft/core/BlockPos;)Ljava/lang/Object;", cancellable = true, require = 1)
    public void getBlockEntityRenderData(BlockPos pos, CallbackInfoReturnable<Object> info) {
        if (overwrite != null) {
            var blockEntity = ((LevelSlice) (Object) this).getBlockEntity(pos);
            info.setReturnValue(PlatformLevelAccess.getInstance().getBlockEntityData(blockEntity));
        }
    }
    
    @Inject(at = @At("HEAD"), method = "getPlatformModelData(Lnet/minecraft/core/BlockPos;)Lnet/caffeinemc/mods/sodium/client/services/SodiumModelData;", cancellable = true,
            require = 1)
    public void getPlatformModelData(BlockPos pos, CallbackInfoReturnable<SodiumModelData> info) {
        if (overwrite != null)
            info.setReturnValue(PlatformModelAccess.getInstance().getModelDataContainer(overwrite, SectionPos.of(pos)).getModelData(pos));
    }
    
    @Inject(at = @At("HEAD"), method = "getBiomeFabric(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", cancellable = true, require = 1)
    public void getBiomeFabric(BlockPos pos, CallbackInfoReturnable<Holder<Biome>> info) {
        if (overwrite != null)
            info.setReturnValue(overwrite.getBiome(pos));
    }
    
    @Inject(at = @At("HEAD"), method = "getBlockEntityRenderAttachment(Lnet/minecraft/core/BlockPos;)Ljava/lang/Object;", cancellable = true, require = 1)
    public void getBlockEntityRenderAttachment(BlockPos pos, CallbackInfoReturnable<Object> info) {
        if (overwrite != null) {
            var blockEntity = ((LevelSlice) (Object) this).getBlockEntity(pos);
            info.setReturnValue(PlatformLevelAccess.getInstance().getBlockEntityData(blockEntity));
        }
    }
    
    @Inject(at = @At("HEAD"), method = "hasBiomeBlend()Z", cancellable = true, require = 1)
    public void hasBiomeBlend(CallbackInfoReturnable<Boolean> info) {
        if (overwrite != null)
            info.setReturnValue(Minecraft.getInstance().options.biomeBlendRadius().get() > 0);
    }
}
