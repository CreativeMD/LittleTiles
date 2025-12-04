package team.creative.littletiles.common.packet.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;

public class WrenchRequestInfoPacket extends CreativePacket {
    
    public List<StructureLocation> structures;
    
    public WrenchRequestInfoPacket(List<StructureLocation> structures) {
        this.structures = structures;
    }
    
    public WrenchRequestInfoPacket() {}
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        List<List<Component>> result = new ArrayList<>();
        for (int i = 0; i < structures.size(); i++)
            try {
                var s = structures.get(i).find(player.level());
                result.add(s.wrenchInfo());
            } catch (LittleActionException e) {}
        LittleTiles.NETWORK.sendToClient(new WrenchReceiveInfoPacket(result), player);
        
    }
    
}
