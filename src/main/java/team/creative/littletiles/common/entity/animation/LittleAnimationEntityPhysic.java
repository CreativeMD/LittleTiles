package team.creative.littletiles.common.entity.animation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.LittleEntityPhysic;
import team.creative.littletiles.common.level.little.LittleSubLevel;

public class LittleAnimationEntityPhysic extends LittleEntityPhysic<LittleAnimationEntity> {
    
    private boolean blocksChanged = false;
    
    public LittleAnimationEntityPhysic(LittleAnimationEntity parent) {
        super(parent);
    }
    
    @Override
    public void setSubLevel(LittleSubLevel level) {
        level.registerBlockChangeListener((pos, state) -> blocksChanged = true);
    }
    
    protected boolean isWithinBoundsNoEdge(BlockPos pos) {
        return Math.floor(minX) < pos.getX() && maxX > pos.getX() && Math.floor(minY) < pos.getY() && maxY > pos.getY() && Math.floor(minZ) < pos.getZ() && maxZ > pos.getZ();
    }
    
    @Override
    public void tick() {
        if (blocksChanged) {
            minX = Double.MAX_VALUE;
            minY = Double.MAX_VALUE;
            minZ = Double.MAX_VALUE;
            maxX = -Double.MAX_VALUE;
            maxY = -Double.MAX_VALUE;
            maxZ = -Double.MAX_VALUE;
            
            for (BETiles block : parent.getSubLevel()) {
                if (block.isEmpty() || isWithinBoundsNoEdge(block.getBlockPos()))
                    continue;
                AABB bb = block.getBlockBB();
                minX = Math.min(minX, bb.minX);
                minY = Math.min(minY, bb.minY);
                minZ = Math.min(minZ, bb.minZ);
                maxX = Math.max(maxX, bb.maxX);
                maxY = Math.max(maxY, bb.maxY);
                maxZ = Math.max(maxZ, bb.maxZ);
            }
            setBB(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
        }
    }
    
    @Override
    public void loadExtra(CompoundTag nbt) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {}
    
}
