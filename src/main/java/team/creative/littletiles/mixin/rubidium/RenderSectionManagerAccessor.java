package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;

@Mixin(RenderSectionManager.class)
public interface RenderSectionManagerAccessor {
    
    @Invoker(remap = false)
    public RenderSection callGetRenderSection(int x, int y, int z);
    
}
