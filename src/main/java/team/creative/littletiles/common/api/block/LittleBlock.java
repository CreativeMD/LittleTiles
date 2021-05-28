package team.creative.littletiles.common.api.block;

import net.minecraft.block.Block;

public abstract class LittleBlock {
    
    public abstract boolean isTranslucent();
    
    public abstract boolean is(Block block);
    
    public abstract Block getBlock();
    
}
