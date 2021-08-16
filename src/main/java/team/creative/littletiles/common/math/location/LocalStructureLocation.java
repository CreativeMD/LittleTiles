package team.creative.littletiles.common.math.location;

import java.util.Arrays;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;

public class LocalStructureLocation {
    
    public final BlockPos pos;
    public final int index;
    
    public LocalStructureLocation(BlockPos pos, int index) {
        this.pos = pos;
        this.index = index;
    }
    
    public LocalStructureLocation(LittleStructure structure) {
        this(structure.getPos(), structure.getIndex());
    }
    
    public LocalStructureLocation(CompoundTag nbt) {
        int[] posArray = nbt.getIntArray("pos");
        if (posArray.length != 3)
            throw new IllegalArgumentException("Invalid pos array length " + Arrays.toString(posArray));
        
        pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        index = nbt.getInt("index");
    }
    
    public CompoundTag write() {
        CompoundTag nbt = new CompoundTag();
        nbt.putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.putInt("index", index);
        return nbt;
    }
    
    public LittleStructure find(Level level) throws LittleActionException {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof BETiles) {
            IStructureParentCollection structure = ((BETiles) te).getStructure(index);
            if (structure != null)
                return structure.getStructure();
            throw new LittleActionException.StructureNotFoundException();
        } else
            throw new LittleActionException.TileEntityNotFoundException();
    }
}
