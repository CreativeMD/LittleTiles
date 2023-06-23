package team.creative.littletiles.client.rubidium.pipeline;

import me.jellysquid.mods.sodium.client.model.light.cache.HashLightDataCache;
import net.minecraft.world.level.BlockAndTintGetter;

public class LittleLightDataAccess extends HashLightDataCache {
    
    public LittleLightDataAccess() {
        super(null);
    }
    
    public void prepare(BlockAndTintGetter level) {
        this.world = level;
        clearCache();
    }
    
}
