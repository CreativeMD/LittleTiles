package team.creative.littletiles.client.mod.sodium;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.ModList;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

public class SodiumManager {
    
    private static final String[] MODIDS = new String[] { "sodium" };
    private static final boolean INSTALLED = check();
    public static RenderingLevelHandler RENDERING_LEVEL;
    public static RenderingLevelHandler RENDERING_ANIMATION;
    private static final ImmutableList<RenderType> CHUNK_BUFFER_LAYERS = ImmutableList.of(RenderType.solid(), RenderType.cutout(), RenderType.translucent());
    
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
    
    public static List<RenderType> chunkBufferLayers() {
        if (INSTALLED)
            return CHUNK_BUFFER_LAYERS;
        return RenderType.chunkBufferLayers();
    }
    
}
