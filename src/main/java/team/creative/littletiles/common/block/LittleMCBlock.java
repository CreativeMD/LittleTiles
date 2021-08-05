package team.creative.littletiles.common.block;

import net.minecraft.world.level.block.Block;
import team.creative.littletiles.common.api.block.LittleBlock;

public class LittleMCBlock extends LittleBlock {
    
    public final Block block;
    private final boolean translucent;
    
    public LittleMCBlock(Block block) {
        this.block = block;
        this.translucent = !block.defaultBlockState().getMaterial().isSolid() || !block.defaultBlockState().getMaterial().isSolid() || !block.defaultBlockState()
                .isSolidRender(p_200015_1_, p_200015_2_).isOpaqueCube();
    }
}
