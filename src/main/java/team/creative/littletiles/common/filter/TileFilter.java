package team.creative.littletiles.common.filter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.registry.NamedRegistry;
import team.creative.creativecore.common.util.registry.NamedRegistry.RegistryException;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.block.BlockTile;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public abstract class TileFilter {
    
    public static final NamedRegistry<TileFilter> REGISTRY = new NamedRegistry<>();
    
    public static TileFilter load(String id, CompoundTag nbt) {
        try {
            TileFilter filter = REGISTRY.create(id);
            filter.loadNBT(nbt);
            return filter;
        } catch (RegistryException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static TileFilter load(CompoundTag nbt) {
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
    
    public CompoundTag writeNBT(CompoundTag nbt) {
        saveNBT(nbt);
        nbt.putString("type", REGISTRY.getId(this));
        return nbt;
    }
    
    protected abstract void saveNBT(CompoundTag nbt);
    
    protected abstract void loadNBT(CompoundTag nbt);
    
    public abstract boolean is(IParentCollection parent, LittleTile tile);
    
    public static LittleBoxes getAbsoluteBoxes(Level level, BlockPos pos, BlockPos pos2, TileFilter selector) {
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
                    
                    position.set(posX, posY, posZ);
                    
                    BETiles be = BlockTile.loadBE(level, position);
                    
                    if (be == null)
                        continue;
                    
                    for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                        if (selector.is(pair.key, pair.value))
                            boxes.addBoxes(pair.key, pair.value);
                }
            }
        }
        
        return boxes;
    }
    
    public static List<LittleBox> getBoxes(Level level, BlockPos pos, TileFilter selector) {
        List<LittleBox> boxes = new ArrayList<>();
        BETiles be = BlockTile.loadBE(level, pos);
        for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
            if (selector.is(pair.key, pair.value))
                for (LittleBox box : pair.value)
                    boxes.add(box);
        return boxes;
    }
    
}
