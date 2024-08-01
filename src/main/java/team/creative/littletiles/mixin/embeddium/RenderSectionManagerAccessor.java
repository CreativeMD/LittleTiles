package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderSectionManager.class)
public interface RenderSectionManagerAccessor {
    
    @Invoker(remap = false)
    public RenderSection callGetRenderSection(int x, int y, int z);
    
    @Accessor(remap = false)
    public RenderRegionManager getRegions();
    
}
