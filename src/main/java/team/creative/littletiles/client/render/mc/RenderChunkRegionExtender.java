package team.creative.littletiles.client.render.mc;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;

public interface RenderChunkRegionExtender {
    
    public static RenderChunkRegion createFake(Level level) {
        RenderChunkRegion region = CreativeHackery.allocateInstance(RenderChunkRegion.class);
        ((RenderChunkRegionExtender) region).setFake(level);
        return region;
    }
    
    public void setFake(Level level);
    
}
