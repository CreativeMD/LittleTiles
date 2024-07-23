package team.creative.littletiles.client.render.mc;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection;

public interface ViewAreaExtender {
    
    public RenderSection getSection(long pos);
    
}
