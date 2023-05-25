package team.creative.littletiles.mixin.client.level;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.ClientLevelExtender;
import team.creative.littletiles.client.level.little.LittleClientChunkCache;
import team.creative.littletiles.client.level.little.LittleClientLevel;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements ClientLevelExtender {
    
    @Shadow
    @Final
    private TransientEntitySectionManager<Entity> entityStorage;
    
    @Shadow
    @Final
    private BlockStatePredictionHandler blockStatePredictionHandler;
    
    @Unique
    private ClientLevel as() {
        return (ClientLevel) (Object) this;
    }
    
    @Redirect(at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/ClientChunkCache"), method = "<init>", require = 1)
    public ClientChunkCache newClientChunkCache(ClientLevel level, int distance) {
        if (level instanceof LittleClientLevel little) {
            LittleClientChunkCache cache = CreativeHackery.allocateInstance(LittleClientChunkCache.class);
            cache.init(little);
            return cache;
        }
        return new ClientChunkCache(level, distance);
    }
    
    @Override
    public TransientEntitySectionManager getEntityStorage() {
        return entityStorage;
    }
    
    @Override
    public BlockStatePredictionHandler blockStatePredictionHandler() {
        return blockStatePredictionHandler;
    }
    
    @Override
    public void handleBlockChangedAckExtender(int sequence) {
        as().handleBlockChangedAck(sequence);
    }
    
    @Override
    public void setServerVerifiedBlockStateExtender(BlockPos pos, BlockState state, int sequence) {
        as().setServerVerifiedBlockState(pos, state, sequence);
    }
    
    @Override
    public void syncBlockStateExtender(BlockPos pos, BlockState state, Vec3 vec) {
        as().syncBlockState(pos, state, vec);
    }
    
    @Inject(method = "removeEntity(ILnet/minecraft/world/entity/Entity$RemovalReason;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;onClientRemoval()V"), cancellable = true, require = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    public void removeEntity(int id, RemovalReason reason, CallbackInfo info, Entity entity) {
        if (LittleTilesClient.ANIMATION_HANDLER.checkInTransition(entity))
            info.cancel();
    }
    
}
