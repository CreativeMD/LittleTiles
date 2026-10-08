package team.creative.littletiles.client.mod.iris;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.MeshData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

public class IrisManager {
    
    private static final String MODID = "iris";
    private static final boolean INSTALLED = ModList.get().isLoaded(MODID);
    
    public static boolean installed() {
        return INSTALLED;
    }
    
    public static void init() {
        if (installed())
            IrisInteractor.init();
    }
    
    public static boolean isShaders() {
        if (INSTALLED)
            return IrisInteractor.isShaders();
        return false;
    }

    public static boolean uploadCachedVertices(VertexConsumer consumer, ByteBuffer buffer, VertexFormat format) {
        if (INSTALLED && IrisInteractor.isShaders())
            return IrisInteractor.uploadCachedVertices(consumer, buffer, format);
        return false;
    }

    public static void recalculateCacheNormals(MeshData mesh) {
        if (INSTALLED && IrisInteractor.isShaders())
            IrisInteractor.recalculateCacheNormals(mesh);
    }
    
    public static void beginBlock(Object buffers, BlockState state, BlockPos pos) {
        if (INSTALLED)
            IrisInteractor.beginBlock(buffers, state, pos);
    }
    
    public static void resetBlockContext(Object buffers) {
        if (INSTALLED)
            IrisInteractor.resetBlockContext(buffers);
    }
    
}
