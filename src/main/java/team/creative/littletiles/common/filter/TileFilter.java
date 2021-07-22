package team.creative.littletiles.common.filter;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxesSimple;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.creative.creativecore.common.util.registry.NamedRegistry;
import team.creative.creativecore.common.util.registry.NamedRegistry.RegistryException;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxes;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public abstract class TileFilter {
    
    public static final NamedRegistry<TileFilter> REGISTRY = new NamedRegistry<>();
    
    public static TileFilter load(String id, CompoundNBT nbt) {
        try {
            TileFilter filter = REGISTRY.create(id);
            filter.loadNBT(nbt);
            return filter;
        } catch (RegistryException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static TileFilter load(CompoundNBT nbt) {
        try {
            TileFilter filter = REGISTRY.create(nbt.getString("type"));
            filter.loadNBT(nbt);
            return filter;
        } catch (RegistryException e) {
            throw new RuntimeException(e);
        }
    }
    
    static {
        // Init Selectors
        REGISTRY.register("any", AnyFilter.class);
        REGISTRY.register("and", AndFilter.class);
        REGISTRY.register("or", OrFilter.class);
        REGISTRY.register("not", NotFilter.class);
        REGISTRY.register("block", BlockFilter.class);
        REGISTRY.register("color", ColorFilter.class);
        REGISTRY.register("nostructure", NoStructureFilter.class);
    }
    
    public TileFilter() {
        
    }
    
    public CompoundNBT writeNBT(CompoundNBT nbt) {
        saveNBT(nbt);
        nbt.putString("type", REGISTRY.getId(this));
        return nbt;
    }
    
    protected abstract void saveNBT(CompoundNBT nbt);
    
    protected abstract void loadNBT(CompoundNBT nbt);
    
    public abstract boolean is(IParentCollection parent, LittleTile tile);
    
    public static LittleBoxes getAbsoluteBoxes(World world, BlockPos pos, BlockPos pos2, TileSelector selector) {
        LittleBoxes boxes = new LittleBoxesSimple(pos, LittleGrid.min());
        
        int minX = Math.min(pos.getX(), pos2.getX());
        int maxX = Math.max(pos.getX(), pos2.getX());
        int minY = Math.min(pos.getY(), pos2.getY());
        int maxY = Math.max(pos.getY(), pos2.getY());
        int minZ = Math.min(pos.getZ(), pos2.getZ());
        int maxZ = Math.max(pos.getZ(), pos2.getZ());
        
        MutableBlockPos position = new MutableBlockPos();
        
        for (int posX = minX; posX <= maxX; posX++) {
            for (int posY = minY; posY <= maxY; posY++) {
                for (int posZ = minZ; posZ <= maxZ; posZ++) {
                    
                    position.setPos(posX, posY, posZ);
                    
                    TileEntityLittleTiles te = BlockTile.loadTe(world, position);
                    
                    if (te == null)
                        continue;
                    
                    for (Pair<IParentCollection, LittleTile> pair : te.allTiles())
                        if (selector.is(pair.key, pair.value))
                            boxes.addBox(pair.key, pair.value);
                }
            }
        }
        
        return boxes;
    }
    
    public static List<LittleBox> getBoxes(World world, BlockPos pos, TileSelector selector) {
        List<LittleBox> boxes = new ArrayList<>();
        TileEntityLittleTiles te = BlockTile.loadTe(world, pos);
        for (Pair<IParentCollection, LittleTile> pair : te.allTiles())
            if (selector.is(pair.key, pair.value))
                boxes.add(pair.value.getBox());
        return boxes;
    }
    
}
