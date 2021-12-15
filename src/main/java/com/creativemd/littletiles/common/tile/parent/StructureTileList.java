package com.creativemd.littletiles.common.tile.parent;

import java.security.InvalidParameterException;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.connection.IStructureConnection;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.CorruptedLinkException;
import com.creativemd.littletiles.common.structure.exception.MissingBlockException;
import com.creativemd.littletiles.common.structure.exception.MissingStructureException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.type.LittleFixedStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class StructureTileList extends ParentTileList implements IStructureTileList, IStructureConnection {
    
    private TileList parent;
    
    private Object cache;
    private int structureIndex;
    private int attribute;
    private BlockPos relativePos;
    
    public StructureTileList(TileList parent, int index, int attribute) {
        this.parent = parent;
        this.structureIndex = index;
        this.attribute = attribute;
    }
    
    public StructureTileList(TileList parent, NBTTagCompound nbt) {
        this.parent = parent;
        read(nbt);
    }
    
    public void setParent(TileList parent) {
        this.parent = parent;
    }
    
    @Override
    protected void readExtra(NBTTagCompound nbt) {
        if (nbt.hasKey("structure")) {
            NBTTagCompound structureNBT = nbt.getCompoundTag("structure");
            cache = create(structureNBT, this);
        } else {
            int[] array = nbt.getIntArray("coord");
            if (array.length == 3)
                relativePos = new BlockPos(array[0], array[1], array[2]);
            else
                throw new InvalidParameterException("No valid coord given " + nbt);
        }
        attribute = nbt.getInteger("type");
        structureIndex = nbt.getInteger("index");
    }
    
    @Override
    protected void writeExtra(NBTTagCompound nbt) {
        if (isMain()) {
            NBTTagCompound structureNBT = new NBTTagCompound();
            ((LittleStructure) cache).writeToNBT(structureNBT);
            nbt.setTag("structure", structureNBT);
        } else
            nbt.setIntArray("coord", new int[] { relativePos.getX(), relativePos.getY(), relativePos.getZ() });
        nbt.setInteger("type", attribute);
        nbt.setInteger("index", structureIndex);
    }
    
    @Override
    public boolean isStructure() {
        return true;
    }
    
    @Override
    public boolean isStructureChild(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException {
        return getStructure().isChildOf(structure);
    }
    
    @Override
    public TileEntityLittleTiles getTe() {
        return parent.te;
    }
    
    @Override
    public int getAttribute() {
        if (isMain())
            return attribute;
        return attribute & LittleStructureAttribute.NON_ACTIVE_MASK;
    }
    
    @Override
    public void setAttribute(int attribute) {
        this.attribute = attribute;
        getTe().updateTiles(false);
    }
    
    @Override
    public BlockPos getStructurePosition() {
        return relativePos.add(getPos());
    }
    
    public LittleStructure setStructureNBT(NBTTagCompound nbt) {
        if (this.cache instanceof LittleStructure && ((LittleStructure) this.cache).type.id.equals(nbt.getString("id")))
            ((LittleStructure) this.cache).loadFromNBT(nbt);
        else {
            if (this.cache instanceof LittleStructure)
                ((LittleStructure) this.cache).unload();
            this.cache = create(nbt, this);
        }
        return (LittleStructure) cache;
    }
    
    @Override
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException {
        if (isMain())
            return (LittleStructure) cache;
        TileEntityLittleTiles te = getTileEntity();
        if (!te.hasLoaded())
            throw new NotYetConnectedException();
        IStructureTileList structure = te.getStructure(structureIndex);
        if (structure != null)
            if (structure == this)
                throw new CorruptedLinkException();
            else
                return structure.getStructure();
        throw new MissingStructureException(te.getPos());
    }
    
    protected TileEntityLittleTiles getTileEntity() throws CorruptedConnectionException, NotYetConnectedException {
        if (isMain())
            throw new RuntimeException("Main block cannot look for tileentity");
        if (cache instanceof TileEntityLittleTiles && !((TileEntityLittleTiles) cache).isInvalid())
            return (TileEntityLittleTiles) cache;
        
        if (relativePos == null)
            throw new CorruptedLinkException();
        
        World world = getTe().getWorld();
        
        BlockPos absoluteCoord = getStructurePosition();
        Chunk chunk = world.getChunkFromBlockCoords(absoluteCoord);
        if (WorldUtils.checkIfChunkExists(chunk)) {
            TileEntity te = world.getTileEntity(absoluteCoord);
            if (te instanceof TileEntityLittleTiles)
                return (TileEntityLittleTiles) (cache = te);
            else
                throw new MissingBlockException(absoluteCoord);
        } else
            throw new NotYetConnectedException();
    }
    
    @Override
    public boolean isMain() {
        return relativePos == null;
    }
    
    @Override
    public boolean isClient() {
        return parent.isClient();
    }
    
    @Override
    public int getIndex() {
        return structureIndex;
    }
    
    @Override
    public boolean isRemoved() {
        return parent == null;
    }
    
    public void removed() {
        parent = null;
    }
    
    public void remove() {
        parent.removeStructure(structureIndex);
    }
    
    @Override
    public void unload() {
        super.unload();
        if (isMain())
            ((LittleStructure) cache).unload();
    }
    
    @Override
    public int totalSize() {
        return size();
    }
    
    @Deprecated
    public void flipForWarpDrive(Axis axis) {
        relativePos = RotationUtils.flip(relativePos, axis);
    }
    
    @Deprecated
    public void rotateForWarpDrive(Rotation rotation, int steps) {
        for (int rotationStep = 0; rotationStep < steps; rotationStep++)
            relativePos = RotationUtils.rotate(relativePos, rotation);
    }
    
    public static void setRelativePos(StructureTileList list, BlockPos pos) {
        list.relativePos = pos;
    }
    
    public static void updateStatus(StructureTileList list) {
        if (list.cache != null)
            list.relativePos = null;
    }
    
    public static LittleStructure create(NBTTagCompound nbt, StructureTileList mainBlock) {
        if (nbt == null)
            return null;
        
        String id = nbt.getString("id");
        LittleStructureType type = LittleStructureRegistry.getStructureType(id);
        if (type != null) {
            LittleStructure structure = type.createStructure(mainBlock);
            structure.loadFromNBT(nbt);
            
            return structure;
            
        }
        
        System.out.println("Could not find structureID=" + id);
        LittleStructure structure = new LittleFixedStructure(LittleStructureRegistry.getStructureType(LittleFixedStructure.class), mainBlock);
        structure.loadFromNBT(nbt);
        return structure;
    }
    
}
