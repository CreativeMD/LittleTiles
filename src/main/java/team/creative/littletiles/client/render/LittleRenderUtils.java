package team.creative.littletiles.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.littletiles.mixin.LevelRendererAccessor;
import team.creative.littletiles.mixin.ViewAreaAccessor;

public class LittleRenderUtils {
    
    private static Minecraft mc = Minecraft.getInstance();
    
    public static RenderChunk getRenderChunk(BlockPos pos) {
        return ((ViewAreaAccessor) ((LevelRendererAccessor) mc.levelRenderer).getViewArea()).getChunkAt(pos);
    }
    
    public static Object getRenderChunk(IOrientatedLevel level, BlockPos pos) {
        //TODO Still needs to be implemented
        return null;
    }
    
}
