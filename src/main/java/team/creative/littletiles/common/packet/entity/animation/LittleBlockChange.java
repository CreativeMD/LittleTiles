package team.creative.littletiles.common.packet.entity.animation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

public record LittleBlockChange(BlockPos pos, CompoundTag block) {
    
    private static CompoundTag get(BlockGetter level, BlockPos pos) {
        BlockEntity block = level.getBlockEntity(pos);
        if (block instanceof BETiles tiles)
            return LittleAnimationEntity.saveBE(tiles);
        return null;
    }
    
    public LittleBlockChange(BlockGetter level, BlockPos pos) {
        this(pos, get(level, pos));
    }
    
    public LittleBlockChange(BETiles entity) {
        this(entity.getBlockPos(), LittleAnimationEntity.saveBE(entity));
    }
    
    public boolean isEmpty() {
        return block == null;
    }
    
}
