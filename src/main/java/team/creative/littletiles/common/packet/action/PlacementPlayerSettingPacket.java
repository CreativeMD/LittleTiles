package team.creative.littletiles.common.packet.action;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

public class PlacementPlayerSettingPacket extends CreativePacket {
    
    public PlacementPlayerSetting setting;
    
    public PlacementPlayerSettingPacket(PlacementPlayerSetting setting) {
        this.setting = setting;
    }
    
    public PlacementPlayerSettingPacket() {}
    
    @Override
    public void executeClient(Player player) {
        LittleTilesClient.ACTION_HANDLER.setting = setting;
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        CompoundTag nbt;
        if (player.getPersistentData().contains(Player.PERSISTED_NBT_TAG))
            nbt = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        else
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, nbt = new CompoundTag());
        nbt.put(PlacementPlayerSetting.SETTING_KEY, setting.save());
    }
    
}
