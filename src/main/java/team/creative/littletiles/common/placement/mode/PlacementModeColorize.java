package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.tile.LittleTileColored;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.placement.PlacementContext;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlacementModeColorize extends PlacementMode {
    
    public PlacementModeColorize(String name, PreviewMode mode) {
        super(name, mode, true);
    }
    
    @Override
    public boolean shouldConvertBlock() {
        return true;
    }
    
    @Override
    public boolean checkAll() {
        return false;
    }
    
    @Override
    public List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos) {
        return null;
    }
    
    @Override
    public boolean placeTile(PlacementContext context, LittleStructure structure, LittleTile tile) throws LittleActionException {
        if (!requiresCollisionTest)
            return Collections.EMPTY_LIST;
        List<LittleTile> tiles = new ArrayList<>();
        for (LittleTile lt : LittleActionDestroyBoxes.removeBox(block.getTe(), block.getContext(), tile.getBox(), false)) {
            LittleTile newTile = LittleTileColored.setColor(lt, tile instanceof LittleTileColored ? ((LittleTileColored) tile).color : ColorUtils.WHITE);
            if (newTile != null) {
                placement.removedTiles.addTile(parent, lt);
                tiles.add(newTile);
            }
        }
        return tiles;
    }
}