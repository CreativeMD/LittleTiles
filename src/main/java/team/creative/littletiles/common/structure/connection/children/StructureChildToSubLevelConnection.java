package team.creative.littletiles.common.structure.connection.children;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.entity.LittleEntity;
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
        LittleEntity animation = LittleTiles.ANIMATION_HANDLERS.get(super.getLevel()).find(entityUUID);
        if (animation != null)
            return (Level) animation.getSubLevel();
        throw new MissingAnimationException(entityUUID);
    }
    
    @Override
    public LittleEntity getAnimation() {
        return null;
    }
    
    @Override
    public boolean isLinkToAnotherWorld() {
        return true;
    }
}
