package team.creative.littletiles.mixin.server.network;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
    
    @Accessor
    public Vec3 getAwaitingPositionFromClient();
    
    @Accessor
    @Mutable
    public void setServer(MinecraftServer server);
    
    @Accessor
    @Mutable
    public void setConnection(Connection con);
    
    @Invoker
    public CompletableFuture<List<FilteredText>> callFilterTextPacket(List<String> lines);
    
}
