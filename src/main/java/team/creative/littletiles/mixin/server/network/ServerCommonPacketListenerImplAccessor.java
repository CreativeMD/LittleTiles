package team.creative.littletiles.mixin.server.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@Mixin(ServerCommonPacketListenerImpl.class)
public interface ServerCommonPacketListenerImplAccessor {
    
    @Accessor
    @Mutable
    public void setServer(MinecraftServer server);
    
    @Accessor
    @Mutable
    public void setConnection(Connection con);
    
}
