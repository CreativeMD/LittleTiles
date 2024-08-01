package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EmbeddiumWorldRenderer.class)
public interface EmbeddiumWorldRendererAccessor {
    
    @Accessor(remap = false)
    public RenderSectionManager getRenderSectionManager();
    
}
