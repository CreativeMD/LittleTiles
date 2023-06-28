package team.creative.littletiles.client.mod.oculus;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.mixin.rubidium.ChunkVertexBufferBuilderAccessor;

public class OculusManager {
    
    private static final String MODID = "oculus";
    private static final boolean INSTALLED = ModList.get().isLoaded(MODID);
    
    public static boolean installed() {
        return INSTALLED;
    }
    
    public static void init() {
        if (installed())
            OculusInteractor.init();
    }
    
    public static boolean isShaders() {
        return INSTALLED && OculusInteractor.isShaders();
    }
    
    public static void setLocalPos(Object buffers, BlockPos pos) {
        if (OculusInteractor.isShaders())
            OculusInteractor.setLocalPos((ChunkBuildBuffers) buffers, pos);
    }
    
    public static void setMaterialId(Object buffers, BlockState state) {
        if (OculusInteractor.isShaders())
            OculusInteractor.setMaterialId((ChunkBuildBuffers) buffers, state);
    }
    
    public static void resetBlockContext(Object buffers) {
        if (OculusInteractor.isShaders())
            OculusInteractor.resetBlockContext((ChunkBuildBuffers) buffers);
    }
    
    public static void setMid(Object vertexBuffer, Vec3d center) {
        if (OculusInteractor.isShaders())
            OculusInteractor.setMid((ChunkVertexBufferBuilderAccessor) vertexBuffer, center);
        
    }
    
}
