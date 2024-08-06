package team.creative.littletiles.mixin.client.render;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection;
import net.minecraft.core.SectionPos;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.SectionCompilerResultsExtender;

@Mixin(targets = "net/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection$RebuildTask")
public abstract class RebuildTaskMixin {
    
    private static final String COMPILE_CALL = "Lnet/minecraft/client/renderer/chunk/SectionCompiler;compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;";
    
    @Shadow(aliases = { "this$0" })
    public RenderSection this$1;
    
    @Inject(at = @At(value = "INVOKE", target = COMPILE_CALL), method = "doTask(Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Ljava/util/concurrent/CompletableFuture;",
            require = 1)
    private void compileStart(SectionBufferBuilderPack pack, CallbackInfoReturnable<CompletableFuture> info) {
        LittleRenderPipelineType.startCompile((RenderChunkExtender) this$1);
    }
    
    @Inject(at = @At(value = "INVOKE", target = COMPILE_CALL, shift = Shift.AFTER),
            method = "doTask(Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Ljava/util/concurrent/CompletableFuture;", require = 1)
    private void compileEnd(SectionBufferBuilderPack pack, CallbackInfoReturnable<CompletableFuture> info) {
        LittleRenderPipelineType.endCompile((RenderChunkExtender) this$1);
    }
    
    @Inject(method = "doTask(Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Ljava/util/concurrent/CompletableFuture;", at = @At("TAIL"), cancellable = true, require = 1,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injected(SectionBufferBuilderPack pack, CallbackInfoReturnable<CompletableFuture> cir, RenderChunkRegion renderchunkregion, SectionPos sectionpos,
            SectionCompiler.Results results) {
        cir.setReturnValue(cir.getReturnValue().whenComplete((result, exception) -> {
            if (((Enum) result).ordinal() == 0) { // Successful
                ((RenderChunkExtender) this$1).prepareUpload();
                var caches = ((SectionCompilerResultsExtender) (Object) results).getCaches();
                if (caches != null)
                    for (Tuple<RenderType, BufferCollection> tuple : caches.tuples())
                        ((RenderChunkExtender) this$1).uploaded(tuple.key, tuple.value);
            }
        }));
    }
    
}
