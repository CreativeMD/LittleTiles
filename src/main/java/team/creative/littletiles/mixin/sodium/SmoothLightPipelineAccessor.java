package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline;

@Mixin(SmoothLightPipeline.class)
public interface SmoothLightPipelineAccessor {
    
    @Accessor
    public void setCachedPos(long pos);
    
}
