package team.creative.littletiles.common.packet.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.action.LittleInteraction;
import team.creative.littletiles.server.LittleTilesServer;

public class LittleInteractionPacket extends CreativePacket {
    
    public int index;
    public boolean rightclick;
    public boolean start;
    
    public LittleInteractionPacket(LittleInteraction interaction, boolean start) {
        this.index = interaction.index;
        this.rightclick = interaction.rightclick;
        this.start = start;
    }
    
    public LittleInteractionPacket() {}
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        if (start)
            LittleTilesServer.INTERACTION.start(player, index, rightclick);
        else
            LittleTilesServer.INTERACTION.end(player, index);
    }
    
}
