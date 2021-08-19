package team.creative.littletiles.common.math.location;

import java.util.Arrays;
import java.util.UUID;

import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;

public class StructureLocation {
    
    public final BlockPos pos;
    public final int index;
    public final UUID levelUUID;
    
    public StructureLocation(BlockPos pos, int index, UUID world) {
        this.pos = pos;
        this.index = index;
        this.levelUUID = world;
    }
    
    public StructureLocation(Level level, BlockPos pos, int index) {
        this.pos = pos;
        this.index = index;
        if (level instanceof CreativeLevel)
            this.levelUUID = ((CreativeLevel) level).parent.getUUID();
        else
            this.levelUUID = null;
    }
    
    public StructureLocation(LittleStructure structure) {
        this(structure.getLevel(), structure.getPos(), structure.getIndex());
    }
    
    public StructureLocation(CompoundTag nbt) {
        int[] posArray = nbt.getIntArray("pos");
        if (posArray.length != 3)
            throw new IllegalArgumentException("Invalid pos array length " + Arrays.toString(posArray));
        
        pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        index = nbt.getInt("index");
        if (nbt.contains("world"))
            levelUUID = UUID.fromString(nbt.getString("world"));
        else
            levelUUID = null;
    }
    
    public CompoundTag write(CompoundTag nbt) {
        nbt.putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.putInt("index", index);
        if (levelUUID != null)
            nbt.putString("world", levelUUID.toString());
        return nbt;
    }
    
    public LittleStructure find(Level level) throws LittleActionException {
        if (levelUUID != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(level.isClientSide, levelUUID);
            if (animation == null)
                throw new MissingAnimationException(levelUUID);
            
            level = animation.fakeWorld;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BETiles) {
            IStructureParentCollection structure = ((BETiles) be).getStructure(index);
            if (structure != null)
                return structure.getStructure();
            throw new LittleActionException.StructureNotFoundException();
        } else
            throw new LittleActionException.TileEntityNotFoundException();
    }
}
