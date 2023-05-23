package team.creative.littletiles.client.level;

import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.phys.Vec3;

public interface ClientLevelExtender {
    
    public TransientEntitySectionManager getEntityStorage();
    
    public BlockStatePredictionHandler blockStatePredictionHandler();
    
    public void handleBlockChangedAckExtender(int sequence);
    
    public void setServerVerifiedBlockStateExtender(BlockPos pos, BlockState state, int sequence);
    
    public void syncBlockStateExtender(BlockPos pos, BlockState state, Vec3 vec);
    
}
