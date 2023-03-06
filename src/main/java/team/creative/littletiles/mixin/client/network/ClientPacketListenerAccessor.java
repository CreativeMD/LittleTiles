package team.creative.littletiles.mixin.client.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {
    
    @Accessor
    public RandomSource getRandom();
    
    @Invoker
    public void callPostAddEntitySoundInstance(Entity entity);
    
    @Invoker
    public void callApplyLightData(int x, int z, ClientboundLightUpdatePacketData data);
    
}
