package team.creative.littletiles.mixin.client.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler$ServerVerifiedState")
public interface ServerVerifiedStateAccessor {
    
    @Accessor
    public int getSequence();
    
}
