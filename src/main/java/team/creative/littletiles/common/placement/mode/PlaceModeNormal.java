package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlaceModeNormal extends PlacementMode {
    
    public PlaceModeNormal(String name, PreviewMode mode, boolean placeInside) {
        super(name, mode, placeInside);
    }
    
    @Override
    public PlacementMode place() {
        if (Screen.hasControlDown())
            return PlacementMode.fill;
        return super.place();
    }
    
    @Override
    public List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos) {
        List<BlockPos> coords = new ArrayList<>();
        coords.add(pos);
        return coords;
    }
    
    @Override
    public void placeTile(Placement placement, PlacementBlock block, IParentCollection parent, LittleStructure structure, LittleTile tile, boolean requiresCollisionTest) throws LittleActionException {
        List<LittleTile> tiles = new ArrayList<>();
        Pair<IParentCollection, LittleTile> intersecting = null;
        if (!requiresCollisionTest || (intersecting = block.getTe().intersectingTile(tile.getBox())) == null)
            tiles.add(tile);
        else if (this instanceof PlaceModeAll) {
            if (intersecting.key == parent)
                System.out.println("Structure is not valid ... some tiles will be left out");
            else
                throw new LittleActionException("Could not place all tiles");
        } else
            placement.unplaceableTiles.addTile(parent, tile);
        return tiles;
    }
}
