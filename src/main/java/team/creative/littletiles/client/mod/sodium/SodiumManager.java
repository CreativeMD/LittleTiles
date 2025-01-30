package team.creative.littletiles.client.mod.sodium;

import net.neoforged.fml.ModList;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

public class SodiumManager {
    
    private static final String[] MODIDS = new String[] { "sodium" };
    private static final boolean INSTALLED = check();
    public static RenderingLevelHandler RENDERING_LEVEL;
    public static RenderingLevelHandler RENDERING_ANIMATION;
    
    private static boolean check() {
        ModList list = ModList.get();
        for (int i = 0; i < MODIDS.length; i++)
            if (list.isLoaded(MODIDS[i]))
                return true;
        return false;
    }
    
    public static boolean installed() {
        return INSTALLED;
    }
    
    public static void init() {
        if (installed())
            SodiumInteractor.init();
    }
    
    public static LittleEntityRenderManager createRenderManager(LittleAnimationEntity entity) {
        return SodiumInteractor.createRenderManager(entity);
    }
    
}
