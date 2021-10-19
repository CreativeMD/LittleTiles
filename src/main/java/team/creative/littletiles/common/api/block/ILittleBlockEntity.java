package team.creative.littletiles.common.api.block;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public interface ILittleBlockEntity {
    
    @Nullable
    public BlockState getState(AABB box, boolean realistic);
    
}
