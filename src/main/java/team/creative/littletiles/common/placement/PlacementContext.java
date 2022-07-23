package team.creative.littletiles.common.placement;

import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;

public class PlacementContext {
    
    protected ParentCollection parent;
    public final Placement placement;
    public final PlacementBlock block;
    public final PlacementResult result;
    public final boolean collisionTest;
    
    public PlacementContext(Placement placement, PlacementBlock block, PlacementResult result, boolean collisionTest) {
        this.placement = placement;
        this.block = block;
        this.result = result;
        this.collisionTest = collisionTest;
    }
    
    public BETiles getBE() {
        return block.getBE();
    }
    
    public boolean isSpaceFor(LittleBox box) {
        return getBE().isSpaceFor(box);
    }
    
    public ParentCollection getParent() {
        return parent;
    }
    
    public void setParent(ParentCollection parent) {
        this.parent = parent;
    }
    
    public void addRemoved(LittleTile tile) {
        placement.removedTiles.add(parent, tile);
    }
    
    public boolean removeTile(LittleTile tile) {
        boolean changed = false;
        LittleBoxReturnedVolume volume = new LittleBoxReturnedVolume();
        for (LittleBox box : tile) {
            for (LittleTile removedTile : LittleActionDestroyBoxes.removeBox(block.getBE(), parent.getGrid(), box, false, volume)) {
                addRemoved(removedTile);
                changed = true;
            }
            if (volume.has())
                placement.addRemovedIngredient(block, tile, volume);
            volume.clear();
        }
        
        getBE().convertTo(block.getGrid());
        return changed;
    }
    
    public void addUnplaceable(LittleElement element, LittleBox box) {
        placement.unplaceableTiles.add(parent.getGrid(), element, box);
    }
    
    public void placeTile(LittleTile tile) {
        parent.add(tile.copy());
        result.addPlacedTile(parent, tile);
        
    }
    
}
