package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.RenderType;

@Mixin(TerrainRenderPass.class)
public interface TerrainRenderPassAccessor {
    
    @Accessor(remap = false)
    public RenderType getLayer();
    
}
