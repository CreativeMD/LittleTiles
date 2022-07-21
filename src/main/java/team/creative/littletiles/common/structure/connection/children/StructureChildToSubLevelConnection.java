package team.creative.littletiles.common.structure.connection.children;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.level.LittleAnimationHandlers;
import team.creative.littletiles.common.level.LittleNeighborUpdateCollector;
import team.creative.littletiles.common.structure.connection.ILevelPositionProvider;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class StructureChildToSubLevelConnection extends StructureChildConnection {
    
    public final UUID entityUUID;
    
    public StructureChildToSubLevelConnection(ILevelPositionProvider parent, boolean dynamic, int childId, BlockPos relative, int index, int attribute, UUID entityUUID) {
        super(parent, false, dynamic, childId, relative, index, attribute);
        this.entityUUID = entityUUID;
    }
    
    public StructureChildToSubLevelConnection(ILevelPositionProvider parent, CompoundTag nbt) {
        super(parent, false, nbt);
        this.entityUUID = UUID.fromString(nbt.getString("entity"));
    }
    
    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt = super.save(nbt);
        nbt.putString("entity", entityUUID.toString());
        return nbt;
    }
    
    @Override
    protected Level getLevel() throws CorruptedConnectionException, NotYetConnectedException {
        LittleLevelEntity animation = LittleAnimationHandlers.get(super.getLevel()).find(entityUUID);
        if (animation != null)
            return animation.getFakeLevel();
        throw new MissingAnimationException(entityUUID);
    }
    
    @Override
    public LittleLevelEntity getAnimation() {
        return null;
    }
    
    @Override
    public void destroyStructure(LittleNeighborUpdateCollector neighbor) throws CorruptedConnectionException, NotYetConnectedException {
        getStructure().onStructureDestroyed();
        LittleLevelEntity animation = LittleAnimationHandlers.get(super.getLevel()).find(entityUUID);
        if (animation != null)
            animation.markRemoved();
        neighbor = animation != null ? new LittleNeighborUpdateCollector(animation.getFakeLevel()) : LittleNeighborUpdateCollector.EMPTY;
        for (StructureChildConnection child : getStructure().children.all())
            child.destroyStructure(neighbor);
        neighbor.process();
    }
    
    @Override
    public boolean isLinkToAnotherWorld() {
        return true;
    }
}
