package team.creative.littletiles.common.packet.structure;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.type.bed.LittleBed;

public class BedUpdate extends StructurePacket {
    
    public int playerID;
    
    public BedUpdate() {}
    
    public BedUpdate(StructureLocation location) {
        super(location);
        this.playerID = -1;
    }
    
    public BedUpdate(StructureLocation location, Player player) {
        super(location);
        this.playerID = player.getId();
    }
    
    @Override
    public void execute(Player player, LittleStructure structure) {
        requiresClient(player);
        
        Entity entity = playerID == -1 ? player : player.level.getEntity(playerID);
        if (entity instanceof Player) {
            if (structure instanceof LittleBed bed)
                try {
                    bed.trySleep((Player) entity, structure.getHighestCenterVec());
                } catch (CorruptedConnectionException | NotYetConnectedException e) {
                    e.printStackTrace();
                }
            
        }
    }
    
}
