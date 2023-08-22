package team.creative.littletiles.client.mod.rubidium.pipeline;

import me.jellysquid.mods.sodium.client.model.light.data.HashLightDataCache;
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
