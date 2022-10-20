package team.creative.littletiles.common.math.location;

import java.util.Arrays;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.level.little.CreativeLevel;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

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
        if (context.parent.isStructure()) {
            this.isStructure = true;
            this.index = ((StructureParentCollection) context.parent).getIndex();
        } else {
            this.isStructure = false;
            this.index = -1;
        }
        this.pos = context.parent.getPos();
        this.box = context.box.copy();
        if (context.parent.getLevel() instanceof CreativeLevel)
            this.levelUUID = ((CreativeLevel) context.parent.getLevel()).getHolder().getUUID();
        else
            this.levelUUID = null;
    }
    
    public TileLocation(CompoundTag nbt) {
        int[] posArray = nbt.getIntArray("pos");
        if (posArray.length != 3)
            throw new IllegalArgumentException("Invalid pos array length " + Arrays.toString(posArray));
        
        pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        isStructure = nbt.contains("index");
        index = nbt.getInt("index");
        box = LittleBox.create(nbt.getIntArray("box"));
        if (nbt.contains("world"))
            levelUUID = UUID.fromString(nbt.getString("world"));
        else
            levelUUID = null;
    }
    
    public CompoundTag write(CompoundTag nbt) {
        nbt.putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        if (isStructure)
            nbt.putInt("index", index);
        nbt.putIntArray("box", box.getArray());
        if (levelUUID != null)
            nbt.putString("world", levelUUID.toString());
        return nbt;
    }
    
    public LittleTileContext find(Level level) throws LittleActionException {
        if (levelUUID != null) {
            LittleLevelEntity entity = LittleAnimationHandlers.find(level.isClientSide, levelUUID);
            if (entity == null)
                throw new MissingAnimationException(levelUUID);
            
            level = entity.getFakeLevel();
        }
        
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BETiles) {
            IParentCollection list = ((BETiles) be).noneStructureTiles();
            if (isStructure)
                list = ((BETiles) be).getStructure(index);
            for (LittleTile tile : list)
                if (tile.contains(box))
                    return new LittleTileContext(list, tile, box);
            throw new LittleActionException.TileNotFoundException();
        }
        throw new LittleActionException.TileEntityNotFoundException();
    }
}
