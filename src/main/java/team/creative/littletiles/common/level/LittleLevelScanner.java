package team.creative.littletiles.common.level;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;

public class LittleLevelScanner {
    
    public static LittleBoxes scan(Level level, BlockPos pos, @Nullable BiFilter<IParentCollection, LittleTile> filter) {
        LittleBoxes boxes = new LittleBoxesSimple(pos, LittleGrid.MIN);
        
        BETiles te = BlockTile.loadBE(level, pos);
        
        if (te == null)
            return boxes;
        
        for (Pair<IParentCollection, LittleTile> pair : te.allTiles())
            if (filter == null || filter.is(pair.key, pair.value))
                boxes.addBoxes(pair.key, pair.value);
            
        return boxes;
    }
    
    public static LittleBoxes scan(Level level, BlockPos pos, BlockPos pos2, @Nullable BiFilter<IParentCollection, LittleTile> filter) {
        LittleBoxes boxes = new LittleBoxesSimple(pos, LittleGrid.MIN);
        
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
                    
                    BETiles te = BlockTile.loadBE(level, position);
                    
                    if (te == null)
                        continue;
                    
                    for (Pair<IParentCollection, LittleTile> pair : te.allTiles())
                        if (filter == null || filter.is(pair.key, pair.value))
                            boxes.addBoxes(pair.key, pair.value);
                }
            }
        }
        
        return boxes;
    }
    
}
