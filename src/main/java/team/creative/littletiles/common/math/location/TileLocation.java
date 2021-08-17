package team.creative.littletiles.common.math.location;

import java.util.Arrays;
import java.util.UUID;

import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.LittleTileContext;
import team.creative.littletiles.common.tile.parent.IParentCollection;
import team.creative.littletiles.common.tile.parent.StructureParentCollection;

public class TileLocation {
    
    public final BlockPos pos;
    public final boolean isStructure;
    public final int index;
    public final LittleBox box;
    public final UUID levelUUID;
    
    public TileLocation(BlockPos pos, boolean isStructure, int index, LittleBox box, UUID world) {
        this.pos = pos;
        this.isStructure = isStructure;
        this.index = index;
        this.box = box;
        this.levelUUID = world;
    }
    
    public TileLocation(LittleTileContext context) {
        if (list.isStructure()) {
            this.isStructure = true;
            this.index = ((StructureParentCollection) list).getIndex();
        } else {
            this.isStructure = false;
            this.index = -1;
        }
        this.pos = list.getPos();
        this.box = tile.getBox().copy();
        if (list.getLevel() instanceof CreativeWorld)
            this.levelUUID = ((CreativeWorld) list.getWorld()).parent.getUniqueID();
        else
            this.levelUUID = null;
    }
    
    public TileLocation(NBTTagCompound nbt) {
        int[] posArray = nbt.getIntArray("pos");
        if (posArray.length != 3)
            throw new IllegalArgumentException("Invalid pos array length " + Arrays.toString(posArray));
        
        pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        isStructure = nbt.hasKey("index");
        index = nbt.getInteger("index");
        box = LittleBox.createBox(nbt.getIntArray("box"));
        if (nbt.hasKey("world"))
            worldUUID = UUID.fromString(nbt.getString("world"));
        else
            worldUUID = null;
    }
    
    public NBTTagCompound write() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        if (isStructure)
            nbt.setInteger("index", index);
        nbt.setIntArray("box", box.getArray());
        if (worldUUID != null)
            nbt.setString("world", worldUUID.toString());
        return nbt;
    }
    
    public Pair<IParentCollection, LittleTile> find(Level level) throws LittleActionException {
        if (worldUUID != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(world.isRemote, worldUUID);
            if (animation == null)
                throw new MissingAnimationException(worldUUID);
            
            world = animation.fakeWorld;
        }
        
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityLittleTiles) {
            IParentTileList list = ((TileEntityLittleTiles) te).noneStructureTiles();
            if (isStructure)
                list = ((TileEntityLittleTiles) te).getStructure(index);
            for (LittleTile tile : list)
                if (tile.getBox().equals(box))
                    return new Pair<>(list, tile);
            throw new LittleActionException.TileNotFoundException();
        }
        throw new LittleActionException.TileEntityNotFoundException();
    }
}
