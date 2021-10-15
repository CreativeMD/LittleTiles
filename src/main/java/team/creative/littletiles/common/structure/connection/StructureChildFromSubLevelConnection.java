package team.creative.littletiles.common.structure.connection;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.SubLevel;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class StructureChildFromSubLevelConnection extends StructureChildConnection {
    
    public StructureChildFromSubLevelConnection(ILevelPositionProvider parent, boolean dynamic, int childId, BlockPos relative, int index, int attribute) {
        super(parent, true, dynamic, childId, relative, index, attribute);
    }
    
    public StructureChildFromSubLevelConnection(ILevelPositionProvider parent, CompoundTag nbt) {
        super(parent, true, nbt);
    }
    
    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.putBoolean("subWorld", true);
        return nbt;
    }
    
    @Override
    protected Level getLevel() throws CorruptedConnectionException, NotYetConnectedException {
        return ((SubLevel) parent.getLevel()).parentLevel;
    }
    
    @Override
    public EntityAnimation getAnimation() {
        SubLevel fakeWorld = (SubLevel) parent.getLevel();
        return (EntityAnimation) fakeWorld.parent;
    }
    
    @Override
    public void destroyStructure() {
        SubLevel fakeWorld = (SubLevel) parent.getLevel();
        ((EntityAnimation) fakeWorld.parent).markRemoved();
        parent.onStructureDestroyed();
    }
    
    @Override
    public boolean isLinkToAnotherWorld() {
        return true;
    }
    
}
