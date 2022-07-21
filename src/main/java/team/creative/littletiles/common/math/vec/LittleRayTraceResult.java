package team.creative.littletiles.common.math.vec;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import team.creative.creativecore.common.world.CreativeWorld;

public class LittleRayTraceResult {
    
    public final BlockRayTraceResult result;
    public final CreativeWorld world;
    
    public LittleRayTraceResult(BlockRayTraceResult result, CreativeWorld world) {
        this.result = result;
        this.world = world;
    }
    
    public Vector3d getHitVec() {
        return result.getLocation();
    }
    
    public BlockPos getBlockPos() {
        return result.getBlockPos();
    }
}
