package team.creative.littletiles.mixin.client.render;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent.AdditionalSectionRenderer;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.SectionCompilerResultsExtender;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
    
    private static final String COMPILE_CALL = "Lnet/minecraft/client/renderer/chunk/SectionCompiler;compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;";
    
    @Inject(method = COMPILE_CALL, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/SectionCompiler;handleBlockEntity(Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"),
            require = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    public void compile(SectionPos pos, RenderChunkRegion region, VertexSorting sorting, SectionBufferBuilderPack pack, List<AdditionalSectionRenderer> additionalRenderers,
            CallbackInfoReturnable<SectionCompiler.Results> info, SectionCompiler.Results results, BlockPos blockpos, BlockPos blockpos1, VisGraph visgraph, PoseStack posestack,
            Map<RenderType, BufferBuilder> map, RandomSource randomsource, BlockPos blockpos2, BlockState blockstate, BlockEntity blockentity) {
        if (blockentity instanceof BETiles tiles)
            LittleRenderPipelineType.compile(pos.asLong(), tiles, x -> (ChunkBufferUploader) getOrBeginLayer(map, pack, x), x -> ((SectionCompilerResultsExtender) (Object) results)
                    .getOrCreate(x));
    }
    
    @Shadow
    public abstract BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> map, SectionBufferBuilderPack pack, RenderType type);
    
    @Shadow
    public abstract <E extends BlockEntity> void handleBlockEntity(SectionCompiler.Results results, E blockEntity);
}
