package team.creative.littletiles.common.block.little.tile.parent;

import java.security.InvalidParameterException;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.connection.IStructureConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.CorruptedLinkException;
import team.creative.littletiles.common.structure.exception.MissingBlockException;
import team.creative.littletiles.common.structure.exception.MissingStructureException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.LittleFixedStructure;

public class StructureParentCollection extends ParentCollection implements IStructureParentCollection, IStructureConnection {
    
    private BlockParentCollection parent;
    
    private Object cache;
    private int structureIndex;
    private int attribute;
    private BlockPos relativePos;
    
    public StructureParentCollection(BlockParentCollection parent, int index, int attribute) {
        this.parent = parent;
        this.structureIndex = index;
        this.attribute = attribute;
    }
    
    public StructureParentCollection(BlockParentCollection parent, CompoundTag nbt) {
        this.parent = parent;
        load(nbt);
    }
    
    public void setParent(BlockParentCollection parent) {
        this.parent = parent;
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        if (nbt.contains("structure")) {
            CompoundTag structureNBT = nbt.getCompound("structure");
            cache = create(structureNBT, this);
        } else {
            int[] array = nbt.getIntArray("coord");
            if (array.length == 3)
                relativePos = new BlockPos(array[0], array[1], array[2]);
            else
                throw new InvalidParameterException("No valid coord given " + nbt);
        }
        attribute = nbt.getInt("type");
        structureIndex = nbt.getInt("index");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        if (isMain()) {
            CompoundTag structureNBT = new CompoundTag();
            ((LittleStructure) cache).save(structureNBT);
            nbt.put("structure", structureNBT);
        } else
            nbt.putIntArray("coord", new int[] { relativePos.getX(), relativePos.getY(), relativePos.getZ() });
        nbt.putInt("type", attribute);
        nbt.putInt("index", structureIndex);
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
    public BETiles getBE() {
        return parent.be;
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
    }
    
    @Override
    public BlockPos getStructurePosition() {
        return relativePos.offset(getPos());
    }
    
    public LittleStructure setStructureNBT(CompoundTag nbt) {
        if (this.cache instanceof LittleStructure && ((LittleStructure) this.cache).type.id.equals(nbt.getString("id")))
            ((LittleStructure) this.cache).load(nbt);
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
        BETiles te = getBlockEntity();
        if (!te.hasLoaded())
            throw new NotYetConnectedException();
        IStructureParentCollection structure = te.getStructure(structureIndex);
        if (structure != null)
            if (structure == this)
                throw new CorruptedLinkException();
            else
                return structure.getStructure();
        throw new MissingStructureException(te.getBlockPos());
    }
    
    protected BETiles getBlockEntity() throws CorruptedConnectionException, NotYetConnectedException {
        if (isMain())
            throw new RuntimeException("Main block cannot look for tileentity");
        if (cache instanceof BETiles && !((BETiles) cache).isRemoved())
            return (BETiles) cache;
        
        if (relativePos == null)
            throw new CorruptedLinkException();
        
        Level level = getBE().getLevel();
        
        BlockPos absoluteCoord = getStructurePosition();
        LevelChunk chunk = level.getChunkAt(absoluteCoord);
        if (LevelUtils.checkIfChunkExists(chunk)) {
            BlockEntity te = level.getBlockEntity(absoluteCoord);
            if (te instanceof BETiles)
                return (BETiles) (cache = te);
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
    public void mirrorForWarpDrive(Axis axis) {
        relativePos = axis.mirror(relativePos);
    }
    
    @Deprecated
    public void rotateForWarpDrive(Rotation rotation, int steps) {
        for (int rotationStep = 0; rotationStep < steps; rotationStep++)
            relativePos = rotation.transform(relativePos);
    }
    
    public static void setRelativePos(StructureParentCollection list, BlockPos pos) {
        list.relativePos = pos;
    }
    
    public static void updateStatus(StructureParentCollection list) {
        if (list.cache != null)
            list.relativePos = null;
    }
    
    public static LittleStructure create(CompoundTag nbt, StructureParentCollection mainBlock) {
        if (nbt == null)
            return null;
        
        String id = nbt.getString("id");
        LittleStructureType type = LittleStructureRegistry.getStructureType(id);
        if (type != null) {
            LittleStructure structure = type.createStructure(mainBlock);
            structure.load(nbt);
            
            return structure;
            
        }
        
        System.out.println("Could not find structureID=" + id);
        LittleStructure structure = new LittleFixedStructure(LittleStructureRegistry.getStructureType(LittleFixedStructure.class), mainBlock);
        structure.load(nbt);
        return structure;
    }
    
}
