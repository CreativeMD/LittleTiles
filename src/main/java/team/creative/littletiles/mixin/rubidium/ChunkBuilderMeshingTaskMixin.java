package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.mod.rubidium.data.BuiltSectionMeshPartsExtender;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(ChunkBuilderMeshingTask.class)
public class ChunkBuilderMeshingTaskMixin implements RebuildTaskExtender {
    
    @Shadow(remap = false)
    @Final
    private RenderSection render;
    
    @Unique
    public ChunkLayerMap<BufferCollection> caches;
    
    @Unique
    public ChunkBuildContext buildContext;
    
    @Inject(at = @At("HEAD"), remap = false, require = 1,
            method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    public void performBuildStart(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> info) {
        LittleRenderPipelineType.startCompile((RenderChunkExtender) render, this);
        this.buildContext = buildContext;
    }
    
    @Redirect(
            method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            remap = false, require = 1, at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;",
                    remap = true))
    public BlockEntityRenderer<BlockEntity> getRendererRedirect(BlockEntityRenderDispatcher dispatcher, BlockEntity entity) {
        if (entity instanceof BETiles be)
            LittleRenderPipelineType.compile((RenderChunkExtender) render, be, this);
        return dispatcher.getRenderer(entity);
    }
    
    @Inject(at = @At("TAIL"), remap = false, require = 1,
            method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;")
    public void performBuildEnd(ChunkBuildContext buildContext, CancellationToken cancellationSource, CallbackInfoReturnable<ChunkBuildOutput> info) {
        LittleRenderPipelineType.endCompile((RenderChunkExtender) render, this);
        this.buildContext = null;
        this.caches = null;
    }
    
    @Redirect(
            method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            remap = false, at = @At(value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildBuffers;createMesh(Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)Lme/jellysquid/mods/sodium/client/render/chunk/data/BuiltSectionMeshParts;"))
    public BuiltSectionMeshParts createMesh(ChunkBuildBuffers buffers, TerrainRenderPass pass) {
        BuiltSectionMeshParts parts = buffers.createMesh(pass);
        if (parts != null && caches != null)
            ((BuiltSectionMeshPartsExtender) parts).setBuffers(caches.get(((TerrainRenderPassAccessor) pass).getLayer()));
        return parts;
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
    
    @Override
    public BufferCache upload(RenderType layer, BufferCache cache) {
        if (cache.upload((ChunkBufferUploader) buildContext.buffers.get(DefaultMaterials.forRenderLayer(layer)))) {
            getOrCreateBuffers(layer).queueForUpload(cache);
            return cache;
        }
        return null;
    }
    
}
