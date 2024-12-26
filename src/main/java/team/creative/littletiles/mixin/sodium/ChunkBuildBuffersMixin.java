package team.creative.littletiles.mixin.sodium;

import java.nio.ByteBuffer;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

@Mixin(ChunkBuildBuffers.class)
public class ChunkBuildBuffersMixin {
    
    @Unique
    private int translucentOffset;
    
    @Inject(method = "createMesh(Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;Z)Lnet/caffeinemc/mods/sodium/client/render/chunk/data/BuiltSectionMeshParts;",
            at = @At(value = "FIELD",
                    target = "Lnet/caffeinemc/mods/sodium/client/model/quad/properties/ModelQuadFacing;UNASSIGNED:Lnet/caffeinemc/mods/sodium/client/model/quad/properties/ModelQuadFacing;",
                    opcode = Opcodes.GETSTATIC), require = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    public void moveTranslucentBuffers(TerrainRenderPass pass, boolean forceUnassigned, CallbackInfoReturnable<BuiltSectionMeshParts> info, BakedChunkModelBuilder builder,
            List<ByteBuffer> vertexBuffers) {
        
        translucentOffset = 0;
        for (int i = 0; i < vertexBuffers.size() - 1; i++) // counting all buffers, UNASSIGNED is per definition the last one that's why it is not included
            if (vertexBuffers.get(i) != null)
                translucentOffset += vertexBuffers.get(i).capacity();
    }
    
}
