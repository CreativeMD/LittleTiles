package team.creative.littletiles.common.math.location;

import java.util.Arrays;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

public class TileLocation implements IGridBased {
    
    public final BlockPos pos;
    public final boolean isStructure;
    public final int index;
    private LittleGrid grid;
    public final LittleBox box;
    public final UUID levelUUID;
    
    public TileLocation(BlockPos pos, boolean isStructure, int index, LittleGrid grid, LittleBox box, UUID world) {
        this.pos = pos;
        this.isStructure = isStructure;
        this.index = index;
        this.grid = grid;
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
        this.grid = context.parent.getGrid();
        this.pos = context.parent.getPos();
        this.box = context.box.copy();
        if (context.parent.getLevel() instanceof ISubLevel sub)
            this.levelUUID = sub.getHolder().getUUID();
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
        grid = LittleGrid.get(nbt);
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
        grid.set(nbt);
        nbt.putIntArray("box", box.getArray());
        if (levelUUID != null)
            nbt.putString("world", levelUUID.toString());
        return nbt;
    }
    
    public LittleTileContext find(LevelAccessor level) throws LittleActionException {
        if (levelUUID != null) {
            LittleEntity entity = LittleTiles.ANIMATION_HANDLERS.find(level.isClientSide(), levelUUID);
            if (entity == null)
                throw new MissingAnimationException(levelUUID);
            
            level = entity.getSubLevel();
        }
        
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BETiles) {
            IParentCollection list;
            if (isStructure)
                list = ((BETiles) be).getStructure(index);
            else
                list = ((BETiles) be).noneStructureTiles();
            
            var result = ((BETiles) be).sameGrid(this, () -> {
                for (LittleTile tile : list)
                    if (tile.contains(box))
                        return new LittleTileContext(list, tile, box.copy());
                return null;
            });
            
            if (result == null)
                throw new LittleActionException.TileNotFoundException();
            return result;
        }
        throw new LittleActionException.BlockEntityNotFoundException();
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        this.box.convertTo(grid, to);
        this.grid = to;
    }
    
    @Override
    public int getSmallest() {
        return box.getSmallest(grid);
    }
}
