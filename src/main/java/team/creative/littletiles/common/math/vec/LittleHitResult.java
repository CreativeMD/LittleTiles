package team.creative.littletiles.common.math.vec;

import net.minecraft.world.phys.HitResult;
import team.creative.creativecore.common.level.CreativeLevel;

public class LittleHitResult {
    
    public final HitResult hit;
    public final CreativeLevel level;
    
    public LittleHitResult(HitResult result, CreativeLevel level) {
        this.hit = result;
        this.level = level;
    }
}
