package team.creative.littletiles.common.packet.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTilesRegistry;

public class ChangedColorPacket extends CreativePacket {
    
    public int color;
    
    public ChangedColorPacket(int color) {
        this.color = color;
    }
    
    public ChangedColorPacket() {}
    
    @Override
    public void execute(Player player) {
        player.getMainHandItem().set(LittleTilesRegistry.COLOR, color);
        player.inventoryMenu.broadcastChanges();
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
