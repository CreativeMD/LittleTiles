package team.creative.littletiles.mixin.rubidium;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.jellysquid.mods.sodium.client.model.IndexBufferBuilder;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import net.minecraft.world.phys.Vec3;

@Mixin(BlockRenderer.class)
public class BlockRendererMixin {
    
    @Inject(method = "writeGeometry", remap = false, require = 1, at = @At(value = "FIELD",
            target = "Lme/jellysquid/mods/sodium/client/render/vertex/type/ChunkVertexEncoder$Vertex;color:I", remap = false, opcode = Opcodes.PUTFIELD, shift = Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void mulColor(BlockRenderContext ctx, ChunkVertexBufferBuilder vertexBuffer, IndexBufferBuilder indexBuffer, Vec3 offset, ModelQuadView quad, int[] colors, float[] brightness, int[] lightmap, CallbackInfo info, ModelQuadOrientation orientation, ChunkVertexEncoder.Vertex[] vertices, int dstIndex, int srcIndex, ChunkVertexEncoder.Vertex out) {
        int color = colors != null ? colors[srcIndex] : quad.getColor(srcIndex);
        float w = brightness[srcIndex];
        float r = ColorABGR.unpackRed(color) * w;
        float g = ColorABGR.unpackGreen(color) * w;
        float b = ColorABGR.unpackBlue(color) * w;
        int a = ColorABGR.unpackAlpha(color);
        out.color = ColorABGR.pack((int) r, (int) g, (int) b, a);
    }
    
}
