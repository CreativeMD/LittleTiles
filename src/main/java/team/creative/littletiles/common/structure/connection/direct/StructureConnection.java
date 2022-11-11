package team.creative.littletiles.common.structure.connection.direct;

import java.util.Arrays;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.math.location.LocalStructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;
import team.creative.littletiles.common.structure.connection.IStructureConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.MissingBlockException;
import team.creative.littletiles.common.structure.exception.MissingStructureException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class StructureConnection implements IStructureConnection {
    
    public final BlockPos pos;
    public final int index;
    private int attribute;
    private final Level level;
    
    private BETiles cachedBE;
    
    public StructureConnection(Level level, BlockPos pos, int index, int attribute) {
        this.level = level;
        this.pos = pos;
        this.index = index;
        this.attribute = attribute;
    }
    
    public StructureConnection(LittleStructure structure) {
        this(structure.getLevel(), structure.getPos(), structure.getIndex(), structure.getAttribute());
    }
    
    public StructureConnection(Level level, LocalStructureLocation location) {
        this(level, location.pos, location.index, LittleStructureAttribute.INVALID);
    }
    
    public StructureConnection(Level level, CompoundTag nbt) {
        this.level = level;
        int[] posArray = nbt.getIntArray("p");
        if (posArray.length != 3)
            throw new IllegalArgumentException("Invalid pos array length " + Arrays.toString(posArray));
        
        pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        index = nbt.getInt("i");
        attribute = nbt.getInt("a");
    }
    
    public CompoundTag write() {
        CompoundTag nbt = new CompoundTag();
        nbt.putIntArray("p", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.putInt("i", index);
        nbt.putInt("a", attribute);
        return nbt;
    }
    
    @Override
    public BlockPos getStructurePosition() {
        return pos;
    }
    
    protected BETiles getBlockEntity() throws CorruptedConnectionException, NotYetConnectedException {
        if (cachedBE != null && !cachedBE.isRemoved())
            return cachedBE;
        
        BlockPos absoluteCoord = getStructurePosition();
        LevelChunk chunk = level.getChunkAt(absoluteCoord);
        if (LevelUtils.checkIfChunkExists(chunk)) {
            BlockEntity be = level.getBlockEntity(absoluteCoord);
            if (be instanceof BETiles)
                return cachedBE = (BETiles) be;
            else
                throw new MissingBlockException(absoluteCoord);
        } else
            throw new NotYetConnectedException();
    }
    
    @Override
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException {
        BETiles be = getBlockEntity();
        if (!be.hasLoaded())
            throw new NotYetConnectedException();
        IStructureParentCollection structure = be.getStructure(index);
        if (structure != null && attribute == LittleStructureAttribute.INVALID)
            attribute = structure.getAttribute();
        if (structure != null)
            return structure.getStructure();
        throw new MissingStructureException(be.getBlockPos());
    }
    
    @Override
    public int getIndex() {
        return index;
    }
    
    @Override
    public int getAttribute() {
        if (attribute == LittleStructureAttribute.INVALID)
            try {
                getStructure();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                System.out.println("Invalid attribute returned");
                e.printStackTrace();
            }
        return attribute;
    }
    
}
