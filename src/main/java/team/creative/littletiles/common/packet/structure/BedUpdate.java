package team.creative.littletiles.common.packet.structure;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.LittleBed;

public class BedUpdate extends CreativePacket {
    
    public StructureLocation location;
    public int playerID;
    
    public BedUpdate() {}
    
    public BedUpdate(StructureLocation location) {
        this.location = location;
        this.playerID = -1;
    }
    
    public BedUpdate(StructureLocation location, Player player) {
        this(location);
        this.playerID = player.getId();
    }
    
    @Override
    public void executeClient(Player player) {
        Entity entity = playerID == -1 ? player : player.level.getEntity(playerID);
        if (entity instanceof Player) {
            try {
                LittleStructure structure = location.find(player.level);
                if (structure instanceof LittleBed)
                    ((LittleBed) structure).trySleep((Player) entity, structure.getHighestCenterVec());
            } catch (LittleActionException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
