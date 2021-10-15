package team.creative.littletiles.common.packet.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.item.tooltip.ActionMessage;

public class ActionMessagePacket extends CreativePacket {
    
    public ActionMessage message;
    
    public ActionMessagePacket(ActionMessage message) {
        this.message = message;
    }
    
    public ActionMessagePacket() {
        
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClient(Player player) {
        LittleTilesClient.displayActionMessage(message);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        
    }
    
}
