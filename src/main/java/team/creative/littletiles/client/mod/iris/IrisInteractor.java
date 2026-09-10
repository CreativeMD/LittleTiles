package team.creative.littletiles.client.mod.iris;

import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
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
    
    public static void beginBlock(Object buffers, BlockState state, BlockPos pos) {
        var ids = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        if (ids == null)
            return;
        if (buffers instanceof BlockSensitiveBufferBuilder ext)
            ext.beginBlock(ids.getOrDefault(state, -1), state.liquid() ? (byte) 1 : (byte) 0, (byte) state.getLightEmission(), pos.getX(), pos.getY(), pos.getZ());
        else if (buffers instanceof VertexEncoderInterface ext)
            ext.beginBlock(ids.getOrDefault(state, -1), state.liquid() ? (byte) 1 : (byte) 0, (byte) state.getLightEmission(), pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static void resetBlockContext(Object buffers) {
        if (buffers instanceof BlockSensitiveBufferBuilder ext)
            ext.endBlock();
        else if (buffers instanceof VertexEncoderInterface ext)
            ext.restoreBlock();
    }
    
}
