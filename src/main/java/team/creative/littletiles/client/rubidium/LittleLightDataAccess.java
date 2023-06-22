package team.creative.littletiles.client.rubidium;

import me.jellysquid.mods.sodium.client.model.light.data.LightDataAccess;
import net.minecraft.world.level.BlockAndTintGetter;

public class LittleLightDataAccess extends LightDataAccess {
    
    private boolean computed;
    private long cache;
    
    public LittleLightDataAccess() {}
    
    public void prepare(BlockAndTintGetter level) {
        this.world = level;
        this.computed = false;
    }
    
    @Override
    public long get(int x, int y, int z) {
        if (!computed) {
            cache = compute(x, y, z);
            computed = true;
        }
        return cache;
    }
    
}
