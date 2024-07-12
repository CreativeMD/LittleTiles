package team.creative.littletiles.client.mod.oculus;

import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildBuffers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

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
        if (isShaders())
            OculusInteractor.setLocalPos((ChunkBuildBuffers) buffers, pos);
    }
    
    public static void setMaterialId(Object buffers, BlockState state) {
        if (isShaders())
            OculusInteractor.setMaterialId((ChunkBuildBuffers) buffers, state);
    }
    
    public static void resetBlockContext(Object buffers) {
        if (isShaders())
            OculusInteractor.resetBlockContext((ChunkBuildBuffers) buffers);
    }
    
    public static Object createVertexFormat(Object format) {
        return OculusInteractor.createVertexFormat(format);
    }
}
