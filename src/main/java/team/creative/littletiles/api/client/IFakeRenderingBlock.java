package team.creative.littletiles.api.client;

import net.minecraft.world.level.block.state.BlockState;

public interface IFakeRenderingBlock {
    
    public BlockState getFakeState(BlockState state);
    
}
