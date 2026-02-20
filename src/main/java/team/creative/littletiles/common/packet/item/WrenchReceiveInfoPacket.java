package team.creative.littletiles.common.packet.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.tool.LittleToolWrench;

public class WrenchReceiveInfoPacket extends CreativePacket {
    
    public List<List<Component>> result;
    
    public WrenchReceiveInfoPacket(List<List<Component>> result) {
        this.result = result;
    }
    
    public WrenchReceiveInfoPacket() {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClient(Player player) {
        if (LittleTilesClient.PREVIEW_RENDERER.tool() instanceof LittleToolWrench w)
            w.receive(result);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
