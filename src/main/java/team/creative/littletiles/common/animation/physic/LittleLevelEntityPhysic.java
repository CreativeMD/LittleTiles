package team.creative.littletiles.common.animation.physic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.level.listener.LevelBoundsListener;
import team.creative.creativecore.common.level.system.BlockUpdateLevelSystem;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.OBB;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.littletiles.common.animation.entity.LittleLevelEntity;
import team.creative.littletiles.common.api.block.LittlePhysicBlock;

public class LittleLevelEntityPhysic implements LevelBoundsListener {
    
    public final LittleLevelEntity parent;
    
    private OBB orientatedBB;
    private boolean preventPush = false;
    
    public LittleLevelEntityPhysic(LittleLevelEntity parent) {
        this.parent = parent;
    }
    
    public IVecOrigin getOrigin() {
        return parent.getOrigin();
    }
    
    public OBB getOrientatedBB() {
        return orientatedBB;
    }
    
    public AABB getBB() {
        return getOrigin().getAxisAlignedBox(orientatedBB);
    }
    
    public void ignoreCollision(Runnable run) {
        preventPush = true;
        try {
            run.run();
        } finally {
            preventPush = false;
        }
    }
    
    public boolean shouldPush() {
        return !preventPush;
    }
    
    @Override
    public void rescan(CreativeLevel level, BlockUpdateLevelSystem system, Iterable<BlockPos> possible) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        
        for (BlockPos pos : possible) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof LittlePhysicBlock) {
                LittlePhysicBlock phy = (LittlePhysicBlock) state.getBlock();
                minX = Math.min(minX, phy.bound(level, pos, Facing.WEST));
                minY = Math.min(minX, phy.bound(level, pos, Facing.DOWN));
                minZ = Math.min(minX, phy.bound(level, pos, Facing.NORTH));
                maxX = Math.max(maxX, phy.bound(level, pos, Facing.EAST));
                maxY = Math.max(maxX, phy.bound(level, pos, Facing.UP));
                maxZ = Math.max(maxX, phy.bound(level, pos, Facing.SOUTH));
            } else {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minX, pos.getY());
                minZ = Math.min(minX, pos.getZ());
                maxX = Math.max(maxX, pos.getX() + 1);
                maxY = Math.max(maxX, pos.getY() + 1);
                maxZ = Math.max(maxX, pos.getZ() + 1);
            }
        }
        orientatedBB = new OBB(parent.getOrigin(), minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    @Override
    public void rescan(CreativeLevel level, BlockUpdateLevelSystem system, Facing facing, Iterable<BlockPos> possible, int boundary) {
        double value = facing.positive ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (BlockPos pos : possible) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof LittlePhysicBlock)
                value = facing.positive ? Math.max(value, ((LittlePhysicBlock) state.getBlock()).bound(level, pos, facing)) : Math
                        .min(value, ((LittlePhysicBlock) state.getBlock()).bound(level, pos, facing));
            else
                value = facing.positive ? Math.max(value, pos.get(facing.axis.toVanilla()) + 1) : Math.min(value, pos.get(facing.axis.toVanilla()));
            
            if (value == boundary)
                break;
        }
        orientatedBB = orientatedBB.set(facing, value);
    }
    
}
