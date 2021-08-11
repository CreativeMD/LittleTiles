package team.creative.littletiles.common.api.block;

import net.minecraft.world.level.block.Block;

public abstract class LittleBlock {
    
    public abstract boolean isTranslucent();
    
    public abstract boolean is(Block block);
    
    public abstract Block getBlock();
    
    public abstract boolean canBeConvertedToVanilla();
    
    public abstract String blockName();
    
    @Override
    public abstract int hashCode();
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LittleBlock)
            return equals((LittleBlock) obj);
        return false;
    }
    
    public abstract boolean equals(LittleBlock block);
    
}
