package team.creative.littletiles.mixin.sodium;

import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.trigger.SortTriggering;

@Mixin(RenderSectionManager.class)
public interface RenderSectionManagerAccessor {
    
    @Invoker(remap = false)
    public RenderSection callGetRenderSection(int x, int y, int z);
    
    @Accessor(remap = false)
    public RenderRegionManager getRegions();
    
    @Accessor(remap = false)
    public Vector3dc getCameraPosition();
    
    @Accessor(remap = false)
    public SortTriggering getSortTriggering();
    
}
