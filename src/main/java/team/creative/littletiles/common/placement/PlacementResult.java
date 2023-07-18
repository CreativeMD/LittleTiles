package team.creative.littletiles.common.placement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlacementResult {
    
    public final BlockPos pos;
    public final LittleGroup placedPreviews;
    public final LittleBoxes placedBoxes;
    private BlockPos lastPos = null;
    public final List<BETiles> blocks = new ArrayList<>();
    public LittleStructure parentStructure;
    
    public PlacementResult(BlockPos pos) {
        this.pos = pos;
        this.placedPreviews = new LittleGroup();
        this.placedBoxes = new LittleBoxesSimple(pos, LittleGrid.min());
    }
    
    public void addPlacedTile(ParentCollection parent, LittleTile tile) {
        if (lastPos == null || !lastPos.equals(parent.getPos())) {
            lastPos = parent.getPos();
            blocks.add(parent.getBE());
        }
        placedPreviews.add(parent.getGrid(), tile, tile.copy());
        placedBoxes.addBoxes(parent, tile);
    }
    
}
