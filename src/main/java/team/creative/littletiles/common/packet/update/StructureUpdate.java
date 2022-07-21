package team.creative.littletiles.common.packet.update;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;

public class StructureUpdate extends CreativePacket {
    
    public StructureLocation location;
    public CompoundTag structureNBT;
    
    public StructureUpdate() {
        
    }
    
    public StructureUpdate(StructureLocation location, CompoundTag structureNBT) {
        this.location = location;
        this.structureNBT = structureNBT;
    }
    
    @Override
    public void executeClient(Player player) {
        try {
            LittleStructure structure = location.find(player.level);
            structure.mainBlock.getBE().updateTiles(x -> x.get(structure.mainBlock).setStructureNBT(structureNBT));
        } catch (LittleActionException e) {}
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        try {
            LittleStructure structure = location.find(player.level);
            CompoundTag nbt = new CompoundTag();
            structure.save(nbt);
            LittleTiles.NETWORK.sendToClient(new StructureUpdate(location, nbt), player);
        } catch (LittleActionException e) {}
        
    }
    
}
