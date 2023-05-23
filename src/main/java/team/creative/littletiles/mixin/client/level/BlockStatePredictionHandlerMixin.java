package team.creative.littletiles.mixin.client.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.level.BlockStatePredictionHandlerExtender;
import team.creative.littletiles.client.level.ClientLevelExtender;

@Mixin(BlockStatePredictionHandler.class)
public class BlockStatePredictionHandlerMixin implements BlockStatePredictionHandlerExtender {
    
    public ClientLevelExtender level;
    
    @Override
    public void setLevel(ClientLevelExtender level) {
        this.level = level;
    }
    
    @Redirect(method = "endPredictionsUpTo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;syncBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V"),
            require = 1)
    public void syncBlockState(ClientLevel level, BlockPos pos, BlockState state, Vec3 vec) {
        if (this.level != null)
            this.level.syncBlockStateExtender(pos, state, vec);
        else
            level.syncBlockState(pos, state, vec);
    }
    
}
