package com.creativemd.littletiles.common.structure.connection;

import java.security.InvalidParameterException;

import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.CorruptedLinkException;
import com.creativemd.littletiles.common.structure.exception.MissingBlockException;
import com.creativemd.littletiles.common.structure.exception.MissingStructureException;
import com.creativemd.littletiles.common.structure.exception.MissingWorldException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.outdated.connection.StructureLink;
import com.creativemd.littletiles.common.world.LittleNeighborUpdateCollector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class StructureChildConnection implements IStructureConnection {
    
    public final IWorldPositionProvider parent;
    public final boolean isChild;
    public final int childId;
    public final boolean dynamic;
    
    private final int structureIndex;
    private final int attribute;
    private final BlockPos relativePos;
    private TileEntityLittleTiles cachedTe;
    
    public StructureChildConnection(IWorldPositionProvider parent, boolean isChild, boolean dynamic, int childId, BlockPos relative, int index, int attribute) {
        this.parent = parent;
        this.isChild = isChild;
        this.childId = childId;
        this.structureIndex = index;
        this.attribute = attribute;
        this.relativePos = relative;
        this.dynamic = dynamic;
    }
    
    public StructureChildConnection(IWorldPositionProvider parent, boolean isChild, NBTTagCompound nbt) {
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
    
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("child", childId);
        nbt.setIntArray("coord", new int[] { relativePos.getX(), relativePos.getY(), relativePos.getZ() });
        nbt.setInteger("type", attribute);
        nbt.setInteger("index", structureIndex);
        if (dynamic)
            nbt.setBoolean("dynamic", dynamic);
        return nbt;
    }
    
    public void destroyStructure(LittleNeighborUpdateCollector neighbor) throws CorruptedConnectionException, NotYetConnectedException {
        if (!isChild())
            getStructure().removeStructure(neighbor);
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
        if (cachedTe != null && !cachedTe.isInvalid() && !cachedTe.unloaded())
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
    
    public static StructureChildConnection loadFromNBT(IWorldPositionProvider structure, NBTTagCompound nbt, boolean isChild) {
        if (nbt.hasKey("childID")) // Old
            return StructureLink.loadFromNBTOld(structure, nbt, isChild);
        
        if (nbt.hasKey("entity"))
            return new StructureChildToSubWorldConnection(structure, nbt);
        else if (nbt.getBoolean("subWorld"))
            return new StructureChildFromSubWorldConnection(structure, nbt);
        return new StructureChildConnection(structure, isChild, nbt);
    }
    
}
