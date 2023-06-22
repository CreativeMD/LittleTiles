package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import me.jellysquid.mods.sodium.client.model.IndexBufferBuilder;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.blender.ColorBlender;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import me.jellysquid.mods.sodium.client.world.biome.BlockColorsExtended;
import net.minecraft.world.phys.Vec3;

@Mixin(BlockRenderer.class)
public interface BlockRendererAccessor {
    
    @Accessor(remap = false)
    public boolean getUseAmbientOcclusion();
    
    @Accessor(remap = false)
    public ColorBlender getColorBlender();
    
    @Accessor(remap = false)
    public BlockColorsExtended getBlockColors();
    
    @Invoker(remap = false)
    public void callWriteGeometry(BlockRenderContext ctx, ChunkVertexBufferBuilder vertexBuffer, IndexBufferBuilder indexBuffer, Vec3 offset, ModelQuadView quad, int[] colors, float[] brightness, int[] lightmap);
}
