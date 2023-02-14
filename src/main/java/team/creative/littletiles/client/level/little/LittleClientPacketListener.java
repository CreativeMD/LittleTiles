package team.creative.littletiles.client.level.little;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;
import team.creative.littletiles.mixin.client.network.ClientPacketListenerAccessor;

public class LittleClientPacketListener extends ClientPacketListener {
    
    public static LittleClientPacketListener allocateInstance() {
        return CreativeHackery.allocateInstance(LittleClientPacketListener.class);
    }
    
    public LittleClientPacketListener(Minecraft mc, Connection con) {
        super(mc, null, con, null, null, null);
    }
    
    public void init(Minecraft mc, ClientLevel level, ClientLevelData data, Connection con) {
        ((ClientPacketListenerAccessor) this).setMinecraft(mc);
        ((ClientPacketListenerAccessor) this).setConnection(con);
        ((ClientPacketListenerAccessor) this).setLevel(level);
        ((ClientPacketListenerAccessor) this).setLevelData(data);
    }
    
    @Override
    public void handleLogin(ClientboundLoginPacket packet) {}
    
    @Override
    public void handleDisconnect(ClientboundDisconnectPacket packet) {}
    
    @Override
    public void onDisconnect(Component component) {}
    
    @Override
    public void handleRespawn(ClientboundRespawnPacket packet) {}
    
    @Override
    public void handleResourcePack(ClientboundResourcePackPacket packet) {}
    
    @Override
    public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket packet) {}
    
    @Override
    public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket packet) {}
    
    @Override
    public Collection<PlayerInfo> getOnlinePlayers() {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public Collection<UUID> getOnlinePlayerIds() {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @Nullable
    public PlayerInfo getPlayerInfo(UUID uuid) {
        return null;
    }
    
    @Override
    @Nullable
    public PlayerInfo getPlayerInfo(String name) {
        return null;
    }
    
}
