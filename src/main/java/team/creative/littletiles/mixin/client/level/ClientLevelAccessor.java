package team.creative.littletiles.mixin.client.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.world.level.entity.TransientEntitySectionManager;

@Mixin(ClientLevel.class)
public interface ClientLevelAccessor {
    
    @Accessor
    public TransientEntitySectionManager getEntityStorage();
    
    @Accessor
    public ClientPacketListener getConnection();
    
    @Invoker
    public BlockStatePredictionHandler callGetBlockStatePredictionHandler();
    
}
