package team.creative.littletiles.mixin.rubidium;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.minecraft.world.phys.Vec3;

@Mixin(BlockRenderer.class)
public class BlockRendererMixin {
    
    @Inject(method = "writeGeometry", remap = false, require = 1, at = @At(value = "FIELD",
            target = "Lme/jellysquid/mods/sodium/client/render/vertex/type/ChunkVertexEncoder$Vertex;color:I", remap = false, opcode = Opcodes.PUTFIELD, shift = Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void mulColor(BlockRenderContext ctx, ChunkModelBuilder builder, Vec3 offset, Material material, BakedQuadView quad, int[] colors, QuadLightData light, CallbackInfo info, ModelQuadOrientation orientation, ChunkVertexEncoder.Vertex[] vertices, int dstIndex, int srcIndex, ChunkVertexEncoder.Vertex out) {
        int color = colors != null ? colors[srcIndex] : quad.getColor(srcIndex);
        float w = light.br[srcIndex];
        float r = ColorARGB.unpackRed(color) * w;
        float g = ColorARGB.unpackGreen(color) * w;
        float b = ColorARGB.unpackBlue(color) * w;
        int a = ColorARGB.unpackAlpha(color);
        out.color = ColorABGR.pack((int) r, (int) g, (int) b, a);
    }
    
}
