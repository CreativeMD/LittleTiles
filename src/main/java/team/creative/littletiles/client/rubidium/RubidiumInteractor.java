package team.creative.littletiles.client.rubidium;

import net.minecraftforge.fml.ModList;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;

public class RubidiumInteractor {
    
    private static final String MODID = "rubidium";
    private static final boolean installed = ModList.get().isLoaded(MODID);
    
    public static boolean isInstalled() {
        return installed;
    }
    
    public static void init() {
        if (isInstalled())
            RubidiumManager.init();
    }
    
    public static boolean isRubidiumBuffer(BufferHolder first, BufferHolder second) {
        return installed && (RubidiumManager.isRubidiumBuffer(first) || RubidiumManager.isRubidiumBuffer(second));
    }
    
    public static BufferHolder combineBuffers(BufferHolder first, BufferHolder second) {
        return RubidiumManager.combineBuffers(first, second);
    }
    
}
