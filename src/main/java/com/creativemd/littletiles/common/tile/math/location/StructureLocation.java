package com.creativemd.littletiles.common.tile.math.location;

import java.util.Arrays;
import java.util.UUID;

import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.MissingAnimationException;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureLocation {
    
    public final BlockPos pos;
    public final int index;
    public final UUID worldUUID;
    
    public StructureLocation(BlockPos pos, int index, UUID world) {
        this.pos = pos;
        this.index = index;
        this.worldUUID = world;
    }
    
    public StructureLocation(World world, BlockPos pos, int index) {
        this.pos = pos;
        this.index = index;
        if (world instanceof CreativeWorld)
            this.worldUUID = ((CreativeWorld) world).parent.getUniqueID();
        else
            this.worldUUID = null;
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
    
    public LittleStructure find(World world) throws LittleActionException {
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
