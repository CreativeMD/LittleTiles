package team.creative.littletiles.client.mod.rubidium.pipeline;

import org.embeddedt.embeddium.impl.model.light.data.HashLightDataCache;

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
