package team.creative.littletiles.common.structure.connection;

import java.security.InvalidParameterException;

import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.outdated.connection.StructureLink;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import team.creative.creativecore.common.util.mc.WorldUtils;
import team.creative.littletiles.common.entity.EntityAnimation;
import team.creative.littletiles.common.structure.LittleStructure;
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
    public final boolean dynamic;
    
    private final int structureIndex;
    private final int attribute;
    private final BlockPos relativePos;
    private TileEntityLittleTiles cachedTe;
    
    public StructureChildConnection(ILevelPositionProvider parent, boolean isChild, boolean dynamic, int childId, BlockPos relative, int index, int attribute) {
        this.parent = parent;
        this.isChild = isChild;
        this.childId = childId;
        this.structureIndex = index;
        this.attribute = attribute;
        this.relativePos = relative;
        this.dynamic = dynamic;
    }
    
    public StructureChildConnection(ILevelPositionProvider parent, boolean isChild, NBTTagCompound nbt) {
        this.parent = parent;
        this.isChild = isChild;
        this.childId = nbt.getInteger("child");
        this.attribute = nbt.getInteger("type");
        this.structureIndex = nbt.getInteger("index");
        this.dynamic = nbt.getBoolean("dynamic");
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
    
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putInt("child", childId);
        nbt.putIntArray("coord", new int[] { relativePos.getX(), relativePos.getY(), relativePos.getZ() });
        nbt.putInt("type", attribute);
        nbt.putInt("index", structureIndex);
        if (dynamic)
            nbt.putBoolean("dynamic", dynamic);
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
        return relativePos.add(parent.getPos());
    }
    
    @Override
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException {
        TileEntityLittleTiles te = getTileEntity();
        if (!te.hasLoaded())
            throw new NotYetConnectedException();
        IStructureTileList structure = te.getStructure(structureIndex);
        if (structure != null)
            return structure.getStructure();
        throw new MissingStructureException(te.getPos());
    }
    
    protected World getWorld() throws CorruptedConnectionException, NotYetConnectedException {
        return parent.getWorld();
    }
    
    protected TileEntityLittleTiles getTileEntity() throws CorruptedConnectionException, NotYetConnectedException {
        if (cachedTe != null && !cachedTe.isInvalid())
            return cachedTe;
        
        if (relativePos == null)
            throw new CorruptedLinkException();
        
        World world = getWorld();
        
        if (world == null)
            throw new MissingWorldException();
        
        BlockPos absoluteCoord = getStructurePosition();
        Chunk chunk = world.getChunkFromBlockCoords(absoluteCoord);
        if (WorldUtils.checkIfChunkExists(chunk)) {
            TileEntity te = world.getTileEntity(absoluteCoord);
            if (te instanceof TileEntityLittleTiles)
                return cachedTe = (TileEntityLittleTiles) te;
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
    
    public static StructureChildConnection loadFromNBT(ILevelPositionProvider structure, CompoundTag nbt, boolean isChild) {
        if (nbt.contains("childID")) // Old
            return StructureLink.loadFromNBTOld(structure, nbt, isChild);
        
        if (nbt.contains("entity"))
            return new StructureChildToSubWorldConnection(structure, nbt);
        else if (nbt.getBoolean("subWorld"))
            return new StructureChildFromSubWorldConnection(structure, nbt);
        return new StructureChildConnection(structure, isChild, nbt);
    }
    
}
