package team.creative.littletiles.common.packet.structure;

import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.block.entity.BETiles.BlockEntityInteractor;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;

public class StructureUpdate extends StructurePacket {
    
    public CompoundTag structureNBT;
    public boolean notifyNeighbours;
    
    public StructureUpdate() {}
    
    public StructureUpdate(StructureLocation location, CompoundTag structureNBT, boolean notifyNeighbours) {
        super(location);
        this.structureNBT = structureNBT;
        this.notifyNeighbours = notifyNeighbours;
    }
    
    @Override
    public void execute(Player player, LittleStructure structure) {
        requiresClient(player);
        Consumer<BlockEntityInteractor> action = x -> x.get(structure.mainBlock).setStructureNBT(structureNBT);
        if (notifyNeighbours)
            structure.mainBlock.getBE().updateTiles(action);
        else
            structure.mainBlock.getBE().updateTilesSecretly(action);
    }
    
}
