package team.creative.littletiles.client.level.little;

import java.util.Collection;
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
    
    public ClientPacketListener getVanillaListener() {
        return Minecraft.getInstance().getConnection();
    }
    
    @Override
    public void handleLogin(ClientboundLoginPacket packet) {
        getVanillaListener().handleLogin(packet);
    }
    
    @Override
    public void handleDisconnect(ClientboundDisconnectPacket packet) {
        getVanillaListener().handleDisconnect(packet);
    }
    
    @Override
    public void onDisconnect(Component component) {
        getVanillaListener().onDisconnect(component);
    }
    
    @Override
    public void handleRespawn(ClientboundRespawnPacket packet) {
        getVanillaListener().handleRespawn(packet);
    }
    
    @Override
    public void handleResourcePack(ClientboundResourcePackPacket packet) {
        getVanillaListener().handleResourcePack(packet);
    }
    
    @Override
    public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket packet) {
        getVanillaListener().handlePlayerInfoUpdate(packet);
    }
    
    @Override
    public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket packet) {
        getVanillaListener().handlePlayerInfoRemove(packet);
    }
    
    @Override
    public Collection<PlayerInfo> getOnlinePlayers() {
        return getVanillaListener().getOnlinePlayers();
    }
    
    @Override
    public Collection<UUID> getOnlinePlayerIds() {
        return getVanillaListener().getOnlinePlayerIds();
    }
    
    @Override
    @Nullable
    public PlayerInfo getPlayerInfo(UUID uuid) {
        return getVanillaListener().getPlayerInfo(uuid);
    }
    
    @Override
    @Nullable
    public PlayerInfo getPlayerInfo(String name) {
        return getVanillaListener().getPlayerInfo(name);
    }
    
}
