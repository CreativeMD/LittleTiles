package team.creative.littletiles.common.block.little.registry;

import team.creative.littletiles.api.common.block.LittleBlock;

public interface LittleBlockProvider {
    
    public boolean isSpecialBlock();
    
    public LittleBlock getLittleBlock();
    
    public void setCache(LittleBlock block, boolean special);
    
}
