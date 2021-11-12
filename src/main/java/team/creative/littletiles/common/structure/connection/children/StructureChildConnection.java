package team.creative.littletiles.common.structure.connection.children;

import java.security.InvalidParameterException;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.creativecore.common.util.mc.WorldUtils;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.ILevelPositionProvider;
import team.creative.littletiles.common.structure.connection.IStructureConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.CorruptedLinkException;
import team.creative.littletiles.common.structure.exception.MissingBlockException;
import team.creative.littletiles.common.structure.exception.MissingStructureException;
import team.creative.littletiles.common.structure.exception.MissingWorldException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class StructureChildConnection implements IStructureConnection {
    
    public final ILevelPositionProvider parent;
    public final boolean isChild;
    public final int childId;
    public final boolean extension;
    
    private final int structureIndex;
    private final int attribute;
    private final BlockPos relativePos;
    private BETiles cachedBE;
    
    public StructureChildConnection(ILevelPositionProvider parent, boolean isChild, boolean extension, int childId, BlockPos relative, int index, int attribute) {
        this.parent = parent;
        this.isChild = isChild;
        this.childId = childId;
        this.structureIndex = index;
        this.attribute = attribute;
        this.relativePos = relative;
        this.extension = extension;
    }
    
    public StructureChildConnection(ILevelPositionProvider parent, boolean isChild, CompoundTag nbt) {
        this.parent = parent;
        this.isChild = isChild;
        this.childId = nbt.getInt("child");
        this.attribute = nbt.getInt("type");
        this.structureIndex = nbt.getInt("index");
        this.extension = nbt.getBoolean("extension");
        int[] array = nbt.getIntArray("coord");
        if (array.length == 3)
            relativePos = new BlockPos(array[0], array[1], array[2]);
        else
            throw new InvalidParameterException("No valid coord given " + nbt);
    }
    
    public boolean isChild() {
        return isChild;
    }
    
    public int getChildId() {
        return childId;
    }
    
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("child", childId);
        nbt.putIntArray("coord", new int[] { relativePos.getX(), relativePos.getY(), relativePos.getZ() });
        nbt.putInt("type", attribute);
        nbt.putInt("index", structureIndex);
        if (extension)
            nbt.putBoolean("extension", extension);
        return nbt;
    }
    
    public void destroyStructure() throws CorruptedConnectionException, NotYetConnectedException {
        if (!isChild())
            getStructure().removeStructure();
    }
    
    public EntityAnimation getAnimation() {
        return null;
    }
    
    @Override
    public BlockPos getStructurePosition() {
        return relativePos.offset(parent.getPos());
    }
    
    @Override
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException {
        BETiles be = getBlockEntity();
        if (!be.hasLoaded())
            throw new NotYetConnectedException();
        IStructureParentCollection structure = be.getStructure(structureIndex);
        if (structure != null)
            return structure.getStructure();
        throw new MissingStructureException(be.getBlockPos());
    }
    
    protected Level getLevel() throws CorruptedConnectionException, NotYetConnectedException {
        return parent.getLevel();
    }
    
    protected BETiles getBlockEntity() throws CorruptedConnectionException, NotYetConnectedException {
        if (cachedBE != null && !cachedBE.isRemoved())
            return cachedBE;
        
        if (relativePos == null)
            throw new CorruptedLinkException();
        
        Level level = getLevel();
        
        if (level == null)
            throw new MissingWorldException();
        
        BlockPos absoluteCoord = getStructurePosition();
        LevelChunk chunk = level.getChunkAt(absoluteCoord);
        if (WorldUtils.checkIfChunkExists(chunk)) {
            BlockEntity be = level.getBlockEntity(absoluteCoord);
            if (be instanceof BETiles)
                return cachedBE = (BETiles) be;
            else
                throw new MissingBlockException(absoluteCoord);
        } else
            throw new NotYetConnectedException();
    }
    
    @Override
    public int getIndex() {
        return structureIndex;
    }
    
    @Override
    public int getAttribute() {
        return attribute;
    }
    
    public static StructureChildConnection load(ILevelPositionProvider structure, CompoundTag nbt, boolean isChild) {
        if (nbt.contains("entity"))
            return new StructureChildToSubLevelConnection(structure, nbt);
        else if (nbt.getBoolean("subWorld"))
            return new StructureChildFromSubLevelConnection(structure, nbt);
        return new StructureChildConnection(structure, isChild, nbt);
    }
    
}
