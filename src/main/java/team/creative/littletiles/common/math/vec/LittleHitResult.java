package team.creative.littletiles.common.math.vec;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LittleHitResult {
    
    public final HitResult hit;
    public final Level level;
    
    public LittleHitResult(HitResult result, Level level) {
        this.hit = result;
        this.level = level;
    }
    
    public boolean isBlock() {
        return hit instanceof BlockHitResult;
    }
    
    public boolean isEntity() {
        return hit instanceof EntityHitResult;
    }
    
    public BlockHitResult asBlockHit() {
        return (BlockHitResult) hit;
    }
    
    public EntityHitResult asEntityHit() {
        return (EntityHitResult) hit;
    }
}
