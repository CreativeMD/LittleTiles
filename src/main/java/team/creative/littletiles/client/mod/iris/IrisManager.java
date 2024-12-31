package team.creative.littletiles.client.mod.iris;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
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
    
    public static void beginBlock(ChunkBuildBuffers buffers, BlockState state, BlockPos pos) {
        if (INSTALLED)
            IrisInteractor.beginBlock(buffers, state, pos);
    }
    
    public static void resetBlockContext(ChunkBuildBuffers buffers) {
        if (INSTALLED)
            IrisInteractor.resetBlockContext(buffers);
    }
    
}
