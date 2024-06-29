package team.creative.littletiles.mixin.common.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.block.Block;
import team.creative.littletiles.api.common.block.LittleBlock;
import team.creative.littletiles.common.block.little.registry.LittleBlockProvider;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;

@Mixin(Block.class)
public class BlockMixin implements LittleBlockProvider {
    
    @Unique
    private boolean specialBlock;
    
    @Unique
    private LittleBlock block;
    
    private void checkCache() {
        if (block == null)
            LittleBlockRegistry.calculateCache((Block) (Object) this);
    }
    
    @Override
    public LittleBlock getLittleBlock() {
        checkCache();
        return block;
    }
    
    @Override
    public boolean isSpecialBlock() {
        checkCache();
        return specialBlock;
    }
    
    @Override
    public void setCache(LittleBlock block, boolean special) {
        this.block = block;
        this.specialBlock = special;
    }
}
