package team.creative.littletiles.mixin.sodium;

import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderTask;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.mod.sodium.buffer.SodiumBufferUploader;
import team.creative.littletiles.client.mod.sodium.renderer.BlockRendererExtender;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin extends ChunkBuilderTask<ChunkBuildOutput> {
    
    @Unique
    public ChunkLayerMap<BufferCollection> caches;
    
    @Unique
    public ChunkBuildContext buildContext;
    
    private ChunkBuilderMeshingTaskMixin(RenderSection render, int time, Vector3dc absoluteCameraPos) {
        super(render, time, absoluteCameraPos);
    }
    
    @Inject(at = @At("TAIL"), remap = false, require = 1,
            method = "<init>(Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSection;ILorg/joml/Vector3dc;Lnet/caffeinemc/mods/sodium/client/world/cloned/ChunkRenderContext;Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/SortBehavior;Z)V")
    public void onCreated(RenderSection render, int time, Vector3dc absoluteCameraPos, ChunkRenderContext renderContext, SortBehavior sortBehavior, boolean forceSort,
            CallbackInfo info) {
        LittleRenderPipelineType.startCompile((RenderChunkExtender) render);
    }
    
    @Inject(at = @At("HEAD"), remap = false, require = 1,
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    public void performBuildStart(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> info) {
        this.buildContext = buildContext;
    }
    
    @Unique
    public SodiumBufferUploader getOrCreateUploader(RenderType layer) {
        SodiumBufferUploader uploader = (SodiumBufferUploader) buildContext.buffers.get(DefaultMaterials.forRenderLayer(layer));
        if (layer == RenderType.translucent())
            uploader.setTranslucentCollector(((BlockRendererExtender) buildContext.cache.getBlockRenderer()).getTranslucentCollector());
        return uploader;
    }
    
    @Redirect(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            remap = false, require = 1, at = @At(value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
                    remap = true))
    public BlockEntity getBlockEntity(LevelSlice slice, BlockPos pos) {
        BlockEntity entity = slice.getBlockEntity(pos);
        if (entity instanceof BETiles be)
            LittleRenderPipelineType.compile(SectionPos.asLong(render.getChunkX(), render.getChunkY(), render.getChunkZ()), be, this::getOrCreateUploader,
                this::getOrCreateBuffers);
        return entity;
    }
    
    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformLevelRenderHooks;runChunkMeshAppenders(Ljava/util/List;Ljava/util/function/Function;Lnet/caffeinemc/mods/sodium/client/world/LevelSlice;)V"),
            remap = false, require = 1,
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    public void beforeBuildEnds(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> info) {
        LittleRenderPipelineType.beforeCompileEnds((RenderChunkExtender) render, this::getOrCreateUploader, this::getOrCreateBuffers);
    }
    
    @Inject(at = @At("TAIL"), remap = false, require = 1,
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    public void performBuildEnd(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> info) {
        LittleRenderPipelineType.endCompile((RenderChunkExtender) render);
        this.buildContext = null;
        this.caches = null;
    }
    
    @Unique
    public BufferCollection getOrCreateBuffers(RenderType layer) {
        if (caches == null)
            caches = new ChunkLayerMap<>();
        BufferCollection cache = caches.get(layer);
        if (cache == null)
            caches.put(layer, cache = new BufferCollection());
        return cache;
    }
    
}
