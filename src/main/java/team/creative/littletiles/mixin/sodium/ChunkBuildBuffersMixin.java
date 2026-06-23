package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

@Mixin(ChunkBuildBuffers.class)
public class ChunkBuildBuffersMixin {
    
    @Unique
    private int translucentOffset;
    
    @Inject(method = "createMesh(Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;IZZ)Lnet/caffeinemc/mods/sodium/client/render/chunk/data/BuiltSectionMeshParts;",
            at = @At("RETURN"), require = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    public void moveTranslucentBuffers(TerrainRenderPass pass, int visibleSlices, boolean forceUnassigned, boolean sliceReordering,
            CallbackInfoReturnable<BuiltSectionMeshParts> info, BakedChunkModelBuilder builder, int[] vertexSegments, int vertexTotal) {
        if (forceUnassigned) {
            var unassigned = builder.getVertexBuffer(ModelQuadFacing.UNASSIGNED);
            // counting all buffers except of unassigned. This is to know how far translucent bytes are moved
            translucentOffset = (vertexTotal - unassigned.count()) * ((ChunkMeshBufferBuilderAccessor) unassigned).getStride();
        }
    }
    
}
