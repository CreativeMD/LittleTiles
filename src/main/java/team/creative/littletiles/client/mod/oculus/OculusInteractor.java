package team.creative.littletiles.client.mod.oculus;

import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildBuffers;

import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.rubidium.RubidiumManager;

public class OculusInteractor {
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Oculus extension");
    }
    
    public static boolean isShaders() {
        return IrisApi.getInstance().isShaderPackInUse();
    }
    
    public static void setLocalPos(ChunkBuildBuffers buffers, BlockPos pos) {
        if (buffers instanceof ChunkBuildBuffersExt ext)
            ext.iris$setLocalPos(pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static void setMaterialId(ChunkBuildBuffers buffers, BlockState state) {
        if (buffers instanceof ChunkBuildBuffersExt ext)
            ext.iris$setMaterialId(state, ExtendedDataHelper.BLOCK_RENDER_TYPE, (byte) state.getLightEmission());
    }
    
    public static void resetBlockContext(ChunkBuildBuffers buffers) {
        if (buffers instanceof ChunkBuildBuffersExt ext)
            ext.iris$resetBlockContext();
    }
    
    public static Object getShader(Object object) {
        if (object instanceof ShaderChunkRendererExt ex && ex.iris$getOverride() != null)
            return ex.iris$getOverride().getInterface();
        return null;
    }
    
    public static Object createVertexFormat(Object format) {
        if (RubidiumManager.installed())
            return OculusSodiumInteractor.createVertexFormatEmbeddium(format);
        return null;
    }
    
}
