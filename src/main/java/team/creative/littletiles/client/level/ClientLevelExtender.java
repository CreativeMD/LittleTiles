package team.creative.littletiles.client.level;

import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.world.level.entity.TransientEntitySectionManager;

public interface ClientLevelExtender {
    
    public TransientEntitySectionManager getEntityStorage();
    
    public BlockStatePredictionHandler blockStatePredictionHandler();
    
}
