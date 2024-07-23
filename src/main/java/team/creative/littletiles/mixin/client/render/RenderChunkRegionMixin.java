package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import team.creative.littletiles.client.render.mc.RenderChunkRegionExtender;

@Mixin(RenderChunkRegion.class)
public class RenderChunkRegionMixin implements RenderChunkRegionExtender {
    
    @Unique
    private boolean fake;
    
    @Mutable
    @Shadow
    protected Level level;
    
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true, require = 1)
    public void getBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> info) {
        if (fake)
            info.setReturnValue(level.getBlockState(pos));
    }
    
    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true, require = 1)
    public void getFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> info) {
        if (fake)
            info.setReturnValue(level.getFluidState(pos));
    }
    
    @Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true, require = 1)
    public void getBlockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> info) {
        if (fake)
            info.setReturnValue(level.getBlockEntity(pos));
    }
    
    @Inject(method = "getModelData", at = @At("HEAD"), cancellable = true, require = 1)
    public void getModelData(BlockPos pos, CallbackInfoReturnable<ModelData> info) {
        if (fake)
            info.setReturnValue(level.getModelData(pos));
    }
    
    @Inject(method = "getAuxLightManager", at = @At("HEAD"), cancellable = true, require = 1)
    public void getAuxLightManager(ChunkPos pos, CallbackInfoReturnable<AuxiliaryLightManager> info) {
        if (fake)
            info.setReturnValue(level.getAuxLightManager(pos));
    }
    
    @Override
    public void setFake(Level level) {
        this.fake = true;
        this.level = level;
    }
}
