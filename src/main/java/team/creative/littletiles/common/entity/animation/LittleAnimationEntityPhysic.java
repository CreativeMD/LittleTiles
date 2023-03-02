package team.creative.littletiles.common.entity.animation;

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.common.entity.LittleEntityPhysic;
import team.creative.littletiles.common.level.little.LittleSubLevel;

public class LittleAnimationEntityPhysic extends LittleEntityPhysic<LittleAnimationEntity> {
    
    private HashMap<BlockPos, BlockState> states = new HashMap<>();
    
    public LittleAnimationEntityPhysic(LittleAnimationEntity parent) {
        super(parent);
    }
    
    @Override
    public void setSubLevel(LittleSubLevel level) {
        // TODO Auto-generated method stub
        level.registerBlockChangeListener((pos, state) -> {
            if (state.isAir())
                states.remove(pos);
            else
                states.put(pos, state);
        });
    }
    
    @Override
    public void tick() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void load(CompoundTag nbt) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public CompoundTag save() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
