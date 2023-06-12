package team.creative.littletiles.common.packet.structure;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;

public abstract class StructurePacket extends CreativePacket {
    
    public StructureLocation location;
    
    public StructurePacket(LittleStructure structure) {
        this(structure.getStructureLocation());
    }
    
    public StructurePacket(StructureLocation location) {
        this.location = location;
    }
    
    public StructurePacket() {}
    
    public abstract void execute(Player player, LittleStructure structure);
    
    @Override
    public void execute(Player player) {
        try {
            execute(player, location.find(player.level()));
        } catch (LittleActionException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
