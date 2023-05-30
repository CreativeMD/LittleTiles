package team.creative.littletiles.common.packet.structure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;

public class StructureUpdate extends StructurePacket {
    
    public CompoundTag structureNBT;
    
    public StructureUpdate() {}
    
    public StructureUpdate(StructureLocation location, CompoundTag structureNBT) {
        super(location);
        this.structureNBT = structureNBT;
    }
    
    @Override
    public void execute(Player player, LittleStructure structure) {
        requiresClient(player);
        structure.mainBlock.getBE().updateTilesSecretly(x -> x.get(structure.mainBlock).setStructureNBT(structureNBT));
    }
    
}
