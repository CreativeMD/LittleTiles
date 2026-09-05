package team.creative.littletiles.client.mod.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
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
    
    public static int vertexStride(VertexFormat format) {
        if (INSTALLED)
            return IrisInteractor.vertexStride(format);
        return format.getVertexSize();
    }

    public static boolean beginBlock(VertexConsumer consumer, BlockState state, BlockPos localPos) {
        if (INSTALLED)
            return IrisInteractor.beginBlock(consumer, state, localPos);
        return false;
    }

    public static boolean beginBlock(VertexConsumer consumer, BlockState state, int localX, int localY, int localZ) {
        if (INSTALLED)
            return IrisInteractor.beginBlock(consumer, state, localX, localY, localZ);
        return false;
    }

    public static void endBlock(VertexConsumer consumer) {
        if (INSTALLED)
            IrisInteractor.endBlock(consumer);
    }
    
    public static void setBlockContext(BlockRenderer renderer, BlockState state, BlockPos pos) {
        if (INSTALLED)
            IrisInteractor.setBlockContext(renderer, state, pos);
    }
    
}
