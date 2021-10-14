package team.creative.littletiles.client.api;

import net.minecraft.world.level.block.state.BlockState;

public interface IFakeRenderingBlock {
    
    public BlockState getFakeState(BlockState state);
    
}
