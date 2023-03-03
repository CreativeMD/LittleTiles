package team.creative.littletiles.common.entity.level;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.api.common.block.LittlePhysicBlock;
import team.creative.littletiles.common.entity.INoPushEntity;
import team.creative.littletiles.common.entity.LittleEntityPhysic;
import team.creative.littletiles.common.level.little.LevelBoundsListener;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.level.little.LittleSubLevel;

public class LittleLevelEntityPhysic extends LittleEntityPhysic<LittleLevelEntity> implements LevelBoundsListener {
    
    protected static final Predicate<Entity> noAnimation = x -> !(x.getFirstPassenger() instanceof INoPushEntity);
    
    private BlockUpdateLevelSystem updateSystem;
    public boolean noCollision = false;
    
    public LittleLevelEntityPhysic(LittleLevelEntity parent) {
        super(parent);
    }
    
    @Override
    public void setSubLevel(LittleSubLevel level) {
        updateSystem = new BlockUpdateLevelSystem(level);
        level.registerBlockChangeListener(updateSystem::blockChanged);
        updateSystem.registerLevelBoundListener(this);
    }
    
    @Override
    public void loadExtra(CompoundTag nbt) {
        updateSystem.load(nbt.getCompound("bounds"));
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.put("bounds", updateSystem.save());
    }
    
    @Override
    public void tick() {
        updateSystem.tick(parent);
    }
    
    public BlockUpdateLevelSystem getBlockUpdateLevelSystem() {
        return updateSystem;
    }
    
    @Override
    public void rescan(LittleLevel level, BlockUpdateLevelSystem system, Facing facing, Iterable<BlockPos> possible, int boundary) {
        double value = facing.positive ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (BlockPos pos : possible) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof LittlePhysicBlock block)
                value = facing.positive ? Math.max(value, block.bound(level, pos, facing)) : Math.min(value, block.bound(level, pos, facing));
            else
                value = facing.positive ? Math.max(value, pos.get(facing.axis.toVanilla()) + 1) : Math.min(value, pos.get(facing.axis.toVanilla()));
            
            if (value == boundary)
                break;
        }
        set(facing, value);
    }
    
    @Override
    public void afterChangesApplied(BlockUpdateLevelSystem system) {
        setBB(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }
    
}
