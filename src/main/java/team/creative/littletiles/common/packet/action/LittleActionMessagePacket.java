package team.creative.littletiles.common.packet.action;

import com.creativemd.littletiles.common.util.tooltip.ActionMessage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.client.LittleTilesClient;

public class LittleActionMessagePacket extends CreativePacket {
    
    public ActionMessage message;
    
    public LittleActionMessagePacket(ActionMessage message) {
        this.message = message;
    }
    
    public LittleActionMessagePacket() {
        
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
