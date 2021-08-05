package team.creative.littletiles.common.math.vec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.level.CreativeLevel;

public class LittleHitResult {
    
    public final BlockHitResult result;
    public final CreativeLevel level;
    
    public LittleHitResult(BlockHitResult result, CreativeLevel level) {
        this.result = result;
        this.level = level;
    }
    
    public Vec3 getHitVec() {
        return result.getLocation();
    }
    
    public BlockPos getBlockPos() {
        return result.getBlockPos();
    }
}
