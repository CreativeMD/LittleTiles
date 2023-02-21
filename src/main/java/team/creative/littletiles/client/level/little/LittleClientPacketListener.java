package team.creative.littletiles.client.level.little;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
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
    
    public Minecraft mc() {
        return ((ClientPacketListenerAccessor) this).getMinecraft();
    }
    
    public ClientLevel level() {
        return ((ClientPacketListenerAccessor) this).getLevel();
    }
    
    public void ensureRunningOnSameThread(Packet packet) throws RunningOnDifferentThreadException {
        PacketUtils.ensureRunningOnSameThread(packet, this, mc());
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
    public void handleBlockDestruction(ClientboundBlockDestructionPacket packet) {
        ensureRunningOnSameThread(packet);
        level().destroyBlockProgress(packet.getId(), packet.getPos(), packet.getProgress());
    }
    
    @Override
    public void handleExplosion(ClientboundExplodePacket packet) {
        ensureRunningOnSameThread(packet);
        Explosion explosion = new Explosion(level(), (Entity) null, packet.getX(), packet.getY(), packet.getZ(), packet.getPower(), packet.getToBlow());
        explosion.finalizeExplosion(true);
        mc().player.setDeltaMovement(mc().player.getDeltaMovement().add(packet.getKnockbackX(), packet.getKnockbackY(), packet.getKnockbackZ()));
    }
    
    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket packet) {
        ensureRunningOnSameThread(packet);
        BlockPos blockpos = packet.getPos();
        level().getBlockEntity(blockpos, packet.getType()).ifPresent((x) -> {
            x.onDataPacket(getConnection(), packet);
            
            if (x instanceof CommandBlockEntity && mc().screen instanceof CommandBlockEditScreen screen)
                screen.updateGui();
        });
    }
    
    @Override
    public void handleBlockEvent(ClientboundBlockEventPacket packet) {
        ensureRunningOnSameThread(packet);
        level().blockEvent(packet.getPos(), packet.getBlock(), packet.getB0(), packet.getB1());
    }
    
    @Override
    public void handleLevelEvent(ClientboundLevelEventPacket packet) {
        ensureRunningOnSameThread(packet);
        if (packet.isGlobalEvent())
            level().globalLevelEvent(packet.getType(), packet.getPos(), packet.getData());
        else
            level().levelEvent(packet.getType(), packet.getPos(), packet.getData());
        
    }
    
    @Override
    public void handleSoundEvent(ClientboundSoundPacket packet) {
        ensureRunningOnSameThread(packet);
        level().playSeededSound(mc().player, packet.getX(), packet.getY(), packet.getZ(), packet.getSound(), packet.getSource(), packet.getVolume(), packet.getPitch(), packet
                .getSeed());
    }
    
    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket packet) {
        ensureRunningOnSameThread(packet);
        Entity entity = this.level().getEntity(packet.getId());
        if (entity != null)
            level().playSeededSound(mc().player, entity, packet.getSound(), packet.getSource(), packet.getVolume(), packet.getPitch(), packet.getSeed());
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
