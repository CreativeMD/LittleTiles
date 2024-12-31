package team.creative.littletiles.client.mod.iris;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.LittleTiles;

public class IrisInteractor {
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Iris extension");
    }
    
    public static boolean isShaders() {
        return IrisApi.getInstance().isShaderPackInUse();
    }
    
    public static void beginBlock(ChunkBuildBuffers buffers, BlockState state, BlockPos pos) {
        if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null)
            return;
        if (buffers instanceof BlockSensitiveBufferBuilder ext)
            ext.beginBlock(WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(state), state.liquid() ? (byte) 1 : (byte) 0, (byte) state.getLightEmission(), pos.getX(), pos
                    .getY(), pos.getZ());
    }
    
    public static void resetBlockContext(ChunkBuildBuffers buffers) {
        if (buffers instanceof BlockSensitiveBufferBuilder ext)
            ext.endBlock();
    }
    
}
