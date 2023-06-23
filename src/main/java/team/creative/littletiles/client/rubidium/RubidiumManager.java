package team.creative.littletiles.client.rubidium;

import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.rubidium.pipeline.LittleRenderPipelineTypeRubidium;

public class RubidiumManager {
    
    public static final LittleRenderPipelineTypeRubidium PIPELINE = new LittleRenderPipelineTypeRubidium();
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Rubidium extension");
    }
    
    public static BlockRenderPass getPass(RenderType layer) {
        if (layer == RenderType.solid())
            return BlockRenderPass.SOLID;
        if (layer == RenderType.cutout())
            return BlockRenderPass.CUTOUT;
        if (layer == RenderType.cutoutMipped())
            return BlockRenderPass.CUTOUT_MIPPED;
        if (layer == RenderType.translucent())
            return BlockRenderPass.TRANSLUCENT;
        if (layer == RenderType.tripwire())
            return BlockRenderPass.TRIPWIRE;
        throw new IllegalArgumentException();
    }
}
