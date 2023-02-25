package team.creative.littletiles.common.math.vec;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.common.entity.level.LittleEntity;
import team.creative.littletiles.common.level.little.LittleSubLevel;

public class LittleHitResult extends EntityHitResult {
    
    public final HitResult hit;
    public final LittleSubLevel level;
    
    public LittleHitResult(Entity entity, HitResult result, LittleSubLevel level) {
        super(entity);
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
    
    public LittleEntity getHolder() {
        if (level instanceof ISubLevel)
            return (LittleEntity) ((ISubLevel) level).getHolder();
        return null;
    }
}
