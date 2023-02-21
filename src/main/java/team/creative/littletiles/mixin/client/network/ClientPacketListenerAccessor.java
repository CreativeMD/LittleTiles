package team.creative.littletiles.mixin.client.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {
    
    @Accessor
    @Mutable
    public void setMinecraft(Minecraft mc);
    
    @Accessor
    public Minecraft getMinecraft();
    
    @Accessor
    public void setLevel(ClientLevel level);
    
    @Accessor
    public ClientLevel getLevel();
    
    @Accessor
    public void setLevelData(ClientLevel.ClientLevelData data);
    
    @Accessor
    @Mutable
    public void setConnection(Connection con);
    
}
