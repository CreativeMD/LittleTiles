package team.creative.littletiles.client.mod.oculus;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.irisshaders.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.rubidium.RubidiumManager;
import team.creative.littletiles.mixin.rubidium.ChunkMeshBufferBuilderAccessor;

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
            ext.iris$setMaterialId(state, ExtendedDataHelper.BLOCK_RENDER_TYPE, (byte) 0);
    }
    
    public static void resetBlockContext(ChunkBuildBuffers buffers) {
        if (buffers instanceof ChunkBuildBuffersExt ext)
            ext.iris$resetBlockContext();
    }
    
    public static void setMid(ChunkMeshBufferBuilderAccessor vertexBuffer, Vec3d center) {
        if (vertexBuffer.getEncoder() instanceof XHFPTerrainVertexExtender ex)
            ex.setCenter(center);
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
