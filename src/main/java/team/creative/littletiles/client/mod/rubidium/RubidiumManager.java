package team.creative.littletiles.client.mod.rubidium;

import net.minecraftforge.fml.ModList;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;

public class RubidiumManager {
    
    private static final String MODID = "rubidium";
    private static final boolean INSTALLED = ModList.get().isLoaded(MODID);
    
    public static boolean installed() {
        return INSTALLED;
    }
    
    public static void init() {
        if (installed())
            RubidiumInteractor.init();
    }
    
    public static boolean isRubidiumBuffer(BufferHolder first, BufferHolder second) {
        return INSTALLED && (RubidiumInteractor.isRubidiumBuffer(first) || RubidiumInteractor.isRubidiumBuffer(second));
    }
    
    public static BufferHolder combineBuffers(BufferHolder first, BufferHolder second) {
        return RubidiumInteractor.combineBuffers(first, second);
    }
    
}
