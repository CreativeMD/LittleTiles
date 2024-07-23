package team.creative.littletiles.mixin.rubidium;

import java.util.ArrayList;
import java.util.Collection;

import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildOutput;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import team.creative.littletiles.client.mod.rubidium.data.BuiltSectionMeshPartsExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;

@Mixin(RenderRegionManager.class)
public class RenderRegionManagerMixin {
    
    @Inject(method = "uploadMeshes(Lorg/embeddedt/embeddium/impl/gl/device/CommandList;Lorg/embeddedt/embeddium/impl/render/chunk/region/RenderRegion;Ljava/util/Collection;)V",
            at = @At("HEAD"), require = 1, remap = false)
    private void afterStorageSet(CommandList commandList, RenderRegion region, Collection<ChunkBuildOutput> results, CallbackInfo info) {
        for (ChunkBuildOutput output : results)
            ((RenderChunkExtender) output.render).prepareUpload();
    }
    
    @Inject(method = "uploadMeshes(Lorg/embeddedt/embeddium/impl/gl/device/CommandList;Lorg/embeddedt/embeddium/impl/render/chunk/region/RenderRegion;Ljava/util/Collection;)V",
            at = @At("TAIL"), require = 1, remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
    private void endUploadMeshes(CommandList commandList, RenderRegion region, Collection<ChunkBuildOutput> results, CallbackInfo info, ArrayList uploads) {
        for (Object upload : uploads) {
            PendingSectionUploadAccessor p = (PendingSectionUploadAccessor) upload;
            ((RenderChunkExtender) p.getSection()).uploaded(((TerrainRenderPassAccessor) p.getPass()).getLayer(), ((BuiltSectionMeshPartsExtender) p.getMeshData()).getBuffers());
        }
    }
}
