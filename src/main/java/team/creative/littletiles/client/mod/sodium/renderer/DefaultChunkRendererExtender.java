package team.creative.littletiles.client.mod.sodium.renderer;

import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;

public interface DefaultChunkRendererExtender {
    
    public void begin(RenderType layer);
    
    public void end(RenderType layer);
    
    public static void setRenderRegionOffset(ChunkShaderInterface shader, BlockPos pos, CameraTransform camera) {
        float x = getCameraTranslation(((pos.getX() >> 4 >> RenderRegion.REGION_WIDTH_SH) << RenderRegion.REGION_WIDTH_SH) << 4, camera.intX, camera.fracX);
        float y = getCameraTranslation(((pos.getY() >> 4 >> RenderRegion.REGION_HEIGHT_SH) << RenderRegion.REGION_HEIGHT_SH) << 4, camera.intY, camera.fracY);
        float z = getCameraTranslation(((pos.getZ() >> 4 >> RenderRegion.REGION_LENGTH_SH) << RenderRegion.REGION_LENGTH_SH) << 4, camera.intZ, camera.fracZ);
        shader.setRegionOffset(x, y, z);
    }
    
    public static BlockPos regionOffset(BlockPos pos) {
        return new BlockPos(((pos.getX() >> 4 >> RenderRegion.REGION_WIDTH_SH) << RenderRegion.REGION_WIDTH_SH) << 4, ((pos
                .getY() >> 4 >> RenderRegion.REGION_HEIGHT_SH) << RenderRegion.REGION_HEIGHT_SH) << 4, ((pos
                        .getZ() >> 4 >> RenderRegion.REGION_LENGTH_SH) << RenderRegion.REGION_LENGTH_SH) << 4);
    }
    
    private static float getCameraTranslation(int chunkBlockPos, int cameraBlockPos, float cameraPos) {
        return (chunkBlockPos - cameraBlockPos) - cameraPos;
    }
}
