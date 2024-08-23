package team.creative.littletiles.client.mod.embeddium.renderer;

import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderInterface;
import org.embeddedt.embeddium.impl.render.viewport.CameraTransform;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;

public interface DefaultChunkRendererExtender {
    
    public static final int REGION_WIDTH_M = RenderRegion.REGION_WIDTH - 1;
    public static final int REGION_HEIGHT_M = RenderRegion.REGION_HEIGHT - 1;
    public static final int REGION_LENGTH_M = RenderRegion.REGION_LENGTH - 1;
    
    public static final int REGION_WIDTH_SH = Integer.bitCount(REGION_WIDTH_M);
    public static final int REGION_HEIGHT_SH = Integer.bitCount(REGION_HEIGHT_M);
    public static final int REGION_LENGTH_SH = Integer.bitCount(REGION_LENGTH_M);
    
    public void begin(RenderType layer);
    
    public void end(RenderType layer);
    
    public static void setRenderRegionOffset(ChunkShaderInterface shader, BlockPos pos, CameraTransform camera) {
        float x = getCameraTranslation(((pos.getX() >> 4 >> REGION_WIDTH_SH) << REGION_WIDTH_SH) << 4, camera.intX, camera.fracX);
        float y = getCameraTranslation(((pos.getY() >> 4 >> REGION_HEIGHT_SH) << REGION_HEIGHT_SH) << 4, camera.intY, camera.fracY);
        float z = getCameraTranslation(((pos.getZ() >> 4 >> REGION_LENGTH_SH) << REGION_LENGTH_SH) << 4, camera.intZ, camera.fracZ);
        shader.setRegionOffset(x, y, z);
    }
    
    public static BlockPos regionOffset(BlockPos pos) {
        return new BlockPos(((pos.getX() >> 4 >> REGION_WIDTH_SH) << REGION_WIDTH_SH) << 4, ((pos.getY() >> 4 >> REGION_HEIGHT_SH) << REGION_HEIGHT_SH) << 4, ((pos
                .getZ() >> 4 >> REGION_LENGTH_SH) << REGION_LENGTH_SH) << 4);
    }
    
    private static float getCameraTranslation(int chunkBlockPos, int cameraBlockPos, float cameraPos) {
        return (chunkBlockPos - cameraBlockPos) - cameraPos;
    }
}
