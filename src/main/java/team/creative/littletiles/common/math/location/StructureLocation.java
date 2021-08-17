package team.creative.littletiles.common.math.location;

import java.util.Arrays;
import java.util.UUID;

import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

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
        this(structure.getWorld(), structure.getPos(), structure.getIndex());
    }
    
    public StructureLocation(NBTTagCompound nbt) {
        int[] posArray = nbt.getIntArray("pos");
        if (posArray.length != 3)
            throw new IllegalArgumentException("Invalid pos array length " + Arrays.toString(posArray));
        
        pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        index = nbt.getInteger("index");
        if (nbt.hasKey("world"))
            worldUUID = UUID.fromString(nbt.getString("world"));
        else
            worldUUID = null;
    }
    
    public NBTTagCompound write() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.setInteger("index", index);
        if (worldUUID != null)
            nbt.setString("world", worldUUID.toString());
        return nbt;
    }
    
    public LittleStructure find(Level level) throws LittleActionException {
        if (worldUUID != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(world.isRemote, worldUUID);
            if (animation == null)
                throw new MissingAnimationException(worldUUID);
            
            world = animation.fakeWorld;
        }
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityLittleTiles) {
            IStructureTileList structure = ((TileEntityLittleTiles) te).getStructure(index);
            if (structure != null)
                return structure.getStructure();
            throw new LittleActionException.StructureNotFoundException();
        } else
            throw new LittleActionException.TileEntityNotFoundException();
    }
}
