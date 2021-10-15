package team.creative.littletiles.common.structure.connection;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.level.WorldAnimationHandler;
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
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.putString("entity", entityUUID.toString());
        return nbt;
    }
    
    @Override
    protected Level getLevel() throws CorruptedConnectionException, NotYetConnectedException {
        EntityAnimation animation = WorldAnimationHandler.getHandler(super.getLevel()).findAnimation(entityUUID);
        if (animation != null)
            return animation.fakeWorld;
        throw new MissingAnimationException(entityUUID);
    }
    
    @Override
    public EntityAnimation getAnimation() {
        return null;
    }
    
    @Override
    public void destroyStructure() throws CorruptedConnectionException, NotYetConnectedException {
        getStructure().onStructureDestroyed();
        EntityAnimation animation = WorldAnimationHandler.getHandler(super.getLevel()).findAnimation(entityUUID);
        if (animation != null)
            animation.markRemoved();
        for (StructureChildConnection child : getStructure().getChildren())
            child.destroyStructure();
    }
    
    @Override
    public boolean isLinkToAnotherWorld() {
        return true;
    }
}
