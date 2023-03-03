package team.creative.littletiles.common.entity.level;

import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.player.Player;

public interface LittleLevelPacketListener {
    
    public PacketListener getPacketListener(Player player);
    
}
