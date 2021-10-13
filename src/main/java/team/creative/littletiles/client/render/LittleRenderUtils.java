package team.creative.littletiles.client.render;

import net.minecraft.client.renderer.RenderType;

public class LittleRenderUtils {
    
    public static final RenderType[] BLOCK_LAYERS = new RenderType[] { RenderType.solid(), RenderType.cutoutMipped(), RenderType.cutout(), RenderType.translucent() };
    
}
