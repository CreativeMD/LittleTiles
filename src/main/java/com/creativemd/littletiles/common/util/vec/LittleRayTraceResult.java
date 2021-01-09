package com.creativemd.littletiles.common.util.vec;

import com.creativemd.creativecore.common.world.CreativeWorld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class LittleRayTraceResult {
    
    public final RayTraceResult result;
    public final CreativeWorld world;
    
    public LittleRayTraceResult(RayTraceResult result, CreativeWorld world) {
        this.result = result;
        this.world = world;
    }
    
    public Vec3d getHitVec() {
        return result.hitVec;
    }
    
    public BlockPos getBlockPos() {
        return result.getBlockPos();
    }
}
