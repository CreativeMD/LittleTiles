package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.culling.Frustum;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    
    @Accessor
    public ViewArea getViewArea();
    
    @Accessor
    public Frustum getCapturedFrustum();
    
    @Accessor
    public Frustum getCullingFrustum();
    
}
