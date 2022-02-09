package team.creative.littletiles.common.structure.connection.children;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.structure.connection.ILevelPositionProvider;
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
    public CompoundTag save(CompoundTag nbt) {
        nbt = super.save(nbt);
        nbt.putBoolean("subWorld", true);
        return nbt;
    }
    
    @Override
    protected Level getLevel() throws CorruptedConnectionException, NotYetConnectedException {
        return ((ISubLevel) parent.getLevel()).getParent();
    }
    
    @Override
    public EntityAnimation getAnimation() {
        IOrientatedLevel fakeWorld = (IOrientatedLevel) parent.getLevel();
        return (EntityAnimation) fakeWorld.getHolder();
    }
    
    @Override
    public void destroyStructure() {
        IOrientatedLevel fakeWorld = (IOrientatedLevel) parent.getLevel();
        ((EntityAnimation) fakeWorld.getHolder()).markRemoved();
        parent.onStructureDestroyed();
    }
    
    @Override
    public boolean isLinkToAnotherWorld() {
        return true;
    }
    
}
